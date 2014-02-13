

package org.obicere.cc.gui.projects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class SearchPanel extends JPanel {

    private static final String DEFAULT = "Search...";
    private static final Dimension OPTION_SIZE = new Dimension(100, 35);

    private final JCheckBox complete;
    private final JCheckBox name;
    private final JCheckBox incomplete;



    public SearchPanel() {
        super(new BorderLayout());
        final JPanel options = new JPanel(new FlowLayout(FlowLayout.LEADING));
        final JPanel padding = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        final JTextField search = new JTextField();
        final ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                search(search.getText());
            }
        };

        complete = new JCheckBox("Complete");
        incomplete = new JCheckBox("Incomplete");
        name = new JCheckBox("Name");

        complete.setPreferredSize(OPTION_SIZE);
        incomplete.setPreferredSize(OPTION_SIZE);
        name.setPreferredSize(OPTION_SIZE);

        options.add(complete);
        options.add(incomplete);
        options.add(name);

        complete.setSelected(true);
        incomplete.setSelected(true);
        name.setSelected(true);

        name.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                search.setEnabled(name.isSelected());
                search(search.getText());
            }
        });
        complete.addItemListener(listener);
        incomplete.addItemListener(listener);

        padding.add(search);
        search.setPreferredSize(new Dimension(310, 20));
        search.setText(DEFAULT);
        search.setForeground(Color.DARK_GRAY);
        search.setFont(search.getFont().deriveFont(Font.ITALIC));
        search.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (search.getText().equals(DEFAULT)) {
                    search.setText("");
                    search.setForeground(Color.BLACK);
                    search.setFont(search.getFont().deriveFont(Font.PLAIN));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (search.getText().trim().isEmpty()) {
                    search.setText(DEFAULT);
                    search.setForeground(Color.DARK_GRAY);
                    search.setFont(search.getFont().deriveFont(Font.ITALIC));
                }
            }
        });
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                search(search.getText());
            }
        });
        add(padding, BorderLayout.CENTER);
        add(options, BorderLayout.SOUTH);
    }



    public synchronized void search(final String key) {
        String fixedKey = "";
        if (name.isSelected()) {
            fixedKey = key.replaceAll("\\s+", "");
            fixedKey = fixedKey.replace(DEFAULT, "");
        }
        final ProjectSelector projectSelector = ProjectTabPanel.getInstance().getProjectSelector();
        projectSelector.refine(fixedKey, complete.isSelected(), name.isSelected(), incomplete.isSelected());
    }

}
