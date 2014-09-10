package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Obicere
 */
public class ColorSetting extends SettingPanel {

    private static final int COLOR_SELECTOR_SIZE = 15;

    private Color selectedColor;

    public ColorSetting(final SettingsShutDownHook hook, final String value, final String description) {
        super(hook, value, description);
        this.selectedColor = hook.getPropertyAsColor(getKey());
    }

    @Override
    protected void buildPanel() {
        final SettingsShutDownHook hook = getHook();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        final JLabel label = new JLabel(getDescriptor());
        final JButton selectColor = new JButton(null, createIcon(selectedColor));

        selectColor.setBorderPainted(false);
        selectColor.setContentAreaFilled(false);

        selectColor.addActionListener(e -> {

            selectedColor = JColorChooser.showDialog(null, "Select new color.", selectedColor);
            selectColor.setIcon(createIcon(selectedColor));

            hook.setProperty(getKey(), selectedColor);
        });

        add(label);
        add(Box.createHorizontalGlue());
        add(selectColor);
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
}
