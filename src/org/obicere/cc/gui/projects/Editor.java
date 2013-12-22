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

import org.obicere.cc.configuration.Global;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.configuration.Language;
import org.obicere.cc.executor.Executor;
import org.obicere.cc.gui.CodePane;
import org.obicere.cc.gui.GUI;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

/**
 * The main panel for editing the runner's code, displaying instructions and
 * displaying results.
 *
 * @author Obicere
 * @since 1.0
 */

public class Editor extends JPanel {

    private static final long serialVersionUID = 4203077483497169333L;
    private final CodePane codePane;
    private final ResultsTable resultsTable;
    private final JTextArea instructions;
    private final Project project;
    private static final Font CONSOLOAS_12 = new Font("Consolas", Font.PLAIN, 12);
    private final Font defaultInstructionFont;

    /**
     * Constructs a new editor based off of the project. Will load code and
     * skeleton for the runner if necessary.
     *
     * @param project The project to base this runner off of.
     */

    public Editor(final Project project) {
        super(new BorderLayout());

        this.project = project;
        this.instructions = new JTextArea();
        this.codePane = new CodePane(project.getCurrentCode(), true, Language.JAVA);
        this.resultsTable = new ResultsTable(project);
        this.defaultInstructionFont = instructions.getFont();

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
        codePane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R && e.isControlDown()) {
                    saveAndRun();
                    if (instructions.getFont().equals(CONSOLOAS_12)) {
                        setInstructionsText(project.getProperties().getDescription(), false);
                    }
                }
            }
        });
        instructions.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (instructions.getFont().equals(CONSOLOAS_12)) {
                    setInstructionsText(project.getProperties().getDescription(), false);
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
        mainSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_MAINSPLIT_DIVIDER_LOCATION)));
        textSplit.setDividerLocation(Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_TEXTSPLIT_DIVIDER_LOCATION)));
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

    public void clearSaveFiles() {
        final int n = JOptionPane.showConfirmDialog(null, "This will delete all progress on this project.\nDo you wish to continue?", "Continue?", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            if (project.getFile().exists()) {
                final File classF = new File(project.getFile().getAbsolutePath().replace(".java", ".class"));
                final File data = new File(Paths.SETTINGS + File.separator + "data.dat");
                if (project.getFile().delete() && classF.delete()) {
                    try {
                        final String words = new String(IOUtils.readData(data)).replace(String.format("|%040x|", new BigInteger(project.getName().getBytes())), "");
                        IOUtils.write(data, words.getBytes());
                        codePane.setText(project.getProperties().getSkeleton());
                        codePane.highlightKeywords();
                        return;
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "Error deleting current code!");
        }
    }

    public void saveAndRun() {
        if (codePane.getText().length() == 0 || project == null) {
            return;
        }
        if (!project.save(codePane.getText())) {
            JOptionPane.showMessageDialog(null, "Error saving current code!");
            return;
        }
        resultsTable.setResults(Executor.runAndGetResults(project));
    }

}
