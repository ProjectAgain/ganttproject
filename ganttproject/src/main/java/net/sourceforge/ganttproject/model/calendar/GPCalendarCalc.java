package net.sourceforge.ganttproject.model.calendar;

import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.model.time.TimeUnit;

import java.util.Date;
import java.util.List;

public interface GPCalendarCalc extends GPCalendar {
  enum MoveDirection {
    FORWARD, BACKWARD
  }

  String EXTENSION_POINT_ID = "net.sourceforge.ganttproject.calendar";
  GPCalendarCalc PLAIN = new AlwaysWorkingTimeCalendarImpl();

  GPCalendarCalc copy();

  Date findClosest(Date time, TimeUnit timeUnit, MoveDirection direction, DayType dayType, Date limit);

  Date findClosest(Date time, TimeUnit timeUnit, MoveDirection direction, DayType dayType);

  Date findClosestWorkingTime(Date time);

  List<GPCalendarActivity> getActivities(Date startDate, Date endDate);

  List<GPCalendarActivity> getActivities(Date startDate, TimeUnit timeUnit, long l);

  /**
   * @return true when weekends are only shown and taken into account for the
   * task scheduling.
   */
  boolean getOnlyShowWeekends();

  /**
   * @param onlyShowWeekends must be set to true if weekends are only shown and not taken into
   *                         account for the task scheduling
   */
  void setOnlyShowWeekends(boolean onlyShowWeekends);

  /**
   * Adds <code>shift</code> period to <code>input</code> date taking into
   * account this calendar working/non-working time If input date corresponds to
   * Friday midnight and this calendar if configured to have a weekend on
   * Saturday and Sunday then adding a shift of "1 day" will result to the
   * midnight of the next Monday
   */
  Date shiftDate(Date input, TimeDuration shift);
}
