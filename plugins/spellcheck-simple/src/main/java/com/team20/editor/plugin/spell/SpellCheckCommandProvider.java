package com.team20.editor.plugin.spell;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.command.CommandDescriptor;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.CommandRegistry;
import com.team20.editor.extension.spi.command.CommandProvider;
import com.team20.editor.extension.spi.spellcheck.SpellChecker;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SpellCheckCommandProvider implements CommandProvider {

    @Override
    public String getProviderName() {
        return "spell-check";
    }

    @Override
    public List<CommandDescriptor> getCommandDescriptors() {
        return List.of(CommandDescriptor.of(
                "spell-check",
                () -> unsupported(),
                "spell-check [file]  - 检查当前活动文件或指定路径（文本或 XML）的拼写"));
    }

    @Override
    public void registerFactories(CommandRegistry registry) {
        registry.registerFactory("spell-check", raw -> (Workspace ws) -> run(ws, raw));
    }

    private static Command unsupported() {
        return ws -> {
            throw new UnsupportedOperationException("请通过工厂执行：spell-check [file]");
        };
    }

    private static void run(Workspace ws, String raw) {
        SpellChecker checker = loadChecker();
        if (checker == null) {
            System.out.println("拼写检查器不可用");
            return;
        }

        String arg = raw == null ? "" : raw.trim();
        if (arg.isEmpty()) {
            // 1) 无参数：检查活动文件（必须已打开）
            Editor active = ws.getActiveEditor();
            if (active == null) {
                System.out.println("无活动文件");
                return;
            }
            String content = active.getContent();
            boolean isXml = active.getClass().getName().endsWith(".xml.XmlEditor");
            if (isXml)
                runXmlSpellCheck(content, checker);
            else
                runTextSpellCheck(content, checker);
            return;
        }

        // 2) 有参数：先尝试按名称在工作区中查找已打开的编辑器
        try {
            Editor opened = ws.getEditor(arg);
            if (opened != null) {
                String content = opened.getContent();
                boolean isXml = opened.getClass().getName().endsWith(".xml.XmlEditor");
                if (isXml)
                    runXmlSpellCheck(content, checker);
                else
                    runTextSpellCheck(content, checker);
                return;
            }
        } catch (Throwable ignored) {
            // 忽略：工作区未打开该文件，继续尝试磁盘读取
        }

        // 3) 磁盘路径：若存在，则直接读取并检查（不自动打开到工作区）
        Path p = Path.of(arg);
        if (Files.exists(p) && Files.isRegularFile(p)) {
            String content = readFileSilently(p);
            if (content == null) {
                System.out.println("无法读取文件: " + arg);
                return;
            }
            boolean isXml = arg.toLowerCase().endsWith(".xml") || looksLikeXml(content);
            System.out.println("Using checker: " + checker.getClass().getName());
            if (isXml)
                runXmlSpellCheck(content, checker); // 内部会自动剥离 '# log' 首行
            else
                runTextSpellCheck(content, checker);
            return;
        }

        // 4) 两者都找不到
        System.out.println("未找到文件: " + arg);
    }

    private static void runTextSpellCheck(String content, SpellChecker checker) {
        var suggestions = checker.checkText(content, "en-US");
        if (suggestions == null || suggestions.isEmpty()) {
            System.out.println("拼写检查无错误");
            return;
        }
        System.out.println("拼写检查结果:");
        for (var s : suggestions) {
            if (s.line > 0 && s.column > 0)
                System.out.printf("第%d行，第%d列: \"%s\" -> 建议: %s%n",
                        s.line, s.column, s.token, String.join("/", s.candidates));
            else
                System.out.printf("\"%s\" -> 建议: %s%n", s.token, String.join("/", s.candidates));
        }
    }

    private static void runXmlSpellCheck(String xmlRaw, SpellChecker checker) {
        // 兼容首行 "# log ..." 的文件：剥离后再解析
        String xml = stripLeadingLogLine(xmlRaw);
        List<ElementText> elements = collectElementTexts(xml);
        List<XmlSuggestion> out = new ArrayList<>();

        for (ElementText et : elements) {
            var list = checker.checkText(et.text, "en-US");
            if (list == null)
                continue;
            for (var s : list) {
                out.add(new XmlSuggestion(et.id, s.token, s.candidates));
            }
        }

        if (out.isEmpty()) {
            System.out.println("拼写检查无错误");
            return;
        }

        System.out.println("拼写检查结果:");
        for (var s : out) {
            System.out.printf("元素 %s: \"%s\" -> 建议: %s%n",
                    s.elementId, s.token, String.join("/", s.candidates));
        }
    }

    private static String readFileSilently(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean looksLikeXml(String content) {
        if (content == null)
            return false;
        String head = content.stripLeading();
        if (head.startsWith("#")) {
            // 去掉首行注释再判断
            head = stripLeadingLogLine(head).stripLeading();
        }
        return head.startsWith("<?xml") || head.startsWith("<");
    }

    private static String stripLeadingLogLine(String content) {
        if (content == null)
            return null;
        int firstNewline = content.indexOf('\n');
        if (firstNewline >= 0) {
            String first = content.substring(0, firstNewline).trim();
            if (first.startsWith("#")) {
                return content.substring(firstNewline + 1);
            }
        } else {
            // 单行文件且为 # 开头
            if (content.trim().startsWith("#"))
                return "";
        }
        return content;
    }

    private static SpellChecker loadChecker() {
        for (SpellChecker sc : ServiceLoader.load(SpellChecker.class))
            return sc;
        return null;
    }

    // 解析 XML，收集每个带 id 的元素的所有文本（子树内 TEXT_NODE 合并）
    private static List<ElementText> collectElementTexts(String xml) {
        List<ElementText> out = new ArrayList<>();
        try {
            var f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            var b = f.newDocumentBuilder();
            var doc = b.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            Element root = doc.getDocumentElement();
            dfs(root, out);
        } catch (Exception e) {
            // 解析失败：回退为整文检查（无元素 id）
            out.add(new ElementText("(document)", xml));
        }
        return out;
    }

    private static void dfs(Element el, List<ElementText> out) {
        String id = el.getAttribute("id");
        String txt = collectText(el);
        if (id != null && !id.isBlank() && !txt.isBlank()) {
            out.add(new ElementText(id, txt));
        }
        NodeList nodes = el.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element child)
                dfs(child, out);
        }
    }

    private static String collectText(Element el) {
        NodeList nodes = el.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                String v = n.getNodeValue();
                if (v != null && !v.isBlank())
                    sb.append(v).append('\n');
            }
        }
        return sb.toString();
    }

    private record ElementText(String id, String text) {
    }

    private record XmlSuggestion(String elementId, String token, List<String> candidates) {
    }
}