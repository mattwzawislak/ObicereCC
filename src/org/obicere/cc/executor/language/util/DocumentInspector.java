package org.obicere.cc.executor.language.util;

import org.obicere.cc.executor.language.Language;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the given document based off of a language's specification. This
 * will flag all literals: comments and strings. This will also flag all
 * keywords as defined in the language's implementation.
 * <p>
 * This does of course assume that the keyword consists only of
 * alphanumeric characters. It should be set that this will not set flags
 * for the {@link org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#PLAINTEXT}
 * or {@link org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#OPERATOR}
 * flags. It should be assumed that any index not set will be plaintext and
 * should be treated as such.
 * <p>
 * This will also combine indexers to save performance when applying the
 * flags to style a document. This along with the default plaintext flag
 * greatly increase efficiency.
 * <p>
 * For example, applying this technique to the following code:
 * <pre>
 * {@code public String foo(int num){
 *       return "bar" + num; // test comment
 *   }}
 * </pre>
 * Would produce the following:
 * <pre>
 * Bound[ 0,  6], KEYWORD
 * Bound[18, 21], KEYWORD
 * Bound[29, 35], KEYWORD
 * Bound[36, 41], LITERAL
 * Bound[49, 64], LITERAL
 * </pre>
 *
 * @author Obicere
 * @version 1.0
 */
public class DocumentInspector {

    private final Language language;

    private LinkedList<TypeDocumentIndexer> content;

    private TypeDocumentIndexer polled;

    /**
     * Constructs an inspector based off of the {@link
     * org.obicere.cc.executor.language.Language}'s specifications. The
     * language cannot be <code>null</code>, since there has to be a
     * definitive specification.
     *
     * @param language The language to use the specification for.
     */

    public DocumentInspector(final Language language) {
        Objects.requireNonNull(language);
        this.language = language;
    }

    /**
     * Applies the current indexer parameters onto the specified code. The
     * indexer will only be applied to the substring of the code based off
     * of the indexes. This is to avoid applying styling to sections of
     * code that may not be in the visible viewport. For example, having
     * thousands of lines of code, but only a few dozen being visible,
     * there is no reason to style the entire document.
     *
     * @param rawCode The code to style based off of the specification.
     * @param start   The starting index for the styling to begin.
     * @param end     The ending index for the styling to finish.
     */

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
            char c = code.charAt(i);
            final boolean literal = c == 0;
            if (literal) {
                while (i < length - 1 && code.charAt(i) == 0) { // remove the entire literal
                    i++;
                }
                continue;
            }
            final StringBuilder match = new StringBuilder(1);
            if (Character.isLetterOrDigit(c)) {
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

    /**
     * Provides the content that was generated from the previous rendering.
     * This is set lazily through the application of the inspection.
     * <p>
     * In the case that there is still a remaining element yet to be added,
     * it will be added to ensure that the entirety of the inspection is
     * available.
     *
     * @return The list of generated elements from the application.
     */

    public LinkedList<TypeDocumentIndexer> getContent() {
        if (polled != null && content != null) {
            content.add(polled);
            polled = null;
        }
        return content;
    }

    /**
     * Removes the literals from the given code. A literal is considered
     * anything the language will specify it as. This is to ensure that
     * keywords are not matched within strings or comments.
     *
     * @param builder The code to remove the literals from.
     */

    private void removeLiterals(final StringBuilder builder) {
        for (final String literal : language.getLiteralMatches()) {
            clearMatches(builder, literal);
        }
    }

    /**
     * Removes all matches of the <code>regex</code> from the
     * <code>code</code>. A {@link java.lang.StringBuilder} is used as a
     * {@link String} is immutable. Mutation through the builder is quite a
     * bit faster. The exact optimization would be a worst-case scenario of
     * <code>O(n)</code> with the builder and <code>O(n<sup>2</sup></code>
     * for the {@link String}.
     * <p>
     * When this is being done potentially thousands of times per second,
     * this needs to be as efficient as possible. A further optimization
     * could be made by storing the literals in the language as a {@link
     * java.util.regex.Pattern}, but has been avoided in the case that a
     * language wishes to use a file-based-constant system.
     * <p>
     * All matches of the patterns are replaced with <code>null</code>
     * characters. This is because a <code>null</code> character is
     * unlikely to exist in standard code. Should one exist, then it is
     * also unlikely to exist outside of a literal. Should one exist
     * outside a literal, I really don't want to know what language would
     * accept such a thing.
     *
     * @param code  The code to remove the literals from.
     * @param regex The literal to match.
     * @return The cleaned up code.
     */

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

    /**
     * Adds the indexer to the list of current indexers. This has been
     * loosely optimized to combine flags together. If the last flag and
     * the current flag are the same, and the end of the previous flag and
     * start of the new flag are the same, then they can be combined.
     * <p>
     * For example, the following two flags could be combined as one in
     * this context:
     * <pre>
     * {@code Bound[0, n], T
     *   Bound[n, m], T }
     * </pre>
     * Could be combined as such:
     * <pre>
     * {@code Bound[0, m], T}
     * </pre>
     *
     * @param start The starting index for the styling to begin.
     * @param end   The ending index for the styling to finish.
     * @param flag  The flag to apply to the selected element.
     */

    private void addIndexer(final int start, final int end, final TypeDocumentIndexer.TypeFlag flag) {
        if (polled != null) {
            if (polled.getFlag() == flag && polled.getBound().getMax() == start) {
                // We can chain the two together to avoid redundancy
                polled = new TypeDocumentIndexer(polled.getBound().getMin(), end, flag);
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
