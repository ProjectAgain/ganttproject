/*
Copyright 2013 BarD Software s.r.o

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
package net.sourceforge.ganttproject.model.calendar;

import com.google.common.base.Objects;

import java.awt.*;
import java.util.Date;

/**
 * This class represents a calendar event, which roughly corresponds to VEVENT data type from iCal
 * specification. Event has a date, a summary, a type and recurrence flag. CalendarEvent objects are immutable.
 *
 * @author dbarashev
 */
public class CalendarEvent {
  /**
   * Event type. WORKING_DAY means a regular working day and assumes that tasks can be scheduled on this date.
   * HOLIDAY means non-working day and assumes that tasks can't be scheduled on that day.
   * NEUTRAL means that this event will have a type inherited from the base calendar, if any, or from
   * weekend configuration.
   */
  public enum Type {
    HOLIDAY, WORKING_DAY, NEUTRAL
  }

  public final boolean isRecurring;
  public final Date myDate;
  private final Color myColor;
  private final String myTitle;
  private final Type myType;

  CalendarEvent(Date date, boolean recurring, Type type, String title, Color color) {
    myDate = date;
    isRecurring = recurring;
    myType = type;
    myTitle = title;
    myColor = color;
  }

  public static CalendarEvent newEvent(Date date, boolean isRecurring, Type type, String title, Color color) {
    return new CalendarEvent(date, isRecurring, type, title, color);
  }

  @Override
  public boolean equals(Object obj) {
    if (false == obj instanceof CalendarEvent) {
      return false;
    }
    CalendarEvent that = (CalendarEvent) obj;
    return Objects.equal(this.myDate, that.myDate) && Objects.equal(this.isRecurring, that.isRecurring)
           && Objects.equal(this.myType, that.myType);
  }

  public Color getColor() {
    return myColor;
  }

  public String getTitle() {
    return myTitle;
  }

  public Type getType() {
    return myType;
  }

  @Override
  public int hashCode() {
    return this.myDate.hashCode();
  }

  @Override
  public String toString() {
    return "Date=" + myDate + " repeating=" + isRecurring;
  }
}
