/*
 * Created on 18.10.2004
 */
package net.sourceforge.ganttproject.test.task.calendar;

import com.google.common.collect.ImmutableList;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.calendar.CalendarEvent;
import net.sourceforge.ganttproject.model.calendar.GPCalendar;
import net.sourceforge.ganttproject.model.calendar.WeekendCalendarImpl;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.impl.GregorianTimeUnitStack;
import net.sourceforge.ganttproject.test.task.TaskTestCase;

import java.text.DateFormat;
import java.util.Locale;

/**
 * @author bard
 */
public class TestWeekendCalendar extends TaskTestCase {
  static {
    new CalendarFactory() {
      {
        setLocaleApi(new LocaleApi() {
          @Override
          public Locale getLocale() {
            return Locale.US;
          }

          @Override
          public DateFormat getShortDateFormat() {
            return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
          }
        });
      }
    };
  }

  public void testTaskOverlappingWeekendIsTwoDaysShorter() {
    Task t = getTaskManager().createTask();
    t.setStart(TestSetupHelper.newFriday());// Friday
    t.setEnd(TestSetupHelper.newTuesday()); // Tuesday
    assertEquals("Unexpected length of task=" + t
        + " which overlaps weekend", 2f, t.getDuration().getLength(
        GregorianTimeUnitStack.DAY), 0.1);
  }

  private WeekendCalendarImpl myWeekendCalendar = new WeekendCalendarImpl();

  @Override
  protected TaskManager newTaskManager() {
    return TestSetupHelper.newTaskManagerBuilder().withCalendar(myWeekendCalendar).build();
  }

    public void testNoWeekendsButHasHolidays() {
        WeekendCalendarImpl noWeekendsOneHolidayCalendar = new WeekendCalendarImpl();
        for (int i=1; i<=7; i++) {
            noWeekendsOneHolidayCalendar.setWeekDayType(i, GPCalendar.DayType.WORKING);
        }
        noWeekendsOneHolidayCalendar.setPublicHolidays(ImmutableList.of(
            CalendarEvent.newEvent(TestSetupHelper.newMonday().getTime(), false, CalendarEvent.Type.HOLIDAY, null, null)));
        TaskManager mgr = TestSetupHelper.newTaskManagerBuilder().withCalendar(noWeekendsOneHolidayCalendar).build();
        Task t = mgr.newTaskBuilder()
            .withStartDate(TestSetupHelper.newFriday().getTime())
            .build();
        t.setEnd(TestSetupHelper.newWendesday());
        assertEquals(4.0f, t.getDuration().getLength(GregorianTimeUnitStack.DAY));
  }
}
