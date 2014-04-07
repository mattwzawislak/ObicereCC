package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    private static final Dimension PREFERRRED_SIZE = new Dimension(400, 120);

    public ShutDownHookPanel(final ShutDownHook hook){
        final JCheckBox allowed = new JCheckBox(hook.getPurpose());
        final JLabel description = new JLabel(hook.getDescription());

        allowed.addChangeListener(e -> hook.setAllowed(allowed.isSelected()));

        add(allowed);
        add(description);

    }

    @Override
    public Dimension getPreferredSize(){
        return PREFERRRED_SIZE;
    }

    @Override
    public Dimension getMinimumSize(){
        return PREFERRRED_SIZE;
    }

}
