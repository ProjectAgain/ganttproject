/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

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
package net.sourceforge.ganttproject.model.task;

public class CustomColumEvent {

  public static final int EVENT_ADD = 0;
  public static final int EVENT_REBUILD = 2;
  public static final int EVENT_REMOVE = 1;
  public static final int EVENT_RENAME = 3;
  protected final String myColName;
  protected final int myType;
  private final CustomColumn myColumn;
  private final String myOldName;

  public CustomColumEvent(int type, String colName) {
    myType = type;
    myColName = colName;
    myColumn = null;
    myOldName = colName;
  }

  public CustomColumEvent(int type, CustomColumn column) {
    myType = type;
    myColumn = column;
    myColName = column.getName();
    myOldName = myColName;
  }

  CustomColumEvent(String oldName, CustomColumn column) {
    myOldName = oldName;
    myType = EVENT_RENAME;
    myColName = column.getName();
    myColumn = column;
  }

  public String getColName() {
    return myColName;
  }

  public CustomColumn getColumn() {
    return myColumn;
  }

  public String getOldName() {
    return myOldName;
  }

  public int getType() {
    return myType;
  }
}
