package org.obicere.cc.executor.language.impl.java;

import org.obicere.cc.executor.language.CodeFormatter;

/**
 * @author Obicere
 */
public class JavaCodeFormatter implements CodeFormatter {

    private static final String BRACKET_CASE_MATCH = ".*?(\\{)\\s*(\\})";

    @Override
    public int newlineEntered(final String code, final StringBuilder add, final int caret, final int row, final int column) {
        add.append("\n");

        int newCaret = caret + 1; // accommodate for the newline
        int tabs = 0;
        final String[] lines = code.split("\n");
        final int max = Math.min(row + 1, lines.length);
        final String currentLine = lines[max - 1];// Don't attempt to check lines when none exist

        int shortCounter = 0;
        for (int i = 0; i < max; i++) {
            int open = 0;
            int close = 0;
            int block = 0;
            final char[] set = lines[i].toCharArray();
            boolean lineIsStatement = false;
            int bound = i == row ? Math.min(column + 1, set.length) : set.length;
            for (int j = 0; j < bound; j++) {
                switch (set[j]) {
                    case '{':
                        open++;
                        break;
                    case '}':
                        close++;
                        break;
                    case '(':
                        block++;
                        break;
                    case ';':
                        lineIsStatement = true;
                        break;
                }
            }
            tabs += open - close;
            if (open == 0 && block > 0 && !lineIsStatement) {
                tabs++;
                shortCounter++;
            } else if (lineIsStatement) {
                tabs -= shortCounter;
                shortCounter = 0;
            }
        }
        for (int i = 0; i < tabs; i++) {
            add.append("\t");
        }
        newCaret += tabs;

        if (currentLine.matches(BRACKET_CASE_MATCH)) {
            final int openMatchIndex = currentLine.indexOf('{');
            final int closeMatchIndex = currentLine.indexOf('}', column);
            if (openMatchIndex < column && column <= closeMatchIndex) {
                final String soFar = add.toString();
                add.append('\t');
                add.append(soFar); // Effectively duplicate the string since we are adding
                newCaret++;        // two lines here with the first having an extra tab
            }
        }
        return newCaret;
    }
}
