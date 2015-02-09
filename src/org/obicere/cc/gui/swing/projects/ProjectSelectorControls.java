package org.obicere.cc.gui.swing.projects;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.language.Language;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

public class ProjectSelectorControls extends JPanel {

    private static final String    DEFAULT     = "Search...";
    private static final Dimension OPTION_SIZE = new Dimension(100, 35);

    private static ProjectSelectorControls instance;

    private final JCheckBox complete;
    private final JCheckBox name;
    private final JCheckBox incomplete;

    private final JComboBox<Language> languageSelector;


    public static ProjectSelectorControls getControls() {
        if (instance == null) {
            instance = new ProjectSelectorControls();
        }
        return instance;
    }

    private ProjectSelectorControls() {
        super(new BorderLayout());
        final JPanel options = new JPanel(new FlowLayout(FlowLayout.LEADING));
        final JPanel padding = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));

        final JPanel languagePadding = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        final JTextField search = new JTextField();
        final ItemListener listener = e -> search(search.getText());

        final JLabel languageLabel = new JLabel("Selected Language: ");
        languageSelector = new JComboBox<>(new Vector<>(Domain.getGlobalDomain().getLanguageManager().getSupportedLanguages()));

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

        name.addItemListener(e -> {
            search.setEnabled(name.isSelected());
            search(search.getText());
        });
        complete.addItemListener(listener);
        incomplete.addItemListener(listener);

        languagePadding.add(languageLabel);
        languagePadding.add(languageSelector);

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
        add(languagePadding, BorderLayout.EAST);
    }

    public synchronized void search(final String key) {
        String fixedKey = "";
        if (name.isSelected()) {
            fixedKey = key.replaceAll("\\s+", "");
            fixedKey = fixedKey.replace(DEFAULT, "");
        }
        final ProjectSelector projectSelector = Domain.getGlobalDomain().getFrameManager().getTab(ProjectTabPanel.class).getProjectSelector();
        projectSelector.refine(fixedKey, complete.isSelected(), name.isSelected(), incomplete.isSelected());
    }

    public Language getSelectedLanguage() {
        return (Language) languageSelector.getSelectedItem();
    }

}
