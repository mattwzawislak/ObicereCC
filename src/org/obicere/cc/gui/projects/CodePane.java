package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.language.CodeFormatter;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.executor.language.util.Bound;
import org.obicere.cc.executor.language.util.DocumentInspector;
import org.obicere.cc.executor.language.util.TypeDocumentIndexer;
import org.obicere.cc.shutdown.CodeCompletionHook;
import org.obicere.cc.shutdown.EditorHook;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

/**
 * Used for styling a file based off of a specific programming {@link
 * org.obicere.cc.executor.language.Language}. This provides the styling used for literals and
 * keywords. It will also handle all the font, code completion and styling settings.
 * <p>
 * The code pane will maintain the code completion by monitoring previous key strokes that can be
 * easily completed under the {@link org.obicere.cc.shutdown.CodeCompletionHook}. There is one
 * second of delay between when the opening character is pressed and when the closing character(s)
 * that follow are consumed. This is to ensure that given a proficient typing speed, that the code
 * completion will not conflict with the actual typing of the user.
 * <p>
 * The currently supported code completion pairs as of v1.0 are:
 * <pre><ul>
 * <li>Curly brackets: {}
 * <li>Square brackets: []
 * <li>Parenthesis: ()
 * </ul>
 * </pre>
 * <p>
 * The code completion metrics on all of these will automatically add the closing character once the
 * opening character is typed. Note, that this will not occur on document changes where snippets of
 * code are pasted, only on a keystroke. Once the closing character is added, the caret is then
 * moved to in between the brackets.
 * <p>
 * There is also code completion present through newline feeds. The sole function of this is to help
 * provide a more seamless experience for the user. This involves adding the correct amount of tabs
 * and setting the correct caret position. This is very language specific, as the notion for what
 * deserves a tab is defined purely on the language. This should be handled by the language's {@link
 * org.obicere.cc.executor.language.CodeFormatter}. Should one not be defined, then no linefeed
 * completion will be provided.
 * <p>
 * As of v1.0, there is no listener for when hook settings are updated. Due to this, the code pane
 * will check for modifications by remembering the last update. This should be improved on future
 * releases, along with the possibility of moving some content out of this class.
 *
 * @author Obicere
 * @version 1.0
 */

public class CodePane extends JTextPane {

    private static final SimpleAttributeSet KEYWORD_SET   = new SimpleAttributeSet();
    private static final SimpleAttributeSet PLAINTEXT_SET = new SimpleAttributeSet();
    private static final SimpleAttributeSet LITERAL_SET   = new SimpleAttributeSet();

    private static final int    FONT_SIZE;
    private static final String FONT_FAMILY_NAME;

    private static final EditorHook         EDITOR     = Domain.getGlobalDomain().getHookManager().hookByClass(EditorHook.class);
    private static final CodeCompletionHook COMPLETION = Domain.getGlobalDomain().getHookManager().hookByClass(CodeCompletionHook.class);

    /**
     * Assert that the hook values have already been set prior to the construction of the first code
     * panel.
     */

    static {
        StyleConstants.setForeground(LITERAL_SET, EDITOR.getPropertyAsColor(EditorHook.STRING_STYLING_COLOR));
        StyleConstants.setForeground(KEYWORD_SET, EDITOR.getPropertyAsColor(EditorHook.KEYWORD_STYLING_COLOR));
        StyleConstants.setForeground(PLAINTEXT_SET, EDITOR.getPropertyAsColor(EditorHook.NORMAL_STYLING_COLOR));

        FONT_FAMILY_NAME = EDITOR.getPropertyAsString(EditorHook.EDITOR_FONT_TYPE);
        FONT_SIZE = EDITOR.getPropertyAsInt(EditorHook.EDITOR_FONT_SIZE);
    }

    private HashMap<Character, Long> lastHit = new HashMap<>();

    private long lastUpdate;

    private final Language language;

    /**
     * Constructs a new code pane, sets the tab stops, the font styling and the input maps for
     * handling code completion. A new code pane has to be defined for each file that will be
     * opened. This is since each file and its metrics are unique to the language.
     *
     * @param content  The default text for this code pane; from the source file.
     * @param language The language of the file opened here.
     */

    public CodePane(final String content, final Language language) {
        this.language = language;

        final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap actionMap = getActionMap();

        setKeyMaps(inputMap, actionMap);
        setContentType("java");
        setText(content);
        setTabStops(FONT_FAMILY_NAME, FONT_SIZE);
        addCaretListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                final JTextComponent component = (JTextComponent) e.getSource();
                final int position = component.getCaretPosition();
                final Rectangle r = component.modelToView(position);
                r.x += 2;
                component.scrollRectToVisible(r);
                styleDocument(); // Whenever a new character gets added, caret updates.
            } catch (final BadLocationException ignored) {
            }
        }));

    }

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to add default styling upon the viewport changing. This assumes that every code pane
     * is contained within a {@link javax.swing.JScrollPane}, which is added by the swing library by
     * default.
     * <p>
     * Should no such scroll pane exist, then no updating will happen automatically.
     */

    @Override
    public void addNotify() {
        super.addNotify();
        final Container con = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (con != null) {
            ((JScrollPane) con).getViewport().addChangeListener(e -> styleDocument());
        }
    }

    /**
     * Sets the correct width for the tab stops based off of the font family name and the font size.
     * This will use the {@link Font#PLAIN} mask, as no sane person would consider using bold or
     * italics (or both) when writing code. At least I hope...
     * <p>
     * This will add up to 50 tab stops available. This should be a sufficiently large amount to
     * handle any document.
     * <p>
     * The value used here for the tab widths is language specific, most will use a value of
     * <code>4</code>, however some languages will do more or less. This value is in spaces. For
     * example with tab-size 4:
     * <pre>
     * {@code public void foo(){
     *       System.out.println("foo");
     *   }
     * }
     * </pre>
     * And with tab-size 5:
     * <pre>
     * {@code public void foo(){
     *        System.out.println("foo");
     *   }
     * }
     * </pre>
     * <p>
     * The difference is merely the extra space before the print statement. For this to be effective
     * though, a monospaced font should always be used, however this is not enforced. Such examples
     * would include:
     * <pre>
     * <ul>
     * <li> Consolas
     * <li> Courier New
     * <li> Monospaced
     * </ul>
     * </pre>
     *
     * @param familyName
     * @param size
     *
     * @see org.obicere.cc.executor.language.Language#getTabSize()
     */

    private void setTabStops(final String familyName, final int size) {
        final StyleContext context = StyleContext.getDefaultStyleContext();
        final TabStop[] tabs = new TabStop[50];
        final FontMetrics metrics = getFontMetrics(new Font(familyName, Font.PLAIN, size));
        // Create a string of length equal to the tab size by formatting
        final int width = metrics.stringWidth(String.format(String.format("%%%ss", language.getTabSize()), " "));
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabStop(width * i);
        }
        final TabSet tabSet = new TabSet(tabs);

        final AttributeSet params1 = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        final AttributeSet params2 = context.addAttribute(params1, StyleConstants.FontSize, size);
        final AttributeSet params3 = context.addAttribute(params2, StyleConstants.FontFamily, familyName);
        final StyledDocument doc = getStyledDocument();
        doc.setParagraphAttributes(0, getText().length(), params3, true);
    }

    /**
     * Provides the closing character should the code completion settings allow this character to be
     * automatically completed. If code completion for the pair is not set, then the
     * <code>null</code> character is returned.
     * <p>
     * If the character is also not supported by code completion regardless of settings, then
     * <code>0</code> is returned.
     *
     * @param c The character to match with the closing pair.
     *
     * @return The closing pair if and only if code completion is allowed on the group.
     */

    private char getClosingCharacter(final char c) {
        switch (c) {
            case '(':
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.PARENTHESES_COMPLETION)) {
                    return 0;
                }
                return ')';
            case '{':
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.CURLY_BRACKET_COMPLETION)) {
                    return 0;
                }
                return '}';
            case '[':
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.SQUARE_BRACKET_COMPLETION)) {
                    return 0;
                }
                return ']';
            default:
                return 0;
        }
    }

    /**
     * Registers a hit to mark to handle the delay between when a character is automatically added
     * and when it stops being automatically consumed. This is used to provide ease of the code
     * completion, so if the user wants to type the character generated by the code completion, then
     * two copies of the character will not appear.
     *
     * @param c The character to consume.
     */

    private void registerHit(final char c) {
        lastHit.put(c, System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    /**
     * Temporary solution until the event dispatching has been created for hook updates. This will
     * monitor the last update to the settings. If the updates don't match, the settings are
     * reloaded from the hook. This should be seldom checked, due to the constant time cost to check
     * for updates, yet should be called often enough that the settings are reflected properly to
     * the user.
     */

    private void checkForUpdates() {
        final long editorUpdate = EDITOR.getLastEditorUpdate();
        if (lastUpdate != editorUpdate) {
            StyleConstants.setForeground(LITERAL_SET, EDITOR.getPropertyAsColor(EditorHook.STRING_STYLING_COLOR));
            StyleConstants.setForeground(KEYWORD_SET, EDITOR.getPropertyAsColor(EditorHook.KEYWORD_STYLING_COLOR));
            StyleConstants.setForeground(PLAINTEXT_SET, EDITOR.getPropertyAsColor(EditorHook.NORMAL_STYLING_COLOR));

            final String familyName = EDITOR.getPropertyAsString(EditorHook.EDITOR_FONT_TYPE);
            final int size = EDITOR.getPropertyAsInt(EditorHook.EDITOR_FONT_SIZE);
            lastUpdate = editorUpdate;

            setTabStops(familyName, size);
        }
    }

    /**
     * Styles the document by applying the {@link org.obicere.cc.executor.language.util.DocumentInspector}
     * to the current text. This will use the {@link org.obicere.cc.executor.language.Language}
     * specific functions to determine what should be applied to what.
     * <pre>
     * <ul>
     * <li>{@link #PLAINTEXT_SET}: applied to all operators, names and other symbols including
     * whitespace.
     * <li>{@link #KEYWORD_SET}: applies to all keywords registered by {@link
     * org.obicere.cc.executor.language.Language#getKeywords()}
     * <li>{@link #LITERAL_SET}: applies to all matches of literals by the language. This should
     * include comments, strings and characters.
     * </ul>
     * </pre>
     * <p>
     * This has been optimized to only render what <i>should</i> be visible. This is to avoid
     * styling a 10,000 line document when you can only see 10 lines.
     * <p>
     * The next optimization resides in the {@link org.obicere.cc.executor.language.util.DocumentInspector}
     * to reduce the number of styles that need to be applied.
     * <p>
     * The last optimization is also based off of how the document inspector creates the styles.
     * This will apply a one-time style across the visible document with the {@link #PLAINTEXT_SET},
     * then filling in the appropriate style where necessary. This avoids having to switch between
     * styles abundantly.
     * <p>
     * This has to do with a prime concept applied by the {@link org.obicere.cc.executor.language.util.Flag}
     * and how certain flags can be combined to create larger and fewer flags. Since the most costly
     * part of this function is applying the styles, this small change makes a substantial
     * difference.
     * <p>
     * On the last note, this will also check for updates made by the hook, so any changes in font
     * family, font size or colors will be reflected upon the next update.
     */

    public void styleDocument() {
        if (!EDITOR.getPropertyAsBoolean(EditorHook.ENABLED_STYLING)) {
            return;
        }
        final String document = getText();
        final int characterCount = document.length();

        final Rectangle visible = getVisibleRect();
        final Point start = new Point(visible.x, visible.y);
        final Point end = new Point(visible.x + visible.width, visible.y + visible.height);

        final int min = viewToModel(start);
        final int max = viewToModel(end);

        final int startRender = (min < 0 ? 0 : min);
        final int endRender = (max > characterCount ? characterCount : max);

        checkForUpdates(); // Be sure to get the latest changes.

        final StyledDocument style = getStyledDocument();
        final DocumentInspector inspector = new DocumentInspector(language);

        inspector.apply(document, startRender, endRender);
        final List<TypeDocumentIndexer> segments = inspector.getContent();

        style.setCharacterAttributes(startRender, endRender, PLAINTEXT_SET, true);
        for (final TypeDocumentIndexer next : segments) {
            if (next == null) {
                continue;
            }
            final Bound bound = next.getBound();
            final SimpleAttributeSet set;
            switch (next.getFlag()) {
                case LITERAL:
                    set = LITERAL_SET;
                    break;
                case KEYWORD:
                    set = KEYWORD_SET;
                    break;
                default:
                    set = PLAINTEXT_SET;
                    break;

            }
            style.setCharacterAttributes(bound.getMin(), (int) bound.getDelta(), set, true);
        }
    }

    /**
     * Retrieves the current line the caret is present on. This is done by splitting the document
     * into lines implicitly, then getting the element that contains the caret index.
     *
     * @return The line the caret is present on.
     */

    public int getCaretLine() {
        final Document doc = getDocument();
        final Element map = doc.getDefaultRootElement();
        return map.getElementIndex(getCaretPosition());
    }

    /**
     * Retrieves where in the given line the caret is located. This is represented by an index.
     * Should the line not contain the caret, then <code>-1</code> is returned.
     * <p>
     * The line should be provided, otherwise an additional check needs to be made to get the
     * current line.
     *
     * @param line The line to scan for the caret.
     *
     * @return The caret index in the given line.
     */

    public int getCaretPositionInLine(final int line) {
        final Document doc = getDocument();
        final Element map = doc.getDefaultRootElement();
        final int caret = getCaretPosition();

        final Element elementLine = map.getElement(line);
        final int start = elementLine.getStartOffset();
        if (start <= caret && caret <= elementLine.getEndOffset()) {
            return caret - start;
        }
        return -1;
    }

    /**
     * Provides all of the input map elements to handle code completion and formatting.
     *
     * @param input  The input map of the code pane.
     * @param action The action map of the code pane.
     */

    private void setKeyMaps(final InputMap input, final ActionMap action) {

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "Compile");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "Save");

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Newline");

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, true), "Finish Open");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, true), "Finish Open Delay");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");

        action.put("Finish Open Delay", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int index = getCaretPosition();
                final String code = getText();
                final char close = code.charAt(index - 1);

                if (!lastHit.containsKey(close)) {
                    return;
                }
                final long register = lastHit.get(close);
                if (System.currentTimeMillis() - register < 1000) {
                    setText(code.substring(0, index - 1) + code.substring(index));
                    setCaretPosition(index);
                    styleDocument();
                }
            }
        });

        action.put("Finish Open", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int index = getCaretPosition();

                final String code = getText();
                final char open = code.charAt(index - 1); // Get the opening char at previous index
                final char close = getClosingCharacter(open);
                if (close == 0) {
                    return;
                }
                registerHit(close);

                setText(code.substring(0, index) + close + code.substring(index));
                setCaretPosition(index);
                styleDocument();
            }
        });
        action.put("Compile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Editor parent = (Editor) SwingUtilities.getAncestorOfClass(Editor.class, CodePane.this);
                if (parent == null) {
                    JOptionPane.showMessageDialog(null, "Failed to run code.", "Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    parent.saveAndRun();
                }
            }
        });
        action.put("Save", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Editor parent = (Editor) SwingUtilities.getAncestorOfClass(Editor.class, CodePane.this);
                if (parent == null) {
                    JOptionPane.showMessageDialog(null, "Failed to save code.", "Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    parent.save();
                }
            }
        });
        action.put("Newline", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final CodeFormatter formatter = language.getCodeFormatter();
                if (formatter == null) {
                    return;
                }

                final int line = getCaretLine();
                final int caret = getCaretPosition();
                final StringBuilder builder = new StringBuilder(getText());
                final int newCaret = formatter.newlineEntered(builder, caret, line, getCaretPositionInLine(line));

                setText(builder.toString());
                setCaretPosition(newCaret);
                styleDocument();
            }
        });
    }
}