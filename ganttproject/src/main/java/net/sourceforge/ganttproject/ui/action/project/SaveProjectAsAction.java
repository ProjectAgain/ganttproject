/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.ui.action.project;

import net.sourceforge.ganttproject.ui.GanttProjectUI;
import net.sourceforge.ganttproject.ui.action.GPAction;

import java.awt.event.ActionEvent;

class SaveProjectAsAction extends GPAction {
  private GanttProjectUI myMainFrame;

  SaveProjectAsAction(GanttProjectUI mainFrame) {
    super("project.saveas");
    myMainFrame = mainFrame;
  }

  @Override
  protected String getIconFilePrefix() {
    return "saveas_";
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (calledFromAppleScreenMenu(e)) {
      return;
    }
    myMainFrame.saveAsProject();
  }
}
