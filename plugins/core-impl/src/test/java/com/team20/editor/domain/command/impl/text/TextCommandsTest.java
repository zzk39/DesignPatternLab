package com.team20.editor.domain.command.impl.text;

import com.team20.editor.domain.editor.text.TextEditor;
import com.team20.editor.domain.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本编辑命令单元测试
 */
public class TextCommandsTest {

    private Workspace workspace;
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        editor = new TextEditor("test.txt");
        editor.loadContent("Hello World\nThis is line 2\nThis is line 3");
        workspace.addEditor(editor);
        workspace.setActiveEditor(editor);
    }

    // =========== AppendCommand 测试 ===========

    @Test
    void testAppendCommandExecute() {
        AppendCommand cmd = new AppendCommand("New line");
        cmd.execute(workspace);

        assertTrue(editor.getContent().contains("New line"));
        assertTrue(editor.isModified());
    }

    @Test
    void testAppendCommandNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AppendCommand(null));
    }

    @Test
    void testAppendCommandToString() {
        AppendCommand cmd = new AppendCommand("test text");
        String str = cmd.toString();
        assertTrue(str.contains("append"));
        assertTrue(str.contains("test text"));
    }

    @Test
    void testAppendCommandUndo() {
        String originalContent = editor.getContent();
        AppendCommand cmd = new AppendCommand("New line");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertEquals(originalContent, editor.getContent());
    }

    @Test
    void testAppendCommandRedo() {
        AppendCommand cmd = new AppendCommand("New line");
        cmd.execute(workspace);
        String afterExecute = editor.getContent();
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertTrue(editor.getContent().contains("New line"));
    }

    // =========== InsertCommand 测试 ===========

    @Test
    void testInsertCommandExecute() {
        InsertCommand cmd = new InsertCommand(1, 6, " Beautiful");
        cmd.execute(workspace);

        assertEquals("Hello Beautiful World", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertCommandInvalidLineThrows() {
        assertThrows(IllegalArgumentException.class, () -> new InsertCommand(0, 1, "text"));
    }

    @Test
    void testInsertCommandInvalidColThrows() {
        assertThrows(IllegalArgumentException.class, () -> new InsertCommand(1, 0, "text"));
    }

    @Test
    void testInsertCommandNullTextThrows() {
        assertThrows(IllegalArgumentException.class, () -> new InsertCommand(1, 1, null));
    }

    @Test
    void testInsertCommandToString() {
        InsertCommand cmd = new InsertCommand(1, 5, "test");
        String str = cmd.toString();
        assertTrue(str.contains("insert"));
        assertTrue(str.contains("1:5"));
        assertTrue(str.contains("test"));
    }

    @Test
    void testInsertCommandUndo() {
        String originalLine = editor.getLine(1);
        InsertCommand cmd = new InsertCommand(1, 6, "X");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertEquals(originalLine, editor.getLine(1));
    }

    // =========== DeleteCommand 测试 ===========

    @Test
    void testDeleteCommandExecute() {
        DeleteCommand cmd = new DeleteCommand(1, 1, 6);
        cmd.execute(workspace);

        assertEquals("World", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testDeleteCommandInvalidLineThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteCommand(0, 1, 1));
    }

    @Test
    void testDeleteCommandInvalidColThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteCommand(1, 0, 1));
    }

    @Test
    void testDeleteCommandNegativeLengthThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteCommand(1, 1, -1));
    }

    @Test
    void testDeleteCommandToString() {
        DeleteCommand cmd = new DeleteCommand(1, 5, 3);
        String str = cmd.toString();
        assertTrue(str.contains("delete"));
        assertTrue(str.contains("1:5"));
        assertTrue(str.contains("3"));
    }

    @Test
    void testDeleteCommandUndo() {
        String originalLine = editor.getLine(1);
        DeleteCommand cmd = new DeleteCommand(1, 1, 5);
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertEquals(originalLine, editor.getLine(1));
    }

    // =========== ReplaceCommand 测试 ===========

    @Test
    void testReplaceCommandExecute() {
        ReplaceCommand cmd = new ReplaceCommand(1, 7, 5, "Universe");
        cmd.execute(workspace);

        assertEquals("Hello Universe", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testReplaceCommandToString() {
        ReplaceCommand cmd = new ReplaceCommand(1, 5, 3, "new");
        String str = cmd.toString();
        assertTrue(str.contains("replace"));
    }

    @Test
    void testReplaceCommandUndo() {
        String originalLine = editor.getLine(1);
        ReplaceCommand cmd = new ReplaceCommand(1, 1, 5, "Goodbye");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertEquals(originalLine, editor.getLine(1));
    }

    // =========== ShowCommand 测试 ===========

    @Test
    void testShowCommandExecuteAll() {
        ShowCommand cmd = new ShowCommand(null, null);
        cmd.execute(workspace);
        // ShowCommand只是显示，不修改内容
        assertFalse(editor.isModified());
    }

    @Test
    void testShowCommandExecuteRange() {
        ShowCommand cmd = new ShowCommand(1, 2);
        cmd.execute(workspace);
        // ShowCommand只是显示，不修改内容
        assertFalse(editor.isModified());
    }

    @Test
    void testShowCommandToString() {
        ShowCommand cmd = new ShowCommand(1, 5);
        String str = cmd.toString();
        assertTrue(str.contains("show"));
    }

    // =========== 边界情况测试 ===========

    @Test
    void testCommandWithNoActiveEditor() {
        Workspace emptyWorkspace = new Workspace();
        AppendCommand cmd = new AppendCommand("test");

        assertThrows(RuntimeException.class, () -> cmd.execute(emptyWorkspace));
    }

    @Test
    void testUndoWithoutExecuteThrows() {
        AppendCommand cmd = new AppendCommand("test");
        assertThrows(IllegalStateException.class, () -> cmd.undo(workspace));
    }
}
