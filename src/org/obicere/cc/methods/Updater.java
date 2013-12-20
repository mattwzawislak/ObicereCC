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

package org.obicere.cc.methods;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.configuration.Global.URLs;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Updates all Runner information and program data. This is used in conjunction
 * with the boot class and only should be used in there. This class handles most
 * client data and also calls upon the other loaders.
 *
 * @author Obicere
 * @since 1.0
 */
public class Updater {

    private static double updatedClientVersion = 0.0;
    private static LinkedHashMap<String, Double> updatedRunnersList = new LinkedHashMap<>();

    private static double currentClientVersion = 1.00;
    private static LinkedHashMap<String, Double> currentRunnersList = new LinkedHashMap<>();

    private Updater() {
    }

    /**
     * Returns the current client version. This is hard-coded every update.
     *
     * @return Client version.
     * @since 1.0
     */

    public static double clientVersion() {
        return currentClientVersion;
    }

    /**
     * Updates the client and all public Runners. No alpha implementation yet.
     * Should only ever be ran once.
     *
     * @since 1.0
     */

    public static void update() {
        Project.loadCurrent();
        if (!isInternetReachable()) {
            return;
        }
        final HashSet<String> sources = new HashSet<>();
        sources.add(URLs.BIN);
        try {
            final File sourceFile = new File(Paths.SETTINGS, "sources.txt");
            if (sourceFile.exists() || sourceFile.createNewFile()) {
                final BufferedReader br = new BufferedReader(new FileReader(sourceFile));
                String next;
                while ((next = br.readLine()) != null) {
                    if (!next.startsWith("#")) {
                        sources.add(next);
                    }
                }
                br.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        for (final String src : sources) {
            final byte[] updatedClientInfo = downloadCurrentClientInfo(src);
            if (updatedClientInfo == null) {
                return;
            }
            parseUpdate(updatedClientInfo, src);
            if (updatedClientVersion > currentClientVersion) {
                JOptionPane.showMessageDialog(null, "Update available - Please download again.", "Update", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            final String runner = "Runner";
            for (final Project p : Project.DATA) {
                currentRunnersList.put(p.getName() + runner, p.getProperties().getVersion());
            }
            for (final String key : updatedRunnersList.keySet()) {
                if (!currentRunnersList.containsKey(key) || updatedRunnersList.get(key) > currentRunnersList.get(key)) {
                    download(key, src);
                }
            }
        }
        currentRunnersList = null;
        updatedRunnersList = null;
        Project.loadCurrent();
    }

    /**
     * Downloads a runner name from a URL. The URL is constructed from the
     * <tt>src</tt> followed by the <tt>runnerName</tt>.
     *
     * @param runnerName The name of the Runner, in pure form such as
     *                   <tt>SimpleAdditionRunner</tt>
     * @param src        The depot of the Runner.
     * @since 1.0
     */

    private static void download(final String runnerName, final String src) {
        try {
            Splash.setStatus("Downloading " + runnerName + ".class");

            final byte[] data = IOUtils.download(new URL(src + runnerName + ".class"));
            final File out = new File(Global.Paths.SOURCE, runnerName + ".class");
            IOUtils.write(out, data);

            Splash.setStatus("Downloading " + runnerName + ".xml");
            final byte[] xdata = IOUtils.download(new URL(src + runnerName + ".xml"));
            final File xout = new File(Global.Paths.SOURCE, runnerName + ".xml");
            IOUtils.write(xout, xdata);

        } catch (final IOException e) {
            System.err.println("Unable to download " + runnerName);
            e.printStackTrace();
        }

    }

    /**
     * Reads the update information and runner information from a source. Source
     * specific arguments do matter.
     *
     * @param data The byte data downloaded from the source.txt file.
     * @param src  The URL directory for special loads.
     * @since 1.0
     */

    private static void parseUpdate(final byte[] data, final String src) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
            if (src.equals(URLs.BIN)) {
                updatedClientVersion = Double.parseDouble(in.readLine());
            }
            String s;
            while ((s = in.readLine()) != null) {
                String[] split = s.split("-");
                updatedRunnersList.put(split[0], Double.parseDouble(split[1]));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads information from a source. This will automatically be called for
     * the default directory, as implemented by myself.
     *
     * @param src The source of the current bin
     * @return The byte data to be read.
     * @see {@link Updater#parseUpdate(byte[], String)}
     * @since 1.0
     */

    private static byte[] downloadCurrentClientInfo(final String src) {
        try {
            return IOUtils.download(new URL(src + "version.dat"));
        } catch (final IOException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    /**
     * Ping common sites to check Internet availability.
     *
     * @return a boolean to check if Internet is available. Used before
     *         attempting to update.
     * @since {@link Updater#update()}
     */

    public static boolean isInternetReachable() {
        Splash.setStatus("Checking connection");
        try {
            final URL url = new URL(URLs.HOME);
            final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            if(urlConn.getResponseCode() != HttpURLConnection.HTTP_OK){
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    if (interfaces.nextElement().isUp()) {
                        Splash.setStatus("Site is down at this time.");
                        Thread.sleep(1000); // Allow time so user can see the message
                        return false;
                    }
                }
                Splash.setStatus("Not connected to the internet");
                Thread.sleep(1000); // Allow time so user can see the message
            }
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
