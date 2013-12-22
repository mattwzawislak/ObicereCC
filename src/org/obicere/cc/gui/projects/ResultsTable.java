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
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * This is always linked to a specific Java editor and project. It displays the
 * results of that runner openly with color coordination.
 *
 * @author Obicere
 * @since 1.0
 */

public class ResultsTable extends JTable implements TableCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 5610470469686875396L;

    private boolean[] resultsCorrect;
    private final Project project;
    private static final Color CORRECT = new Color(37, 133, 0);
    private static final String[] HEADERS = new String[]{"Correct Answer", "Your Answer", "Parameters"};

    /**
     * Constructs a new <tt>ResultsTable</tt> instance. This sets all
     * static information in terms of display.
     *
     * @param project The project to which this table will display results of.
     * @since 1.0
     */

    public ResultsTable(final Project project) {
        this.project = project;

        final DefaultTableModel model = new DefaultTableModel() {
            private static final long serialVersionUID = 7189238275994159770L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        setModel(model);
        setEnabled(false);
        model.setColumnIdentifiers(HEADERS);
        model.setColumnCount(3);
        model.insertRow(0, HEADERS);
        getColumnModel().getColumn(0).setMinWidth(125);
        getColumnModel().getColumn(0).setMaxWidth(125);
        getColumnModel().getColumn(1).setMinWidth(125);
        getColumnModel().getColumn(1).setMaxWidth(125);
    }

    /**
     * Sets the results of the table. This will also check whether or not the
     * result is true and change the color of the cells accordingly. Note: may
     * not work for color blind people.
     *
     * @param results The results returned from the execution of the runner.
     * @since 1.0
     */

    public void setResults(final Result[] results) {
        synchronized (getTreeLock()) {
            final DefaultTableModel m = (DefaultTableModel) getModel();
            final int rowCount = m.getRowCount();
            for(int i = 1; i < rowCount; i++){
                m.removeRow(1);
            }
            if (results != null) {
                resultsCorrect = new boolean[results.length];
                boolean wrong = false;
                for (int i = 0; i < results.length; i++) {
                    resultsCorrect[i] = results[i].isCorrect();
                    if (!wrong && !resultsCorrect[i]) {
                        wrong = true;
                    }
                    final Object[] arr = {results[i].getCorrectAnswer(), results[i].getResult(), Arrays.toString(results[i].getParameters())};
                    m.insertRow(i + 1, arr);
                }
                if (!wrong) {
                    try {
                        final File complete = new File(Paths.SETTINGS + File.separator + "data.dat");
                        final byte[] data = IOUtils.readData(complete);
                        String info = new String(data);
                        final String format = String.format("|%040x|", new BigInteger(project.getName().getBytes()));
                        if (!info.contains(format)) {
                            info = info.concat(format);
                        }
                        IOUtils.write(complete, info.getBytes());
                        project.setComplete(true);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            setModel(m);
        }
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final JLabel label = new JLabel(value == null ? "" : value.toString());
        if (row == 0) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            return label;
        }
        label.setForeground(resultsCorrect[row - 1] ? CORRECT : Color.RED);
        return label;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return this;
    }

}
