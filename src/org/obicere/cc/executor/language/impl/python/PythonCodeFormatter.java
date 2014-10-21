package org.obicere.cc.executor.language.impl.python;

import org.obicere.cc.executor.language.CodeFormatter;

/**
 * @author Obicere
 */
public class PythonCodeFormatter implements CodeFormatter {

    @Override
    public int newlineEntered(final String code, final StringBuilder add, final int caret, final int row, final int column) {

        return 0;
    }
}
