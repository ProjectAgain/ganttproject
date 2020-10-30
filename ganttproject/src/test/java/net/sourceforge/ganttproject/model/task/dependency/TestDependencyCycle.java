/*
 * Created on 12.11.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.ganttproject.model.task.dependency;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDependencyCycle extends TaskTestCase {
    @Test
    public void testDependencyTargetedToNestedTask() throws Exception {
        Task supertask = getTaskManager().createTask();
        Task nestedTask = getTaskManager().createTask();
        nestedTask.move(supertask);
        assertIsLooping(supertask, nestedTask);
    }

    @Test
    public void testLoopingDependencyTargetedAtSupertask() throws Exception {
        Task supertask = getTaskManager().createTask();
        supertask.setName("supertask");
        Task nestedTask = getTaskManager().createTask();
        nestedTask.setName("nestedtask");
        nestedTask.move(supertask);
        Task dependantTask = getTaskManager().createTask();
        dependantTask.setName("dependanttask");
        getTaskManager().getDependencyCollection().createDependency(dependantTask, nestedTask);
        assertIsLooping(supertask, dependantTask);
    }

    @Test
    public void testSimpleCycle() throws Exception {
        Task dependant = getTaskManager().createTask();
        Task dependee = getTaskManager().createTask();
        getTaskManager().getDependencyCollection().createDependency(dependant, dependee);
        assertIsLooping(dependee, dependant);
    }

    private void assertIsLooping(Task dependant, Task dependee) {
        boolean loopCreated = true;
        try {
            TaskDependency loopingDependency =
                getTaskManager().getDependencyCollection().createDependency(dependant, dependee);
            assertNotNull(loopingDependency, "Either exception is thrown, or result is not null");
        } catch (TaskDependencyException e) {
            // An exception is thrown if the loop is prevented/detected (which is good behavior)
            loopCreated = false;
        }
        assertFalse(loopCreated, "Dependency loop has been successfully created...");
    }
}
