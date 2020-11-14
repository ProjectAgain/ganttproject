/*
Copyright 2014 BarD Software s.r.o

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

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import net.projectagain.ganttplanner.app.LegacyApp;
import net.sourceforge.ganttproject.io.XmlParser;
import net.sourceforge.ganttproject.parser.AbstractTagHandler;
import net.sourceforge.ganttproject.parser.HolidayTagHandler;
import net.sourceforge.ganttproject.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Reads calendars in XML format from GanttProject's installation directory.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public class GPCalendarProvider {
  private static class CalendarTagHandler extends AbstractTagHandler {
    private final GPCalendarCalc myCalendar;
    private final HolidayTagHandler myHolidayHandler;

    CalendarTagHandler(GPCalendarCalc calendar, HolidayTagHandler holidayHandler) {
      super("calendar");
      myCalendar = calendar;
      myHolidayHandler = holidayHandler;
    }

    @Override
    protected void onEndElement() {
      myHolidayHandler.onCalendarLoaded();
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
      myCalendar.setName(attrs.getValue("name"));
      myCalendar.setID(attrs.getValue("id"));
      myCalendar.setBaseCalendarID(attrs.getValue("base-id"));
      return true;
    }
  }

  private static final Logger log = getLogger(GPCalendarProvider.class);
  private static GPCalendarProvider ourInstance;
  private final List<GPCalendar> myCalendars;

  private GPCalendarProvider(List<GPCalendar> calendars) {
    myCalendars = calendars;
  }

  public static synchronized GPCalendarProvider getInstance() {
    if (ourInstance == null) {
      List<GPCalendar> calendars = readCalendars();
      Collections.sort(calendars, Comparator.comparing(GPCalendar::getName));
      ourInstance = new GPCalendarProvider(calendars);
    }
    return ourInstance;
  }

  private static GPCalendar readCalendar(File resource) {
    WeekendCalendarImpl calendar = new WeekendCalendarImpl();

    HolidayTagHandler holidayHandler = new HolidayTagHandler(calendar);
    CalendarTagHandler calendarHandler = new CalendarTagHandler(calendar, holidayHandler);
    XmlParser parser = new XmlParser(
      ImmutableList.of(calendarHandler, holidayHandler),
      ImmutableList.of()
    );
    try {
      parser.parse(new BufferedInputStream(new FileInputStream(resource)));
      return calendar;
    } catch (IOException e) {
      log.error("Exception", e);
      return null;
    }
  }

  private static List<GPCalendar> readCalendars() {
    try {
      Resource resource = LegacyApp.getInstance().getResource("/calendar");
      if (!resource.exists()) {
        log.warn("There are no calendars defined.");
        return Collections.emptyList();
      }

      URL resolved = resource.getURL();
      File dir = new File(resolved.getFile());
      if (dir.exists() && dir.isDirectory() && dir.canRead()) {
        List<GPCalendar> calendars = Lists.newArrayList();
        for (File f: dir.listFiles()) {
          if ("calendar".equalsIgnoreCase(FileUtil.getExtension(f))) {
            try {
              GPCalendar calendar = readCalendar(f);
              if (calendar != null) {
                calendars.add(calendar);
              }
            } catch (Throwable e) {
              log.error("Failure when reading calendar file " + f.getAbsolutePath(), e);
            }
          }
        }
        return calendars;
      }
    } catch (IOException e) {
      log.error("Exception", e);
    }
    return Collections.emptyList();
  }

  public List<GPCalendar> getCalendars() {
    return myCalendars;
  }
}
