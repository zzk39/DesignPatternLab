package com.team20.editor.domain.editor.text;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TextEditorTest {

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
    void insertReplaceDeleteBasic() {
        TextEditor e = new TextEditor("t2.txt");
        e.loadContent("abc\ndef");
        e.insert(1, 4, "X"); // insert at end of first line
        assertEquals("abcX", e.getLine(1));
        e.replace(2, 1, 3, "Z");
        assertEquals("Z", e.getLine(2));
        e.delete(1, 4, 1);
        assertEquals("abc", e.getLine(1));
    }
}
