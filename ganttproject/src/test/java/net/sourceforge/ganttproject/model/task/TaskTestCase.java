/*
Copyright 2003-2018 Dmitry Barashev, BarD Software s.r.o

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
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import org.junit.jupiter.api.BeforeEach;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public abstract class TaskTestCase {
  private TaskManager myTaskManager;

  protected TaskDependency createDependency(Task dependant, Task dependee) throws TaskDependencyException {
    return getTaskManager().getDependencyCollection().createDependency(dependant, dependee);
  }

  protected Task createTask(GanttCalendar start) {
    return createTask(start, 1);
  }

  protected Task createTask(GanttCalendar start, int duration) {
    Task result = createTask();
    result.setStart(start);
    result.setDuration(getTaskManager().createLength(duration));
    return result;
  }

  protected Task createTask() {
    Task result = getTaskManager().createTask();
    result.move(getTaskManager().getRootTask());
    result.setName(String.valueOf(result.getTaskID()));
    return result;
  }

  protected TaskManager getTaskManager() {
    return myTaskManager;
  }

  protected void setTaskManager(TaskManager taskManager) {
    myTaskManager = taskManager;
  }

  protected TaskManager newTaskManager() {
    return TestSetupHelper.newTaskManagerBuilder().build();
  }

  @BeforeEach
  protected void setUp() throws Exception {
    myTaskManager = newTaskManager();
  }

  protected void tearDown() throws Exception {
    myTaskManager = null;
  }

  static {
    new CalendarFactory() {
      {
        setLocaleApi(new LocaleApi() {
          public Locale getLocale() {
            return Locale.US;
          }

          public DateFormat getShortDateFormat() {
            return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
          }
        });
      }
    };
  }
}
