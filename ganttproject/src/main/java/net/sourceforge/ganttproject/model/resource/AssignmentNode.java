/*
Copyright 2003-2012 Dmitry Barashev, GanttProject Team

This file is part of GanttProject, an opensource project management tool.

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
package net.sourceforge.ganttproject.model.resource;

import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.roles.Role;
import net.sourceforge.ganttproject.model.task.ResourceAssignment;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.ui.chart.ResourceDefaultColumn;

import java.util.EnumSet;
import java.util.Set;

public class AssignmentNode extends ResourceTableNode {
  private static final Set<ResourceDefaultColumn> ourApplicableColumns = EnumSet.of(ResourceDefaultColumn.ROLE_IN_TASK);
  private final ResourceAssignment resourceAssignment;

  public AssignmentNode(ResourceAssignment res) {
    super(res, ourApplicableColumns);
    resourceAssignment = res;
  }

  public ResourceAssignment getAssignment() {
    return resourceAssignment;
  }

  @Override
  public Object getCustomField(CustomPropertyDefinition def) {
    return null;
  }

  public Role getRoleForAssigment() {
    return resourceAssignment.getRoleForAssignment();
  }

  public void setRoleForAssigment(Role role) {
    resourceAssignment.setRoleForAssignment(role);
  }

  @Override
  public Object getStandardField(ResourceDefaultColumn def) {
    switch (def) {
      case NAME:
        return getTask().getName();
      case ROLE_IN_TASK:
        return getRoleForAssigment();
      default:
        return "";
    }
  }

  public Task getTask() {
    return resourceAssignment.getTask();
  }

  @Override
  public void setCustomField(CustomPropertyDefinition def, Object val) {
  }

  @Override
  public void setStandardField(ResourceDefaultColumn def, Object value) {
    switch (def) {
      case ROLE_IN_TASK:
        setRoleForAssigment((Role) value);
        return;
    }
  }

  @Override
  public String toString() {
    return resourceAssignment.getTask().getName();
  }
}
