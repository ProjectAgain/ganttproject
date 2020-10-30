/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2010 Dmitry Barashev

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

import net.sourceforge.ganttproject.model.time.GanttCalendar;

/**
 * @author nbohn
 */
public class GanttPreviousStateTask {

  private final boolean hasNested;
  private final boolean isMilestone;
  private final int myDuration;
  private final int myId;
  private final GanttCalendar myStart;

  public GanttPreviousStateTask(int id, GanttCalendar start, int duration, boolean isMilestone, boolean hasNested) {
    myId = id;
    myStart = start;
    myDuration = duration;
    this.isMilestone = isMilestone;
    this.hasNested = hasNested;
  }

  public int getDuration() {
    return myDuration;
  }

  public int getId() {
    return myId;
  }

  public GanttCalendar getStart() {
    return myStart;
  }

  public boolean hasNested() {
    return hasNested;
  }

  public boolean isMilestone() {
    return isMilestone;
  }
}
