package org.obicere.cc.gui.projects;

import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.shutdown.LayoutHook;
import org.obicere.cc.shutdown.SaveProgressHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.projects.Project;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.TimerTask;
import java.util.Timer;

public class Editor extends JPanel {

    private static final Font CONSOLOAS_12 = new Font("Consolas", Font.PLAIN, 12);

    private final CodePane     codePane;
    private final ResultsTable resultsTable;
    private final JTextArea    instructions;
    private final JPanel       instructionButtons;
    private final Project      project;
    private final Font         defaultInstructionFont;
    private final Language     language;
    private final SaveProgressHook hook = ShutDownHookManager.hookByClass(SaveProgressHook.class);

    public Editor(final Project project, final Language language) {
        super(new BorderLayout());

        this.project = project;
        this.instructions = new JTextArea();
        this.codePane = new CodePane(project.getCurrentCode(language), language);
        this.resultsTable = new ResultsTable(project);
        this.instructionButtons = new JPanel(new WrapLayout(WrapLayout.LEFT));
        this.defaultInstructionFont = instructions.getFont();
        this.language = language;

        final LayoutHook hook = ShutDownHookManager.hookByClass(LayoutHook.class);

        final JButton reset = new JButton("Reset");
        final JButton run = new JButton("Run");
        final JButton save = new JButton("Save");

        final JButton clearError = new JButton("Clear");
        final JButton copy = new JButton("Copy");
        final JPanel rightSide = new JPanel(new BorderLayout());
        final JPanel buttons = new JPanel();
        final JPanel instructionPanel = new JPanel();

        final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsTable, rightSide);
        final JSplitPane textSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, instructionPanel, mainSplit);

        run.setToolTipText("Runs the project. (Ctrl+R)");
        run.addActionListener(e -> saveAndRun());

        save.addActionListener(e -> save());

        codePane.requestFocus();

        reset.addActionListener(e -> clearSaveFiles());

        textSplit.setDividerLocation(hook.getPropertyAsInt(LayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION));
        textSplit.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("dividerLocation")) {
                if (hook.getPropertyAsBoolean(LayoutHook.SAVE_LAYOUT)) {
                    hook.setProperty(LayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION, evt.getNewValue());
                }
            }
        });

        mainSplit.setDividerLocation(hook.getPropertyAsInt(LayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION));
        mainSplit.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("dividerLocation")) {
                if (hook.getPropertyAsBoolean(LayoutHook.SAVE_LAYOUT)) {
                    hook.setProperty(LayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION, evt.getNewValue());
                }
            }
        });

        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(reset);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(run);
        buttons.add(save);

        rightSide.add(new JScrollPane(codePane), BorderLayout.CENTER);
        rightSide.add(buttons, BorderLayout.SOUTH);

        instructions.setEditable(false);
        instructions.setBackground(new Color(0xededed));
        instructions.setFont(CONSOLOAS_12);
        instructions.setToolTipText("Instructions and any errors will appear here.");

        clearError.addActionListener(e -> setInstructionsText(project.getDescription(), false));

        copy.addActionListener(e -> {
            final StringSelection selection = new StringSelection(instructions.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });

        instructionButtons.add(clearError);
        instructionButtons.add(copy);

        instructionPanel.setLayout(new BorderLayout());
        instructionPanel.add(new JScrollPane(instructions), BorderLayout.CENTER);
        instructionPanel.add(instructionButtons, BorderLayout.SOUTH);

        add(textSplit, BorderLayout.CENTER);

        codePane.highlightKeywords();
    }

    public String getProjectName() {
        return project.getName();
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
            final String name = project.getName();
            if (deleteSource || deleteCompiled) {
                if (hook.isComplete(name)) {
                    codePane.setText(language.getSkeleton(project));
                    codePane.highlightKeywords();
                    hook.setComplete(name, false);
                }
                return;
            }
            JOptionPane.showMessageDialog(null, "Error deleting current code!");
        }

    }

    public void saveAndRun() {
        save();
        if (codePane.getText().length() == 0) {
            return;
        }
        final Thread thread = new Thread(() -> {
            final Result[] results = language.compileAndRun(project);
            resultsTable.setResults(results);
        });
        thread.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    setInstructionsText(language.getName() + ": program execution timed out after 10 seconds.", true);
                }
            }
        }, 10000);
    }

    public void save() {
        if (!project.save(codePane.getText(), language)) {
            JOptionPane.showMessageDialog(null, "Error saving current code!");
        }
    }
}
