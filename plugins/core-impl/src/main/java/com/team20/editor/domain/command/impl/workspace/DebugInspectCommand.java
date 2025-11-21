package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.bootstrap.ApplicationContext;
import com.team20.editor.extension.registry.DefaultCommandRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * debug-inspect -- prints active editor state to help diagnose dirty/save
 * issues.
 *
 * Usage: debug-inspect
 */
public class DebugInspectCommand implements Command {

    @Override
    public void execute(Workspace workspace) {
        try {
            Editor active = workspace.getActiveEditor();
            System.out.println("=== DebugInspectCommand ===");
            if (active == null) {
                System.out.println("No active editor");
                return;
            }
            String name = active.getName();
            System.out.println("Editor name: " + name);
            String content = null;
            try {
                content = active.getContent();
            } catch (Throwable t) {
                System.out.println("Failed to get editor content: " + t.getMessage());
            }
            System.out.println("In-memory content length: " + (content == null ? 0 : content.length()));
            if (content != null && content.length() > 0) {
                String snippet = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                System.out.println("Content (head):\n" + snippet);
            }

            // call isModified() directly
            try {
                boolean modified = active.isModified();
                System.out.println("isModified() => " + modified);
            } catch (Throwable t) {
                System.out.println("isModified() invocation failed: " + t.getMessage());
            }

            // CloseCommand.isEditorDirty (call via reflection to avoid compile deps)
            try {
                Class<?> cc = Class.forName("com.team20.editor.domain.command.impl.workspace.CloseCommand");
                java.lang.reflect.Method isDirty = cc.getMethod("isEditorDirty",
                        com.team20.editor.domain.editor.Editor.class);
                Object dirty = isDirty.invoke(null, active);
                System.out.println("CloseCommand.isEditorDirty(active) => " + String.valueOf(dirty));
            } catch (Throwable t) {
                System.out.println("Cannot call CloseCommand.isEditorDirty: " + t.getMessage());
            }

            // Try load disk content via PersistenceManager
            try {
                ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
                if (ctx != null && ctx.persistenceManager() != null && name != null && !name.isBlank()) {
                    try {
                        String disk = ctx.persistenceManager().load(name);
                        System.out.println("Disk content length: " + (disk == null ? 0 : disk.length()));
                        if (disk != null && disk.length() > 0) {
                            String ds = disk.length() > 200 ? disk.substring(0, 200) + "..." : disk;
                            System.out.println("Disk content (head):\n" + ds);
                        }
                    } catch (Throwable io) {
                        System.out.println("PersistenceManager.load failed: " + io.getMessage());
                    }
                } else {
                    // fallback to direct file read
                    if (name != null && !name.isBlank()) {
                        try {
                            Path p = Path.of(name);
                            if (Files.exists(p)) {
                                String disk = Files.readString(p);
                                System.out
                                        .println("Disk content length (direct): " + (disk == null ? 0 : disk.length()));
                            } else {
                                System.out.println("Disk file not found at path: " + p.toAbsolutePath());
                            }
                        } catch (Throwable t) {
                            System.out.println("Direct file read failed: " + t.getMessage());
                        }
                    } else {
                        System.out.println("Editor has no name; cannot read disk file");
                    }
                }
            } catch (Throwable t) {
                System.out.println("Disk check failed: " + t.getMessage());
            }

            System.out.println("=== End DebugInspect ===");
        } catch (Throwable t) {
            System.out.println("DebugInspectCommand error: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return "debug-inspect";
    }
}