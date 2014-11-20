package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Paths;
import org.obicere.cc.gui.settings.BooleanSetting;
import org.obicere.cc.gui.settings.ButtonSetting;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * @author Obicere
 */
public class RunnerSourceHook extends SettingsShutDownHook {

    public static final String NAME = "runner.source";

    @HookValue("true")
    public static final String DOWNLOAD_FROM_MAIN_SOURCE      = "source.runner.main";
    public static final String DOWNLOAD_FROM_MAIN_DESCRIPTION = "Download from main source: ";

    public static final String EDIT_SOURCES_BUTTON      = "source.edit";
    public static final String EDIT_SOURCES_DESCRIPTION = "Edit sources: ";
    public static final String EDIT_SOURCES_ACTION      = "Open File";

    public RunnerSourceHook() {
        super("Runner Sources", NAME, PRIORITY_WINDOW_CLOSING);
        providePanel(DOWNLOAD_FROM_MAIN_SOURCE, new BooleanSetting(this, DOWNLOAD_FROM_MAIN_SOURCE, DOWNLOAD_FROM_MAIN_DESCRIPTION));
        providePanel(EDIT_SOURCES_BUTTON, new ButtonSetting(this, EDIT_SOURCES_BUTTON, EDIT_SOURCES_DESCRIPTION, EDIT_SOURCES_ACTION, () -> {
            final Desktop desktop = Desktop.getDesktop();
            final File sourceFile = new File(Paths.DATA, "sources.txt");

            if (desktop.isSupported(Desktop.Action.EDIT)) {
                if (sourceFile.exists()) {
                    try {
                        desktop.edit(sourceFile);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, String.format("<html>Could not open sources.<br>Sources files does not exist.</html>"));
                }
            } else {
                JOptionPane.showMessageDialog(null, String.format("<html>Could not open sources.<br>Source location: %s</html>", sourceFile));
            }
        }));
    }
}
