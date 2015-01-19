package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.LayoutHook;
import org.obicere.cc.shutdown.SaveProgressHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used to contain all the project-dependent information about the current file. This will contain
 * the instruction information for how to solve the current problem. It will show the results table
 * for visualizing the cases and what answer was generated. There is also the main {@link
 * org.obicere.cc.gui.projects.CodePane} for editing the code.
 * <p>
 * The instructions pane will contain the basic information on how to solve the problem. This is
 * stored in the {@link org.obicere.cc.projects.Runner} under the {@link
 * org.obicere.cc.projects.RunnerManifest#description()} field. Errors during runtime will also be
 * displayed here. This is mainly to conserve space, especially since errors shouldn't happen too
 * often, right? In the case an error appears, then buttons for copying the error and removing the
 * error will become available.
 * <p>
 * The results table will show your progress towards solving the problem. Currently, all of the
 * results will be displayed. Future releases may wish to only show the top 10-15 to avoid users
 * hard-coding the answers. Once every result in the table is considered correct, it is seen that
 * the project has been completed and the progress will be saved.
 * <p>
 * The code pane is the basic editor. This is where the user will attempt to solve the problem. The
 * results table and the instructions panel merely help push the user to the right answer. It is
 * important to note that one can reset the project completely if one gets lost, since not every
 * project will explicitly mention what method needs to be present.
 *
 * @author Obicere
 * @version 1.0
 */

public class Editor extends JPanel {

    private static final Font CONSOLAS_12 = new Font("Consolas", Font.PLAIN, 12);

    private final CodePane     codePane;
    private final ResultsTable resultsTable;
    private final JTextArea    instructions;
    private final JPanel       instructionButtons;
    private final Project      project;
    private final Font         defaultInstructionFont;
    private final Language     language;
    private final SaveProgressHook hook = Domain.getGlobalDomain().getHookManager().hookByClass(SaveProgressHook.class);

    /**
     * Constructs a new editor panel for the given project under the specific language. This will
     * load all the resources needed to start the project if not started already; or continue the
     * project from where it was last left off.
     *
     * @param project  The project to load.
     * @param language The language to run the project in.
     */

    public Editor(final Project project, final Language language) {
        super(new BorderLayout());

        this.project = project;
        this.instructions = new JTextArea();
        this.codePane = new CodePane(project.getCurrentCode(language), language);
        this.resultsTable = new ResultsTable(project);
        this.instructionButtons = new JPanel(new WrapLayout(WrapLayout.LEFT));
        this.defaultInstructionFont = instructions.getFont();
        this.language = language;

        final LayoutHook hook = Domain.getGlobalDomain().getHookManager().hookByClass(LayoutHook.class);

        final JButton reset = new JButton("Reset");
        final JButton run = new JButton("Run");
        final JButton save = new JButton("Save");

        final JButton clearError = new JButton("Clear");
        final JButton copy = new JButton("Copy");
        final JPanel rightSide = new JPanel(new BorderLayout());
        final JPanel buttons = new JPanel();
        final JPanel instructionPanel = new JPanel();

        final JScrollPane resultsScrollPane = new JScrollPane(resultsTable);

        final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScrollPane, rightSide);
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
        instructions.setFont(CONSOLAS_12);
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
    }

    /**
     * Retrieves the project name - used to name the tab within the main GUI.
     *
     * @return The project name.
     */

    public String getProjectName() {
        return project.getName();
    }

    /**
     * The language that the current project is working under.
     *
     * @return The language for the current project.
     */

    public Language getLanguage() {
        return language;
    }

    /**
     * Updates the instructions text to display a specific document. In the case that the document
     * should be presented as an error, the appropriate monospaced font will be set. This will
     * generally help when error span multiple lines with ascii styling.
     *
     * @param string The document to display.
     * @param error  Whether or not to treat the document as an error.
     */

    public void setInstructionsText(final String string, final boolean error) {
        if (error) {
            instructions.setFont(CONSOLAS_12);
            instructionButtons.setVisible(true);
        } else {
            instructions.setFont(defaultInstructionFont);
            instructionButtons.setVisible(false);
        }
        instructionButtons.revalidate();
        instructions.setText(string);
    }

    /**
     * Attempts to remove all source and compiled files of the project. If the files do not exist,
     * an error may be thrown saying they failed to be deleted. This can be ignored in most cases.
     * The only case where this technically should fail is if the file is open in another program.
     * In which case, deletion may not happen completely.
     * <p>
     * Initially a dialog will be displayed to confirm the user's actions. In the case that the user
     * rejects, then no files will be deleted. If the files are confirmed to be deleted, then the
     * code pane will be reset with the default skeleton generated from the language for the
     * project. Also, if the project was previously listed as complete, it will no longer be
     * complete.
     * <p>
     * A {@link javax.swing.JOptionPane} will be displayed to notify the user of any errors.
     *
     * @see org.obicere.cc.executor.language.Language#getSkeleton(org.obicere.cc.projects.Project)
     */

    public void clearSaveFiles() {
        final String message = "This will delete all progress on this project.\nDo you wish to continue?";
        final int n = JOptionPane.showConfirmDialog(null, message, "Continue?", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            final File sourceFile = new File(project.getFileName(language) + language.getSourceExtension());
            final File compiledFile = new File(project.getFileName(language) + language.getCompiledExtension());
            final boolean deleteSource = sourceFile.exists() && sourceFile.delete();
            final boolean deleteCompiled = compiledFile.exists() && compiledFile.delete();
            final String name = project.getName();
            if (deleteSource || deleteCompiled) {
                if (hook.isComplete(name)) {
                    hook.setComplete(name, false);
                }
                codePane.setText(language.getSkeleton(project));
                codePane.styleDocument();
                return;
            }
            JOptionPane.showMessageDialog(null, "Code files either did not exist or failed to be deleted.");
        }

    }

    /**
     * Attempts to save and then run the project. There is a hard cap of 10 seconds for this to
     * finish. Should it not finish, then the operation will be interrupted. This interruption can
     * be ignored if the file size is substantially large. This is of course to avoid incomplete
     * file saves.
     * <p>
     * During the actual invocation of the runner: should this interrupt be called, the program
     * should comply and cancel. There is a fail-safe to handle results that were failed to be
     * completed in time.
     *
     * @see org.obicere.cc.executor.Result#newTimedOutResult(org.obicere.cc.executor.Case)
     * @see #save()
     */

    public void saveAndRun() {
        if (codePane.getText().length() == 0) {
            return;
        }
        final Thread thread = new Thread(() -> {
            save();
            final Result[] results = language.compileAndRun(project);
            resultsTable.setResults(results);
        });
        thread.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    final String message = language.getName() + ": program execution timed out after 10 seconds.";
                    SwingUtilities.invokeLater(() -> setInstructionsText(message, true));
                }
            }
        }, 10000);
    }

    /**
     * Attempts to save the file. This can happen independently and since we don't want to deal with
     * interrupts on the thread, this will not be locked into such - unless needed.
     * <p>
     * Should this fail to save properly, a {@link javax.swing.JOptionPane} will be displayed to
     * notify the user of the error. The actual result of why it failed will not be displayed. The
     * user can however navigate to the saving directory and check to see what complications may be
     * present from there.
     */

    public void save() {
        if (!project.save(codePane.getText(), language)) {
            JOptionPane.showMessageDialog(null, "Error saving current code!");
        }
    }
}
