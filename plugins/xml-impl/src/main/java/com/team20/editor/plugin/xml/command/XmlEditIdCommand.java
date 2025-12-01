package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;

public class XmlEditIdCommand implements UndoableCommand {
    private final String oldId;
    private final String newId;

    public XmlEditIdCommand(String oldId, String newId) {
        this.oldId = oldId;
        this.newId = newId;
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
        requireXml(ws).editId(oldId, newId);
        ws.publishCommandEvent("edit-id", oldId + " " + newId);
        System.out.println("OK");
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).editId(newId, oldId);
        ws.publishCommandEvent("undo", "edit-id");
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).editId(oldId, newId);
        ws.publishCommandEvent("redo", "edit-id");
    }
}