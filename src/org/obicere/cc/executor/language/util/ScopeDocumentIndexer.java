package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */
public class ScopeDocumentIndexer implements DocumentIndexer<ScopeDocumentIndexer.ScopeFlag> {

    private final int       level;
    private final ScopeFlag flag;

    private final Bound                bound;
    private final ScopeDocumentIndexer parent;

    public ScopeDocumentIndexer(final int start, final int end) {
        this.bound = new Bound(start, end);
        this.level = 0;
        this.parent = null;
        this.flag = ScopeFlag.PARENT;
    }

    public ScopeDocumentIndexer(final int start, final int end, final ScopeDocumentIndexer parent) {
        this.bound = new Bound(start, end);
        this.level = parent.getLevel() + 1;
        this.parent = parent;
        this.flag = ScopeFlag.CHILD;
    }

    public int getLevel() {
        return level;
    }

    public ScopeDocumentIndexer getParent() {
        return parent;
    }

    public boolean isTopLevelScope() {
        return isParent();
    }

    public boolean isParent() {
        return flag == ScopeFlag.PARENT;
    }

    public boolean isChild() {
        return flag == ScopeFlag.CHILD;
    }

    @Override
    public Bound getBound() {
        return bound;
    }

    @Override
    public ScopeFlag getFlag() {
        return flag;
    }

    public enum ScopeFlag implements Flag {
        PARENT,
        CHILD;

        @Override
        public boolean allowsIntersection(){
            return true;
        }
    }
}
