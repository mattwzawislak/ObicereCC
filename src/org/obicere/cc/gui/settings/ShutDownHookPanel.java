package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.projects.WrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    private static final int COLOR_SELECTOR_SIZE = 15;

    private static final Dimension PREFERRED_SIZE = new Dimension(200, 60);

    public ShutDownHookPanel(final SettingsShutDownHook hook) {
        super(new WrapLayout(WrapLayout.LEFT));
        setBorder(new TitledBorder(hook.getGroupName()));

        final Map<String, String> options = hook.getSettingDescriptions();
        options.forEach(
                (value, description) -> {
                    final Object dynamicObject = hook.getDynamicObject(value);
                    if (dynamicObject instanceof Color) {   // TODO: create a new system where each value has their designated option
                                                            // would allow things such as fonts, colors and other types a lot easier

                        final Color loaded = Color.ORANGE; // TODO: create the hook to fill this
                        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                        final JLabel label = new JLabel("Select Color: ");
                        final JButton selectColor = new JButton(null, createIcon(loaded));
                        selectColor.setBorderPainted(false);
                        selectColor.setContentAreaFilled(false);

                        selectColor.addActionListener(e -> {

                            final Color selectedColor = JColorChooser.showDialog(null, "Select new color.", loaded);
                            selectColor.setIcon(createIcon(selectedColor));

                            // hook.setValue(value, selectedColor); // TODO
                        });

                        panel.add(label);
                        panel.add(selectColor);
                    }

                    if (dynamicObject instanceof Boolean) {
                        final JCheckBox allowed = new JCheckBox(description);
                        allowed.setSelected(hook.getPropertyAsBoolean(value));
                        allowed.addChangeListener(e -> {
                            final boolean selected = allowed.isSelected();
                            hook.toggleSetting(value, selected);
                        });
                        add(allowed);
                    }
                });
    }

    private Icon createIcon(final Color color) {
        final BufferedImage image = new BufferedImage(COLOR_SELECTOR_SIZE, COLOR_SELECTOR_SIZE, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, COLOR_SELECTOR_SIZE, COLOR_SELECTOR_SIZE);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, COLOR_SELECTOR_SIZE, COLOR_SELECTOR_SIZE);
        return new ImageIcon(image);
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    @Override
    public Dimension getMinimumSize() {
        return PREFERRED_SIZE;
    }

}
