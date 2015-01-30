package org.obicere.cc.executor.language.util;

/**
 * The default flag feature for an indexer to use. This will specify
 * whether or not intersections can exist between flags. So it makes sense
 * to have two elements overlap, this can be accommodated. All flags have
 * been held to fulfil this contract to allow more dynamic parsing
 * control.
 * <p>
 * In general, there should be at least one flag and a default flag
 * specified. The default flag may not be explicitly stated but should be
 * specified by the implementation. This will ensure that some
 * optimizations can be made to improve the performance of a parse and
 * reduce the memory complexity.
 * <p>
 * A case where intersections should be allowed would be for dealing with a
 * scope parser. Since a scope may span over one or more scopes. However,
 * intersections should not be allowed in cases such as code styling. It
 * does not make sense that a single character can contain to two different
 * parse groups in a document context.
 * <p>
 * This has been left up to the discretion of the implementation.
 *
 * @author Obicere
 * @version 1.0
 */
public interface Flag {

    /**
     * Denotes whether or not the flag type allows intersection. This can
     * be used primarily to suggest when the given element bounded by the
     * indices has more than one attribute.
     * <p>
     * Of course, some flag groups it would make sense to have
     * intersection, such as a scope indexer. However in a deterministic
     * grammar indexer, intersections do not make sense.
     * <p>
     * The scope of the intersection is flag-dependent. So should a
     * specific flag in the group require intersection, the other flags do
     * not need to allow intersection. However, should only a single flag
     * in the group allow intersection, there wouldn't be a valid purpose.
     * <p>
     * If unsure about whether or not the flag should inherit intersection,
     * most will not. So setting this to <code>false</code> by default is
     * recommended.
     *
     * @return Whether or not the given flag type allows intersection.
     */

    public boolean allowsIntersection();

}
