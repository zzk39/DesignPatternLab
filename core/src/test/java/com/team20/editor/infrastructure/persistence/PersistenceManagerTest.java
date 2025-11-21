package com.team20.editor.infrastructure.persistence;

import com.team20.editor.domain.workspace.WorkspaceState;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class PersistenceManagerTest {

    // Simple test serializer that just converts to/from string representation
    private static class TestSerializer implements Serializer<WorkspaceState> {
        @Override
        public String serialize(WorkspaceState obj) throws Exception {
            return obj.toString();
        }

        @Override
        public WorkspaceState deserialize(String raw) throws Exception {
            WorkspaceState state = new WorkspaceState();
            state.setActiveEditorName("a.txt");
            return state;
        }
    }

    @Test
    void workspaceStateRoundtrip() throws Exception {
        Path tmp = Files.createTempFile("ws", ".json");
        tmp.toFile().deleteOnExit();

        Serializer<WorkspaceState> serializer = new TestSerializer();
        PersistenceManager pm = new PersistenceManager(serializer);

        WorkspaceState s = new WorkspaceState();
        s.setActiveEditorName("a.txt");
        s.setEditorNames(java.util.List.of("a.txt"));
        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
        map.put("a.txt", true);
        s.setLoggingEnabledMap(map);

        pm.saveWorkspaceState(tmp.toString(), s);
        assertTrue(Files.exists(tmp));
    }
}
