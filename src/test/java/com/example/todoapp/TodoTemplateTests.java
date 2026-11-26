package com.example.todoapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

class TodoTemplateTests {

    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9-]*)(?:\\s[^<>]*?)?(/?)>");

    @Test
    void todosTemplateIsUtf8WellFormedHtmlAndHasJapaneseText() throws Exception {
        Path template = Path.of("src/main/resources/templates/todos.html");
        byte[] bytes = Files.readAllBytes(template);
        String source = StandardCharsets.UTF_8.newDecoder()
                .decode(java.nio.ByteBuffer.wrap(bytes)).toString();

        assertThat(source).contains("<meta charset=\"UTF-8\">");
        assertThat(source).contains("やること一覧", "新規登録", "まだ登録されているやることはありません。", "添付有");
        assertThat(source).doesNotContain("繧", "縺");

        String xmlCompatible = source.replaceAll("(?i)<\\!doctype[^>]*>", "")
                .replaceAll("(?i)<(meta|link|input|br|hr|img)(\\s[^<>]*?)?>", "<$1$2/>");
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new java.io.StringReader(xmlCompatible)));

        Matcher tags = TAG.matcher(source);
        java.util.ArrayDeque<String> stack = new java.util.ArrayDeque<>();
        java.util.Set<String> voidTags = java.util.Set.of("meta", "link", "input", "br", "hr", "img");
        while (tags.find()) {
            String closing = tags.group(1);
            String name = tags.group(2).toLowerCase(java.util.Locale.ROOT);
            String selfClosing = tags.group(3);
            if (!closing.isEmpty()) {
                assertThat(stack).isNotEmpty();
                assertThat(stack.pop()).isEqualTo(name);
            } else if (selfClosing.isEmpty() && !voidTags.contains(name)) {
                stack.push(name);
            }
        }
        assertThat(stack).isEmpty();
    }
}
