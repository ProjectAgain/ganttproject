/*
GanttProject is an opensource project management tool.
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
package net.sourceforge.ganttproject.model.calendar.walker;

import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.model.time.TimeDurationImpl;
import net.sourceforge.ganttproject.model.time.TimeUnit;

import java.util.Date;

/**
 * This class walks forward and counts the number of steps which start at
 * working time.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public class WorkingUnitCounter extends ForwardTimeWalker {
  private boolean isMoving = true;
  private Date myEndDate;
  private int myNonWorkingUnitCounter;
  private int myWorkingUnitCounter;

  public WorkingUnitCounter(GPCalendarCalc calendar, TimeUnit timeUnit) {
    super(calendar, timeUnit);
  }

  public TimeDuration getNonWorkingTime() {
    return new TimeDurationImpl(getTimeUnit(), myNonWorkingUnitCounter);
  }

  public TimeDuration run(Date startDate, Date endDate) {
    assert startDate != null : "null start date";
    assert endDate != null : "null end date";
    if (startDate.equals(endDate)) {
      return new TimeDurationImpl(getTimeUnit(), 0);
    }
    isMoving = true;
    myNonWorkingUnitCounter = 0;
    myWorkingUnitCounter = 0;
    myEndDate = endDate;
    walk(startDate);
    return new TimeDurationImpl(getTimeUnit(), myWorkingUnitCounter);
  }

  @Override
  protected boolean isMoving() {
    return isMoving;
  }

  @Override
  protected void processNonWorkingTime(Date intervalStart, Date workingIntervalStart) {
    myNonWorkingUnitCounter++;
    isMoving = workingIntervalStart.before(myEndDate);
  }

  @Override
  protected void processWorkingTime(Date intervalStart, Date nextIntervalStart) {
    myWorkingUnitCounter++;
    isMoving = nextIntervalStart.before(myEndDate);
  }
}
