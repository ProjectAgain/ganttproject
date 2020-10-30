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

import java.util.Collection;
import java.util.Date;

/**
 * Represents a project calendar in GanttProject. Allows for managing weekend days
 * and public holidays.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public interface GPCalendar {
  enum DayType {
    WORKING, NON_WORKING, WEEKEND, HOLIDAY
  }

  /**
   * Flags corresponding to particular features of a calendar day.
   * A day can be working, in the sense that tasks can run at this day,
   * or not working. At the same time, it may or may not be a weekend.
   * Weekend is normally a non-working day, however, it can be made working
   * if project owner decides to.
   */
  interface DayMask {
    int HOLIDAY = 4;
    int WEEKEND = 2;
    int WORKING = 1;
  }

  void addListener(GPCalendarListener listener);

  String getBaseCalendarID();

  void setBaseCalendarID(String id);

  int getDayMask(Date date);

  //public boolean isNonWorkingDay(Date curDayStart);

  CalendarEvent getEvent(Date date);

  String getID();

  void setID(String id);

  String getName();

  void setName(String name);

  /**
   * @return an unmodifiable collection of (public) holidays
   */
  Collection<CalendarEvent> getPublicHolidays();

  void setPublicHolidays(Collection<CalendarEvent> holidays);

  DayType getWeekDayType(int day);

  void importCalendar(GPCalendar calendar, ImportCalendarOption importOption);

  void setWeekDayType(int day, DayType type);
}
