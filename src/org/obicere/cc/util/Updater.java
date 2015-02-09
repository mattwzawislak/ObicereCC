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
import java.io.FileNotFoundException;
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

/**
 * The updater is responsible for checking client versions and file
 * versions as well as downloading updates. The process does require that
 * the project folders be created before. So a priority of <code>2</code>
 * was assigned.
 * <p>
 * This will also contain the functionality of checking the internet
 * connection by pinging the server. This functionality can be used for
 * modifications requiring internet connectivity to the main server.
 * <p>
 * The main functionality of the updater is to read the
 * <code>version.dat</code> file for a given host. For the main server, the
 * version file will also contain the latest client version on the first
 * line. This will be of the form:
 * <p>
 * <code>Major.Minor</code>
 * <p>
 * Such as:
 * <p>
 * <code>1.02</code>
 * <p>
 * Other sources <b>should not</b> provide a client version as of v1.0.
 * <p>
 * It is recommended that external sources do not utilize packages.
 * Packages are supported in form, but there might be adverse effects later
 * on. Due to this, it is recommended not to use a package when storing the
 * runners.
 * <p>
 * The format for the runner information should be:
 * <pre>
 * package.Name - version
 * </pre>
 * <p>
 * Such as:
 * <pre>
 * org.Bar - 1.0
 * Foo - 3.4
 * </pre>
 * <p>
 * With the above configuration, the file layout should be:
 * <p>
 * <pre>
 * |-- org
 * |    |- Bar.class
 * |
 * |- Foo.class
 * </pre>
 * <p>
 * This will then be reflected in the system folders respectively. In the
 * case of <code>Bar.class</code>, it should denote at the top:
 * <p>
 * <code>package org;</code>
 * <p>
 * Whereas <code>Foo.class</code> can just rely off of the default package
 * and no package declaration should be present.
 * <p>
 * One can turn off automatic updates from the main source: {@link
 * Paths#SITE_BIN}. Should this be done, client updates and runner updates
 * from this source will be ignored completely.
 *
 * @author Obicere
 * @version 1.
 * @see org.obicere.cc.projects.RunnerManifest#version()
 * @see org.obicere.cc.configuration.Domain#getClientVersion()
 */

public class Updater extends StartingProcess {

    private double updatedClientVersion = 0.0;

    private LinkedHashMap<String, Double> updatedRunnersList = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> currentRunnersList = new LinkedHashMap<>();

    private final Predicate<String> OUTDATED_FILTER = key -> !currentRunnersList.containsKey(key) || updatedRunnersList.get(key) > currentRunnersList.get(key);

    /**
     * {@inheritDoc}
     */
    public Updater(final Domain access) {
        super(access);
    }

    /**
     * {@inheritDoc}
     */
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

        final AtomicBoolean fileChanged = new AtomicBoolean(false); // needs to be final for use in lambda
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

    /**
     * Attempts to download the given class by <code>name</code> from the
     * source <code>src</code>. This should include the package. It is not
     * necessary to include the <code>.class</code> file type, as this is
     * discarded.
     * <p>
     * The packages will be created automatically in the case they do not
     * exist.
     * <p>
     * This will notify the splash to update the current task.
     * <p>
     * Given the line: <code> org.bar.Foo - 1.0 </code>
     * <p>
     * With the source: <code>http://bar.org/</code>, this will attempt to
     * download the file:
     * <p>
     * <code> http://bar.org/org/bar/Foo.class </code>
     * <p>
     * It is suggested that the source ends with a <code>/</code> to denote
     * a folder. However, this has not been enforced in case people like to
     * break things.
     * <p>
     * Damn rebels.
     *
     * @param name The name of the runner to download, including the
     *             package declaration.
     * @param src  The source to download the runner from.
     */

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

        } catch (final FileNotFoundException e) {
            if (src.endsWith("/")) {
                log.log(Level.WARNING, "Failed to download class: {0}. File does not exist.", src + name);
            } else {
                log.log(Level.WARNING, "Failed to download class: {0}. File does not exist. Possibly add a '/' at the end of the source?", src + name);
            }
            e.printStackTrace();
        } catch (final IOException e) {
            log.log(Level.WARNING, "Failed to download class: " + name);
            e.printStackTrace();
        }

    }

    /**
     * Parses the update from the specified format to a name-version pair.
     * If the source is the site home, then the latest client version is
     * also parsed here.
     *
     * @param data The raw data of the version file.
     * @param src  The source location of the version file.
     * @see #downloadCurrentClientInfo(String)
     */

    private void parseUpdate(final byte[] data, final String src) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
            if (src.equals(Paths.SITE_BIN)) {
                updatedClientVersion = Double.parseDouble(in.readLine());
            }
            String s;
            while ((s = in.readLine()) != null) {
                final String[] split = s.split("\\s*-\\s*");
                updatedRunnersList.put(split[0], Double.parseDouble(split[1]));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads the raw data of the version file from the specific source.
     * This assumes that the file is called <code>version.dat</code> as of
     * v1.0. Should the convention be changed, this will be reflected to
     * modify such changes.
     *
     * @param src The source location of the version file.
     * @return The raw data of the version file if possible, otherwise
     * <code>null</code>.
     * @see #parseUpdate(byte[], String)
     */

    private byte[] downloadCurrentClientInfo(final String src) {
        try {
            return IOUtils.download(new URL(src + "version.dat"));
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks to see if an internet connection can be established by
     * pinging the main site. This will also log the result to the console
     * notifying the time to reach the server. If the site cannot be
     * accessed, then the respective reason will also be printed.
     * <p>
     * If in fact your device is connected to the internet, this will be
     * checked as well. This is not entirely reliable, since the device may
     * appear to be connected, yet not be able to establish a connection to
     * any server.
     *
     * @return <code>true</code> if and only if a connection can be
     * established to the main site.
     * @see Paths#SITE_HOME
     */

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
