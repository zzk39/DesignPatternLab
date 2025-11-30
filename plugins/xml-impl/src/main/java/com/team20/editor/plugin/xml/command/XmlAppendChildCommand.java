package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;

public class XmlAppendChildCommand implements UndoableCommand {
    private final String tag;
    private final String newId;
    private final String parentId;
    private final String text;

    public XmlAppendChildCommand(String tag, String newId, String parentId, String text) {
        this.tag = tag;
        this.newId = newId;
        this.parentId = parentId;
        this.text = text;
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
        requireXml(ws).appendChild(tag, newId, parentId, text);
        System.out.println("OK");
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).delete(newId);
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).appendChild(tag, newId, parentId, text);
    }
}