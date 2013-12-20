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
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TabPane extends JPanel {

    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    public TabPane(final Project project) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 2));
        final JLabel label = new JLabel(project.getName());
        final JPanel buffer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton close = new JButton(new ImageIcon(Global.CLOSE_IMAGE));

        buffer.setOpaque(false);
        buffer.setPreferredSize(new Dimension(146, 24));
        buffer.add(label);

        close.setPreferredSize(new Dimension(24, 24));
        close.setContentAreaFilled(false);
        close.setOpaque(false);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                GUI.removeTab(project.getName());
            }
        });

        add(buffer);
        add(close);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize(){
        return TAB_SIZE;
    }

}
