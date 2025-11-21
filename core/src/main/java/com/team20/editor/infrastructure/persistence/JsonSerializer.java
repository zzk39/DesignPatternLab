package com.team20.editor.infrastructure.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON-based serializer implementation using Gson.
 */
public class JsonSerializer implements Serializer<Object> {

    private final Gson gson;

    public JsonSerializer() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public String serialize(Object obj) throws Exception {
        if (obj == null) return "null";
        return gson.toJson(obj);
    }

    @Override
    public Object deserialize(String raw) throws Exception {
        if (raw == null || raw.isBlank()) return null;
        // For WorkspaceState, we need to know the type
        // This is a simple implementation that assumes WorkspaceState
        return gson.fromJson(raw, com.team20.editor.domain.workspace.WorkspaceState.class);
    }
}
