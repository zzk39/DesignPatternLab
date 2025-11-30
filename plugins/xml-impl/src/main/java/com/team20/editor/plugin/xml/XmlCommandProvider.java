package com.team20.editor.plugin.xml;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.command.CommandDescriptor;
import com.team20.editor.extension.registry.CommandRegistry;
import com.team20.editor.extension.spi.command.CommandProvider;
import com.team20.editor.plugin.xml.command.*;

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
        list.add(CommandDescriptor.undoable("insert-before",
                () -> unsupported("insert-before"),
                "insert-before <tag> <newId> <targetId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("append-child",
                () -> unsupported("append-child"),
                "append-child <tag> <newId> <parentId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("edit-id",
                () -> unsupported("edit-id"),
                "edit-id <oldId> <newId>"));
        list.add(CommandDescriptor.undoable("edit-text",
                () -> unsupported("edit-text"),
                "edit-text <elementId> [\"text\"]"));
        list.add(CommandDescriptor.undoable("delete",
                () -> unsupported("delete"),
                "delete <elementId>"));
        list.add(CommandDescriptor.of("xml-tree",
                () -> unsupported("xml-tree"),
                "xml-tree [file]"));
        return list;
    }

    // 生命周期工厂注册：真正提供 raw 参数 → 命令对象
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
        registry.registerFactory("delete", raw -> {
            String id = raw.trim();
            if (id.isEmpty())
                throw new IllegalArgumentException("用法: delete <elementId>");
            return new XmlDeleteCommand(id);
        });
        registry.registerFactory("xml-tree", raw -> (Command) ws -> {
            com.team20.editor.plugin.xml.XmlCommandProvider.printTree(ws);
        });
    }

    private static Command unsupported(String name) {
        return ws -> {
            throw new UnsupportedOperationException(
                    "命令 '" + name + "' 只能通过工厂（带参数）执行，当前 Supplier 仅占位。");
        };
    }

    private static void printTree(com.team20.editor.domain.workspace.Workspace ws) {
        var ed = ws.getActiveEditor();
        if (!(ed instanceof com.team20.editor.domain.editor.xml.XmlEditor xe)) {
            throw new IllegalStateException("当前活动文件不是 XML 编辑器");
        }
        printElement(xe.getRoot(), "");
    }

    private static void printElement(org.w3c.dom.Element el, String indent) {
        if (el == null) {
            System.out.println("(空文档)");
            return;
        }
        StringBuilder attrs = new StringBuilder();
        var map = el.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            var a = map.item(i);
            if (attrs.length() > 0)
                attrs.append(", ");
            attrs.append(a.getNodeName()).append("=\"").append(a.getNodeValue()).append("\"");
        }
        System.out.println(indent + el.getTagName() + " [" + attrs + "]");
        var children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var n = children.item(i);
            if (n instanceof org.w3c.dom.Element child) {
                printElement(child, indent + "  ");
            } else if (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                String text = n.getNodeValue();
                if (text != null) {
                    text = text.trim();
                    if (!text.isEmpty())
                        System.out.println(indent + "  " + "\"" + text + "\"");
                }
            }
        }
    }

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