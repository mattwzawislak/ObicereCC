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

    private TypeDocumentIndexer polled;

    public DocumentInspector(final Language language) {
        this.language = language;
    }

    public void apply(final String rawCode, final int start, final int end) {
        final StringBuilder code = new StringBuilder(rawCode);
        final int length = code.length();
        content = new LinkedList<>();
        removeLiterals(code);
        for (int i = start; i < end; i++) {
            final int groupStart = i;
            if (i >= length - 1) {
                break;
            }
            final StringBuilder match = new StringBuilder(1);
            char c = code.charAt(i);
            final boolean literal = c == 0;
            if (!literal && Character.isLetterOrDigit(c)) {
                while (i < length - 1 && Character.isLetterOrDigit(c)) {
                    match.append(c);
                    c = code.charAt(++i);
                }
                --i;
            }

            final String result = match.toString();
            if (language.isKeyword(result)) {
                addIndexer(groupStart, groupStart + result.length(), TypeDocumentIndexer.TypeFlag.KEYWORD);
            }
        }
    }

    public LinkedList<TypeDocumentIndexer> getContent() {
        if (polled != null) {
            content.add(polled);
            polled = null;
        }
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

                addIndexer(start, end, TypeDocumentIndexer.TypeFlag.LITERAL);

                final int nextSearchStart = start + length;
                code.replace(start, end, CharBuffer.allocate(length).toString());
                start = code.indexOf(key, nextSearchStart);
                // replace the instance
            }
        }
        return code;
    }

    private void addIndexer(final int start, final int end, final TypeDocumentIndexer.TypeFlag flag) {
        if (polled != null) {
            if (polled.getFlag() == flag && polled.getBound().getEnd() == start) {
                // We can chain the two together to avoid redundancy
                polled = new TypeDocumentIndexer(polled.getBound().getStart(), end, flag);
            } else {
                // Add previous, poll the new indexer
                content.add(polled);
                polled = new TypeDocumentIndexer(start, end, flag);
            }
        } else {
            polled = new TypeDocumentIndexer(start, end, flag);
        }
    }

}
