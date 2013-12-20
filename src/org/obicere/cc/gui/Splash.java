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
import org.obicere.cc.configuration.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The splash screen loaded before the main application. It displays information
 * regarding current status, tips to help you and gives you a nice heart-filled
 * welcome.
 *
 * @author Obicere
 * @since 1.0
 */

public class Splash {

    private static Splash instance;
    private final JFrame frame;

    private static String status = "Loading";
    private String name;
    private final String message;

    private static final Color BACKGROUND = new Color(51, 51, 51);
    private static final Color BORDER = new Color(199, 116, 49);
    private static final Color TEXT_COLOR = new Color(173, 190, 210);
    private static final Font FONT = new Font("Consolas", Font.PLAIN, 14);
    private boolean should;

    /**
     * Constructs a new splash with default arguments.
     *
     * @since 1.0
     */

    private Splash() {
        frame = new JFrame();
        final JPanel splash = new JPanel() {

            @Override
            public void paintComponent(Graphics g1) {
                final Graphics2D g = (Graphics2D) g1;
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(BORDER);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BACKGROUND);
                g.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
                g.setColor(TEXT_COLOR);
                g.setFont(FONT);
                g.drawString(status, 10, 190);
                g.drawString("Welcome to Obicere Computing Challenges, " + name, 10, 15);
                g.drawString(message, 10, 105);
                g.drawImage(Global.ANIMATION_IMAGE, 409, 9, this);
            }
        };

        message = Message.getRandom();
        name = System.getProperty("user.name");
        if (name == null || name.length() == 0) {
            name = "Mr. Anderson"; // matrix.
        }

        frame.setIconImage(Global.ICON_IMAGE);
        frame.setUndecorated(true);
        frame.setSize(600, 200);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(splash);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!should) {
                    System.exit(0);
                }
            }
        });
    }

    public static void display(){
        instance = new Splash();
        instance.getFrame().setVisible(true);
    }

    public static Splash getInstance(){
        return instance;
    }

    /**
     * Used to check if the boot went well and was not terminated prematurely.
     *
     * @param should Whether or not it should dispose and continue basic operation.
     * @since 1.0
     */

    public void shouldDispose(boolean should) {
        this.should = should;
    }

    /**
     * Sets the current status of the splash screen. This is only used during
     * boot, as is the splash itself.
     *
     * @param status The message you would like to relay to the user.
     * @since 1.0
     */

    public static void setStatus(String status) {
        System.out.println(status);
        Splash.status = status;
        instance.getFrame().repaint();
    }

    /**
     * Used to call JFrame-specific calls such as <tt>dipose()</tt> or <tt>repaint()</tt>
     *
     * @return The splash screen's JFrame instance.
     * @since 1.0
     */

    public JFrame getFrame() {
        return frame;
    }

}
