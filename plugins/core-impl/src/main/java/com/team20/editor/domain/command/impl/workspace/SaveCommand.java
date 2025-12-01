package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.infrastructure.persistence.PersistenceManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * save [file|all]：保存当前活跃编辑器到磁盘，或保存指定文件，或保存所有已打开的文件。
 *
 * 规则：
 * - 不带参数：保存当前活动编辑器
 * - 参数为 "all"（不区分大小写）：保存所有打开的编辑器
 * - 参数为文件路径：
 *   - 如果该路径对应 workspace 中已打开的编辑器（editor.getName() 相等），则保存该编辑器内容到它的路径；
 *   - 否则将当前活动编辑器内容保存为指定路径（"另存为" 行为）。
 *
 * 保存成功后会尝试清除编辑器的修改标记（若编辑器实现了 setModified(boolean) 方法）。
 * 并发布 command 事件（workspace.publishCommandEvent）以确保日志能记录 save 操作。
 *
 * 注意：不再引用 XML 编辑器类型，避免跨模块依赖。XmlEditor 已在其 content() 中自行决定是否还原首行 "# log"。
 */
public class SaveCommand implements Command {

    private final PersistenceManager pm;
    private String filepath;

    public SaveCommand(PersistenceManager pm) {
        this.pm = pm;
    }

    public SaveCommand(PersistenceManager pm, String filepath) {
        this.pm = pm;
        this.filepath = filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public void execute(Workspace workspace) {
        try {
            if (filepath == null || filepath.isBlank()) {
                // save current active editor
                saveActive(workspace);
            } else if ("all".equalsIgnoreCase(filepath.trim())) {
                saveAll(workspace);
            } else {
                // try to find an opened editor matching the filepath (by name)
                Editor target = workspace.getEditor(filepath);
                if (target != null) {
                    saveEditor(target, workspace, filepath);
                } else {
                    // treat as "save as" for active editor
                    saveAsActive(workspace, filepath);
                }
            }
        } catch (Exception e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }

    private void saveActive(Workspace workspace) {
        Editor active = workspace.getActiveEditor();
        if (active == null) {
            System.out.println("没有打开的文件");
            return;
        }
        Path target = Paths.get(active.getName());
        try {
            String out = composeOutput(active, target);
            pm.writeFile(target, out);
            clearModifiedFlag(active);
            System.out.println("已保存到: " + target);
            workspace.publishWorkspaceEvent("fileSaved", target.toString());
            // publish command event so logging records the save
            try {
                workspace.publishCommandEvent("save", target.toString());
            } catch (Throwable ignored) {
            }
        } catch (Exception e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }

    private void saveAll(Workspace workspace) {
        if (!workspace.hasEditors()) {
            System.out.println("没有打开的文件");
            return;
        }
        int success = 0;
        int fail = 0;
        for (Editor e : workspace.getEditors()) {
            Path target = Paths.get(e.getName());
            try {
                String out = composeOutput(e, target);
                pm.writeFile(target, out);
                clearModifiedFlag(e);
                success++;
                // publish per-file command event so logging records each save
                try {
                    workspace.publishCommandEvent("save", target.toString());
                } catch (Throwable ignored) {
                }
            } catch (Exception ex) {
                System.out.println("保存 " + target + " 失败: " + ex.getMessage());
                fail++;
            }
        }
        System.out.printf("保存完成: 成功=%d, 失败=%d%n", success, fail);
        workspace.publishWorkspaceEvent("filesSaved", java.util.Map.of("success", success, "fail", fail));
    }

    private void saveAsActive(Workspace workspace, String path) {
        Editor active = workspace.getActiveEditor();
        if (active == null) {
            System.out.println("没有打开的文件可另存为: " + path);
            return;
        }
        Path target = Paths.get(path);
        try {
            String out = composeOutput(active, target);
            pm.writeFile(target, out);
            // optionally update editor name? We keep original name; this is "save as"
            clearModifiedFlag(active);
            System.out.println("已保存到: " + path);
            workspace.publishWorkspaceEvent("fileSaved", path);
            try {
                workspace.publishCommandEvent("save", path);
            } catch (Throwable ignored) {
            }
        } catch (Exception e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }

    private void saveEditor(Editor editor, Workspace workspace, String targetPath) {
        Path target = Paths.get(targetPath);
        try {
            String out = composeOutput(editor, target);
            pm.writeFile(target, out);
            clearModifiedFlag(editor);
            System.out.println("已保存: " + targetPath);
            workspace.publishWorkspaceEvent("fileSaved", targetPath);
            try {
                workspace.publishCommandEvent("save", targetPath);
            } catch (Throwable ignored) {
            }
        } catch (Exception e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }

    /**
     * 组合输出内容：
     * - 由具体编辑器提供最终文本（XmlEditor 会在其 content() 中按需补回原始 "# log" 首行）。
     * - 这里不判断编辑器类型，避免跨模块引用导致编译失败。
     */
    private String composeOutput(Editor editor, Path targetPath) {
        String content = editor.getContent();
        return content == null ? "" : content;
    }

    /**
     * 清除编辑器的已修改标记。
     */
    private void clearModifiedFlag(Editor editor) {
        if (editor == null)
            return;
        editor.setModified(false);
    }

    @Override
    public String toString() {
        return "save " + (filepath == null ? "" : filepath);
    }
}