/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.gui;

import org.obicere.cc.executor.language.Language;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <tt>CodePane</tt> class is used to demonstrate a Java editor.
 * It can also be used to showcase Java code with or without the
 * feature of highlighting keywords.
 *
 * @author Obicere
 * @version 1.0
 * @since 1.0
 */

public class CodePane extends JTextPane {

    private static final String MASTER_SPLIT = "([(\\[\\]);\\{}\0.])";
    private static final SimpleAttributeSet KEYWORD_SET = new SimpleAttributeSet();
    private static final SimpleAttributeSet NORMAL_SET = new SimpleAttributeSet();
    private static final SimpleAttributeSet OTHER_SET = new SimpleAttributeSet();
    private static final Font CONSOLAS = new Font("Consolas", Font.PLAIN, 14);

    static {
        StyleConstants.setForeground(OTHER_SET, new Color(40, 116, 167));
        StyleConstants.setForeground(KEYWORD_SET, new Color(120, 43, 8));
        StyleConstants.setForeground(NORMAL_SET, new Color(36, 36, 36));
    }

    private final boolean color;
    private final Language language;

    /**
     * Constructs a basic <tt>CodePane</tt> instance.
     * This sets the content type equal to Java styling.
     *
     * @param color Whether or not to highlight keywords
     */

    public CodePane(final String content, final boolean color, final Language language) {
        this.color = color;
        this.language = language;

        final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap actionMap = getActionMap();
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        final TabStop[] tabs = new TabStop[50];
        final FontMetrics metrics = getFontMetrics(CONSOLAS);
        final int width = metrics.stringWidth("    ");
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabStop(width * i);
        }
        final TabSet tabSet = new TabSet(tabs);
        final AttributeSet paraSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        setContentType("java");
        setFont(CONSOLAS);
        setParagraphAttributes(paraSet, false);
        setText(content);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Newline");

        actionMap.put("Newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int index = getCaretPosition();
                final int lineNumber = getCaretLine();
                final String line = getLine(lineNumber);
                int tabCount = 0;
                for (final char c : line.toCharArray()) {
                    if (c == '\t') {
                        tabCount++;
                    } else {
                        break;
                    }
                }
                if (line.matches(".*?[\\)\\{]\\s*")) {
                    tabCount++;
                }

                final String code = getText();
                final StringBuilder newCode = new StringBuilder(code.substring(0, index));
                newCode.append('\n');
                for (int i = 0; i < tabCount; i++) {
                    newCode.append('\t');
                }
                newCode.append(code.substring(index));
                setText(newCode.toString());
                setCaretPosition(index + 1 + tabCount);
                highlightKeywords();
            }
        });
        addCaretListener(new CaretListener() {

            public void caretUpdate(final CaretEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            final JTextComponent component = (JTextComponent) e.getSource();
                            final int position = component.getCaretPosition();
                            final Rectangle r = component.modelToView(position);
                            r.x += 2;
                            component.scrollRectToVisible(r);
                            highlightKeywords(); // Whenever a new character gets added, caret updates.
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        });
    }

    /**
     * Used in conjunction with {@link org.obicere.cc.gui.CodePane#highlightKeywords()}
     * <br>
     * This will replace all matches of a given <tt>regex</tt> in the given <tt>code</tt>
     * with a <tt>String</tt> composed of null characters.
     *
     * @param code  The code to parse and replace
     * @param regex The regex to match
     * @return The new <tt>code</tt> with all matches removed
     */

    private String clearMatches(String code, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            final String match = matcher.group();
            code = code.replace(match, CharBuffer.allocate(match.length()).toString());
        }
        return code;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    /**
     * This will highlight the Java keywords, from a list of reserved ones. It
     * will change the color of them, given by specific character attribute.
     * <br>
     * This also avoids highlighting keywords inside a String.
     *
     * @since 1.0
     */

    public void highlightKeywords() {
        if (!color) {
            return;
        }
        String code = getText();
        for (final String literal : language.getLiteralMatchers()) {
            code = clearMatches(code, literal);
        }
        code = code.replaceAll(MASTER_SPLIT, " $1 "); // create buffer.
        // this will allow keywords to be adjacent to syntax-related characters
        // EX: (int)

        final StyledDocument style = getStyledDocument();
        int i = 0;
        for (final String word : code.split("\\s")) {
            boolean match = false;
            if (word.matches(MASTER_SPLIT)) {
                match = true;
                // accommodate buffer
                i--;
            }
            if (word.matches("(\u0000)+")) { // an empty String buffer
                style.setCharacterAttributes(i, word.length(), OTHER_SET, true);
            } else {
                final boolean keyword = language.isKeyword(word);
                style.setCharacterAttributes(i, word.length(), keyword ? KEYWORD_SET : NORMAL_SET, true);
            }
            i += word.length() + 1;
            if (match) {
                // accommodate buffer
                i--;
            }
        }
    }

    public int getCaretLine() {
        final Document doc = getDocument();
        final Element map = doc.getDefaultRootElement();
        return map.getElementIndex(getCaretPosition());
    }

    public String getLine(int line) {
        if (getText().length() == 0) {
            return "";
        }
        final Element map = getDocument().getDefaultRootElement();
        final Element branch = map.getElement(line);
        return getText().substring(branch.getStartOffset(), branch.getEndOffset());
    }


}