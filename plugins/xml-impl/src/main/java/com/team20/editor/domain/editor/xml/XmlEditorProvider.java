package com.team20.editor.domain.editor.xml;

import com.team20.editor.domain.editor.Editor;
import com.team20.editor.extension.spi.editor.EditorProvider;

import java.util.List;
import java.util.function.Function;

public class XmlEditorProvider implements EditorProvider {
    @Override
    public String getProviderName() {
        return "xml-editor";
    }

    @Override
    public List<EditorRegistration> getEditorRegistrations() {
        Function<String, Editor> factory = XmlEditor::new;
        return List.of(new EditorRegistration("xml", factory, List.of("xml")));
    }
}