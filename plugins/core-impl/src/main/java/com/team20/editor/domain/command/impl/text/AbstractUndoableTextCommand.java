package com.team20.editor.domain.command.impl.text;

import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.editor.text.TextEditor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;

/**
 * Base class for text-editing UndoableCommands.
 *
 * Responsibilities:
 * - take/restore snapshot for undo/redo
 * - call abstract apply(TextEditor) to perform actual change
 * - publish command events after apply
 * - mark editor as modified (try explicit API first, fallback to reflection)
 *
 * Concrete subclasses must implement:
 * - void apply(TextEditor editor) throws Exception
 * - String getCommandName()
 * - String formatArgs()
 */
public abstract class AbstractUndoableTextCommand implements UndoableCommand {

    protected TextEditor.EditorSnapshot beforeSnapshot;

    @Override
    public final void execute(Workspace workspace) {
        TextEditor editor = getTextEditor(workspace);

        // take snapshot
        beforeSnapshot = editor.createSnapshot();

        try {
            apply(editor);
        } catch (Throwable t) {
            throw new RuntimeException("执行命令失败: " + t.getMessage(), t);
        }

        // mark modified
        markEditorModified(editor);

        // publish event
        try {
            workspace.publishCommandEvent(getCommandName(), formatArgs());
        } catch (Throwable ignored) {
        }
    }

    @Override
    public final void undo(Workspace workspace) {
        if (beforeSnapshot == null) {
            throw new IllegalStateException("无法撤销：命令尚未执行");
        }
        TextEditor editor = getTextEditor(workspace);
        editor.restoreSnapshot(beforeSnapshot);

        try {
            workspace.publishCommandEvent("undo", getCommandName());
        } catch (Throwable ignored) {
        }
    }

    @Override
    public final void redo(Workspace workspace) {
        TextEditor.EditorSnapshot temp = beforeSnapshot;
        execute(workspace);
        beforeSnapshot = temp;
        try {
            workspace.publishCommandEvent("redo", getCommandName());
        } catch (Throwable ignored) {
        }
    }

    protected abstract void apply(TextEditor editor) throws Exception;

    protected abstract String getCommandName();

    /**
     * Format arguments for event publishing (default empty)
     */
    protected String formatArgs() {
        return "";
    }

    protected TextEditor getTextEditor(Workspace workspace) {
        if (workspace.getActiveEditor() == null) {
            throw new IllegalStateException("没有打开的文件");
        }
        if (!(workspace.getActiveEditor() instanceof TextEditor)) {
            throw new IllegalStateException("当前文件不是文本文件");
        }
        return (TextEditor) workspace.getActiveEditor();
    }

    /**
     * Mark the editor as modified.
     */
    protected void markEditorModified(TextEditor editor) {
        editor.setModified(true);
    }
}