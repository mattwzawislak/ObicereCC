package org.obicere.cc.gui.projects;

import org.obicere.cc.executor.Result;
import org.obicere.cc.shutdown.SaveProgressHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.projects.Project;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ResultsTable extends JTable implements TableCellRenderer {

    private static final long     serialVersionUID = 5610470469686875396L;
    private static final Color    CORRECT          = new Color(37, 133, 0);
    private static final String[] HEADERS          = new String[]{"Correct Answer", "Your Answer", "Parameters"};

    private final Project project;
    private final SaveProgressHook hook = ShutDownHookManager.hookByClass(SaveProgressHook.class);

    private boolean[] resultsCorrect;

    public ResultsTable(final Project project) {
        this.project = project;

        final DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(final int row, final int col) {
                return false;
            }
        };

        setModel(model);
        setEnabled(false);
        model.setColumnIdentifiers(HEADERS);
        model.setColumnCount(HEADERS.length);
        model.insertRow(0, HEADERS);

        final TableColumnModel columns = getColumnModel();
        fixSize(columns, 0, 125);
        fixSize(columns, 1, 125);
    }

    private void fixSize(final TableColumnModel model, final int column, final int size){
        model.getColumn(column).setMinWidth(size);
        model.getColumn(column).setMaxWidth(size);
    }

    public void setResults(final Result[] results) {
        synchronized (getTreeLock()) {
            final DefaultTableModel m = (DefaultTableModel) getModel();
            final int rowCount = m.getRowCount();
            for (int i = 1; i < rowCount; i++) { // Remove all rows but the header
                m.removeRow(1);
            }
            if (results != null && results.length > 0) {
                resultsCorrect = new boolean[results.length];
                boolean wrong = false;
                for (int i = 0; i < results.length; i++) {
                    resultsCorrect[i] = results[i].isCorrect();
                    if (!wrong && !resultsCorrect[i]) {
                        wrong = true;
                    }
                    final Object[] arr = {stringValue(results[i].getCorrectAnswer()), stringValue(results[i].getResult()), stringValue(results[i].getParameters())};
                    m.insertRow(i + 1, arr); // +1 for header offset
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
        if (row == 0) { // Headers
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            return label;
        }
        label.setToolTipText(data);
        label.setForeground(resultsCorrect[row - 1] ? CORRECT : Color.RED);
        return label;
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return this;
    }

}
