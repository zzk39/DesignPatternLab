package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;

/**
 * insert-before <tag> <newId> <targetId> ["text"]
 */
public class XmlInsertBeforeCommand implements UndoableCommand {
    private final String tag;
    private final String newId;
    private final String targetId;
    private final String text;

    public XmlInsertBeforeCommand(String tag, String newId, String targetId, String text) {
        this.tag = tag;
        this.newId = newId;
        this.targetId = targetId;
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
        requireXml(ws).insertBefore(tag, newId, targetId, text);
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).delete(newId);
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).insertBefore(tag, newId, targetId, text);
    }
}