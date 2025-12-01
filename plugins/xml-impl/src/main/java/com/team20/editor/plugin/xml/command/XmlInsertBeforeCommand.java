package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.editor.xml.XmlEditor;

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
        String args = String.format("%s %s %s%s", tag, newId, targetId, text != null ? " \"" + text + "\"" : "");
        ws.publishCommandEvent("insert-before", args);
    }

    @Override
    public void undo(Workspace ws) {
        requireXml(ws).delete(newId);
        // 记录撤销的原命令名
        ws.publishCommandEvent("undo", "insert-before");
    }

    @Override
    public void redo(Workspace ws) {
        requireXml(ws).insertBefore(tag, newId, targetId, text);
        // 记录重做的原命令名
        ws.publishCommandEvent("redo", "insert-before");
    }
}