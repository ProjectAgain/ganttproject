package net.sourceforge.ganttproject.model.task.event;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.task.TaskMutator;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import net.sourceforge.ganttproject.model.time.impl.GregorianTimeUnitStack;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class TestTaskScheduleEvent extends TaskTestCase {
    private static class TaskListenerImpl extends TaskListenerAdapter {
        private boolean hasBeenCalled;

        public TaskListenerImpl(TaskManager taskManager) {
            super();
        }

        @Override
        public void taskScheduleChanged(TaskScheduleEvent e) {
            hasBeenCalled = true;
        }

        boolean hasBeenCalled() {
            return hasBeenCalled;
        }
    }

    @Test
    public void testTaskScheduleEventIsSendOnTaskDurationChange() {
        TaskManager taskManager = getTaskManager();
        Task task1 = taskManager.createTask();
        TaskListenerImpl listener = new TaskListenerImpl(taskManager);
        taskManager.addTaskListener(listener);
        TaskMutator mutator = task1.createMutator();
        mutator.setDuration(taskManager.createLength(
            GregorianTimeUnitStack.DAY,
            5
        ));
        mutator.commit();
        assertTrue(
            listener.hasBeenCalled(),
            "Listener has not been called on task duration change"
        );
    }

    @Test
    public void testTaskScheduleEventIsSendOnTaskEndChange() {
        TaskManager taskManager = getTaskManager();
        Task task1 = taskManager.createTask();
        TaskListenerImpl listener = new TaskListenerImpl(taskManager);
        taskManager.addTaskListener(listener);
        GanttCalendar taskEnd = task1.getEnd().clone();
        taskEnd.add(Calendar.DATE, 1);
        TaskMutator mutator = task1.createMutator();
        mutator.setEnd(taskEnd);
        mutator.commit();
        assertTrue(
            listener.hasBeenCalled(),
            "Listener has not been called on task end change"
        );
    }

    @Test
    public void testTaskScheduleEventIsSendOnTaskStartChange() {
        TaskManager taskManager = getTaskManager();
        Task task1 = taskManager.createTask();
        TaskListenerImpl listener = new TaskListenerImpl(taskManager);
        taskManager.addTaskListener(listener);
        GanttCalendar taskStart = task1.getStart().clone();
        taskStart.add(Calendar.DATE, -1);
        TaskMutator mutator = task1.createMutator();
        mutator.setStart(taskStart);
        mutator.commit();
        assertTrue(
            listener.hasBeenCalled(),
            "Listener has not been called on task start change"
        );
    }
}
