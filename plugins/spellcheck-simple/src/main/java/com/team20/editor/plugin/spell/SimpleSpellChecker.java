package com.team20.editor.plugin.spell;

import com.team20.editor.extension.spi.spellcheck.SpellChecker;

import java.util.*;

public class SimpleSpellChecker implements SpellChecker {

    private static final Set<String> DICT = Set.of(
            "hello", "world", "java", "xml", "book", "title", "author", "year", "price", "receive", "occurred",
            "italian", "rowling");

    @Override
    public List<Suggestion> checkText(String text, String language) {
        List<Suggestion> out = new ArrayList<>();
        if (text == null || text.isBlank())
            return out;
        String[] lines = text.split("\\r?\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i];
            int idx = 0;
            while (idx < ln.length()) {
                while (idx < ln.length() && !Character.isLetter(ln.charAt(idx)))
                    idx++;
                int start = idx;
                while (idx < ln.length() && Character.isLetter(ln.charAt(idx)))
                    idx++;
                int end = idx;
                if (end > start) {
                    String tk = ln.substring(start, end);
                    if (!DICT.contains(tk.toLowerCase())) {
                        out.add(new Suggestion(i + 1, start + 1, tk, List.of("receive")));
                    }
                }
            }
        }
        return out;
    }
}