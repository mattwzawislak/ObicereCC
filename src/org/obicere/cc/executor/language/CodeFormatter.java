package org.obicere.cc.executor.language;

/**
 * A utilized used by the language specification to handle code formatting.
 * As of build v1.0 the only prebuilt formatting is through new lines being
 * added to the code.
 * <p>
 * These is merely a commodity and is just used to help ease the use of the
 * user. For example, instead of having to properly intend the code upon a
 * new line being added, the program will handle this automatically.
 * <p>
 * For every utility, the new caret index should be returned. This is to
 * help provide a very seamless procedure for the user. Any modifications
 * should be passed off to the {@link java.lang.StringBuilder} for new
 * code.
 * <p>
 * This will emulate an listener format, if need-be. As of now however,
 * this is still a utility of the specification as opposed to an optional
 * feature.
 *
 * @author Obicere
 * @version 1.0
 */
public interface CodeFormatter {

    /**
     * Upon a new line being added, this method will be invoked by the code
     * pane. This method should handle the addition of whitespace and other
     * optional characters to simulate a new line being added with ease.
     * <p>
     * The caret should also be updated to reflect where the user would
     * most likely be typing next upon the new line's addition. This is
     * most often at the end of the new block being added, but not required
     * by contract.
     *
     * @param code   The current code.
     * @param caret  The current caret index in the code.
     * @param row    The current line the caret is present on.
     * @param column The current index in the line the caret is present
     *               on.
     * @return The new caret index.
     */

    public int newlineEntered(final StringBuilder code, final int caret, final int row, final int column);

}
