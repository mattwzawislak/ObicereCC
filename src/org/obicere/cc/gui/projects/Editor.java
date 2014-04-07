package org.obicere.cc.gui.projects;

import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.CodePane;
import org.obicere.cc.shutdown.SaveLayoutHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class Editor extends JPanel {

    private static final Font CONSOLOAS_12 = new Font("Consolas", Font.PLAIN, 12);
    private static final long serialVersionUID = 4203077483497169333L;

    private final CodePane codePane;
    private final ResultsTable resultsTable;
    private final JTextArea instructions;
    private final JPanel instructionButtons;
    private final Project project;
    private final Font defaultInstructionFont;
    private final Language language;

    public Editor(final Project project, final Language language) {
        super(new BorderLayout());

        this.project = project;
        this.instructions = new JTextArea();
        this.codePane = new CodePane(project.getCurrentCode(language), language);
        this.resultsTable = new ResultsTable(project);
        this.instructionButtons = new JPanel(new WrapLayout(WrapLayout.LEFT));
        this.defaultInstructionFont = instructions.getFont();
        this.language = language;

        final SaveLayoutHook hook = ShutDownHookManager.hookByName(SaveLayoutHook.class, SaveLayoutHook.NAME);
        final JButton run = new JButton("Run");
        final JButton clear = new JButton("Clear Project");
        final JButton clearError = new JButton("Clear");
        final JButton copy = new JButton("Copy");
        final JPanel rightSide = new JPanel(new BorderLayout());
        final JPanel buttons = new JPanel();
        final JPanel instructionPanel = new JPanel();
        final JScrollPane instructionScroll = new JScrollPane(instructions);

        final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsTable, rightSide);
        final JSplitPane textSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, instructionPanel, mainSplit);

        run.setHorizontalTextPosition(SwingConstants.CENTER);
        run.setPreferredSize(new Dimension(200, run.getPreferredSize().height));
        run.setToolTipText("Runs the project. (Ctrl+R)");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndRun();
            }
        });

        codePane.requestFocus();

        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSaveFiles();
            }
        });

        textSplit.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("dividerLocation")) {
                    hook.saveProperty(SaveLayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION, evt.getNewValue());
                }
            }
        });

        mainSplit.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("dividerLocation")) {
                    hook.saveProperty(SaveLayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION, evt.getNewValue());
                }
            }
        });

        buttons.add(clear);
        buttons.add(run);

        rightSide.add(new JScrollPane(codePane), BorderLayout.CENTER);
        rightSide.add(buttons, BorderLayout.SOUTH);

        instructions.setEditable(false);
        instructions.setBackground(new Color(0xededed));
        instructions.setFont(CONSOLOAS_12);
        instructions.setToolTipText("Instructions and any errors will appear here.");

        clearError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setInstructionsText(project.getDescription(), false);
            }
        });
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final StringSelection selection = new StringSelection(instructions.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });

        instructionButtons.add(clearError);
        instructionButtons.add(copy);

        instructionPanel.setLayout(new BorderLayout());
        instructionPanel.add(new JScrollPane(instructions), BorderLayout.CENTER);
        instructionPanel.add(instructionButtons, BorderLayout.SOUTH);

        add(textSplit, BorderLayout.CENTER);
        setName(project.getName());

        codePane.highlightKeywords();
        mainSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION, "300")));
        textSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION, "100")));
    }

    public void setInstructionsText(final String string, final boolean error) {
        if (error) {
            instructions.setFont(CONSOLOAS_12);
            instructionButtons.setVisible(true);
        } else {
            instructions.setFont(defaultInstructionFont);
            instructionButtons.setVisible(false);
        }
        instructionButtons.revalidate();
        instructions.setText(string);
    }

    public Language getLanguage() {
        return language;
    }

    public void clearSaveFiles() {
        final int n = JOptionPane.showConfirmDialog(null, "This will delete all progress on this project.\nDo you wish to continue?", "Continue?", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            final File sourceFile = new File(project.getFileName(language) + language.getSourceExtension());
            final File compiledFile = new File(project.getFileName(language) + language.getCompiledExtension());
            final boolean deleteSource = sourceFile.exists() && sourceFile.delete();
            final boolean deleteCompiled = compiledFile.exists() && compiledFile.delete();
            if (deleteSource || deleteCompiled) {
                if (project.isComplete()) {
                    codePane.setText(language.getSkeleton(project));
                    codePane.highlightKeywords();
                    project.setComplete(false);
                }
                return;
            }
            JOptionPane.showMessageDialog(null, "Error deleting current code!");
        }

    }

    public void saveAndRun() {
        if (codePane.getText().length() == 0 || project == null) {
            return;
        }
        if (!project.save(codePane.getText(), language)) {
            JOptionPane.showMessageDialog(null, "Error saving current code!");
            return;
        }
        final Result[] results = language.compileAndRun(project);
        resultsTable.setResults(results);
    }
}
