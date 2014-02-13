package org.obicere.cc.methods;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.configuration.Global.URLs;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
public class Updater {

    private static double updatedClientVersion = 0.0;
    private static LinkedHashMap<String, Double> updatedRunnersList = new LinkedHashMap<>();

    private static double currentClientVersion = 1.00;
    private static LinkedHashMap<String, Double> currentRunnersList = new LinkedHashMap<>();

    private Updater() {
    }

    public static double clientVersion() {
        return currentClientVersion;
    }

    public static void update() {
        Project.loadCurrent();
        if (!isInternetReachable()) {
            return;
        }
        final HashSet<String> sources = new HashSet<>();
        sources.add(URLs.BIN);
        try {
            final File sourceFile = new File(Paths.DATA, "sources.txt");
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
                currentRunnersList.put(p.getName() + runner, p.getVersion());
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

    private static void download(final String runnerName, final String src) {
        try {
            Splash.setStatus("Downloading " + runnerName + ".class");

            final byte[] data = IOUtils.download(new URL(src + runnerName + ".class"));
            final File out = new File(Global.Paths.SOURCE, runnerName + ".class");
            IOUtils.write(out, data);

        } catch (final IOException e) {
            System.err.println("Unable to download " + runnerName);
            e.printStackTrace();
        }

    }

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

    private static byte[] downloadCurrentClientInfo(final String src) {
        try {
            return IOUtils.download(new URL(src + "version.dat"));
        } catch (final IOException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

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
