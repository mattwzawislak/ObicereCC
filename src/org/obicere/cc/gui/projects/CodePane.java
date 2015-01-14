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

public class CodePane extends JTextPane {

    private static final SimpleAttributeSet KEYWORD_SET   = new SimpleAttributeSet();
    private static final SimpleAttributeSet PLAINTEXT_SET = new SimpleAttributeSet();
    private static final SimpleAttributeSet LITERAL_SET   = new SimpleAttributeSet();

    private static final int    FONT_SIZE;
    private static final String FONT_FAMILY_NAME;

    private static final EditorHook         EDITOR     = Domain.getGlobalDomain().getHookManager().hookByClass(EditorHook.class);
    private static final CodeCompletionHook COMPLETION = Domain.getGlobalDomain().getHookManager().hookByClass(CodeCompletionHook.class);

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

    public CodePane(final String content, final Language language) {
        this.language = language;

        final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap actionMap = getActionMap();

        setContentType("java");
        setText(content);
        setTabStops(FONT_FAMILY_NAME, FONT_SIZE);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "Compile");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "Save");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Newline");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, true), "Finish Open");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, true), "Finish Open Delay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");

        actionMap.put("Finish Open Delay", new AbstractAction() {
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

        actionMap.put("Finish Open", new AbstractAction() {
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
        actionMap.put("Compile", new AbstractAction() {
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
        actionMap.put("Save", new AbstractAction() {
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
        actionMap.put("Newline", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String curCode = getText();
                //for (final String str : language.getLiteralMatches()) {
                //    curCode = clearMatches(curCode, str);
                //}
                final int line = getCaretLine();
                final int index = getCaretPosition();
                final StringBuilder builder = new StringBuilder(getText());
                final CodeFormatter formatter = language.getCodeFormatter();
                final int newCaret = formatter.newlineEntered(curCode, builder, index, line, getCaretPositionInLine(line));

                setText(builder.toString());
                setCaretPosition(newCaret);
                styleDocument();
            }
        });
        addCaretListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                final JTextComponent component = (JTextComponent) e.getSource();
                final int position = component.getCaretPosition();
                final Rectangle r = component.modelToView(position);
                r.x += 2;
                component.scrollRectToVisible(r);
                styleDocument(); // Whenever a new character gets added, caret updates.
            } catch (Exception ignored) {
            }
        }));

    }

    @Override
    public void addNotify() {
        super.addNotify();
        final Container con = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (con != null) {
            ((JScrollPane) con).getViewport().addChangeListener(e -> styleDocument());
        }
    }

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
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.BRACKET_COMPLETION)) {
                    return 0;
                }
                return ']';
            default:
                return 0;
        }
    }

    private void registerHit(final char c) {
        lastHit.put(c, System.currentTimeMillis());
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

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
                case OPERATOR:
                    set = KEYWORD_SET;
                    break;
                default:
                    set = PLAINTEXT_SET;
                    break;

            }
            style.setCharacterAttributes(bound.getStart(), bound.getDelta(), set, true);
        }
    }

    public int getCaretLine() {
        final Document doc = getDocument();
        final Element map = doc.getDefaultRootElement();
        return map.getElementIndex(getCaretPosition());
    }

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
}