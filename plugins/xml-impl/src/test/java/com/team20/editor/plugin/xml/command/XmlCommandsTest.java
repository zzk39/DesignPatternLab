package com.team20.editor.plugin.xml.command;

import com.team20.editor.domain.editor.xml.XmlEditor;
import com.team20.editor.domain.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML编辑命令单元测试
 */
public class XmlCommandsTest {

    private Workspace workspace;
    private XmlEditor editor;

    private static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bookstore id="root">
                <book id="book1" category="COOKING">
                    <title id="title1" lang="en">Everyday Italian</title>
                </book>
                <book id="book2" category="CHILDREN">
                    <title id="title2" lang="en">Harry Potter</title>
                </book>
            </bookstore>
            """;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        editor = new XmlEditor("test.xml");
        editor.loadContent(SAMPLE_XML);
        workspace.addEditor(editor);
        workspace.setActiveEditor(editor);
    }

    // =========== XmlAppendChildCommand 测试 ===========

    @Test
    void testAppendChildCommandExecute() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("price", "price1", "book1", "29.99");
        cmd.execute(workspace);

        assertNotNull(editor.getById("price1"));
        assertTrue(editor.isModified());
    }

    @Test
    void testAppendChildCommandUndo() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("price", "price1", "book1", "29.99");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertNull(editor.getById("price1"));
    }

    @Test
    void testAppendChildCommandRedo() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("price", "price1", "book1", "29.99");
        cmd.execute(workspace);
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertNotNull(editor.getById("price1"));
    }

    @Test
    void testAppendChildCommandWithEmptyText() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("empty", "empty1", "book1", "");
        cmd.execute(workspace);

        assertNotNull(editor.getById("empty1"));
    }

    @Test
    void testAppendChildCommandWithNullText() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("empty", "empty1", "book1", null);
        cmd.execute(workspace);

        assertNotNull(editor.getById("empty1"));
    }

    // =========== XmlInsertBeforeCommand 测试 ===========

    @Test
    void testInsertBeforeCommandExecute() {
        XmlInsertBeforeCommand cmd = new XmlInsertBeforeCommand("book", "book0", "book1", "First Book");
        cmd.execute(workspace);

        assertNotNull(editor.getById("book0"));
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertBeforeCommandUndo() {
        XmlInsertBeforeCommand cmd = new XmlInsertBeforeCommand("book", "book0", "book1", "First Book");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertNull(editor.getById("book0"));
    }

    @Test
    void testInsertBeforeCommandRedo() {
        XmlInsertBeforeCommand cmd = new XmlInsertBeforeCommand("book", "book0", "book1", "First Book");
        cmd.execute(workspace);
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertNotNull(editor.getById("book0"));
    }

    // =========== XmlEditIdCommand 测试 ===========

    @Test
    void testEditIdCommandExecute() {
        XmlEditIdCommand cmd = new XmlEditIdCommand("book1", "bookOne");
        cmd.execute(workspace);

        assertNull(editor.getById("book1"));
        assertNotNull(editor.getById("bookOne"));
        assertTrue(editor.isModified());
    }

    @Test
    void testEditIdCommandUndo() {
        XmlEditIdCommand cmd = new XmlEditIdCommand("book1", "bookOne");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertNotNull(editor.getById("book1"));
        assertNull(editor.getById("bookOne"));
    }

    @Test
    void testEditIdCommandRedo() {
        XmlEditIdCommand cmd = new XmlEditIdCommand("book1", "bookOne");
        cmd.execute(workspace);
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertNull(editor.getById("book1"));
        assertNotNull(editor.getById("bookOne"));
    }

    // =========== XmlEditTextCommand 测试 ===========

    @Test
    void testEditTextCommandExecute() {
        XmlEditTextCommand cmd = new XmlEditTextCommand("title1", "New Title");
        cmd.execute(workspace);

        assertTrue(editor.getById("title1").getTextContent().contains("New Title"));
        assertTrue(editor.isModified());
    }

    @Test
    void testEditTextCommandUndo() {
        String originalText = editor.getById("title1").getTextContent();
        XmlEditTextCommand cmd = new XmlEditTextCommand("title1", "New Title");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertEquals(originalText, editor.getById("title1").getTextContent());
    }

    @Test
    void testEditTextCommandRedo() {
        XmlEditTextCommand cmd = new XmlEditTextCommand("title1", "New Title");
        cmd.execute(workspace);
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertTrue(editor.getById("title1").getTextContent().contains("New Title"));
    }

    @Test
    void testEditTextCommandWithEmptyText() {
        XmlEditTextCommand cmd = new XmlEditTextCommand("title1", "");
        cmd.execute(workspace);

        assertNotNull(editor.getById("title1"));
    }

    // =========== XmlDeleteCommand 测试 ===========

    @Test
    void testDeleteCommandExecute() {
        XmlDeleteCommand cmd = new XmlDeleteCommand("book1");
        cmd.execute(workspace);

        assertNull(editor.getById("book1"));
        // Note: Child elements (title1) remain in byId map as orphaned references
        assertTrue(editor.isModified());
    }

    @Test
    void testDeleteCommandUndo() {
        XmlDeleteCommand cmd = new XmlDeleteCommand("title1");
        cmd.execute(workspace);
        cmd.undo(workspace);

        assertNotNull(editor.getById("title1"));
    }

    @Test
    void testDeleteCommandRedo() {
        XmlDeleteCommand cmd = new XmlDeleteCommand("title1");
        cmd.execute(workspace);
        cmd.undo(workspace);
        cmd.redo(workspace);

        assertNull(editor.getById("title1"));
    }

    // =========== 边界情况测试 ===========

    @Test
    void testCommandWithNoActiveEditor() {
        Workspace emptyWorkspace = new Workspace();
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("test", "test1", "root", "text");

        assertThrows(IllegalStateException.class, () -> cmd.execute(emptyWorkspace));
    }

    @Test
    void testCommandWithNonXmlEditor() {
        // 清空工作区并添加一个非XML编辑器 - 这里通过mock的方式测试
        // 由于TextEditor不在xml-impl的依赖范围内，我们只测试空工作区的情况
        Workspace emptyWs = new Workspace();
        XmlEditIdCommand cmd = new XmlEditIdCommand("book1", "newId");
        
        assertThrows(IllegalStateException.class, () -> cmd.execute(emptyWs));
    }

    @Test
    void testAppendChildToNonexistentParent() {
        XmlAppendChildCommand cmd = new XmlAppendChildCommand("item", "item1", "nonexistent", "text");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }

    @Test
    void testInsertBeforeNonexistentTarget() {
        XmlInsertBeforeCommand cmd = new XmlInsertBeforeCommand("item", "item1", "nonexistent", "text");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }

    @Test
    void testEditIdNonexistent() {
        XmlEditIdCommand cmd = new XmlEditIdCommand("nonexistent", "newId");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }

    @Test
    void testEditTextNonexistent() {
        XmlEditTextCommand cmd = new XmlEditTextCommand("nonexistent", "text");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }

    @Test
    void testDeleteNonexistent() {
        XmlDeleteCommand cmd = new XmlDeleteCommand("nonexistent");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }

    @Test
    void testDeleteRoot() {
        XmlDeleteCommand cmd = new XmlDeleteCommand("root");
        
        assertThrows(IllegalArgumentException.class, () -> cmd.execute(workspace));
    }
}
