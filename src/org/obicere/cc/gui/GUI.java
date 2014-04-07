package org.obicere.cc.gui;

import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.gui.projects.ProjectTabPanel;
import org.obicere.cc.gui.settings.SettingsPanel;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.SaveLayoutHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

public class GUI {

    public static final LinkedList<WindowListener> WINDOW_CLOSING_HOOKS = new LinkedList<>();
    private static JTabbedPane tabs;
    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    public static void buildGUI() {
        final JFrame frame = new JFrame("Obicere Computing Challenges v" + Updater.clientVersion());
        final JPanel main = new JPanel(new BorderLayout());
        final SaveLayoutHook hook = ShutDownHookManager.hookByName(SaveLayoutHook.class, SaveLayoutHook.NAME);
        tabs = new JTabbedPane(SwingConstants.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        final JPanel[] defaultTabs = new JPanel[]{
                ProjectTabPanel.getInstance(),
                SettingsPanel.getInstance()
        };
        for (int i = 0; i < defaultTabs.length; i++) {
            final JPanel mainPane = new JPanel();
            final JPanel tabFill = new JPanel();
            final JLabel label = new JLabel(defaultTabs[i].getName(), JLabel.CENTER);

            tabFill.setLayout(new BorderLayout());
            tabFill.setPreferredSize(TAB_SIZE);
            tabFill.add(label, SwingConstants.CENTER);
            tabFill.setOpaque(false);

            mainPane.setOpaque(false);
            mainPane.add(tabFill);

            tabs.add(defaultTabs[i]);
            tabs.setTabComponentAt(i, mainPane);
        }

        main.add(tabs);
        main.setPreferredSize(new Dimension(1000, 600));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                hook.setProperty(SaveLayoutHook.PROPERTY_FRAME_WIDTH, frame.getWidth());
                hook.setProperty(SaveLayoutHook.PROPERTY_FRAME_HEIGHT, frame.getHeight());
                hook.setProperty(SaveLayoutHook.PROPERTY_FRAME_STATE, frame.getExtendedState());
            }
        });

        WINDOW_CLOSING_HOOKS.forEach(frame::addWindowListener);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(main);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(900, 600));
        final int state = hook.getPropertyAsInt(SaveLayoutHook.PROPERTY_FRAME_STATE);
        frame.setExtendedState(state);
        if (state != JFrame.MAXIMIZED_BOTH) {
            final int width = hook.getPropertyAsInt(SaveLayoutHook.PROPERTY_FRAME_WIDTH);
            final int height = hook.getPropertyAsInt(SaveLayoutHook.PROPERTY_FRAME_HEIGHT);
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
        }
        Splash.setStatus("Complete");
    }

    public synchronized static void openProject(final Project project, final Language language) {
        if (project == null) {
            return;
        }
        final Editor editor = new Editor(project, language);
        editor.setInstructionsText(project.getDescription(), false);
        if (tabByName(project.getName(), language) == null) {
            final int index = tabs.getTabCount();
            tabs.add(editor, index);
            tabs.setTabComponentAt(index, new TabPane(project, language));
        }
        tabs.setSelectedComponent(tabByName(project.getName(), language));
    }

    public synchronized static Editor tabByName(final String name, final Language language) {
        for (final Component c : tabs.getComponents()) {
            if (c instanceof Editor) {
                final Editor c1 = (Editor) c;
                if (name.equals(c1.getName()) && language.equals(c1.getLanguage())) {
                    return c1;
                }
            }
        }
        return null;
    }

    public synchronized static void removeTab(final String name, final Language language) {
        final Editor cur = tabByName(name, language);
        if (cur != null) {
            tabs.remove(cur);
            return;
        }
        System.err.println("Failed to close tab " + name);
    }

}