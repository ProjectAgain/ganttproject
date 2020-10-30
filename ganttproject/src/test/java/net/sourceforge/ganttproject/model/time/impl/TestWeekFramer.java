/*
 * Created on 08.11.2004
 */
package net.sourceforge.ganttproject.model.time.impl;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author bard
 */
public class TestWeekFramer {
  static class TestCalendarFactory implements WeekFramerImpl.ICalendarFactory {
    @Override
    public Calendar newCalendar() {
      return GregorianCalendar.getInstance(Locale.UK);  // Monday is the first day of week in UK locale.
    }
  }

  @Test
  public void testAdjustLeft() {
    WeekFramerImpl framer = new WeekFramerImpl(new TestCalendarFactory());
    Date adjusted = framer.adjustLeft(newMonday());
    Calendar c = (Calendar) Calendar.getInstance().clone();
    c.setTime(adjusted);
    c.add(Calendar.MILLISECOND, -1);
    assertEquals(Calendar.SUNDAY, c
      .get(Calendar.DAY_OF_WEEK), "Unexpected day of week");

    Date adjustedSunday = framer.adjustLeft(newSunday());
    assertEquals(
      adjusted,
      adjustedSunday,
      "Adjusted sunday is expected to be equal to adjusted monday"
    );
  }

  @Test
  public void testAdjustRight() {
    WeekFramerImpl framer = new WeekFramerImpl(new TestCalendarFactory());
    Date adjustedMonday = framer.adjustRight(newMonday());
    Date adjustedSunday = framer.adjustRight(newSunday());
    assertEquals(adjustedMonday, adjustedSunday);
    Calendar c = (Calendar) Calendar.getInstance().clone();
    c.setTime(adjustedMonday);
    assertEquals(
      Calendar.MONDAY,
      c.get(Calendar.DAY_OF_WEEK),
      "Unexpected day of week"
    );
    c.add(Calendar.MILLISECOND, -1);
    assertEquals(
      Calendar.SUNDAY,
      c.get(Calendar.DAY_OF_WEEK),
      "Unexpected day of week"
    );
  }

  @Test
  public void testJumpLeft() {
    WeekFramerImpl framer = new WeekFramerImpl(new TestCalendarFactory());
    Date adjustedMonday = framer.jumpLeft(newMonday());
    Date adjustedSunday = framer.jumpLeft(newSunday());
    assertNotSame(adjustedMonday, adjustedSunday);
    Calendar c = (Calendar) Calendar.getInstance().clone();
    c.setTime(adjustedMonday);
    assertEquals(
      c.get(Calendar.DAY_OF_WEEK),
      Calendar.MONDAY,
      "Unexpected day of week, date=" + c.getTime()
    );
    assertNotSame(adjustedMonday, newMonday());
    c.setTime(adjustedSunday);
    assertEquals(Calendar.SUNDAY,
                 c.get(Calendar.DAY_OF_WEEK), "Unexpected day of week, date=" + c.getTime()
    );
    assertNotSame(adjustedMonday, newSunday());
  }

  private Date newMonday() {
    Calendar c = new TestCalendarFactory().newCalendar();
    c.clear();
    c.set(Calendar.YEAR, 2004);
    c.set(Calendar.MONTH, Calendar.NOVEMBER);
    c.set(Calendar.DAY_OF_MONTH, 8);
    return c.getTime();
  }

  private Date newSunday() {
    Calendar c = new TestCalendarFactory().newCalendar();
    c.clear();
    c.set(Calendar.YEAR, 2004);
    c.set(Calendar.MONTH, Calendar.NOVEMBER);
    c.set(Calendar.DAY_OF_MONTH, 14);
    return c.getTime();
  }
}
