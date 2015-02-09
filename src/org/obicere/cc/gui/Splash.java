package org.obicere.cc.gui;

import org.obicere.cc.configuration.Configuration;
import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;
import org.obicere.cc.configuration.Message;
import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.shutdown.SplashScreenHook;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Splash extends DomainAccess implements Runnable {

    private static final Logger log = Logger.getGlobal();

    private static final int WIDTH  = 600;
    private static final int HEIGHT = 200;

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

    private final float[] alpha = new float[]{
            1,
            1,
            1
    };

    private static final Stroke OUTLINE    = new BasicStroke(1.25f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final Color  BACKGROUND = new Color(0x4f4f4f);
    private static final Color  BORDER     = new Color(0xC77431);
    private static final Color  TEXT_COLOR = new Color(0xADBED2);
    private static final Font   FONT       = new Font("Consolas", Font.PLAIN, 14);

    private String status = "Loading";

    private final JFrame frame;

    private boolean should;

    public Splash(final Domain access) {
        super(access);
        this.frame = new JFrame();
    }

    @Override
    public void run() {
        final ShutDownHook hook = access.getHookManager().hookByClass(SplashScreenHook.class);

        if (hook.getPropertyAsBoolean(SplashScreenHook.NO_SPLASH)) {
            return;
        }

        final String username = hook.getPropertyAsString(SplashScreenHook.USER_NAME);
        final String name;
        if (username == null || username.length() == 0) {
            name = System.getProperty("user.name");
        } else {
            name = username;
        }

        final BufferedImage background = buildBackground(name);
        final JPanel splash = new JPanel() {

            @Override
            public void paintComponent(Graphics g1) {
                super.paintComponent(g1);
                final Graphics2D g = (Graphics2D) g1;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g.drawImage(background, 0, 0, this);
                g.setColor(TEXT_COLOR);
                g.setFont(FONT);
                g.drawString(status, 10, 190);

                for (int i = 0; i < 3; i++) {
                    final Graphics2D newG = (Graphics2D) g.create();
                    final AffineTransform transform = new AffineTransform();
                    transform.rotate(Math.toRadians(120 * i), CENTER_X, CENTER_Y);
                    final Shape newShape = transform.createTransformedShape(POLYGON);
                    final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[i]);

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
        frame.setIconImage(Configuration.ICON);
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
        frame.setVisible(true);
    }

    private BufferedImage buildBackground(final String name) {
        final BufferedImage background = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = (Graphics2D) background.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BORDER);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(BACKGROUND);
        g.fillRect(2, 2, WIDTH - 4, HEIGHT - 4);
        g.setColor(TEXT_COLOR);
        g.setFont(FONT);
        g.drawString("Welcome to Obicere Computing Challenges, " + name, 10, 15);

        final String message = new Message().getRandom();
        final String[] split = message.split("\n");
        int y = 105;
        final int height = g.getFontMetrics().getHeight();
        for (final String aSplit : split) {
            g.drawString(aSplit, 10, y);
            y += height;
        }

        return background;
    }

    public void setStatus(final String status) {
        log.log(Level.INFO, status);
        this.status = status;
        SwingUtilities.invokeLater(frame::repaint);
        // This method might not be called on swing worker thread
        // Better be sure not to tie up calling thread.
    }

    public void shouldDispose(final boolean should) {
        this.should = should;
    }

    public void safelyDispose() {
        shouldDispose(true);
        dispose();
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
                alpha[i] = 1;
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
                alpha[i] = fixedAngle / 180f;
            }
            frame.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            mouseMoved(e);
        }

    }
}
