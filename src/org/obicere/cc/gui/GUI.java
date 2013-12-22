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

package org.obicere.cc.gui;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.gui.projects.ProjectTabPanel;
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

/**
 * Only for use in the boot and the setting and getting of Projects.
 *
 * @author Obicere
 * @see GUI#removeTab(String)
 * @see GUI#tabByName(String)
 * @since 1.0
 */
public class GUI {

    public static final LinkedList<WindowListener> WINDOW_CLOSING_HOOKS = new LinkedList<>();
    private static JTabbedPane tabs;
    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    /**
     * Creates a new GUI instance. Should only be done once per
     * <tt>Runtime</tt>. This will set all listeners and handle the
     * initialization of the static arguments to be used later.
     *
     * @since 1.0
     */
    public static void buildGUI() {
        final JFrame frame = new JFrame("Obicere Computing Challenges v" + Updater.clientVersion());
        final JPanel main = new JPanel(new BorderLayout());
        final SaveLayoutHook hook = ShutDownHookManager.hookByName(SaveLayoutHook.class, SaveLayoutHook.NAME);
        tabs = new JTabbedPane(SwingConstants.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        final JPanel[] defaultTabs = new JPanel[]{
                ProjectTabPanel.getInstance()
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
                hook.saveProperty(SaveLayoutHook.PROPERTY_FRAME_WIDTH, frame.getWidth());
                hook.saveProperty(SaveLayoutHook.PROPERTY_FRAME_HEIGHT, frame.getHeight());
                hook.saveProperty(SaveLayoutHook.PROPERTY_FRAME_STATE, frame.getExtendedState());
            }
        });

        for (final WindowListener listener : WINDOW_CLOSING_HOOKS) {
            frame.addWindowListener(listener);
        }

        frame.add(main);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(900, 600));
        final int state = Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_FRAME_STATE));
        frame.setExtendedState(state);
        if(state != JFrame.MAXIMIZED_BOTH){
            final int width = Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_FRAME_WIDTH));
            final int height = Integer.parseInt((String) hook.getProperty(SaveLayoutHook.PROPERTY_FRAME_HEIGHT));
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setIconImage(Global.ICON_IMAGE);
        Splash.setStatus("Complete");
    }

    /**
     * Adds a project selected from the ProjectSelector.
     *
     * @param project The selected Project you wish to add.
     * @since 1.0
     */

    public synchronized static void openProject(final Project project) {
        if (project == null) {
            return;
        }
        final Editor editor = new Editor(project);
        editor.setInstructionsText(project.getProperties().getDescription(), false);
        if (tabByName(project.getName()) == null) {
            final int index = tabs.getTabCount();
            tabs.add(editor, index);
            tabs.setTabComponentAt(index, new TabPane(project));
        }
        tabs.setSelectedComponent(tabByName(project.getName()));
    }

    /**
     * This will return the <tt>Editor</tt> that corresponds to the
     * given name.
     *
     * @param name The name of the tab, always portrayed through the Runner's name.
     * @return The tab instance of the Java editor.
     * @since 1.0
     */

    public synchronized static Editor tabByName(final String name) {
        for (final Component c : tabs.getComponents()) {
            if (c instanceof Editor) {
                final Editor c1 = (Editor) c;
                if (name.equals(c1.getName())) {
                    return c1;
                }
            }
        }
        return null;
    }

    /**
     * Removes the tab from the tabbed pane. Loads the tab by name.
     *
     * @see GUI#tabByName(String)
     * @since 1.0
     */

    public synchronized static void removeTab(final String name) {
        final Editor cur = tabByName(name);
        if (cur != null) {
            tabs.remove(cur);
            return;
        }
        System.err.println("Failed to close tab " + name);
    }

}