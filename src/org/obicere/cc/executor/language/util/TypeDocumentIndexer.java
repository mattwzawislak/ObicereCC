package org.obicere.cc.executor.language.util;

/**
 * {@inheritDoc}
 * <p>
 * Used for indexing a document for parsing. This will contain information
 * such as what exactly the current element would be parsed as. By default
 * the {@link org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#PLAINTEXT}
 * flag is the specified flag.
 *
 * @author Obicere
 * @version 1.0
 */
public class TypeDocumentIndexer implements DocumentIndexer<TypeDocumentIndexer.TypeFlag> {

    private final Bound    bound;
    private final TypeFlag flag;

    /**
     * Constructs a new indexer over the bounds. The upper bound is
     * exclusive and the lower bound is inclusive. This is such that
     * utilizing the {@link String#substring(int, int)} method will return
     * exactly the element the current flag applies to.
     *
     * @param start The lower bound, inclusive.
     * @param end   The upper bound, exclusive.
     * @param flag  The flag type of the current element.
     */

    public TypeDocumentIndexer(final int start, final int end, final TypeFlag flag) {
        this.bound = new Bound(start, end);
        this.flag = flag;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public Bound getBound() {
        return bound;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public TypeFlag getFlag() {
        return flag;
    }

    /**
     * Specifies whether or not the current indexer's flag is a {@link
     * org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#LITERAL}
     * or not.
     *
     * @return Whether or not the current element is a literal.
     */

    public boolean isLiteral() {
        return flag == TypeFlag.LITERAL;
    }

    /**
     * Specifies whether or not the current indexer's flag is a {@link
     * org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#PLAINTEXT}
     * or not.
     *
     * @return Whether or not the current element is plaintext.
     */

    public boolean isPlaintext() {
        return flag == TypeFlag.PLAINTEXT;
    }

    /**
     * Specifies whether or not the current indexer's flag is a {@link
     * org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#OPERATOR}
     * or not.
     *
     * @return Whether or not the current element is an operator.
     */

    public boolean isOperator() {
        return flag == TypeFlag.OPERATOR;
    }

    /**
     * Specifies whether or not the current indexer's flag is a {@link
     * org.obicere.cc.executor.language.util.TypeDocumentIndexer.TypeFlag#KEYWORD}
     * or not.
     *
     * @return Whether or not the current element is a keyword.
     */

    public boolean isKeyWord() {
        return flag == TypeFlag.KEYWORD;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The format specifies the bound element and the flag type. In such a
     * format:
     * <p>
     * {@code TypeDocumentIndexer[Bound[m, n], T]}
     * <p>
     * Where <code>m</code> is the lower bound, <code>n</code> is the upper
     * bound and <code>T</code> is the type flag that applies to the
     * current element.
     */

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("TypeDocumentIndexer[");
        builder.append(bound);
        builder.append(",");
        builder.append(flag);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Specifies the type the indexer applies to for the current element.
     * For parsing code, this will include plaintext, literals, operators
     * and keywords.
     * <p>
     * By default intersections are not permitted.
     */

    public enum TypeFlag implements Flag {

        PLAINTEXT, // names, symbols (that are not operators) and non-keywords

        LITERAL, // comments and string literals
        OPERATOR, // +, -, /, *, ^, #, %, @, !, =, etc...
        KEYWORD; // keywords specified in the language specification

        /**
         * {@inheritDoc}
         * <p>
         * For the given flag type, there is no reason to have intersection
         * as the current flags are used for parsing documents as opposed
         * to logical flow.
         */

        @Override
        public boolean allowsIntersection() {
            return false;
        }

    }

}
