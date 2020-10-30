/*
GanttProject is an opensource project management tool.
Copyright (C) 2010 Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package net.sourceforge.ganttproject.test.task.calendar;

import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.calendar.CalendarEvent;
import net.sourceforge.ganttproject.model.calendar.GPCalendar;
import net.sourceforge.ganttproject.model.calendar.WeekendCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.walker.WorkingUnitCounter;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.model.time.impl.GregorianTimeUnitStack;
import net.sourceforge.ganttproject.test.task.TaskTestCase;

import java.util.Calendar;

public class TestWorkingUnitCounter extends TaskTestCase {
    public void testWorkingDaysChunk() {
        WeekendCalendarImpl calendar = new WeekendCalendarImpl();
        calendar.setWeekDayType(Calendar.SATURDAY, GPCalendar.DayType.WEEKEND);
        calendar.setWeekDayType(Calendar.SUNDAY, GPCalendar.DayType.WEEKEND);

        WorkingUnitCounter counter = new WorkingUnitCounter(
                calendar, GregorianTimeUnitStack.DAY);
        TimeDuration result = counter.run(TestSetupHelper.newMonday().getTime(), TestSetupHelper.newThursday().getTime());
        Assert.assertEquals(3, result.getLength());
        Assert.assertEquals(GregorianTimeUnitStack.DAY, result.getTimeUnit());
    }

    public void testChunkAcrossWeekend() {
        WeekendCalendarImpl calendar = new WeekendCalendarImpl();
        calendar.setWeekDayType(Calendar.SATURDAY, GPCalendar.DayType.WEEKEND);
        calendar.setWeekDayType(Calendar.SUNDAY, GPCalendar.DayType.WEEKEND);

        WorkingUnitCounter counter = new WorkingUnitCounter(
                calendar, GregorianTimeUnitStack.DAY);
        TimeDuration result = counter.run(TestSetupHelper.newFriday().getTime(), TestSetupHelper.newTuesday().getTime());
        Assert.assertEquals(2, result.getLength());
        Assert.assertEquals(GregorianTimeUnitStack.DAY, result.getTimeUnit());
    }

    public void testChunkAcrossHolidays() {
        WeekendCalendarImpl calendar = new WeekendCalendarImpl();
        calendar.setWeekDayType(Calendar.SATURDAY, GPCalendar.DayType.WEEKEND);
        calendar.setWeekDayType(Calendar.SUNDAY, GPCalendar.DayType.WEEKEND);
        calendar.setPublicHolidays(ImmutableList.of(CalendarEvent.newEvent(TestSetupHelper.newTuesday().getTime(), false, CalendarEvent.Type.HOLIDAY, null, null)));

        WorkingUnitCounter counter = new WorkingUnitCounter(
                calendar, GregorianTimeUnitStack.DAY);
        TimeDuration result = counter.run(TestSetupHelper.newMonday().getTime(), TestSetupHelper.newThursday().getTime());
        Assert.assertEquals(2, result.getLength());
        Assert.assertEquals(GregorianTimeUnitStack.DAY, result.getTimeUnit());
    }
}
