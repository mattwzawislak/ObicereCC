package org.obicere.cc.executor.language.util;

/**
 * Specifies a mathematical bound over two numbers. The bound is inclusive
 * on both ends.
 * <p>
 * So if given two numbers: <code>a</code> and <code>b</code>
 * <pre>
 * if a <= b:
 *     [a, b]
 * else:
 *     [b, a]
 * </pre>
 * <p>
 * The bounds are of type <code>int</code> meaning the lowest minimum bound
 * is {@link Integer#MIN_VALUE} and the largest maximum bound is {@link
 * Integer#MAX_VALUE} spanning a value of 2<sup>32</sup>. For this reason,
 * a <code>long</code> type has been used to store the delta value of the
 * bound.
 *
 * @author Obicere
 * @version 1.0
 */
public class Bound implements Comparable<Bound> {

    private final int min;
    private final int max;

    private final long delta;

    /**
     * Constructs a new bound based off of the two integers, <code>a</code>
     * and <code>b</code>.
     * <pre>
     * {@link Bound#min} is defined to be <code>min(a, b)</code>
     * {@link Bound#max} is defined to be <code>max(a, b)</code>
     * </pre>
     *
     * @param a One of the bounds.
     * @param b The other bound.
     */

    public Bound(final int a, final int b) {
        if (a <= b) {
            min = a;
            max = b;
        } else {
            min = b;
            max = a;
        }
        delta = ((long) max) - min;
    }

    /**
     * The difference between the minimum portion of the bound and the
     * maximum portion of the bound. This has a minimum value of
     * <code>0</code> and a maximum value of <code>2<sup>32</sup></code>.
     *
     * @return The difference in the bound.
     */

    public long getDelta() {
        return delta;
    }

    /**
     * The lower portion of the bound. This value will always be less than
     * - or equal to - the maximum bound.
     *
     * @return The lower bound.
     */

    public int getMin() {
        return min;
    }

    /**
     * The upper portion of the bound. This value will always be greater
     * than - or equal to - the minimum bound.
     *
     * @return The upper bound.
     */

    public int getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Bound && boundEquals((Bound) obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return min * 17 + max * 31;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Prints out the simple class name followed by the mathematical
     * notation for a bound. For example when <code>min = 5</code> and
     * <code>max = 10</code>:
     * <p>
     * {@code "Bound[5, 10]"}
     */
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

    /**
     * Checks to see if two bounds are equal. This will check to see if
     * both the minimum and maximum bounds match.
     *
     * @param bound The bound to compare the current instance to.
     * @return <code>true</code> if and only if the bounds are logically
     * equivalent.
     */

    private boolean boundEquals(final Bound bound) {
        return bound.getMin() == min && bound.getMax() == max;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Arranges the bounds by order of their minimum bound. If the two
     * bounds have the same minimum value, then they are compared by
     * maximum value.
     */

    @Override
    public int compareTo(final Bound o) {
        if (o == null) {
            return 1;
        }
        final int compare = Integer.compare(min, o.getMin());
        if (compare == 0) {
            return Integer.compare(max, o.getMax());
        } else {
            return compare;
        }
    }
}