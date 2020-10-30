// Copyright (C) 2018 BarD Software
package net.sourceforge.ganttproject.ui.desktop;

import java.awt.*;
import java.awt.desktop.*;
import java.io.File;
import java.util.List;

/**
 * @author dbarashev@bardsoftware.com
 */
public class DesktopAdapter {
  public static void install(final GanttProjectApi api) {
    Desktop desktop = Desktop.getDesktop();
    desktop.setAboutHandler(new AboutHandler() {
      @Override
      public void handleAbout(AboutEvent e) {
        api.showAboutDialog();
      }
    });
    desktop.setPreferencesHandler(new PreferencesHandler() {
      @Override
      public void handlePreferences(PreferencesEvent e) {
        api.showPreferencesDialog();
      }
    });
    desktop.setQuitHandler(new QuitHandler() {
      @Override
      public void handleQuitRequestWith(QuitEvent e, final java.awt.desktop.QuitResponse response) {
        api.maybeQuit(new QuitResponse() {
          @Override
          public void performQuit() {
            response.performQuit();
          }

          @Override
          public void cancelQuit() {
            response.cancelQuit();
          }
        });
      }
    });
    desktop.setOpenFileHandler(new OpenFilesHandler() {
      @Override
      public void openFiles(OpenFilesEvent e) {
        List<File> files = e.getFiles();
        if (files.isEmpty()) {
          return;
        }
        File file = files.get(0);
        if (!file.isFile() || !file.canRead()) {
          return;
        }
        api.openFile(file);
      }
    });

  }
}
