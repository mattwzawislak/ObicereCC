package org.obicere.cc.gui.swing.projects;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.Result;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.SaveProgressHook;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ResultsTable extends JTable implements TableCellRenderer {

    private static final long     serialVersionUID = 5610470469686875396L;
    private static final Color    CORRECT          = new Color(37, 133, 0);
    private static final String[] HEADERS          = new String[]{"Correct Answer", "Your Answer", "Parameters"};

    private final Project project;
    private final SaveProgressHook hook = Domain.getGlobalDomain().getHookManager().hookByClass(SaveProgressHook.class);

    private boolean[] resultsCorrect;

    public ResultsTable(final Project project) {
        this.project = project;

        final DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(final int row, final int col) {
                return false;
            }
        };

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setModel(model);
        setEnabled(false);
        model.setColumnIdentifiers(HEADERS);
        model.setColumnCount(HEADERS.length);
    }

    public void setResults(final Result[] results) {
        synchronized (getTreeLock()) {
            final DefaultTableModel m = (DefaultTableModel) getModel();
            final int rowCount = m.getRowCount();
            for (int i = 0; i < rowCount; i++) { // Remove all rows but the header
                m.removeRow(0);
            }

            if (results != null && results.length > 0) {
                resultsCorrect = new boolean[results.length];
                boolean wrong = false;
                for (int i = 0; i < results.length; i++) {
                    resultsCorrect[i] = results[i].isCorrect();
                    if (!wrong && !resultsCorrect[i]) {
                        wrong = true;
                    }
                    final Result result = results[i];
                    final String correct = stringValue(result.getExpectedResult());
                    final String answer = stringValue(result.getResult());
                    final String params = stringValue(result.getParameters());

                    final Object[] arr = {correct, answer, params};
                    m.insertRow(i, arr);
                }
                if (!wrong) {
                    hook.setComplete(project.getName(), true);
                }
            }
            setModel(m);
        }
    }

    private String stringValue(final Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            return Arrays.deepToString(convertToObjectArray(obj));
        }
        return String.valueOf(obj);
    }

    private Object[] convertToObjectArray(Object array) {
        final Class<?> ofArray = array.getClass().getComponentType();
        if (ofArray.isPrimitive()) {
            final ArrayList<Object> ar = new ArrayList<>();
            final int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                ar.add(Array.get(array, i));
            }
            return ar.toArray();
        } else {
            return (Object[]) array;
        }
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final String data = String.valueOf(value);
        final JLabel label = new JLabel(data);
        label.setToolTipText(data);
        label.setForeground(resultsCorrect[row] ? CORRECT : Color.RED);
        return label;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().getWidth() < getParent().getWidth();
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return this;
    }

}
