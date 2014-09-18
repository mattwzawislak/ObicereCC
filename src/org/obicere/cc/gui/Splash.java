package org.obicere.cc.gui;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.configuration.Message;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.shutdown.SplashScreenHook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Splash {

    private static final Logger LOGGER = Logger.getLogger(Splash.class.getCanonicalName());

    private static final SplashScreenHook HOOK = ShutDownHookManager.hookByClass(SplashScreenHook.class);

    private static final int CENTER_X = 500;
    private static final int CENTER_Y = 100;

    private static final Polygon POLYGON = new Polygon(
            new int[]{CENTER_X - 50, CENTER_X, CENTER_X + 50, CENTER_X},
            new int[]{CENTER_Y - 30, CENTER_Y - 1, CENTER_Y - 30, CENTER_Y - 59},
            4);

    private static final Color[] COLORS = new Color[]{
            new Color(0xADBED2),
            new Color(0xC77431),
            new Color(0x333333)
    };

    private static final float[] ALPHA = new float[]{
            1,
            1,
            1
    };

    private static final Stroke OUTLINE    = new BasicStroke(1.25f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final Color  BACKGROUND = new Color(79, 79, 79);
    private static final Color  BORDER     = new Color(199, 116, 49);
    private static final Color  TEXT_COLOR = new Color(173, 190, 210);
    private static final Font   FONT       = new Font("Consolas", Font.PLAIN, 14);

    private static Splash instance;
    private static String status = "Loading";

    private final JFrame frame;
    private final String message;
    private final String name;

    private boolean should;

    private Splash() {
        frame = new JFrame();
        final JPanel splash = new JPanel() {

            @Override
            public void paintComponent(Graphics g1) {
                final Graphics2D g = (Graphics2D) g1;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 3; i++) {
                    final Graphics2D newG = (Graphics2D) g.create();
                    final AffineTransform transform = new AffineTransform();
                    transform.rotate(Math.toRadians(120 * i), CENTER_X, CENTER_Y);
                    final Shape newShape = transform.createTransformedShape(POLYGON);
                    final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA[i]);

                    newG.setStroke(OUTLINE);
                    newG.setColor(COLORS[i]);
                    newG.draw(newShape);
                    newG.setComposite(composite);
                    newG.fill(newShape);
                }
            }
        };
        final Mouse mouse = new Mouse();
        splash.addMouseListener(mouse);
        splash.addMouseMotionListener(mouse);

        message = Message.getRandom();
        final String username = HOOK.getPropertyAsString(SplashScreenHook.USER_NAME);
        if (username == null || username.length() == 0) {
            name = System.getProperty("user.name");
        } else {
            name = username;
        }

        frame.setIconImage(Global.ICON);
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

    public static void display() {
        instance = new Splash();
        instance.getFrame().setVisible(true);
    }

    public static Splash getInstance() {
        return instance;
    }

    public static void setStatus(String status) {
        if (instance == null) {
            return;
        }
        LOGGER.log(Level.INFO, status);
        Splash.status = status;
        instance.getFrame().repaint();
    }

    public void shouldDispose(boolean should) {
        this.should = should;
    }

    public void dispose() {
        frame.dispose();
    }

    private JFrame getFrame() {
        return frame;
    }

    public class Mouse extends MouseAdapter {

        @Override
        public void mouseEntered(final MouseEvent e) {
            mouseMoved(e);
        }

        public void mouseExited(final MouseEvent e) {
            for (int i = 0; i < 3; i++) {
                ALPHA[i] = 1;
            }
            frame.repaint();
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            double dx = e.getX() - CENTER_X;
            double dy = e.getY() - CENTER_Y;

            double inRads = Math.atan2(dy, dx);
            if (inRads < 0) {
                inRads = Math.abs(inRads);
            } else {
                inRads = 2 * Math.PI - inRads;
            }

            int angle = (int) (Math.toDegrees(inRads));
            for (int i = 0; i < 3; i++) {
                int anglePiece = 270 - 120 * i;
                int difference = Math.abs(angle - anglePiece);
                int fixedAngle = 180 - Math.abs(180 - difference);
                ALPHA[i] = fixedAngle / 180f;
            }
            frame.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            mouseMoved(e);
        }

    }
}
