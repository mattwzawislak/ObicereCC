package org.obicere.cc.gui.projects;

import org.obicere.cc.executor.language.Language;
import org.obicere.cc.shutdown.CodeCompletionHook;
import org.obicere.cc.shutdown.EditorHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodePane extends JTextPane {

    private static final String             MASTER_SPLIT = "([(\\[\\]);\\{}\0.])";
    private static final SimpleAttributeSet KEYWORD_SET  = new SimpleAttributeSet();
    private static final SimpleAttributeSet NORMAL_SET   = new SimpleAttributeSet();
    private static final SimpleAttributeSet STRING_SET   = new SimpleAttributeSet();
    private static Font editorFont;

    private static final EditorHook         EDITOR     = ShutDownHookManager.hookByClass(EditorHook.class);
    private static final CodeCompletionHook COMPLETION = ShutDownHookManager.hookByClass(CodeCompletionHook.class);

    static {
        StyleConstants.setForeground(STRING_SET, EDITOR.getPropertyAsColor(EditorHook.STRING_HIGHLIGHT_COLOR));
        StyleConstants.setForeground(KEYWORD_SET, EDITOR.getPropertyAsColor(EditorHook.KEYWORD_HIGHLIGHT_COLOR));
        StyleConstants.setForeground(NORMAL_SET, EDITOR.getPropertyAsColor(EditorHook.NORMAL_HIGHLIGHT_COLOR));

        editorFont = new Font(EDITOR.getPropertyAsString(EditorHook.EDITOR_FONT_TYPE), Font.PLAIN, EDITOR.getPropertyAsInt(EditorHook.EDITOR_FONT_SIZE));
    }

    private HashMap<Character, Long> lastHit = new HashMap<>();

    private long lastUpdate;

    private final Language language;

    public CodePane(final String content, final Language language) {
        this.language = language;

        final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap actionMap = getActionMap();
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        final TabStop[] tabs = new TabStop[50];
        final FontMetrics metrics = getFontMetrics(editorFont);
        final int width = metrics.stringWidth("    ");
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabStop(width * i);
        }
        final TabSet tabSet = new TabSet(tabs);
        final AttributeSet paraSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        setContentType("java");
        setFont(editorFont);
        setParagraphAttributes(paraSet, false);
        setText(content);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Newline");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "Compile");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, true), "Finish Open");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, true), "Finish Open Delay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_DOWN_MASK, true), "Finish Open Delay");

        actionMap.put("Finish Open Delay", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int index = getCaretPosition();
                final String code = getText();
                final char open = code.charAt(index - 1);

                if (!lastHit.containsKey(open)) {
                    return;
                }
                final long lastHit = CodePane.this.lastHit.get(open);
                if (lastHit < 1500) {
                    setText(code.substring(0, index - 1) + code.substring(index));
                    setCaretPosition(index + 1);
                    highlightKeywords();
                }
            }
        });

        actionMap.put("Finish Open", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int index = getCaretPosition();

                final String code = getText();
                final char open = code.charAt(index - 1);
                final char close = getClosingCharacter(open);
                if (close == 0) {
                    return;
                }
                registerHit(open);

                setText(code.substring(0, index) + close + code.substring(index));
                setCaretPosition(index);
                highlightKeywords();
            }
        });
        actionMap.put("Compile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Editor parent = (Editor) SwingUtilities.getAncestorOfClass(Editor.class, CodePane.this);
                if (parent == null) {
                    JOptionPane.showMessageDialog(null, "Failed to save code!", "Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    parent.saveAndRun();
                }
            }
        });
        actionMap.put("Newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.NEW_LINE_COMPLETION)) {
                    return;
                }
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
                if (index != 0 && line.matches(".*?[\\)\\{]\\s*")) {
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
        addCaretListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                final JTextComponent component = (JTextComponent) e.getSource();
                final int position = component.getCaretPosition();
                final Rectangle r = component.modelToView(position);
                r.x += 2;
                component.scrollRectToVisible(r);
                highlightKeywords(); // Whenever a new character gets added, caret updates.
            } catch (Exception ignored) {
            }
        }));
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
            case '<':
                if (!COMPLETION.getPropertyAsBoolean(CodeCompletionHook.TAG_COMPLETION)) {
                    return 0;
                }
                return '>';
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

    private String clearMatches(final String code, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(code);
        final StringBuilder builder = new StringBuilder(code);
        while (matcher.find()) { // For each match
            final String key = matcher.group();
            int start = builder.indexOf(key);
            final int length = key.length();
            while (start > -1) { // For each instance
                final int end = start + key.length();
                final int nextSearchStart = start + length;
                builder.replace(start, end, CharBuffer.allocate(length).toString());
                start = builder.indexOf(key, nextSearchStart);
                // replace the instance
            }
        }
        return builder.toString();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    private void checkForUpdates() {
        final long editorUpdate = EDITOR.getLastEditorUpdate();
        if (lastUpdate != editorUpdate) {
            StyleConstants.setForeground(STRING_SET, EDITOR.getPropertyAsColor(EditorHook.STRING_HIGHLIGHT_COLOR));
            StyleConstants.setForeground(KEYWORD_SET, EDITOR.getPropertyAsColor(EditorHook.KEYWORD_HIGHLIGHT_COLOR));
            StyleConstants.setForeground(NORMAL_SET, EDITOR.getPropertyAsColor(EditorHook.NORMAL_HIGHLIGHT_COLOR));

            editorFont = new Font(EDITOR.getPropertyAsString(EditorHook.EDITOR_FONT_TYPE), Font.PLAIN, EDITOR.getPropertyAsInt(EditorHook.EDITOR_FONT_SIZE));
            lastUpdate = editorUpdate;
            setFont(editorFont);
        }
    }

    public void highlightKeywords() {
        checkForUpdates(); // Be sure to get the latest changes.
        String code = getText();
        for (final String literal : language.getLiteralMatches()) {
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
                style.setCharacterAttributes(i, word.length(), STRING_SET, true);
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