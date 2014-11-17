package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */


public class Bound implements Comparable<Bound> {

    private final int start;
    private final int end;

    private final int delta;

    public Bound(final int a, final int b) {
        if (a <= b) {
            start = a;
            end = b;
        } else {
            start = b;
            end = a;
        }
        delta = end - start;
    }

    public int getDelta() {
        return delta;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Bound && boundEquals((Bound) obj);
    }

    @Override
    public int hashCode() {
        return start;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(10); // 10 is absolute min string length
        builder.append("Bound[");
        builder.append(start);
        builder.append(",");
        builder.append(end);
        builder.append("]");
        return builder.toString();
    }

    private boolean boundEquals(final Bound bound) {
        return bound.start == start;
    }


    @Override
    public int compareTo(final Bound o) {
        return Integer.compare(start, o.getStart());
    }
}