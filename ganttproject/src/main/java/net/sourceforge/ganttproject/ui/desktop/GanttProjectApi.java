// Copyright (C) 2018 BarD Software
package net.sourceforge.ganttproject.ui.desktop;

import java.io.File;

/**
 * @author dbarashev@bardsoftware.com
 */
public interface GanttProjectApi {
  void showAboutDialog();
  void showPreferencesDialog();
  void maybeQuit(QuitResponse quitResponse);
  void openFile(File file);
}
