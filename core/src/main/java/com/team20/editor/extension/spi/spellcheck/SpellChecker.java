package com.team20.editor.extension.spi.spellcheck;

import java.util.List;

/**
 * SPI: 拼写检查适配器。实现可接入任意第三方库/服务。
 */
public interface SpellChecker {

    /**
     * 检查一段纯文本，返回建议列表。
     */
    List<Suggestion> checkText(String text, String language);

    /**
     * 拼写建议模型（最小结构）。
     */
    final class Suggestion {
        public final int line; // 1-based，可为 -1 表示未知
        public final int column; // 1-based，可为 -1 表示未知
        public final String token; // 错误词
        public final List<String> candidates; // 建议

        public Suggestion(int line, int column, String token, List<String> candidates) {
            this.line = line;
            this.column = column;
            this.token = token;
            this.candidates = candidates;
        }
    }
}