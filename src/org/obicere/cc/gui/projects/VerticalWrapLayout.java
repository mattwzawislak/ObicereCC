package org.obicere.cc.gui.projects;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Obicere
 */
public class VerticalWrapLayout implements LayoutManager2 {

    private final Set<Component> components = new LinkedHashSet<>();
    private       int            hgap       = 0;
    private       int            vgap       = 0;

    public void setHGap(int hgap) {
        this.hgap = hgap;
    }

    public void setVGap(int vgap) {
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
        int x = 0;
        int y = 0;
        int max = 0;
        for (final Component c : components) {
            if (c.isVisible()) {
                max = Math.max(c.getPreferredSize().width, max);
            }
        }
        for (final Component c : this.components) {
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                if (y + d.height > parent.getHeight()) {
                    x += max + this.hgap;
                    y = 0;
                }
                c.setBounds(x, y, max, d.height);
                y += d.height + this.vgap;
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }

}