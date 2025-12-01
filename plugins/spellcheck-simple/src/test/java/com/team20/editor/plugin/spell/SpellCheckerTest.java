package com.team20.editor.plugin.spell;

import com.team20.editor.extension.spi.spellcheck.SpellChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpellChecker 单元测试
 * 
 * 注意：由于实际的拼写检查需要网络连接到 LanguageTool API，
 * 这里主要测试接口的基本行为和边界情况。
 */
public class SpellCheckerTest {

    private SpellChecker checker;

    @BeforeEach
    void setUp() {
        checker = new LanguageToolHttpSpellChecker();
    }

    // =========== checkText 基本测试 ===========

    @Test
    void testCheckTextNull() {
        List<SpellChecker.Suggestion> result = checker.checkText(null, "en-US");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCheckTextEmpty() {
        List<SpellChecker.Suggestion> result = checker.checkText("", "en-US");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCheckTextBlank() {
        List<SpellChecker.Suggestion> result = checker.checkText("   ", "en-US");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCheckTextNullLanguage() {
        // null语言应该使用默认值 en-US
        List<SpellChecker.Suggestion> result = checker.checkText("Hello world", null);
        assertNotNull(result);
    }

    @Test
    void testCheckTextEmptyLanguage() {
        // 空语言应该使用默认值 en-US
        List<SpellChecker.Suggestion> result = checker.checkText("Hello world", "");
        assertNotNull(result);
    }

    // =========== Suggestion 类测试 ===========

    @Test
    void testSuggestionConstruction() {
        List<String> candidates = List.of("receive", "received");
        SpellChecker.Suggestion suggestion = new SpellChecker.Suggestion(1, 5, "recieve", candidates);

        assertEquals(1, suggestion.line);
        assertEquals(5, suggestion.column);
        assertEquals("recieve", suggestion.token);
        assertEquals(candidates, suggestion.candidates);
    }

    @Test
    void testSuggestionWithUnknownPosition() {
        List<String> candidates = List.of("correct");
        SpellChecker.Suggestion suggestion = new SpellChecker.Suggestion(-1, -1, "wrong", candidates);

        assertEquals(-1, suggestion.line);
        assertEquals(-1, suggestion.column);
    }

    @Test
    void testSuggestionWithEmptyCandidates() {
        SpellChecker.Suggestion suggestion = new SpellChecker.Suggestion(1, 1, "unknown", List.of());

        assertTrue(suggestion.candidates.isEmpty());
    }

    // =========== Mock SpellChecker 测试 ===========

    @Test
    void testMockSpellChecker() {
        // 测试使用Mock实现的SpellChecker
        SpellChecker mockChecker = new MockSpellChecker();

        List<SpellChecker.Suggestion> result = mockChecker.checkText("Ths is a tset.", "en-US");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Ths", result.get(0).token);
        assertEquals("tset", result.get(1).token);
    }

    @Test
    void testMockSpellCheckerCorrectText() {
        SpellChecker mockChecker = new MockSpellChecker();

        List<SpellChecker.Suggestion> result = mockChecker.checkText("This is correct.", "en-US");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Mock SpellChecker 实现用于测试
     */
    private static class MockSpellChecker implements SpellChecker {
        @Override
        public List<Suggestion> checkText(String text, String language) {
            if (text == null || text.isBlank()) {
                return List.of();
            }

            // 模拟检查一些常见的拼写错误
            java.util.List<Suggestion> suggestions = new java.util.ArrayList<>();

            if (text.contains("Ths")) {
                suggestions.add(new Suggestion(1, text.indexOf("Ths") + 1, "Ths", List.of("This", "The")));
            }
            if (text.contains("tset")) {
                suggestions.add(new Suggestion(1, text.indexOf("tset") + 1, "tset", List.of("test", "set")));
            }
            if (text.contains("recieve")) {
                suggestions.add(new Suggestion(1, text.indexOf("recieve") + 1, "recieve", List.of("receive")));
            }
            if (text.contains("occured")) {
                suggestions.add(new Suggestion(1, text.indexOf("occured") + 1, "occured", List.of("occurred")));
            }

            return suggestions;
        }
    }

    // =========== 接口合规性测试 ===========

    @Test
    void testSpellCheckerInterface() {
        // 验证 LanguageToolHttpSpellChecker 实现了 SpellChecker 接口
        assertTrue(checker instanceof SpellChecker);
    }

    @Test
    void testMultipleSuggestions() {
        SpellChecker mockChecker = new MockSpellChecker();

        List<SpellChecker.Suggestion> result = mockChecker.checkText("recieve occured", "en-US");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // =========== 边界情况测试 ===========

    @Test
    void testLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("This is a normal sentence. ");
        }
        
        // 不应该抛出异常
        List<SpellChecker.Suggestion> result = checker.checkText(sb.toString(), "en-US");
        assertNotNull(result);
    }

    @Test
    void testSpecialCharacters() {
        String textWithSpecialChars = "Hello @#$%^&*() world!";
        
        // 不应该抛出异常
        List<SpellChecker.Suggestion> result = checker.checkText(textWithSpecialChars, "en-US");
        assertNotNull(result);
    }

    @Test
    void testUnicodeText() {
        String unicodeText = "Hello 世界 こんにちは";
        
        // 不应该抛出异常
        List<SpellChecker.Suggestion> result = checker.checkText(unicodeText, "en-US");
        assertNotNull(result);
    }

    @Test
    void testMultilineText() {
        String multilineText = "Line one.\nLine two.\nLine three.";
        
        // 不应该抛出异常
        List<SpellChecker.Suggestion> result = checker.checkText(multilineText, "en-US");
        assertNotNull(result);
    }
}
