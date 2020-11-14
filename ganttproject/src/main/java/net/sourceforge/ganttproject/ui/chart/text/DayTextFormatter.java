/*
Copyright 2003-2012 Dmitry Barashev, GanttProject Team

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
package net.sourceforge.ganttproject.ui.chart.text;

import net.sourceforge.ganttproject.model.calendar.GPCalendar;
import net.sourceforge.ganttproject.ui.chart.grid.Offset;

import java.text.MessageFormat;
import java.util.Date;



public class DayTextFormatter extends CachingTextFormatter implements TimeFormatter {

  @Override
  protected TimeUnitText[] createTimeUnitText(Date adjustedLeft) {
    return new TimeUnitText[] { new TimeUnitText(MessageFormat.format("{0}",
        new Object[] { "" + adjustedLeft.getDate() })) };
  }

  @Override
  public TimeUnitText[] format(Offset curOffset) {
    if ((curOffset.getDayMask() & (GPCalendar.DayMask.WORKING)) == 0) {
      return TimeFormatters.EMPTY_TEXT;
    }
    return super.format(curOffset);
  }
}