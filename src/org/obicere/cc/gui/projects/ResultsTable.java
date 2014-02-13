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

public class ResultsTable extends JTable implements TableCellRenderer {
    private static final long serialVersionUID = 5610470469686875396L;

    private boolean[] resultsCorrect;
    private final Project project;
    private static final Color CORRECT = new Color(37, 133, 0);
    private static final String[] HEADERS = new String[]{"Correct Answer", "Your Answer", "Parameters"};

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
                        final File complete = new File(Paths.DATA + File.separator + "data.dat");
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
