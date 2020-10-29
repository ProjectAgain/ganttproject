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
package biz.ganttproject.core.option;

import net.sourceforge.ganttproject.util.DateParser;
import net.sourceforge.ganttproject.util.InvalidDateException;

import java.util.Date;

public class DefaultDateOption extends GPAbstractOption<Date> implements DateOption {

  public DefaultDateOption(String id) {
    super(id);
  }

  public DefaultDateOption(String id, Date initialValue) {
    super(id, initialValue);
  }

  @Override
  public String getPersistentValue() {
    return getValue() == null ? null : DateParser.getIsoDateNoHours(getValue());
  }

  @Override
  public void loadPersistentValue(String value) {
    try {
      resetValue(DateParser.parse(value), true);
    } catch (InvalidDateException e) {
      e.printStackTrace(); // To change body of catch statement use File |
                           // Settings | File Templates.
    }
  }

}
