package org.obicere.cc.executor.language.util;

/**
 * Splits a document by indexes and by applying a specific flag to it. The
 * system can be used to effectively provide a context to a segment of a
 * document without copying elements from the document.
 * <p>
 * This is useful for parsing a documents, as example:
 * <p>
 * {@code foo bar 123}
 * <p>
 * Could be parsed to:
 * <pre>
 * {@code Bound[0, 3], TEXT
 *   Bound[3, 4], WHITESPACE
 *   Bound[4, 7], TEXT
 *   Bound[7, 8], WHITESPACE
 *   Bound[8, 11], NUMERAL
 * }
 * </pre>
 * Then, the indexers could be iterated over and appropriate functions
 * could then be applied to the specific parts of the text.
 * <p>
 * Although not supported by default in the contract, indexers can be
 * combined to avoid redundancy and possibly improve runtime performance.
 * One could combine the following two flags as such:
 * <pre>
 * {@code Bound[0, n], T
 *   Bound[n, m], T }
 * </pre>
 * Could be combined as such:
 * <pre>
 * {@code Bound[0, m], T}
 * </pre>
 * <p>
 * If appropriate. This is not held to contract in case the parser intends
 * to maintain the independent bounds if necessary.
 *
 * @author Obicere
 * @version 1.0
 */
public interface DocumentIndexer<F extends Flag> {

    /**
     * Provides the indexes of the document to apply the flag for. Although
     * not held by contract, the bounds should not overlap. This is to
     * ensure that the same element cannot provide ambiguity as to how it
     * should be parsed.
     * <p>
     * However there may be cases in which an overlap is necessary to hold
     * to a logical contract.
     *
     * @return The element's bounds that the current flag applies to.
     * @see #getFlag()
     */

    public Bound getBound();

    /**
     * The flag that applies to the current element, bounded by the index.
     * This is used to maintain vital information paired directly to the
     * element's bounds. This, opposed to attempting to map the indexes to
     * the types - or vice versa. The flags are very loosely defined, so
     * this can be applied to almost any scenario for parsing.
     *
     * @return The specific flag for this element.
     */

    public F getFlag();

}
