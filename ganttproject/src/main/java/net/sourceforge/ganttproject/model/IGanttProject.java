/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2011 GanttProject Team

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
package net.sourceforge.ganttproject.model;

import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.document.Document;
import net.sourceforge.ganttproject.model.document.Document.DocumentException;
import net.sourceforge.ganttproject.model.document.DocumentManager;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.roles.RoleManager;
import net.sourceforge.ganttproject.model.task.CustomPropertyManager;
import net.sourceforge.ganttproject.model.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.time.TimeUnitStack;
import net.sourceforge.ganttproject.ui.gui.UIConfiguration;

import java.io.IOException;
import java.util.List;

/**
 * This interface represents a project as a logical business entity, without any
 * UI (except some configuration options :)
 *
 * @author bard
 */
public interface IGanttProject {

  String getProjectName();

  void setProjectName(String projectName);

  String getDescription();

  void setDescription(String description);

  String getOrganization();

  void setOrganization(String organization);

  String getWebLink();

  void setWebLink(String webLink);

  UIConfiguration getUIConfiguration();

  HumanResourceManager getHumanResourceManager();

  RoleManager getRoleManager();

  TaskManager getTaskManager();

  TaskContainmentHierarchyFacade getTaskContainment();

  GPCalendarCalc getActiveCalendar();

  TimeUnitStack getTimeUnitStack();

  void setModified();

  void setModified(boolean modified);

  void close();

  Document getDocument();

  void setDocument(Document document);

  DocumentManager getDocumentManager();

  void addProjectEventListener(ProjectEventListener listener);

  void removeProjectEventListener(ProjectEventListener listener);

  boolean isModified();

  void open(Document document) throws IOException, DocumentException;

  CustomPropertyManager getResourceCustomPropertyManager();

  CustomPropertyManager getTaskCustomColumnManager();

  // CustomColumnsStorage getCustomColumnsStorage();

  List<GanttPreviousState> getBaselines();
}
