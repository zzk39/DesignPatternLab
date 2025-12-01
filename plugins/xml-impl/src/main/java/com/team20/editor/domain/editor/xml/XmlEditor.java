package com.team20.editor.domain.editor.xml;

import com.team20.editor.domain.editor.AbstractEditor;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class XmlEditor extends AbstractEditor {

    private Document doc;
    private final Map<String, Element> byId = new HashMap<>();

    // 记录首行日志头是否存在，以及其原始文本（由 loadContent 捕获）
    private boolean headerPresent = false;
    private String originalHeaderLine = null;

    public XmlEditor(String name) {
        super(name);
        setModified(false);
    }

    @Override
    protected String content() {
        // 如果文件原本就带 # log 头，则在返回内容时补回原始首行
        String xml = serialize();
        if (headerPresent && originalHeaderLine != null && !originalHeaderLine.isBlank()) {
            // 避免重复：若序列化出的内容首行已是 '#'
            String first = firstLine(xml);
            if (first != null && first.trim().startsWith("#")) {
                return xml;
            }
            return originalHeaderLine + "\n" + xml;
        }
        return xml;
    }

    private String firstLine(String s) {
        if (s == null)
            return null;
        int nl = s.indexOf('\n');
        return (nl >= 0) ? s.substring(0, nl) : s;
    }

    @Override
    public void loadContent(String content) {
        try {
            // 允许首行 '# log ...'，剥离后再解析，并记录原始头部
            String effective = normalizeContentAndCaptureHeader(content);
            if (effective == null || effective.isBlank()) {
                String init = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <root id="root">
                        </root>
                        """;
                parse(init);
            } else {
                parse(effective);
            }
            setModified(false);
        } catch (Exception e) {
            throw new IllegalArgumentException("XML 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 规范化内容并捕获首行日志头：
     * 1. 去除 UTF-8 BOM
     * 2. 若首行以 '#' 开头（# log 或 # log -e ...），记录 originalHeaderLine 并剥离首行
     */
    private String normalizeContentAndCaptureHeader(String raw) {
        headerPresent = false;
        originalHeaderLine = null;

        if (raw == null)
            return null;
        String s = stripBom(raw);
        int nl = s.indexOf('\n');
        if (nl >= 0) {
            String first = s.substring(0, nl).trim();
            if (first.startsWith("#")) {
                headerPresent = true;
                originalHeaderLine = first;
                return s.substring(nl + 1);
            }
        } else {
            // 单行文件且以 # 开头，剥离后为空
            if (s.trim().startsWith("#")) {
                headerPresent = true;
                originalHeaderLine = s.trim();
                return "";
            }
        }
        return s;
    }

    private String stripBom(String s) {
        if (s.startsWith("\uFEFF"))
            return s.substring(1);
        return s;
    }

    private void parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        f.setIgnoringComments(true);
        f.setCoalescing(true);
        f.setValidating(false);
        DocumentBuilder b = f.newDocumentBuilder();
        this.doc = b.parse(new InputSource(new StringReader(xml)));
        rebuildIndex();
    }

    private void rebuildIndex() {
        byId.clear();
        Element root = getRoot();
        if (root != null) {
            traverse(root, el -> {
                String id = el.getAttribute("id");
                if (id != null && !id.isBlank()) {
                    byId.put(id, el);
                }
            });
        }
    }

    private void traverse(Element el, Consumer<Element> fn) {
        fn.accept(el);
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element child) {
                traverse(child, fn);
            }
        }
    }

    private String serialize() {
        if (doc == null)
            return "";
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter w = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(w));
            return w.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public Element getRoot() {
        return (doc == null) ? null : doc.getDocumentElement();
    }

    public Element getById(String id) {
        return byId.get(id);
    }

    // 供其他插件查询（若需要）
    public boolean hasLoggingHeaderPresent() {
        return headerPresent;
    }

    public String getOriginalLogHeaderLine() {
        return originalHeaderLine;
    }

    public void insertBefore(String tag, String newId, String targetId, String text) {
        if (byId.containsKey(newId))
            throw new IllegalArgumentException("元素ID已存在: " + newId);
        Element target = byId.get(targetId);
        if (target == null)
            throw new IllegalArgumentException("目标元素不存在: " + targetId);
        if (target == getRoot())
            throw new IllegalArgumentException("不能在根元素前插入元素");

        Element parent = (Element) target.getParentNode();
        Element neo = doc.createElement(tag);
        neo.setAttribute("id", newId);
        if (text != null)
            neo.appendChild(doc.createTextNode(text));
        parent.insertBefore(neo, target);
        byId.put(newId, neo);
        setModified(true);
    }

    public void appendChild(String tag, String newId, String parentId, String text) {
        if (byId.containsKey(newId))
            throw new IllegalArgumentException("元素ID已存在: " + newId);
        Element parent = byId.get(parentId);
        if (parent == null)
            throw new IllegalArgumentException("父元素不存在: " + parentId);

        Element neo = doc.createElement(tag);
        neo.setAttribute("id", newId);
        if (text != null)
            neo.appendChild(doc.createTextNode(text));
        parent.appendChild(neo);
        byId.put(newId, neo);
        setModified(true);
    }

    public void editId(String oldId, String newId) {
        Element el = byId.get(oldId);
        if (el == null)
            throw new IllegalArgumentException("元素不存在: " + oldId);
        if (byId.containsKey(newId))
            throw new IllegalArgumentException("目标ID已存在: " + newId);
        el.setAttribute("id", newId);
        byId.remove(oldId);
        byId.put(newId, el);
        setModified(true);
    }

    public void editText(String id, String text) {
        Element el = byId.get(id);
        if (el == null)
            throw new IllegalArgumentException("元素不存在: " + id);
        NodeList children = el.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                el.removeChild(n);
            }
        }
        if (text != null) {
            el.appendChild(doc.createTextNode(text));
        }
        setModified(true);
    }

    public void delete(String id) {
        Element el = byId.get(id);
        if (el == null)
            throw new IllegalArgumentException("元素不存在: " + id);
        if (el == getRoot())
            throw new IllegalArgumentException("不能删除根元素");
        Element parent = (Element) el.getParentNode();
        parent.removeChild(el);
        byId.remove(id);
        setModified(true);
    }
}