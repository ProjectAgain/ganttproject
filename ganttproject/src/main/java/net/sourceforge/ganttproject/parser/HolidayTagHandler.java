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
package net.sourceforge.ganttproject.parser;


import net.sourceforge.ganttproject.model.calendar.CalendarEvent;
import net.sourceforge.ganttproject.model.calendar.GPCalendar;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.util.ColorConvertion;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author nbohn
 */
public class HolidayTagHandler extends AbstractTagHandler {
  private final Logger log = getLogger(getClass());
  private static final Set<String> TAGS = ImmutableSet.of("date", "calendars");
  private final GPCalendar myCalendar;
  private final List<CalendarEvent> myEvents = Lists.newArrayList();
  private CalendarEventAttrs myAttrs;
  // We may have event titles written as comments after <date> tag.
  // To process them properly we remember the last event created from <date> tag
  // and "patch" it if we find any non-empty cdata afterwards.
  private CalendarEvent myLastEvent = null;

  private static class CalendarEventAttrs {
    final String year;
    final String month;
    final String day;
    final String type;
    final String color;

    CalendarEventAttrs(Attributes atts) {
      this.year = atts.getValue("year");
      this.month = atts.getValue("month");
      this.day = atts.getValue("date");
      this.type = atts.getValue("type");
      this.color = atts.getValue("color");
    }

    @Override
    public String toString() {
      final StringBuffer sb = new StringBuffer("CalendarEventAttrs{");
      sb.append("year='").append(year).append('\'');
      sb.append(", month='").append(month).append('\'');
      sb.append(", day='").append(day).append('\'');
      sb.append(", type='").append(type).append('\'');
      sb.append(", color='").append(color).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }
  public HolidayTagHandler(GPCalendar calendar) {
    super("date", true);
    myCalendar = calendar;
    myAttrs = null;
  }

  /**
   * @see net.sourceforge.ganttproject.parser.TagHandler#endElement(String,
   *      String, String)
   */
  @Override
  public void endElement(String namespaceURI, String sName, String qName) {
    if (!TAGS.contains(qName)) {
      return;
    }
    try {
      if ("date".equals(qName)) {
        loadHoliday(myAttrs);
      }
      if ("calendars".equals(qName)) {
        onCalendarLoaded();
      }
    } finally {
      setTagStarted(false);
    }
  }

  /**
   * @see net.sourceforge.ganttproject.parser.TagHandler#startElement(String,
   *      String, String, Attributes)
   */
  @Override
  public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
    if (!TAGS.contains(qName)) {
      return;
    }
    setTagStarted(true);
    if (qName.equals("date")) {
      processLastEvent();
      myAttrs = new CalendarEventAttrs(attrs);
    }
  }

  private void processLastEvent() {
    if (myLastEvent != null) {
      String cdata = getCdata().replaceAll("^\\p{Space}+", "").replaceAll("\\p{Space}+$", "");
      if (Strings.isNullOrEmpty(cdata)) {
        myEvents.add(myLastEvent);
      } else {
        myEvents.add(CalendarEvent.newEvent(myLastEvent.myDate, myLastEvent.isRecurring, myLastEvent.getType(), cdata, null));
        clearCdata();
      }
      myLastEvent = null;
    }
  }

  private void loadHoliday(CalendarEventAttrs atts) {
    try {
      int month = Integer.parseInt(atts.month);
      int day = Integer.parseInt(atts.day);
      CalendarEvent.Type type = Strings.isNullOrEmpty(atts.type)
             ? CalendarEvent.Type.HOLIDAY : CalendarEvent.Type.valueOf(atts.type);
      Color color = atts.color == null ? null : ColorConvertion.determineColor(atts.color);
      String description = getCdata().replaceAll("^\\p{Space}+", "").replaceAll("\\p{Space}+$", "");
      if (Strings.isNullOrEmpty(atts.year)) {
        Date date = CalendarFactory.createGanttCalendar(1, month - 1, day).getTime();
        myLastEvent = CalendarEvent.newEvent(date, true, type, description, color);
      } else {
        int year = Integer.parseInt(atts.year);
        Date date = CalendarFactory.createGanttCalendar(year, month - 1, day).getTime();
        myLastEvent = CalendarEvent.newEvent(date, false, type, description, color);
      }
      clearCdata();
    } catch (IllegalArgumentException e) {
      log.warn(String.format("Error when parsing calendar data. Raw data: %s", atts.toString()), e);
      log.error(String.format("Cannot parse a part of project file: %s", atts.toString()), e);
    }
  }

  public void onCalendarLoaded() {
    processLastEvent();
    myCalendar.setPublicHolidays(myEvents);
  }
}
