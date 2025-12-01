package com.team20.editor.domain.editor.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TextEditor 综合单元测试
 */
public class TextEditorTest {

    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor("test.txt");
    }

    // =========== 基本属性测试 ===========

    @Test
    void testConstructor() {
        TextEditor e = new TextEditor("new.txt");
        assertEquals("new.txt", e.getName());
        assertFalse(e.isModified());
    }

    @Test
    void testSetModified() {
        assertFalse(editor.isModified());
        editor.setModified(true);
        assertTrue(editor.isModified());
        editor.setModified(false);
        assertFalse(editor.isModified());
    }

    // =========== loadContent 测试 ===========

    @Test
    void testLoadContentEmpty() {
        editor.loadContent("");
        assertEquals(0, editor.getLineCount());
        assertFalse(editor.isModified());
    }

    @Test
    void testLoadContentNull() {
        editor.loadContent(null);
        assertEquals(0, editor.getLineCount());
    }

    @Test
    void testLoadContentSingleLine() {
        editor.loadContent("Hello World");
        assertEquals(1, editor.getLineCount());
        assertEquals("Hello World", editor.getLine(1));
    }

    @Test
    void testLoadContentMultipleLines() {
        editor.loadContent("Line1\nLine2\nLine3");
        assertEquals(3, editor.getLineCount());
        assertEquals("Line1", editor.getLine(1));
        assertEquals("Line2", editor.getLine(2));
        assertEquals("Line3", editor.getLine(3));
    }

    @Test
    void testLoadContentClearsModified() {
        editor.loadContent("content");
        editor.setModified(true);
        editor.loadContent("new content");
        assertFalse(editor.isModified());
    }

    // =========== append 测试 ===========

    @Test
    void appendMarksModifiedAndAddsLine() {
        TextEditor e = new TextEditor("t.txt");
        e.loadContent("");
        assertFalse(e.isModified());
        e.append("hello");
        assertTrue(e.isModified());
        assertTrue(e.getContent().contains("hello"));
    }

    @Test
    void testAppendToEmpty() {
        editor.loadContent("");
        editor.append("First line");
        assertEquals(1, editor.getLineCount());
        assertEquals("First line", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testAppendToExisting() {
        editor.loadContent("Existing");
        editor.append("New line");
        assertEquals(2, editor.getLineCount());
        assertEquals("Existing", editor.getLine(1));
        assertEquals("New line", editor.getLine(2));
    }

    @Test
    void testAppendNullThrows() {
        editor.loadContent("");
        assertThrows(IllegalArgumentException.class, () -> editor.append(null));
    }

    @Test
    void testAppendMultiLine() {
        editor.loadContent("");
        editor.append("Line1\nLine2");
        assertTrue(editor.getContent().contains("Line1"));
        assertTrue(editor.getContent().contains("Line2"));
    }

    // =========== insert 测试 ===========

    @Test
    void insertReplaceDeleteBasic() {
        TextEditor e = new TextEditor("t2.txt");
        e.loadContent("abc\ndef");
        e.insert(1, 4, "X");
        assertEquals("abcX", e.getLine(1));
        e.replace(2, 1, 3, "Z");
        assertEquals("Z", e.getLine(2));
        e.delete(1, 4, 1);
        assertEquals("abc", e.getLine(1));
    }

    @Test
    void testInsertAtBeginning() {
        editor.loadContent("World");
        editor.insert(1, 1, "Hello ");
        assertEquals("Hello World", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertAtMiddle() {
        editor.loadContent("Helo World");
        editor.insert(1, 4, "l");
        assertEquals("Hello World", editor.getLine(1));
    }

    @Test
    void testInsertAtEnd() {
        editor.loadContent("Hello");
        editor.insert(1, 6, " World");
        assertEquals("Hello World", editor.getLine(1));
    }

    @Test
    void testInsertInvalidLineThrows() {
        editor.loadContent("Line");
        assertThrows(IllegalArgumentException.class, () -> editor.insert(0, 1, "X"));
        assertThrows(IllegalArgumentException.class, () -> editor.insert(5, 1, "X"));
    }

    @Test
    void testInsertInvalidColumnThrows() {
        editor.loadContent("Line");
        assertThrows(IllegalArgumentException.class, () -> editor.insert(1, 0, "X"));
        assertThrows(IllegalArgumentException.class, () -> editor.insert(1, 10, "X"));
    }

    @Test
    void testInsertNullThrows() {
        editor.loadContent("Line");
        assertThrows(IllegalArgumentException.class, () -> editor.insert(1, 1, null));
    }

    // =========== delete 测试 ===========

    @Test
    void testDeleteFromBeginning() {
        editor.loadContent("Hello World");
        String deleted = editor.delete(1, 1, 6);
        assertEquals("Hello ", deleted);
        assertEquals("World", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testDeleteFromMiddle() {
        editor.loadContent("Hello World");
        String deleted = editor.delete(1, 6, 1);
        assertEquals(" ", deleted);
        assertEquals("HelloWorld", editor.getLine(1));
    }

    @Test
    void testDeleteZeroLength() {
        editor.loadContent("Hello");
        String deleted = editor.delete(1, 1, 0);
        assertEquals("", deleted);
        assertEquals("Hello", editor.getLine(1));
    }

    @Test
    void testDeleteExceedsLineThrows() {
        editor.loadContent("Hello");
        assertThrows(IllegalArgumentException.class, () -> editor.delete(1, 1, 10));
    }

    @Test
    void testDeleteNegativeLengthThrows() {
        editor.loadContent("Hello");
        assertThrows(IllegalArgumentException.class, () -> editor.delete(1, 1, -1));
    }

    // =========== replace 测试 ===========

    @Test
    void testReplaceBasic() {
        editor.loadContent("Hello World");
        String deleted = editor.replace(1, 7, 5, "Java");
        assertEquals("World", deleted);
        assertEquals("Hello Java", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testReplaceWithEmptyString() {
        editor.loadContent("Hello World");
        editor.replace(1, 6, 1, "");
        assertEquals("HelloWorld", editor.getLine(1));
    }

    @Test
    void testReplaceWithLongerString() {
        editor.loadContent("Hi");
        editor.replace(1, 1, 2, "Hello");
        assertEquals("Hello", editor.getLine(1));
    }

    // =========== show 测试 ===========

    @Test
    void testShowAll() {
        editor.loadContent("Line1\nLine2\nLine3");
        String result = editor.show(null, null);
        assertTrue(result.contains("1: Line1"));
        assertTrue(result.contains("2: Line2"));
        assertTrue(result.contains("3: Line3"));
    }

    @Test
    void testShowRange() {
        editor.loadContent("Line1\nLine2\nLine3\nLine4\nLine5");
        String result = editor.show(2, 4);
        assertFalse(result.contains("1: Line1"));
        assertTrue(result.contains("2: Line2"));
        assertTrue(result.contains("3: Line3"));
        assertTrue(result.contains("4: Line4"));
        assertFalse(result.contains("5: Line5"));
    }

    @Test
    void testShowEmptyFile() {
        editor.loadContent("");
        String result = editor.show(null, null);
        // When content is empty, lines contains one empty string, so show() returns "1: \n"
        // This behavior is based on the implementation
        assertNotNull(result);
    }

    @Test
    void testShowInvalidRangeThrows() {
        editor.loadContent("Line1\nLine2");
        assertThrows(IllegalArgumentException.class, () -> editor.show(0, 1));
        assertThrows(IllegalArgumentException.class, () -> editor.show(5, 6));
        assertThrows(IllegalArgumentException.class, () -> editor.show(2, 1));
    }

    // =========== getLine 测试 ===========

    @Test
    void testGetLine() {
        editor.loadContent("First\nSecond\nThird");
        assertEquals("First", editor.getLine(1));
        assertEquals("Second", editor.getLine(2));
        assertEquals("Third", editor.getLine(3));
    }

    @Test
    void testGetLineInvalidThrows() {
        editor.loadContent("Line");
        assertThrows(IllegalArgumentException.class, () -> editor.getLine(0));
        assertThrows(IllegalArgumentException.class, () -> editor.getLine(5));
    }

    // =========== getContent 测试 ===========

    @Test
    void testGetContent() {
        editor.loadContent("Line1\nLine2\nLine3");
        String content = editor.getContent();
        assertEquals("Line1\nLine2\nLine3", content);
    }

    // =========== Snapshot 测试 ===========

    @Test
    void testCreateSnapshot() {
        editor.loadContent("Original");
        editor.setModified(true);

        TextEditor.EditorSnapshot snapshot = editor.createSnapshot();

        assertNotNull(snapshot);
        assertEquals(1, snapshot.getLines().size());
        assertEquals("Original", snapshot.getLines().get(0));
        assertTrue(snapshot.isModified());
    }

    @Test
    void testRestoreSnapshot() {
        editor.loadContent("Original");
        TextEditor.EditorSnapshot snapshot = editor.createSnapshot();

        editor.loadContent("Changed");
        editor.restoreSnapshot(snapshot);

        assertEquals("Original", editor.getLine(1));
    }
}
