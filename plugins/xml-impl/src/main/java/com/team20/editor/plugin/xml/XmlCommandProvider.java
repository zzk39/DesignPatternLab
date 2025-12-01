package com.team20.editor.plugin.xml;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.command.CommandDescriptor; // domain 包
import com.team20.editor.extension.registry.CommandRegistry;
import com.team20.editor.extension.spi.command.CommandProvider;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.plugin.xml.command.*;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XmlCommandProvider implements CommandProvider {

    @Override
    public String getProviderName() {
        return "xml-commands";
    }

    @Override
    public List<CommandDescriptor> getCommandDescriptors() {
        List<CommandDescriptor> list = new ArrayList<>();
        list.add(CommandDescriptor.undoable("insert-before", () -> unsupported("insert-before"),
                "insert-before <tag> <newId> <targetId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("append-child", () -> unsupported("append-child"),
                "append-child <tag> <newId> <parentId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("edit-id", () -> unsupported("edit-id"),
                "edit-id <oldId> <newId>"));
        list.add(CommandDescriptor.undoable("edit-text", () -> unsupported("edit-text"),
                "edit-text <elementId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("delete", () -> unsupported("delete"),
                "delete <elementId>"));
        list.add(CommandDescriptor.of("xml-tree", () -> unsupported("xml-tree"),
                "xml-tree [file]"));
        return list;
    }

    @Override
    public void registerFactories(CommandRegistry registry) {
        registry.registerFactory("insert-before", raw -> {
            String[] p = splitArgs(raw, 3);
            return new XmlInsertBeforeCommand(p[0], p[1], p[2], p.length >= 4 ? p[3] : null);
        });
        registry.registerFactory("append-child", raw -> {
            String[] p = splitArgs(raw, 3);
            return new XmlAppendChildCommand(p[0], p[1], p[2], p.length >= 4 ? p[3] : null);
        });
        registry.registerFactory("edit-id", raw -> {
            String[] p = raw.trim().split("\\s+");
            if (p.length < 2)
                throw new IllegalArgumentException("用法: edit-id <oldId> <newId>");
            return new XmlEditIdCommand(p[0], p[1]);
        });
        registry.registerFactory("edit-text", raw -> {
            String[] p = splitArgs(raw, 1);
            return new XmlEditTextCommand(p[0], p.length >= 2 ? p[1] : null);
        });
        // 改名：xml-delete
        registry.registerFactory("xml-delete", raw -> {
            String id = raw.trim();
            if (id.isEmpty())
                throw new IllegalArgumentException("用法: xml-delete <elementId>");
            return new XmlDeleteCommand(id);
        });
        // 修复：传递 raw 参数，支持 xml-tree [file]
        registry.registerFactory("xml-tree", raw -> (Workspace ws) -> runXmlTree(ws, raw));
    }

    private static Command unsupported(String name) {
        return ws -> {
            throw new UnsupportedOperationException(
                    "命令 '" + name + "' 只能通过工厂（带参数）执行，当前 Supplier 仅占位。");
        };
    }

    // ========================
    // xml-tree 实现（树状输出 + 支持 [file] 参数）
    // ========================

    private static void runXmlTree(Workspace ws, String raw) {
        String arg = raw == null ? "" : raw.trim();

        // 1) 无参数：使用当前活动编辑器（必须是 XmlEditor）
        if (arg.isEmpty()) {
            var ed = ws.getActiveEditor();
            if (!(ed instanceof com.team20.editor.domain.editor.xml.XmlEditor xe)) {
                System.out.println("错误: 当前活动文件不是 XML 编辑器");
                return;
            }
            // 发布日志事件（写入当前活动文件的日志）
            try {
                ws.publishCommandEvent("xml-tree", "");
            } catch (Throwable ignored) {
            }
            Element root = xe.getRoot();
            printTree(root);
            return;
        }

        // 2) 有参数：优先尝试工作区已打开的编辑器
        try {
            Editor target = ws.getEditor(arg);
            if (target instanceof com.team20.editor.domain.editor.xml.XmlEditor xe) {
                // 注意：这里不发布命令事件，避免把“指定的另一个文件”的树输出
                // 记到“当前活动文件”的日志。若需要记录到该目标文件，需要 core 支持按文件发布事件。
                printTree(xe.getRoot());
                return;
            }
        } catch (Throwable ignored) {
        }

        // 3) 磁盘路径：存在则直接解析并打印（不加入工作区，不发布事件）
        Path p = Path.of(arg);
        if (Files.exists(p) && Files.isRegularFile(p)) {
            String content = readFileSilently(p);
            if (content == null) {
                System.out.println("无法读取文件: " + arg);
                return;
            }
            content = stripLeadingLogLine(content); // 兼容 '# log' 首行
            Element root = parseRootElement(content);
            if (root == null) {
                System.out.println("错误: 不是有效的 XML 文档: " + arg);
                return;
            }
            printTree(root);
            return;
        }

        System.out.println("未找到文件: " + arg);
    }

    private static String readFileSilently(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static String stripLeadingLogLine(String content) {
        if (content == null)
            return null;
        int pos = content.indexOf('\n');
        if (pos >= 0) {
            String first = content.substring(0, pos).trim();
            if (first.startsWith("#"))
                return content.substring(pos + 1);
        } else if (content.trim().startsWith("#")) {
            return "";
        }
        return content;
    }

    private static Element parseRootElement(String xml) {
        try {
            var f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            var b = f.newDocumentBuilder();
            Document doc = b.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            return doc.getDocumentElement();
        } catch (Exception e) {
            return null;
        }
    }

    // 树状输出（使用 ├──/└──/│）
    private static void printTree(Element root) {
        if (root == null) {
            System.out.println("(空文档)");
            return;
        }
        // 根行（无前缀）
        System.out.println(formatElementLine(root));

        // 收集子节点（元素与非空文本）
        List<Node> children = collectPrintableChildren(root);
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            boolean last = (i == children.size() - 1);
            printNodeRecursive(child, "", last);
        }
    }

    private static void printNodeRecursive(Node node, String prefix, boolean isLast) {
        String connector = isLast ? "└── " : "├── ";
        String nextPrefix = prefix + (isLast ? "    " : "│   ");

        if (node instanceof Element el) {
            System.out.println(prefix + connector + formatElementLine(el));

            List<Node> kids = collectPrintableChildren(el);
            for (int i = 0; i < kids.size(); i++) {
                Node c = kids.get(i);
                boolean last = (i == kids.size() - 1);
                printNodeRecursive(c, nextPrefix, last);
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            if (text != null) {
                text = text.trim();
                if (!text.isEmpty()) {
                    System.out.println(prefix + connector + "\"" + text + "\"");
                }
            }
        }
    }

    private static List<Node> collectPrintableChildren(Element el) {
        List<Node> out = new ArrayList<>();
        NodeList nodes = el.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element) {
                out.add(n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = n.getNodeValue();
                if (text != null && !text.trim().isEmpty()) {
                    out.add(n);
                }
            }
        }
        return out;
    }

    private static String formatElementLine(Element el) {
        StringBuilder attrs = new StringBuilder();
        NamedNodeMap map = el.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node a = map.item(i);
            if (attrs.length() > 0)
                attrs.append(", ");
            attrs.append(a.getNodeName()).append("=\"").append(a.getNodeValue()).append("\"");
        }
        return el.getTagName() + " [" + attrs + "]";
    }

    // ========================
    // 其它辅助
    // ========================

    private static String[] splitArgs(String raw, int required) {
        java.util.List<String> out = new java.util.ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (Character.isWhitespace(c) && !inQuote) {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
            } else
                cur.append(c);
        }
        if (cur.length() > 0)
            out.add(cur.toString());
        if (out.size() < required)
            throw new IllegalArgumentException("参数不足");
        return out.toArray(new String[0]);
    }
}