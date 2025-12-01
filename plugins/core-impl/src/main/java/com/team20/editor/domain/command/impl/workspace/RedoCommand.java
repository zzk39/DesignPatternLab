package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.bootstrap.ApplicationContext;

public class RedoCommand implements Command {

    @Override
    public void execute(Workspace workspace) {
        ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
        if (ctx == null) {
            System.out.println("无法执行 redo：ApplicationContext 未就绪");
            return;
        }
        var invoker = ctx.commandInvoker();
        if (invoker == null) {
            System.out.println("无法执行 redo：CommandInvoker 未就绪");
            return;
        }

        try {
            if (!invoker.canRedo()) {
                System.out.println("没有可重做的命令");
                return;
            }
            invoker.redo(workspace);
            System.out.println("重做成功");
        } catch (Throwable t) {
            System.out.println("重做失败: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return "redo";
    }
}