/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

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
package net.sourceforge.ganttproject.model.task.dependency;

import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishStartConstraintImpl;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSupertaskAdjustment extends TaskTestCase {
    @Test
    public void testSupertaskDurationShrinksWhenNestedTasksShrink() throws Exception {
        TaskManager taskManager = getTaskManager();
        Task supertask = taskManager.createTask();
        Task task1 = taskManager.createTask();
        Task task2 = taskManager.createTask();

        task1.move(supertask);
        task2.move(supertask);

        task1.setStart(CalendarFactory.createGanttCalendar(2000, 01, 01));
        task1.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 03));
        task2.setStart(CalendarFactory.createGanttCalendar(2000, 01, 03));
        task2.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 04));
        supertask.setStart(CalendarFactory.createGanttCalendar(2000, 01, 01));
        supertask.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 04));

        task1.setStart(CalendarFactory.createGanttCalendar(2000, 01, 02));
        task2.setStart(CalendarFactory.createGanttCalendar(2000, 01, 02));
        task2.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 03));

        taskManager.getAlgorithmCollection().getScheduler().run();

        assertEquals(CalendarFactory.createGanttCalendar(2000, 01, 02),
                     supertask.getStart(), "Unexpected start of supertask=" + supertask
        );
        assertEquals(CalendarFactory.createGanttCalendar(2000, 01, 03),
                     supertask.getEnd(), "Unexpected end of supertask=" + supertask
        );
    }

    @Test
    public void testSupetaskDurationGrowsWhenNestedTasksGrow() throws Exception {
        TaskManager taskManager = getTaskManager();
        Task supertask = taskManager.createTask();
        Task task1 = taskManager.createTask();
        Task task2 = taskManager.createTask();

        task1.move(supertask);
        task2.move(supertask);

        task1.setStart(CalendarFactory.createGanttCalendar(2000, 01, 01));
        task1.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 03));
        task2.setStart(CalendarFactory.createGanttCalendar(2000, 01, 03));
        task2.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 04));
        supertask.setStart(CalendarFactory.createGanttCalendar(2000, 01, 01));
        supertask.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 04));

        taskManager.getDependencyCollection().createDependency(
            task2, task1, new FinishStartConstraintImpl());

        task1.setEnd(CalendarFactory.createGanttCalendar(2000, 01, 04));
        taskManager.getAlgorithmCollection().getScheduler().run();

        assertEquals(CalendarFactory.createGanttCalendar(2000, 01, 01),
                     supertask.getStart(), "Unexpected start of supertask=" + supertask
        );
        assertEquals(CalendarFactory.createGanttCalendar(2000, 01, 05),
                     supertask.getEnd(), "Unexpected end of supertask=" + supertask
        );
    }

    @Test
    public void testTaskDurationChangeIsPropagatedTwoLevelsUp() throws Exception {
        TaskManager taskManager = getTaskManager();
        Task supertask = taskManager.createTask();
        supertask.move(taskManager.getRootTask());

        Task level1task1 = taskManager.createTask();
        level1task1.move(supertask);
        Task level1task2 = taskManager.createTask();
        level1task2.move(supertask);

        Task level2task1 = taskManager.createTask();
        level2task1.move(level1task2);

        supertask.setStart(TestSetupHelper.newMonday());
        supertask.setEnd(TestSetupHelper.newTuesday());
        level1task1.setStart(TestSetupHelper.newMonday());
        level1task1.setEnd(TestSetupHelper.newTuesday());
        level1task2.setStart(TestSetupHelper.newMonday());
        level1task2.setEnd(TestSetupHelper.newTuesday());
        level2task1.setStart(TestSetupHelper.newMonday());
        level2task1.setEnd(TestSetupHelper.newTuesday());

        level2task1.setEnd(TestSetupHelper.newWednesday());

        taskManager.getAlgorithmCollection().getScheduler().run();

        assertEquals(
            TestSetupHelper.newWednesday(),
            supertask.getEnd(),
            "Unexpected end of the topleveltask=" + supertask
        );
    }
}
