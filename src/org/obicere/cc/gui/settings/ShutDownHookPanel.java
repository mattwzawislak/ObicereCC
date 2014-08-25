package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.projects.WrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    private static final Dimension PREFERRED_SIZE = new Dimension(200, 60);

    public ShutDownHookPanel(final SettingsShutDownHook hook){
        super(new WrapLayout(WrapLayout.LEFT));
        setBorder(new TitledBorder(hook.getGroupName()));

        final Map<String, String> options = hook.getSettingDescriptions();
        options.forEach((value, description) -> {

            final JCheckBox allowed = new JCheckBox(description);
            allowed.setSelected(hook.getPropertyAsBoolean(value));
            allowed.addChangeListener(e -> {
                final boolean selected = allowed.isSelected();
                hook.toggleSetting(value, selected);
            });
            add(allowed);

        });

    }

    @Override
    public Dimension getPreferredSize(){
        return PREFERRED_SIZE;
    }

    @Override
    public Dimension getMinimumSize(){
        return PREFERRED_SIZE;
    }

}
