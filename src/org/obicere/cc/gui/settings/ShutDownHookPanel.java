package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    private static final Dimension PREFERRED_SIZE = new Dimension(200, 60);

    public ShutDownHookPanel(final ShutDownHook hook){
        super(new BorderLayout(10, 10));

        final JCheckBox allowed = new JCheckBox(hook.getPurpose());
        allowed.addChangeListener(e -> hook.setAllowed(allowed.isSelected()));
        add(allowed);

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
