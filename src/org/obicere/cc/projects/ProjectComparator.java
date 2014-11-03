package org.obicere.cc.projects;

import java.util.Comparator;

/**
 * @author Obicere
 */
public class ProjectComparator implements Comparator<Project> {

    @Override
    public int compare(final Project a, final Project b) {
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        final int compareDifficulty = Integer.compare(a.getDifficulty(), b.getDifficulty());
        if (compareDifficulty != 0) {
            return compareDifficulty;
        } else {
            final String aName = a.getName();
            final String bName = b.getName();
            if (aName == null) {
                return -1;
            }
            if (bName == null) {
                return 1;
            }
            return aName.compareTo(bName);
        }
    }
}
