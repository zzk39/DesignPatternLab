package com.team20.editor.domain.command;

import com.team20.editor.domain.workspace.Workspace;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * 简单的命令调度器 / 撤销重做管理器。
 * 仅记录实现了 UndoableCommand 的命令实例。
 */
public class CommandInvoker {

    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    /**
     * 新增：统一执行入口。普通命令只执行；可撤销命令自动入栈。
     */
    public synchronized void execute(Command cmd, Workspace workspace) {
        Objects.requireNonNull(cmd, "cmd");
        cmd.execute(workspace);
        if (cmd instanceof UndoableCommand uc) {
            undoStack.push(uc);
            redoStack.clear();
        }
    }

    /**
     * 兼容旧调用：执行并记录撤销信息。
     */

    /**
     * 执行一个可撤销命令并记录用于 undo。
     */
    public synchronized void executeAndRecord(UndoableCommand cmd, Workspace workspace) {
        Objects.requireNonNull(cmd, "cmd");
        cmd.execute(workspace);
        undoStack.push(cmd);
        redoStack.clear();
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 执行撤销（如果有）
     */
    public synchronized void undo(Workspace workspace) {
        if (!canUndo()) {
            throw new IllegalStateException("没有可撤销的命令");
        }
        UndoableCommand cmd = undoStack.pop();
        cmd.undo(workspace);
        redoStack.push(cmd);
    }

    /**
     * 重做上一次被撤销的命令（如果有）
     */
    public synchronized void redo(Workspace workspace) {
        if (!canRedo()) {
            throw new IllegalStateException("没有可重做的命令");
        }
        UndoableCommand cmd = redoStack.pop();
        cmd.redo(workspace);
        undoStack.push(cmd);
    }

    public synchronized void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }
}