package org.obicere.cc.util;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.Paths;
import org.obicere.cc.process.StartingProcess;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.ProjectLoader;
import org.obicere.cc.shutdown.RunnerSourceHook;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Level;

public class Updater extends StartingProcess {

    private double updatedClientVersion = 0.0;

    private LinkedHashMap<String, Double> updatedRunnersList = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> currentRunnersList = new LinkedHashMap<>();

    private final Predicate<String> OUTDATED_FILTER = key -> !currentRunnersList.containsKey(key) || updatedRunnersList.get(key) > currentRunnersList.get(key);


    public Updater(final Domain access) {
        super(access);
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public void run() {
        ProjectLoader.loadCurrent();
        if (!isInternetReachable()) {
            return;
        }
        final HashSet<String> sources = new HashSet<>();
        final RunnerSourceHook hook = access.getHookManager().hookByClass(RunnerSourceHook.class);
        final boolean downloadMain = hook.getPropertyAsBoolean(RunnerSourceHook.DOWNLOAD_FROM_MAIN_SOURCE);
        if (downloadMain) { // Note that this will allow updates
            sources.add(Paths.SITE_BIN);
        }
        try {
            final File sourceFile = new File(Paths.FOLDER_DATA, "sources.txt");
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
        // Halley's Comment

        final AtomicBoolean fileChanged = new AtomicBoolean(false);
        for (final String src : sources) {
            final byte[] updatedClientInfo = downloadCurrentClientInfo(src);
            if (updatedClientInfo == null) {
                return;
            }
            parseUpdate(updatedClientInfo, src);
            if (updatedClientVersion > access.getClientVersion()) {
                JOptionPane.showMessageDialog(null, "Update available - Please download again.", "Update", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            for (final Project p : ProjectLoader.getData()) {
                currentRunnersList.put(p.getRunnerClass().getCanonicalName(), p.getVersion());
            }
            updatedRunnersList.keySet().stream().filter(OUTDATED_FILTER).forEach(key -> {
                download(key, src);
                if (!fileChanged.get()) {
                    fileChanged.set(true);
                }
            });
        }
        if (fileChanged.get()) {
            ProjectLoader.loadCurrent();
        }
    }

    private void download(final String name, final String src) {
        try {
            final String runnerName;
            if (name.endsWith(".class")) {
                runnerName = name.substring(0, name.length() - 6);
            } else {
                runnerName = name;
            }
            access.getSplash().setStatus("Downloading " + name);

            final byte[] data = IOUtils.download(new URL(src + runnerName.replace(".", "/") + ".class"));

            final int dotIndex = runnerName.lastIndexOf('.');

            final String directory;
            final String fileName;
            if (dotIndex >= 0) {
                directory = runnerName.substring(0, dotIndex);
                fileName = runnerName.substring(dotIndex + 1, runnerName.length());
            } else {
                directory = "";
                fileName = runnerName;
            }

            final File packageDirectory = new File(Paths.FOLDER_SOURCES, directory.replace(".", File.separator));
            if (!packageDirectory.exists() && !packageDirectory.mkdirs()) {
                throw new IOException("Failed to create proper package: " + packageDirectory);
            }
            final File out = new File(packageDirectory, fileName + ".class");
            IOUtils.write(out, data);

        } catch (final IOException e) {
            log.log(Level.WARNING, "Failed to download class: " + name);
            e.printStackTrace();
        }

    }

    private void parseUpdate(final byte[] data, final String src) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
            if (src.equals(Paths.SITE_BIN)) {
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

    private byte[] downloadCurrentClientInfo(final String src) {
        try {
            return IOUtils.download(new URL(src + "version.dat"));
        } catch (final IOException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public boolean isInternetReachable() {
        access.getSplash().setStatus("Checking connection");
        try {
            final URL url = new URL(Paths.SITE_HOME);
            final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            final long responseStart = System.currentTimeMillis();
            urlConn.connect();
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final long responseTime = System.currentTimeMillis() - responseStart;
                log.log(Level.INFO, "Response from the server took {0}ms.", responseTime);
                return true;
            }
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                if (interfaces.nextElement().isUp()) {
                    log.log(Level.WARNING, "Site appears down from this connection.");
                    return false;
                }
            }
            log.log(Level.WARNING, "Not connected to internet. Unable to get updates.");
        } catch (final UnknownHostException host) {
            log.log(Level.WARNING, "Could not connect to host. Unknown host. ");
        } catch (final IOException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Could not connect to server. ");
        }
        return false;
    }
}
