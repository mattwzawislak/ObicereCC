package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */


public class Bound implements Comparable<Bound> {

    private final int min;
    private final int max;

    private final int delta;

    public Bound(final int a, final int b) {
        if (a <= b) {
            min = a;
            max = b;
        } else {
            min = b;
            max = a;
        }
        delta = max - min;
    }

    public int getDelta() {
        return delta;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Bound && boundEquals((Bound) obj);
    }

    @Override
    public int hashCode() {
        return min;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(10); // 10 is absolute min string length
        builder.append("Bound[");
        builder.append(min);
        builder.append(",");
        builder.append(max);
        builder.append("]");
        return builder.toString();
    }

    private boolean boundEquals(final Bound bound) {
        return bound.min == min;
    }


    @Override
    public int compareTo(final Bound o) {
        return Integer.compare(min, o.getMin());
    }
}