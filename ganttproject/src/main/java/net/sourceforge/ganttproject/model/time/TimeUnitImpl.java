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

import net.sourceforge.ganttproject.model.time.TimeUnitGraph.Composition;

import java.util.Date;

/**
 * @author bard Date: 01.02.2004
 */
public class TimeUnitImpl implements TimeUnit {
  private final TimeUnit myDirectAtomUnit;
  private final TimeUnitGraph myGraph;
  private final String myName;

  public TimeUnitImpl(String name, TimeUnitGraph graph, TimeUnit directAtomUnit) {
    myName = name;
    myGraph = graph;
    myDirectAtomUnit = directAtomUnit;
  }

  @Override
  public Date adjustLeft(Date baseDate) {
    throw new UnsupportedOperationException("Time unit=" + this + " doesnt support this operation");
  }

  @Override
  public Date adjustRight(Date baseDate) {
    throw new UnsupportedOperationException("Time unit=" + this + " doesnt support this operation");
  }

  @Override
  public boolean equals(Object obj) {
    if (false == obj instanceof TimeUnitImpl) {
      return false;
    }
    TimeUnitImpl that = (TimeUnitImpl) obj;
    return this.myName.equals(that.myName);
  }

  @Override
  public int getAtomCount(TimeUnit atomUnit) {
    Composition composition = myGraph.getComposition(this, atomUnit);
    if (composition == null) {
      throw new RuntimeException("Failed to find a composition of time unit=" + this + " from time unit=" + atomUnit);
    }
    return composition.getAtomCount();
  }

  @Override
  public TimeUnit getDirectAtomUnit() {
    return myDirectAtomUnit;
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public int hashCode() {
    return myName.hashCode();
  }

  @Override
  public boolean isConstructedFrom(TimeUnit atomUnit) {
    return myGraph.getComposition(this, atomUnit) != null;
  }

  @Override
  public Date jumpLeft(Date baseDate) {
    throw new UnsupportedOperationException("Time unit=" + this + " doesnt support this operation");
  }

  @Override
  public String toString() {
    return getName() + " hash=" + hashCode();
  }
}