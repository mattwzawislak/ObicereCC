package org.obicere.cc.executor.language.util;

import org.obicere.cc.executor.language.Language;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Obicere
 */
public class DocumentInspector {

    private final Language language;

    private LinkedList<TypeDocumentIndexer> content;

    public DocumentInspector(final Language language) {
        this.language = language;
    }

    public void apply(final String rawCode, final int index, final int delta) {
        final StringBuilder code = new StringBuilder(rawCode.trim());
        final int length = code.length();
        content = new LinkedList<>();
        removeLiterals(code);
        int start = rawCode.lastIndexOf(' ', index);
        if(start < 0){
            start = 0;
        }
        for (int i = start; i < delta; i++) {
            final int groupStart = i;
            char next = code.charAt(i);
            if (Character.isWhitespace(next)) {
                int size = 0;
                while (i < length - 1 && Character.isWhitespace(next)) {
                    next = code.charAt(++i);
                    ++size;
                }
                --i;
                content.add(new TypeDocumentIndexer(groupStart, groupStart + size, TypeDocumentIndexer.TypeFlag.PLAINTEXT));
                continue;
            }
            if (i >= length - 1) {
                break;
            }
            final StringBuilder match = new StringBuilder(1);
            char c = code.charAt(i);
            final boolean literal = (c == 0);
            if (literal) {
                while (i < length - 1 && c == 0) {
                    match.append(c);
                    c = code.charAt(++i);
                }
                --i;
            } else if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
                while (i < length - 1 && (Character.isLetterOrDigit(c) || c == '_' || c == '$') && c != 0) {
                    match.append(c);
                    c = code.charAt(++i);
                }
                --i;
            }

            final String result = match.toString();
            final TypeDocumentIndexer.TypeFlag flag;
            if (language.isKeyword(result)) {
                flag = TypeDocumentIndexer.TypeFlag.KEYWORD;
            } else if (literal) {
                flag = TypeDocumentIndexer.TypeFlag.LITERAL;
            } else {
                flag = TypeDocumentIndexer.TypeFlag.PLAINTEXT;
            }
            content.add(new TypeDocumentIndexer(groupStart, groupStart + result.length(), flag));
        }
    }

    public LinkedList<TypeDocumentIndexer> getContent() {
        return content;
    }

    private void removeLiterals(final StringBuilder builder) {
        for (final String literal : language.getLiteralMatches()) {
            clearMatches(builder, literal);
        }
    }

    private StringBuilder clearMatches(final StringBuilder code, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(code);
        while (matcher.find()) { // For each match
            final String key = matcher.group();
            int start = code.indexOf(key);
            final int length = key.length();
            while (start > -1) { // For each instance
                final int end = start + key.length();
                final int nextSearchStart = start + length;
                code.replace(start, end, CharBuffer.allocate(length).toString());
                start = code.indexOf(key, nextSearchStart);
                // replace the instance
            }
        }
        return code;
    }

}
