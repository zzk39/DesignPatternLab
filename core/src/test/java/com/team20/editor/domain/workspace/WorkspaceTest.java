package com.team20.editor.domain.workspace;

import com.team20.editor.domain.editor.Editor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Workspace 单元测试
 */
public class WorkspaceTest {

    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
    }

    @Test
    void testAddEditor() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        assertTrue(workspace.hasEditors());
        assertEquals(1, workspace.getEditorCount());
        assertEquals(editor, workspace.getEditor("test.txt"));
    }

    @Test
    void testAddNullEditor() {
        workspace.addEditor(null);
        assertFalse(workspace.hasEditors());
        assertEquals(0, workspace.getEditorCount());
    }

    @Test
    void testAddDuplicateEditor() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);
        workspace.addEditor(editor);

        assertEquals(1, workspace.getEditorCount());
    }

    @Test
    void testRemoveEditor() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);
        workspace.removeEditor(editor);

        assertFalse(workspace.hasEditors());
        assertEquals(0, workspace.getEditorCount());
        assertNull(workspace.getEditor("test.txt"));
    }

    @Test
    void testRemoveNullEditor() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);
        workspace.removeEditor(null);

        assertTrue(workspace.hasEditors());
        assertEquals(1, workspace.getEditorCount());
    }

    @Test
    void testActiveEditorSetOnFirstAdd() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        assertEquals(editor, workspace.getActiveEditor());
    }

    @Test
    void testSetActiveEditor() {
        Editor editor1 = createMockEditor("file1.txt");
        Editor editor2 = createMockEditor("file2.txt");

        workspace.addEditor(editor1);
        workspace.addEditor(editor2);

        workspace.setActiveEditor(editor2);
        assertEquals(editor2, workspace.getActiveEditor());
    }

    @Test
    void testSetActiveEditorNotInWorkspace() {
        Editor editor1 = createMockEditor("file1.txt");
        Editor editor2 = createMockEditor("file2.txt");

        workspace.addEditor(editor1);
        workspace.setActiveEditor(editor2);

        assertEquals(editor1, workspace.getActiveEditor());
    }

    @Test
    void testGetEditors() {
        Editor editor1 = createMockEditor("file1.txt");
        Editor editor2 = createMockEditor("file2.txt");

        workspace.addEditor(editor1);
        workspace.addEditor(editor2);

        List<Editor> editors = workspace.getEditors();
        assertEquals(2, editors.size());
        assertTrue(editors.contains(editor1));
        assertTrue(editors.contains(editor2));
    }

    @Test
    void testLoggingEnabled() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        assertFalse(workspace.isLoggingEnabled("test.txt"));

        workspace.setLoggingEnabled("test.txt", true);
        assertTrue(workspace.isLoggingEnabled("test.txt"));

        workspace.setLoggingEnabled("test.txt", false);
        assertFalse(workspace.isLoggingEnabled("test.txt"));
    }

    @Test
    void testLoggingEnabledNullFilepath() {
        assertFalse(workspace.isLoggingEnabled(null));
    }

    @Test
    void testLogExclusions() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        Set<String> exclusions = Set.of("append", "delete");
        workspace.setLogExclusions("test.txt", exclusions);

        Set<String> result = workspace.getLogExclusions("test.txt");
        assertTrue(result.contains("append"));
        assertTrue(result.contains("delete"));
    }

    @Test
    void testIsCommandExcluded() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        workspace.setLogExclusions("test.txt", Set.of("append", "delete"));

        assertTrue(workspace.isCommandExcluded("test.txt", "append"));
        assertTrue(workspace.isCommandExcluded("test.txt", "APPEND"));
        assertTrue(workspace.isCommandExcluded("test.txt", "delete"));
        assertFalse(workspace.isCommandExcluded("test.txt", "insert"));
    }

    @Test
    void testIsCommandExcludedNullParams() {
        assertFalse(workspace.isCommandExcluded(null, "append"));
        assertFalse(workspace.isCommandExcluded("test.txt", null));
    }

    @Test
    void testGetState() {
        Editor editor1 = createMockEditor("file1.txt");
        Editor editor2 = createMockEditor("file2.txt");

        workspace.addEditor(editor1);
        workspace.addEditor(editor2);
        workspace.setActiveEditor(editor2);
        workspace.setLoggingEnabled("file1.txt", true);

        WorkspaceState state = workspace.getState();

        assertEquals(2, state.getEditorCount());
        assertEquals("file2.txt", state.getActiveEditorName());
        assertTrue(state.getEditorNames().contains("file1.txt"));
        assertTrue(state.getEditorNames().contains("file2.txt"));
        assertTrue(state.getLoggingEnabledMap().get("file1.txt"));
    }

    @Test
    void testRestoreState() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        WorkspaceState state = new WorkspaceState();
        state.setActiveEditorName("test.txt");
        Map<String, Boolean> logMap = new HashMap<>();
        logMap.put("test.txt", true);
        state.setLoggingEnabledMap(logMap);

        workspace.restoreState(state);

        assertTrue(workspace.isLoggingEnabled("test.txt"));
        assertEquals(editor, workspace.getActiveEditor());
    }

    @Test
    void testRestoreNullState() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        workspace.restoreState(null);
        assertEquals(editor, workspace.getActiveEditor());
    }

    @Test
    void testRemoveActiveEditorSwitchesToLast() {
        Editor editor1 = createMockEditor("file1.txt");
        Editor editor2 = createMockEditor("file2.txt");
        Editor editor3 = createMockEditor("file3.txt");

        workspace.addEditor(editor1);
        workspace.addEditor(editor2);
        workspace.addEditor(editor3);
        workspace.setActiveEditor(editor2);

        workspace.removeEditor(editor2);

        assertEquals(editor3, workspace.getActiveEditor());
    }

    @Test
    void testToString() {
        Editor editor = createMockEditor("test.txt");
        workspace.addEditor(editor);

        String str = workspace.toString();
        assertTrue(str.contains("editors=1"));
        assertTrue(str.contains("active=test.txt"));
    }

    private Editor createMockEditor(String name) {
        Editor editor = mock(Editor.class);
        when(editor.getName()).thenReturn(name);
        return editor;
    }
}
