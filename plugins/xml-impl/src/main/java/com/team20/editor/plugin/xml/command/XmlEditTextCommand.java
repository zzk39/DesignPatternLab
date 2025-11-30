package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlEditTextCommand implements UndoableCommand {
    private final String elementId;
    private final String newText;
    private String oldText = null;

    public XmlEditTextCommand(String elementId, String newText) {
        this.elementId = elementId;
        this.newText = newText;
    }

    private static XmlEditor requireXml(Workspace ws) {
        Editor ed = ws.getActiveEditor();
        if (!(ed instanceof XmlEditor xe)) {
            throw new IllegalStateException("当前活动文件不是 XML 编辑器");
        }
        return xe;
    }

    private static String collectText(Element el) {
        NodeList children = el.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue() != null) {
                sb.append(n.getNodeValue());
            }
        }
        return sb.toString();
    }

    @Override
    public void execute(Workspace ws) {
        XmlEditor xe = requireXml(ws);
        Element el = xe.getById(elementId);
        if (el == null)
            throw new IllegalArgumentException("元素不存在: " + elementId);
        oldText = collectText(el);
        xe.editText(elementId, newText);
        System.out.println("OK");
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).editText(elementId, oldText);
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).editText(elementId, newText);
    }
}