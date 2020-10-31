/*
Copyright 2020 BarD Software s.r.o

This file is part of GanttProject, an open-source project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
// 2020-06-03 Translated to Kotlin from ProjectMenu.java

package net.sourceforge.ganttproject.ui.action.project

import net.sourceforge.ganttproject.storage.StorageDialogAction
import net.sourceforge.ganttproject.storage.StorageDialogBuilder
import net.sourceforge.ganttproject.ui.GanttProjectUI
import net.sourceforge.ganttproject.ui.action.GPAction
import javax.swing.Action
import javax.swing.JMenu
import javax.swing.JMenuItem

/**
 * Collection of actions present in the project menu
 */
class ProjectMenu(projectUI: GanttProjectUI, key: String) : JMenu(GPAction.createVoidAction(key)) {

  private val newProjectAction = NewProjectAction(projectUI)
  val openProjectAction = OpenProjectAction(projectUI.project, projectUI.projectUIFacade)
  val saveProjectAction = SaveProjectAction(projectUI, projectUI.projectUIFacade)

  private val saveAsProjectAction = StorageDialogAction(
    projectUI.project, projectUI.projectUIFacade, projectUI.documentManager,
    StorageDialogBuilder.Mode.SAVE, "project.saveas"
  )

  private val projectSettingsAction = ProjectPropertiesAction(projectUI)
  private val importAction = ProjectImportAction(projectUI.uiFacade, projectUI)
  private val exportAction = ProjectExportAction(projectUI.uiFacade, projectUI, projectUI.ganttOptions.pluginPreferences)
  private val printAction = PrintAction(projectUI)
  private val printPreviewAction = ProjectPreviewAction(projectUI)
  private val exitAction = ExitAction(projectUI)

  override fun add(a: Action): JMenuItem {
    a.putValue(Action.SHORT_DESCRIPTION, null)
    return super.add(a)
  }

  init {
    listOf(
      newProjectAction, openProjectAction, saveProjectAction, saveAsProjectAction, projectSettingsAction,
      null,
      importAction, exportAction, printAction, printPreviewAction,
      null,
      exitAction
    ).forEach { if (it == null) addSeparator() else add(it) }
    toolTipText = null
  }
}
