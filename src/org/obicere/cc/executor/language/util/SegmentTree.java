package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */
public class SegmentTree<D extends DocumentIndexer<? extends Flag>> {

    private SegmentNode root;

    public SegmentTree(final int maxRange) {
        root = new SegmentNode(0, maxRange);
    }

    public boolean add(final D index) {
        return add(new SegmentNode(index));
    }

    private boolean add(final SegmentNode node) {
        if (root.addToLeft(node) != null) {
            return true;
        } else if (root.addToRight(node) != null) {
            return true;
        }
        return false;
    }

    public void printDepth() {
        print(root);
    }

    private void print(final SegmentNode node) {
        if (node == null) {
            return;
        }
        System.out.println(node);
        print(node.left);
        print(node.right);
    }

    private class SegmentNode {

        private final D value;

        private int start;
        private int end;

        private SegmentNode left;
        private SegmentNode right;

        protected SegmentNode(final int start, final int end) {
            this.start = start;
            this.end = end;
            this.value = null;
        }

        public SegmentNode(final D value) {
            final Bound bound = value.getBound();
            this.start = bound.getMin();
            this.end = bound.getMax();
            this.value = value;
        }

        public D getValue() {
            return value;
        }

        public SegmentNode addToRight(final SegmentNode node) {
            if (right == null) {
                if (rightMatch(node)) {
                    right = node;
                    return this;
                }
                return null;
            }
            if (right.leftMatch(node)) {
                return right.addToLeft(node);
            } else if (right.rightMatch(node)) {
                return right.addToRight(node);
            }
            return null;
        }

        public SegmentNode addToLeft(final SegmentNode node) {
            if (left == null) {
                if (leftMatch(node)) {
                    left = node;
                    return this;
                }
                return null;
            }
            if (left.leftMatch(node)) {
                return left.addToLeft(node);
            } else if (left.rightMatch(node)) {
                return left.addToRight(node);
            }
            return null;
        }

        private boolean leftMatch(final SegmentNode node) {
            return start == node.start;
        }

        private boolean rightMatch(final SegmentNode node) {
            return end == node.end;
        }

        @Override
        public String toString() {
            if (value == null) {
                return toRootString();
            }
            return value.toString();
        }

        private String toRootString() {
            final StringBuilder builder = new StringBuilder(32);
            builder.append("DocumentIndexer[ROOT,Bound[");
            builder.append(start);
            builder.append(",");
            builder.append(end);
            builder.append("]]");
            return builder.toString();
        }

    }

}
