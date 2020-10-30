package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.model.calendar.AlwaysWorkingTimeCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.roles.RoleManager;
import net.sourceforge.ganttproject.model.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.model.task.CustomColumnsManager;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.task.TaskManagerConfig;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import net.sourceforge.ganttproject.model.time.TimeUnitStack;
import net.sourceforge.ganttproject.model.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.ui.gui.NotificationManager;
import net.sourceforge.ganttproject.ui.viewmodel.option.ColorOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.DefaultColorOption;

import java.awt.*;
import java.net.URL;

public class TestSetupHelper {
    public static class TaskManagerBuilder implements TaskManagerConfig {
        private DefaultColorOption myDefaultColorOption = new DefaultColorOption("taskcolor", Color.CYAN);
        private GPCalendarCalc myGPCalendar = new AlwaysWorkingTimeCalendarImpl();
        private HumanResourceManager myResourceManager;

        private RoleManager myRoleManager;
        private TimeUnitStack myTimeUnitStack;

        public TaskManagerBuilder() {
            myTimeUnitStack = new GPTimeUnitStack();
            myRoleManager = new RoleManagerImpl();
            myResourceManager =
                new HumanResourceManager(myRoleManager.getDefaultRole(), new CustomColumnsManager(), myRoleManager);
        }

        public TaskManager build() {
            return TaskManager.Access.newInstance(null, this);
        }

        @Override
        public GPCalendarCalc getCalendar() {
            return myGPCalendar;
        }

        @Override
        public Color getDefaultColor() {
            return myDefaultColorOption.getValue();
        }

        @Override
        public ColorOption getDefaultColorOption() {
            return myDefaultColorOption;
        }

        @Override
        public NotificationManager getNotificationManager() {
            return null;
        }

        @Override
        public URL getProjectDocumentURL() {
            return null;
        }

        @Override
        public HumanResourceManager getResourceManager() {
            return myResourceManager;
        }

        @Override
        public TimeUnitStack getTimeUnitStack() {
            return myTimeUnitStack;
        }

        public TaskManagerBuilder withCalendar(GPCalendarCalc calendar) {
            myGPCalendar = calendar;
            return this;
        }
    }

    public static GanttCalendar newFriday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 15);
    }

    public static GanttCalendar newMonday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 18);
    }

    public static GanttCalendar newSaturday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 16);
    }

    public static GanttCalendar newSunday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 17);
    }

    public static TaskManagerBuilder newTaskManagerBuilder() {
        return new TaskManagerBuilder();
    }

    public static GanttCalendar newThursday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 21);
    }

    public static GanttCalendar newTuesday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 19);
    }

    public static GanttCalendar newWednesday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 20);
    }
}
