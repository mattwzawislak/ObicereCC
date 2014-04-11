package org.obicere.cc.gui;

import org.obicere.cc.configuration.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
            }
        };

        message = Message.getRandom();
        name = System.getProperty("user.name");
        if (name == null || name.length() == 0) {
            name = "Mr. Anderson";
        }

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

    public void shouldDispose(boolean should) {
        this.should = should;
    }

    public static void setStatus(String status) {
        if(instance == null){
            return;
        }
        System.out.println(status);
        Splash.status = status;
        instance.getFrame().repaint();
    }

    public JFrame getFrame() {
        return frame;
    }

}
