package org.obicere.cc.gui.swing;

import org.obicere.cc.configuration.Configuration;
import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.AbstractFrameManager;
import org.obicere.cc.gui.swing.projects.Editor;
import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.util.Reflection;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.LayoutHook;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.logging.Level;

public class SwingFrameManager extends AbstractFrameManager {

    private final LinkedList<WindowListener> windowClosingHooks = new LinkedList<>();

    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    private final LinkedList<JPanel> mainTabs = new LinkedList<>();

    private JFrame frame;

    private JTabbedPane tabs;

    public SwingFrameManager(final Domain access) {
        super(access);
    }

    @Override
    public Window getWindow() {
        return frame;
    }

    @Override
    public void buildGUI() {
        frame = new JFrame("Obicere Computing Challenges v" + getAccess().getClientVersion());

        final JPanel main = new JPanel(new BorderLayout());
        final LayoutHook hook = access.getHookManager().hookByClass(LayoutHook.class);
        tabs = new JTabbedPane(SwingConstants.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Reflection.hasAnnotation(MainTabPanel.class).forEach(e -> {
            final MainTabPanel manifest = e.getAnnotation(MainTabPanel.class);

            final JPanel mainPane = new JPanel();
            final JPanel tabFill = new JPanel();
            final JLabel label = new JLabel(manifest.name(), JLabel.CENTER);

            tabFill.setLayout(new BorderLayout());
            tabFill.setPreferredSize(TAB_SIZE);
            tabFill.add(label, SwingConstants.CENTER);
            tabFill.setOpaque(false);

            mainPane.setOpaque(false);
            mainPane.add(tabFill);

            final JPanel tab = (JPanel) Reflection.newInstance(e);
            mainTabs.add(tab);
            tabs.add(tab);
            tabs.setTabComponentAt(manifest.index(), mainPane);
        });

        main.add(tabs);
        main.setPreferredSize(new Dimension(1000, 600));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (hook.getPropertyAsBoolean(LayoutHook.SAVE_LAYOUT)) {
                    hook.setProperty(LayoutHook.PROPERTY_FRAME_WIDTH, frame.getWidth());
                    hook.setProperty(LayoutHook.PROPERTY_FRAME_HEIGHT, frame.getHeight());
                    hook.setProperty(LayoutHook.PROPERTY_FRAME_STATE, frame.getExtendedState());
                }
            }
        });

        windowClosingHooks.forEach(frame::addWindowListener);

        frame.setIconImage(Configuration.ICON);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(main);
        frame.setMinimumSize(new Dimension(900, 600));

        final int state = hook.getPropertyAsInt(LayoutHook.PROPERTY_FRAME_STATE);
        frame.setExtendedState(state);
        if (state != JFrame.MAXIMIZED_BOTH) {
            final int width = hook.getPropertyAsInt(LayoutHook.PROPERTY_FRAME_WIDTH);
            final int height = hook.getPropertyAsInt(LayoutHook.PROPERTY_FRAME_HEIGHT);
            frame.setSize(width, height);
            frame.setLocationByPlatform(true);
        }
        access.getSplash().setStatus("Complete");

        frame.setVisible(true);
    }

    @Override
    public void addWindowClosingHook(final ShutDownHook hook) {
        windowClosingHooks.add(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hook.start();
            }
        });
    }

    public synchronized void openProject(final Project project, final Language language) {
        if (project == null) {
            return;
        }
        final Editor editor = new Editor(project, language);
        editor.setInstructionsText(project.getDescription(), false);
        if (getTab(project.getName(), language) == null) {
            final int index = tabs.getTabCount();
            tabs.add(editor, index);
            tabs.setTabComponentAt(index, new TabPane(project, language));
        }
        tabs.setSelectedComponent(getTab(project.getName(), language));
    }

    @SuppressWarnings("unchecked")
    public <T> T getTab(final Class<T> cls) {
        for (final JPanel tab : mainTabs) {
            if (tab.getClass().equals(cls)) {
                return (T) tab;
            }
        }
        return null;
    }

    @Override
    public synchronized Editor getTab(final String name, final Language language) {
        for (final Component c : tabs.getComponents()) {
            if (c instanceof Editor) {
                final Editor c1 = (Editor) c;
                if (name.equals(c1.getProjectName()) && language.equals(c1.getLanguage())) {
                    return c1;
                }
            }
        }
        return null;
    }

    public synchronized void removeTab(final String name, final Language language) {
        final Editor cur = getTab(name, language);
        if (cur != null) {
            tabs.remove(cur);
            tabs.setSelectedIndex(0); // Move back to selection screen
            return;
        }
        log.log(Level.WARNING, "Failed to close tab: " + name);
    }

}