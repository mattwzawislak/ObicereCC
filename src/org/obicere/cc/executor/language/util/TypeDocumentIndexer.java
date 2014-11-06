package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */
public class TypeDocumentIndexer implements DocumentIndexer<TypeDocumentIndexer.TypeFlag> {

    private final Bound    bound;
    private final TypeFlag flag;

    public TypeDocumentIndexer(final int index, final TypeFlag flag) {
        this.bound = new Bound(index, index);
        this.flag = flag;
    }

    public TypeDocumentIndexer(final int start, final int end, final TypeFlag flag) {
        this.bound = new Bound(start, end);
        this.flag = flag;
    }

    @Override
    public Bound getBound() {
        return bound;
    }

    @Override
    public TypeFlag getFlag() {
        return flag;
    }

    public boolean isLiteral() {
        return flag == TypeFlag.LITERAL;
    }

    public boolean isPlaintext() {
        return flag == TypeFlag.PLAINTEXT;
    }

    public boolean isOperator() {
        return flag == TypeFlag.OPERATOR;
    }

    public boolean isOpening() {
        return flag == TypeFlag.TAB_INCREASE;
    }

    public boolean isClosing() {
        return flag == TypeFlag.TAB_DECREASE;
    }

    public boolean isKeyWord() {
        return flag == TypeFlag.KEYWORD;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("TypeDocumentIndexer[");
        builder.append(flag);
        builder.append(",");
        builder.append(bound);
        builder.append("]");
        return builder.toString();
    }

    public enum TypeFlag implements Flag {

        PLAINTEXT,

        LITERAL,
        OPERATOR,
        KEYWORD,

        TAB_INCREASE,
        TAB_DECREASE;

        @Override
        public boolean allowsIntersection() {
            return false;
        }

    }

}
