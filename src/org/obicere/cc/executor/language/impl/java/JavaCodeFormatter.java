package org.obicere.cc.executor.language.impl.java;

import org.obicere.cc.executor.language.CodeFormatter;

/**
 * {@inheritDoc}
 *
 * @author Obicere
 * @version 1.0
 */
public class JavaCodeFormatter implements CodeFormatter {

    private static final String BRACKET_CASE_MATCH = ".*?(\\{)\\s*(\\})";

    @Override
    public int newlineEntered(final StringBuilder add, final int caret, final int row, final int column) {
        final StringBuilder append = new StringBuilder();
        append.append("\n");

        int newCaret = caret + 1; // accommodate for the newline
        int tabs = 0;
        final String[] lines = add.toString().split("\n");
        final int max = Math.min(row, lines.length - 1);
        final String currentLine = lines[max];// Don't attempt to check lines when none exist

        int shortCounter = 0;
        for (int i = 0; i <= max; i++) {
            int open = 0;
            int close = 0;
            final char[] set = lines[i].trim().toCharArray();
            boolean lineIsStatement = false;
            boolean nextLineExpression = false;
            int bound = i == row ? Math.min(column + 1, set.length) : set.length;
            for (int j = 0; j < bound; j++) {
                switch (set[j]) {
                    case '{':
                        lineIsStatement = false;
                        open++;
                        break;
                    case '}':
                        close++;
                        break;
                    case ')':
                        if (j == bound - 1) {
                            nextLineExpression = true;
                        }
                        break;
                    case ';':
                        lineIsStatement = true;
                        break;
                }
            }
            tabs += open - close;
            if (lineIsStatement) {
                tabs -= shortCounter;
                shortCounter = 0;
            } else if (nextLineExpression) {
                shortCounter++;
                tabs++;
            }
        }
        for (int i = 0; i < tabs; i++) {
            append.append("\t");
        }
        newCaret += tabs;

        if (currentLine.matches(BRACKET_CASE_MATCH)) {
            final int openMatchIndex = currentLine.indexOf('{');
            final int closeMatchIndex = currentLine.indexOf('}', column);
            if (openMatchIndex < column && column <= closeMatchIndex) {
                final String soFar = append.toString();
                append.append('\t');
                append.append(soFar); // Effectively duplicate the string since we are adding
                newCaret++;        // two lines here with the first having an extra tab
            }
        }
        add.insert(caret, append);
        return newCaret;
    }
}
