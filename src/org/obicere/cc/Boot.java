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

package org.obicere.cc;

import com.alee.laf.WebLookAndFeel;
import org.obicere.cc.configuration.Global;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.Executor;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

/**
 * The boot class is responsible for basic loading for the client. Bringing all
 * the classes into unison, it effectively creates what is known as ObicereCC.
 * Advanced technology to help you learn Java and fulfill what I like to know as
 * 'Good standing'. Helping others for free. From everything to the
 * CustomClassLoader class to the Project class,everything here was made for you,
 * the user. I hope you have a great time running this application.
 * <br>
 * <br>
 *
 * @author Obicere
 * @version 1.0
 * @since 1.0
 */

public class Boot {

    /**
     * Nothing truly big to see here. Runs the application. Really should be
     * monitored but it isn't.
     *
     * @param args ignored. Or is it?
     * @since 1.0
     */

    public static void main(final String[] args) {
        ShutDownHookManager.setup();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Splash.display();
                }
            });
        } catch (final InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Paths.build();
        Updater.update();
        Language.loadLanguages();
        Splash.setStatus("Loading framework");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(new WebLookAndFeel());
                    GUI.buildGUI();
                    Splash.getInstance().shouldDispose(true);
                    Splash.getInstance().getFrame().dispose();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
