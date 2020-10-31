// Copyright (C) 2018 BarD Software
package net.sourceforge.ganttproject.ui;

import net.sourceforge.ganttproject.model.IGanttProject;
import net.sourceforge.ganttproject.ui.desktop.DesktopAdapter;
import net.sourceforge.ganttproject.ui.desktop.GanttProjectApi;
import net.sourceforge.ganttproject.ui.desktop.QuitResponse;
import net.sourceforge.ganttproject.ui.action.edit.SettingsDialogAction;
import net.sourceforge.ganttproject.model.document.Document;
import net.sourceforge.ganttproject.ui.gui.ProjectUIFacade;
import net.sourceforge.ganttproject.ui.gui.UIFacade;

import java.io.File;
import java.io.IOException;

/**
 * @author dbarashev@bardsoftware.com
 */
public class DesktopIntegration {
  public static boolean isMacOs() {
    return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
  }

  static void setup(final GanttProjectUI app) {
    final IGanttProject project = app.getProject();
    final UIFacade uiFacade = app.getUIFacade();
    final ProjectUIFacade projectUiFacade = app.getProjectUIFacade();

    try {
      DesktopAdapter.install(new GanttProjectApi() {
        @Override
        public void showAboutDialog() {
          AboutKt.showAboutDialog();
        }

        @Override
        public void showPreferencesDialog() {
          new SettingsDialogAction().actionPerformed(null);
        }

        @Override
        public void maybeQuit(QuitResponse quitResponse) {
          if (app.quitApplication()) {
            quitResponse.performQuit();
          } else {
            quitResponse.cancelQuit();
          }
        }

        @Override
        public void openFile(final File file) {
          javax.swing.SwingUtilities.invokeLater(() -> {
            if (projectUiFacade.ensureProjectSaved(project)) {
              Document myDocument = project.getDocumentManager().getDocument(file.getAbsolutePath());
              try {
                projectUiFacade.openProject(myDocument, project, null);
              } catch (Document.DocumentException | IOException ex) {
                uiFacade.showErrorDialog(ex);
              }
            }
          });
        }
      });
    } catch (UnsupportedOperationException e) {
      // Intentionally empty
    }
  }
}
