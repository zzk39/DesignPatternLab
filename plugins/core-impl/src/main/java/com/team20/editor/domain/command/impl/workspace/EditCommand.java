package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;

/**
 * EditCommand: 切换活动文件。
 */
public class EditCommand implements Command {

    private final String filepath;

    public EditCommand(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public void execute(Workspace workspace) {
        if (filepath == null || filepath.isBlank()) {
            System.out.println("用法: edit <file>");
            return;
        }

        Editor existing = workspace.getEditor(filepath);
        if (existing == null) {
            System.out.println("文件未打开: " + filepath);
            return;
        }

        workspace.setActiveEditor(existing);

        // 🆕 发布编辑器激活事件（用于统计时长）
        try {
            workspace.publishCommandEvent("edit", filepath);
        } catch (Throwable ignored) {
        }

        System.out.println("切换到已打开文件: " + filepath);
    }

    @Override
    public String toString() {
        return "edit " + (filepath == null ? "" : filepath);
    }
}