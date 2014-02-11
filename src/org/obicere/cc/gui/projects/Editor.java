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

package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.CodePane;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.shutdown.SaveLayoutHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 * The main panel for editing the runner's code, displaying instructions and
 * displaying results.
 *
 * @author Obicere
 * @since 1.0
 */

public class Editor extends JPanel {

    private static final Font CONSOLOAS_12 = new Font("Consolas", Font.PLAIN, 12);
    private static final long serialVersionUID = 4203077483497169333L;

    private final CodePane codePane;
    private final ResultsTable resultsTable;
    private final JTextArea instructions;
    private final Project project;
    private final Font defaultInstructionFont;
    private final Language language;

    /**
     * Constructs a new editor based off of the project. Will load code and
     * skeleton for the runner if necessary.
     *
     * @param project The project to base this runner off of.
     */

    public Editor(final Project project, final Language language) {
        super(new BorderLayout());

        this.project = project;
        this.instructions = new JTextArea();
        this.codePane = new CodePane(project.getCurrentCode(language), language);
        this.resultsTable = new ResultsTable(project);
        this.defaultInstructionFont = instructions.getFont();
        this.language = language;

        final SaveLayoutHook hook = ShutDownHookManager.hookByName(SaveLayoutHook.class, SaveLayoutHook.NAME);
        final JButton run = new JButton("Run");
        final JButton clear = new JButton("Clear Project");
        final JPanel rightSide = new JPanel(new BorderLayout());
        final JPanel buttons = new JPanel();

        final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsTable, rightSide);
        final JSplitPane textSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(instructions), mainSplit);

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

        instructions.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (instructions.getFont().equals(CONSOLOAS_12)) {
                    setInstructionsText(project.getDescription(), false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

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

        add(textSplit, BorderLayout.CENTER);
        setName(project.getName());

        codePane.highlightKeywords();
        mainSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION, "300")));
        textSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION, "100")));
    }

    /**
     * Used to change the instruction panel's text. Most commonly, this is done
     * for error representation.
     *
     * @param string The <tt>String</tt> you would like to set the text to.
     * @see {@link Editor#append(String)}
     * @since 1.0
     */

    public void setInstructionsText(final String string, final boolean error) {
        if (error) {
            instructions.setFont(CONSOLOAS_12);
        } else {
            instructions.setFont(defaultInstructionFont);
        }
        instructions.setText(string);
    }

    /**
     * Appends a String to the instructions pane. This will be mostly used for
     * direct error sourcing.
     *
     * @param string The <tt>String</tt> you would like to append to the current
     *               text.
     * @since 1.0
     */

    public void append(String string) {
        instructions.append(string);
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
