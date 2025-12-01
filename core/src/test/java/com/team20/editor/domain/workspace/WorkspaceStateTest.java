package com.team20.editor.domain.workspace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkspaceState 单元测试
 */
public class WorkspaceStateTest {

    private WorkspaceState state;

    @BeforeEach
    void setUp() {
        state = new WorkspaceState();
    }

    @Test
    void testSetAndGetEditorCount() {
        state.setEditorCount(5);
        assertEquals(5, state.getEditorCount());
    }

    @Test
    void testSetAndGetActiveEditorName() {
        state.setActiveEditorName("test.txt");
        assertEquals("test.txt", state.getActiveEditorName());
    }

    @Test
    void testSetAndGetEditorNames() {
        List<String> names = List.of("file1.txt", "file2.txt", "file3.xml");
        state.setEditorNames(names);
        assertEquals(names, state.getEditorNames());
        assertEquals(3, state.getEditorNames().size());
    }

    @Test
    void testSetAndGetLoggingEnabledMap() {
        Map<String, Boolean> logMap = new HashMap<>();
        logMap.put("file1.txt", true);
        logMap.put("file2.txt", false);
        state.setLoggingEnabledMap(logMap);

        Map<String, Boolean> result = state.getLoggingEnabledMap();
        assertNotNull(result);
        assertTrue(result.get("file1.txt"));
        assertFalse(result.get("file2.txt"));
    }

    @Test
    void testSetAndGetLoggingExclusionsMap() {
        Map<String, Set<String>> exclusions = new HashMap<>();
        exclusions.put("file1.txt", Set.of("append", "delete"));
        exclusions.put("file2.txt", Set.of("insert"));
        state.setLoggingExclusionsMap(exclusions);

        Map<String, Set<String>> result = state.getLoggingExclusionsMap();
        assertNotNull(result);
        assertTrue(result.get("file1.txt").contains("append"));
        assertTrue(result.get("file1.txt").contains("delete"));
        assertTrue(result.get("file2.txt").contains("insert"));
    }

    @Test
    void testSetAndGetEditorDurations() {
        Map<String, Long> durations = new HashMap<>();
        durations.put("file1.txt", 3600L);
        durations.put("file2.txt", 1800L);
        state.setEditorDurations(durations);

        Map<String, Long> result = state.getEditorDurations();
        assertNotNull(result);
        assertEquals(3600L, result.get("file1.txt"));
        assertEquals(1800L, result.get("file2.txt"));
    }

    @Test
    void testToString() {
        state.setEditorCount(2);
        state.setActiveEditorName("active.txt");
        state.setEditorNames(List.of("active.txt", "other.txt"));

        String str = state.toString();
        assertTrue(str.contains("editorCount=2"));
        assertTrue(str.contains("activeEditorName='active.txt'"));
    }

    @Test
    void testDefaultValues() {
        WorkspaceState newState = new WorkspaceState();
        assertEquals(0, newState.getEditorCount());
        assertNull(newState.getActiveEditorName());
        assertNull(newState.getEditorNames());
        assertNull(newState.getLoggingEnabledMap());
        assertNull(newState.getLoggingExclusionsMap());
        assertNull(newState.getEditorDurations());
    }
}
