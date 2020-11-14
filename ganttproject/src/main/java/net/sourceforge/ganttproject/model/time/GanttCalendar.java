/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2011 Thomas Alexandre, GanttProject Team

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
package net.sourceforge.ganttproject.model.time;

import net.sourceforge.ganttproject.model.time.CalendarFactory.LocaleApi;
import net.sourceforge.ganttproject.model.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.util.DateParser;
import net.sourceforge.ganttproject.util.InvalidDateException;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Class use for calendar
 */
public class GanttCalendar extends java.util.GregorianCalendar {
  final static private int[] comparissons = {Calendar.YEAR, Calendar.MONTH, Calendar.DATE};
  public static Comparator<GanttCalendar> COMPARATOR = new Comparator<GanttCalendar>() {
    @Override
    public int compare(GanttCalendar o1, GanttCalendar o2) {
      return o1.compareTo(o2);
    }
  };
  private final LocaleApi myLocaleApi;
  private GanttCalendar myShiftedValue;

  GanttCalendar(CalendarFactory.LocaleApi localeApi) {
    super();
    set(Calendar.HOUR_OF_DAY, 0);
    set(Calendar.MINUTE, 0);
    set(Calendar.SECOND, 0);
    set(Calendar.MILLISECOND, 0);
    myLocaleApi = localeApi;
  }

  GanttCalendar(int year, int month, int date, CalendarFactory.LocaleApi localeApi) {
    super(year, month, date);
    myLocaleApi = localeApi;
  }

  GanttCalendar(Date date, CalendarFactory.LocaleApi localeApi) {
    super();
    myLocaleApi = localeApi;
    setTime(date);
  }

  public static GanttCalendar parseXMLDate(String s) {
    GanttCalendar result = CalendarFactory.createGanttCalendar();
    result.clear();
    try {
      Date date = DateParser.parse(s);
      result.setTime(date);
    } catch (InvalidDateException e) {
      // Get "/" characters
      int fb = s.indexOf('/');
      int sb = s.indexOf('/', fb + 1);
      // Get all fields
      String d = s.substring(0, fb);
      String m = s.substring(fb + 1, sb);
      String y = s.substring(sb + 1);

      // Set the date
      result.set(Calendar.YEAR, Integer.parseInt(y));
      result.set(Calendar.MONTH, Integer.parseInt(m) - 1);
      result.set(Calendar.DATE, Integer.parseInt(d));
    }
    return result;
  }

  /**
   * @return a copy of the current date
   */
  @Override
  public GanttCalendar clone() {
    GanttCalendar clone = CalendarFactory.createGanttCalendar(getYear(), getMonth(), getDay());
    return clone;
  }

  /**
   * This function compares the calendar date with the given date
   *
   * @return 0 If the two date are equals<br/>
   * -1 if the date is before 'when'<br/>
   * 1 if the date is after 'when'
   */
  public int compareTo(GanttCalendar when) {
    for (int comparisson: comparissons) {
      switch (module(this.get(comparisson) - when.get(comparisson))) {
        case -1:
          return -1;
        case 1:
          return 1;
      }
    }
    return 0;
  }

  /**
   * @return true if the calendar date equals to 'when'
   */
  public boolean equals(GanttCalendar when) {
    return getYear() == when.getYear() && getMonth() == when.getMonth() && getDay() == when.getDay();
  }

  public int getDate() {
    return this.get(Calendar.DATE);
  }

  public int getDay() {
    return this.get(Calendar.DAY_OF_MONTH);
  }

  public GanttCalendar getDisplayValue() {
    if (myShiftedValue == null) {
      myShiftedValue = CalendarFactory.createGanttCalendar(GPTimeUnitStack.DAY.jumpLeft(getTime()));
    }
    return myShiftedValue;
  }

  public int getMonth() {
    return this.get(Calendar.MONTH);
  }

  public int getYear() {
    return this.get(Calendar.YEAR);
  }

  @Override
  public String toString() {
    return myLocaleApi.getShortDateFormat().format(getTime());
  }

//  /** @return the actually date */
//  public static String getDateAndTime() {
//    GanttCalendar c = new GanttCalendar();
//    return c.toString() + " - " + GanttLanguage.getInstance().formatTime(c);
//  }

  public String toXMLString() {
    return DateParser.getIsoDateNoHours(getTime());
  }

  /**
   * @return the sign represented by an integer
   */
  private int module(int number) {
    if (number > 0) {
      return 1;
    } else if (number < 0) {
      return -1;
    } else {
      return 0;
    }
  }
}