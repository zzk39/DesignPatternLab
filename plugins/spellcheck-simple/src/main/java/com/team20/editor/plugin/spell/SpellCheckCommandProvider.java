package com.team20.editor.plugin.spell;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.command.CommandDescriptor;
import com.team20.editor.extension.registry.CommandRegistry;
import com.team20.editor.extension.spi.command.CommandProvider;
import com.team20.editor.extension.spi.spellcheck.SpellChecker;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

import java.util.List;
import java.util.ServiceLoader;

public class SpellCheckCommandProvider implements CommandProvider {

    @Override
    public String getProviderName() {
        return "spell-check";
    }

    // 精确匹配：返回 List<domain.command.CommandDescriptor>
    @Override
    public List<CommandDescriptor> getCommandDescriptors() {
        // 使用静态工厂方法，描述字符串 + undoable=false
        return List.of(CommandDescriptor.of(
                "spell-check",
                () -> (Command) SpellCheckCommandProvider::run,
                "Check spelling for active file (text or xml). Usage: spell-check"));
    }

    // 可选：也可以在此注册工厂（若你的注册器支持该生命周期）
    @Override
    public void registerFactories(CommandRegistry registry) {
        registry.registerFactory("spell-check", raw -> (Command) SpellCheckCommandProvider::run);
    }

    private static void run(Workspace ws) {
        SpellChecker checker = loadChecker();
        if (checker == null) {
            System.out.println("拼写检查器不可用");
            return;
        }

        Editor ed = ws.getActiveEditor();
        if (ed == null) {
            System.out.println("无活动文件");
            return;
        }

        String content = ed.getContent();
        boolean isXml = ed.getClass().getName().endsWith(".xml.XmlEditor");

        var suggestions = isXml ? checker.checkText(extractXmlText(content), "en")
                : checker.checkText(content, "en");

        if (suggestions.isEmpty()) {
            System.out.println("拼写检查无错误");
        } else {
            System.out.println("拼写检查结果:");
            for (var s : suggestions) {
                if (s.line > 0 && s.column > 0)
                    System.out.printf("第%d行，第%d列: \"%s\" -> 建议: %s%n",
                            s.line, s.column, s.token, String.join("/", s.candidates));
                else
                    System.out.printf("\"%s\" -> 建议: %s%n", s.token, String.join("/", s.candidates));
            }
        }
    }

    private static String extractXmlText(String xml) {
        try {
            var f = DocumentBuilderFactory.newInstance();
            var b = f.newDocumentBuilder();
            var doc = b.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            StringBuilder sb = new StringBuilder();
            visit(doc.getDocumentElement(), sb);
            return sb.toString();
        } catch (Exception e) {
            return xml;
        }
    }

    private static void visit(Element el, StringBuilder sb) {
        var nodes = el.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            var n = nodes.item(i);
            if (n.getNodeType() == Node.TEXT_NODE)
                sb.append(n.getNodeValue()).append('\n');
            else if (n instanceof Element c)
                visit(c, sb);
        }
    }

    private static SpellChecker loadChecker() {
        for (SpellChecker sc : ServiceLoader.load(SpellChecker.class))
            return sc;
        return null;
    }
}