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
package net.sourceforge.ganttproject.model.time;

/**
 * Created by IntelliJ IDEA.
 *
 * @author bard Date: 31.01.2004
 */
public interface TimeUnit extends DateFrameable {
  int DAY = 0;

  /**
   * @return number of atoms used to create current TimeUnit
   *
   * @throws UnsupportedOperationException if current TimeUnit does not have constant number of atoms
   */
  int getAtomCount(TimeUnit atomUnit);

  /**
   * @return the TimeUnit which is used to build the current TimeUnit
   */
  TimeUnit getDirectAtomUnit();

  String getName();

  boolean isConstructedFrom(TimeUnit unit);
}
