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

import net.projectagain.ganttplanner.core.domain.MProjectBase;
import net.projectagain.ganttplanner.core.domain.MProjectEventSource;
import net.projectagain.ganttplanner.core.domain.MProjectRepositoryLegacy;
import net.projectagain.ganttplanner.core.infrastructure.DocumentManagerAccess;
import net.projectagain.ganttplanner.core.infrastructure.HumanResourceManagerAccess;
import net.projectagain.ganttplanner.core.infrastructure.RoleManagerAccess;
import net.projectagain.ganttplanner.core.infrastructure.TaskManagerAccess;
import net.sourceforge.ganttproject.model.time.TimeUnitStack;
import net.sourceforge.ganttproject.ui.gui.UIConfiguration;

/**
 * This interface represents a project as a logical business entity, without any
 * UI (except some configuration options :)
 *
 * @author bard
 */
public interface IGanttProject
  extends MProjectBase, MProjectRepositoryLegacy, MProjectEventSource,
          DocumentManagerAccess, HumanResourceManagerAccess, RoleManagerAccess, TaskManagerAccess
{

  TimeUnitStack getTimeUnitStack();

  UIConfiguration getUIConfiguration();

  // CustomColumnsStorage getCustomColumnsStorage();
}
