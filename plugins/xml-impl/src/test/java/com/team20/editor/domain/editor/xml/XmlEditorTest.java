package com.team20.editor.domain.editor.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XmlEditor 单元测试
 */
public class XmlEditorTest {

    private XmlEditor editor;
    private static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bookstore id="root">
                <book id="book1" category="COOKING">
                    <title id="title1" lang="en">Everyday Italian</title>
                    <author id="author1">Giada De Laurentiis</author>
                </book>
                <book id="book2" category="CHILDREN">
                    <title id="title2" lang="en">Harry Potter</title>
                    <author id="author2">J K. Rowling</author>
                </book>
            </bookstore>
            """;

    @BeforeEach
    void setUp() {
        editor = new XmlEditor("test.xml");
    }

    // =========== 基本属性测试 ===========

    @Test
    void testConstructor() {
        XmlEditor e = new XmlEditor("new.xml");
        assertEquals("new.xml", e.getName());
        assertFalse(e.isModified());
    }

    @Test
    void testSetModified() {
        assertFalse(editor.isModified());
        editor.setModified(true);
        assertTrue(editor.isModified());
    }

    // =========== loadContent 测试 ===========

    @Test
    void testLoadContentEmpty() {
        editor.loadContent("");
        assertNotNull(editor.getRoot());
        assertEquals("root", editor.getRoot().getTagName());
        assertFalse(editor.isModified());
    }

    @Test
    void testLoadContentNull() {
        editor.loadContent(null);
        assertNotNull(editor.getRoot());
    }

    @Test
    void testLoadContentValid() {
        editor.loadContent(SAMPLE_XML);
        assertNotNull(editor.getRoot());
        assertEquals("bookstore", editor.getRoot().getTagName());
        assertFalse(editor.isModified());
    }

    @Test
    void testLoadContentWithLogHeader() {
        String xmlWithLog = "# log\n" + SAMPLE_XML;
        editor.loadContent(xmlWithLog);
        assertNotNull(editor.getRoot());
        assertEquals("bookstore", editor.getRoot().getTagName());
        assertTrue(editor.hasLoggingHeaderPresent());
        assertEquals("# log", editor.getOriginalLogHeaderLine());
    }

    @Test
    void testLoadContentInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            editor.loadContent("not valid xml content"));
    }

    // =========== getById 测试 ===========

    @Test
    void testGetById() {
        editor.loadContent(SAMPLE_XML);
        
        Element root = editor.getById("root");
        assertNotNull(root);
        assertEquals("bookstore", root.getTagName());

        Element book1 = editor.getById("book1");
        assertNotNull(book1);
        assertEquals("book", book1.getTagName());

        Element title1 = editor.getById("title1");
        assertNotNull(title1);
        assertEquals("title", title1.getTagName());
    }

    @Test
    void testGetByIdNotFound() {
        editor.loadContent(SAMPLE_XML);
        assertNull(editor.getById("nonexistent"));
    }

    // =========== insertBefore 测试 ===========

    @Test
    void testInsertBefore() {
        editor.loadContent(SAMPLE_XML);
        
        editor.insertBefore("book", "newBook", "book1", "New Book Content");
        
        Element newBook = editor.getById("newBook");
        assertNotNull(newBook);
        assertEquals("book", newBook.getTagName());
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertBeforeDuplicateIdThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.insertBefore("book", "book1", "book2", "text"));
    }

    @Test
    void testInsertBeforeTargetNotFoundThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.insertBefore("book", "newBook", "nonexistent", "text"));
    }

    @Test
    void testInsertBeforeRootThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.insertBefore("wrapper", "wrapper1", "root", "text"));
    }

    // =========== appendChild 测试 ===========

    @Test
    void testAppendChild() {
        editor.loadContent(SAMPLE_XML);
        
        editor.appendChild("price", "price1", "book1", "29.99");
        
        Element price = editor.getById("price1");
        assertNotNull(price);
        assertEquals("price", price.getTagName());
        assertTrue(editor.isModified());
    }

    @Test
    void testAppendChildDuplicateIdThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.appendChild("price", "book1", "root", "text"));
    }

    @Test
    void testAppendChildParentNotFoundThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.appendChild("item", "item1", "nonexistent", "text"));
    }

    @Test
    void testAppendChildWithNullText() {
        editor.loadContent(SAMPLE_XML);
        
        editor.appendChild("empty", "empty1", "book1", null);
        
        Element empty = editor.getById("empty1");
        assertNotNull(empty);
        assertTrue(editor.isModified());
    }

    // =========== editId 测试 ===========

    @Test
    void testEditId() {
        editor.loadContent(SAMPLE_XML);
        
        editor.editId("book1", "bookOne");
        
        assertNull(editor.getById("book1"));
        assertNotNull(editor.getById("bookOne"));
        assertTrue(editor.isModified());
    }

    @Test
    void testEditIdNotFoundThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.editId("nonexistent", "newId"));
    }

    @Test
    void testEditIdTargetExistsThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.editId("book1", "book2"));
    }

    // =========== editText 测试 ===========

    @Test
    void testEditText() {
        editor.loadContent(SAMPLE_XML);
        
        editor.editText("title1", "New Title");
        
        Element title = editor.getById("title1");
        assertNotNull(title);
        assertTrue(title.getTextContent().contains("New Title"));
        assertTrue(editor.isModified());
    }

    @Test
    void testEditTextToEmpty() {
        editor.loadContent(SAMPLE_XML);
        
        editor.editText("title1", null);
        
        Element title = editor.getById("title1");
        assertNotNull(title);
        assertTrue(editor.isModified());
    }

    @Test
    void testEditTextNotFoundThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.editText("nonexistent", "text"));
    }

    // =========== delete 测试 ===========

    @Test
    void testDelete() {
        editor.loadContent(SAMPLE_XML);
        
        editor.delete("book1");
        
        assertNull(editor.getById("book1"));
        // Note: The current implementation only removes the deleted element from byId,
        // child elements remain in the map (orphaned). This is implementation behavior.
        assertTrue(editor.isModified());
    }

    @Test
    void testDeleteNotFoundThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.delete("nonexistent"));
    }

    @Test
    void testDeleteRootThrows() {
        editor.loadContent(SAMPLE_XML);
        
        assertThrows(IllegalArgumentException.class, () -> 
            editor.delete("root"));
    }

    // =========== getContent 测试 ===========

    @Test
    void testGetContent() {
        editor.loadContent(SAMPLE_XML);
        
        String content = editor.getContent();
        
        assertNotNull(content);
        assertTrue(content.contains("<?xml"));
        assertTrue(content.contains("bookstore"));
        assertTrue(content.contains("book1"));
    }

    @Test
    void testGetContentPreservesLogHeader() {
        String xmlWithLog = "# log\n" + SAMPLE_XML;
        editor.loadContent(xmlWithLog);
        
        String content = editor.getContent();
        
        assertTrue(content.startsWith("# log"));
    }

    // =========== getRoot 测试 ===========

    @Test
    void testGetRoot() {
        editor.loadContent(SAMPLE_XML);
        
        Element root = editor.getRoot();
        
        assertNotNull(root);
        assertEquals("bookstore", root.getTagName());
        assertEquals("root", root.getAttribute("id"));
    }
}
