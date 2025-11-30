package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;

public class XmlDeleteCommand implements UndoableCommand {
    private final String elementId;
    private String beforeSnapshot; // 整文档快照（简化实现）

    public XmlDeleteCommand(String elementId) {
        this.elementId = elementId;
    }

    private static XmlEditor requireXml(Workspace ws) {
        Editor ed = ws.getActiveEditor();
        if (!(ed instanceof XmlEditor xe)) {
            throw new IllegalStateException("当前活动文件不是 XML 编辑器");
        }
        return xe;
    }

    @Override
    public void execute(Workspace ws) {
        XmlEditor xe = requireXml(ws);
        beforeSnapshot = xe.getContent();
        xe.delete(elementId);
        System.out.println("OK");
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).loadContent(beforeSnapshot);
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).delete(elementId);
    }
}