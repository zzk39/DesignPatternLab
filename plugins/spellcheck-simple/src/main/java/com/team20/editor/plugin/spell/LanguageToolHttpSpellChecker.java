package com.team20.editor.plugin.spell;

import com.team20.editor.extension.spi.spellcheck.SpellChecker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SpellChecker 适配器：调用 LanguageTool 公共 HTTP API。
 *
 * 默认端点：https://api.languagetool.org/v2/check
 * 可通过以下配置覆盖：
 * - 系统属性: -Dlanguagetool.endpoint=<url>
 * - 环境变量: LT_ENDPOINT=<url>
 *
 * 语言默认 en-US，可在调用 checkText(text, language) 时覆盖，如 "en"、"en-US"、"zh-CN"。
 */
public class LanguageToolHttpSpellChecker implements SpellChecker {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Gson gson = new GsonBuilder().create();

    private final String endpoint;

    public LanguageToolHttpSpellChecker() {
        String ep = System.getProperty("languagetool.endpoint");
        if (ep == null || ep.isBlank()) {
            ep = System.getenv("LT_ENDPOINT");
        }
        if (ep == null || ep.isBlank()) {
            ep = "https://api.languagetool.org/v2/check";
        }
        this.endpoint = ep;
    }

    @Override
    public List<Suggestion> checkText(String text, String language) {
        List<Suggestion> out = new ArrayList<>();
        if (text == null || text.isBlank())
            return out;

        String lang = (language == null || language.isBlank()) ? "en-US" : language;

        try {
            String body = "language=" + urlEncode(lang)
                    + "&text=" + urlEncode(text);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Team20-TextEditor/1.0 (+https://example.com)")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 400) {
                System.err.println("Warning: LanguageTool HTTP error " + resp.statusCode());
                return out;
            }

            JsonObject root = gson.fromJson(resp.body(), JsonObject.class);
            if (root == null || !root.has("matches") || !root.get("matches").isJsonArray()) {
                return out;
            }

            JsonArray matches = root.getAsJsonArray("matches");
            for (JsonElement e : matches) {
                if (!e.isJsonObject())
                    continue;
                JsonObject m = e.getAsJsonObject();

                int offset = optInt(m, "offset", -1);
                int len = optInt(m, "length", 0);
                if (offset < 0 || len <= 0 || offset + len > text.length())
                    continue;

                String token = text.substring(offset, offset + len);

                // replacements -> 候选建议
                List<String> candidates = new ArrayList<>();
                if (m.has("replacements") && m.get("replacements").isJsonArray()) {
                    for (JsonElement r : m.getAsJsonArray("replacements")) {
                        if (r.isJsonObject()) {
                            JsonObject ro = r.getAsJsonObject();
                            if (ro.has("value")) {
                                candidates.add(ro.get("value").getAsString());
                            }
                        }
                    }
                }

                // 计算行列号（从 1 开始）
                Position pos = toLineColumn(text, offset);

                out.add(new Suggestion(pos.line, pos.column, token, candidates));
            }
        } catch (Exception ex) {
            // 网络/解析错误仅告警，不影响主流程
            System.err.println("Warning: LanguageTool request failed: " + ex.getMessage());
        }

        return out;
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(Objects.toString(s, ""), StandardCharsets.UTF_8);
    }

    private static int optInt(JsonObject o, String key, int def) {
        try {
            if (o.has(key))
                return o.get(key).getAsInt();
            return def;
        } catch (Exception ignored) {
            return def;
        }
    }

    private static Position toLineColumn(String text, int offset) {
        int line = 1;
        int colInLine = 1;
        int lastBreak = -1;

        // 统计到 offset 之前的换行符数量
        for (int i = 0; i < offset && i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                line++;
                lastBreak = i;
            }
        }
        colInLine = offset - lastBreak; // '\n' 后的第一个字符列为 1
        return new Position(line, colInLine);
    }

    private static class Position {
        final int line;
        final int column;

        Position(int l, int c) {
            this.line = l;
            this.column = c;
        }
    }
}