package org.obicere.cc.gui.layout;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Obicere
 */
public class VerticalWrapLayout implements LayoutManager2 {

    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);

    private final Set<Component> components = new LinkedHashSet<>();
    private final int hgap;
    private final int vgap;

    public VerticalWrapLayout() {
        this.hgap = 5;
        this.vgap = 5;
    }

    public VerticalWrapLayout(final int hgap, final int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        this.components.add(comp);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container target) {
    }


    @Override
    public void addLayoutComponent(String name, Component comp) {
        this.components.add(comp);
    }

    @Override
    public void layoutContainer(Container parent) {
        layout(parent);
    }

    private void layout(final Container parent) {
        int x = hgap;
        int y = vgap;
        int maxX = 0;
        for (final Component c : components) {
            if (c.isVisible()) {
                maxX = Math.max(c.getPreferredSize().width, maxX);
            }
        }
        for (final Component c : this.components) {
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                if (y != vgap && y + d.height > parent.getHeight()) { // y != vgap to avoid immediate wrap
                    x += maxX + this.hgap;
                    y = vgap;
                }
                c.setBounds(x, y, maxX, d.height);
                y += d.height + this.vgap;
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return ZERO_DIMENSION;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return ZERO_DIMENSION;
    }

    @Override
    public Dimension maximumLayoutSize(Container parent) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }

}