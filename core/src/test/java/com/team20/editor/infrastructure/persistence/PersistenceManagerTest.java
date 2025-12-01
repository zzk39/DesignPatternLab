package com.team20.editor.infrastructure.persistence;

import com.team20.editor.domain.workspace.WorkspaceState;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PersistenceManagerTest {

    @Test
    void workspaceStateRoundtrip() throws Exception {
        Path tmp = Files.createTempFile("ws", ".json");
        tmp.toFile().deleteOnExit();

        Serializer serializer = new JsonSerializer();
        PersistenceManager pm = new PersistenceManager(serializer);

        WorkspaceState s = new WorkspaceState();
        s.setActiveEditorName("a.txt");
        s.setEditorNames(List.of("a.txt"));
        Map<String, Boolean> logMap = Map.of("a.txt", true);
        s.setLoggingEnabledMap(logMap);

        pm.saveWorkspaceState(tmp.toString(), s);
        WorkspaceState loaded = pm.loadWorkspaceState(tmp.toString());

        assertNotNull(loaded);
        assertEquals("a.txt", loaded.getActiveEditorName());
        assertEquals(List.of("a.txt"), loaded.getEditorNames());
        assertNotNull(loaded.getLoggingEnabledMap());
        assertTrue(loaded.getLoggingEnabledMap().get("a.txt"));
    }
}
