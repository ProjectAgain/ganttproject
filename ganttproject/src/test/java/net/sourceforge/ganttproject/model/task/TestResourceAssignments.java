package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.calendar.AlwaysWorkingTimeCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.resource.HumanResource;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.roles.RoleManager;
import net.sourceforge.ganttproject.model.time.TimeUnitStack;
import net.sourceforge.ganttproject.model.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.ui.gui.NotificationManager;
import net.sourceforge.ganttproject.ui.viewmodel.option.ColorOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestResourceAssignments {
    private HumanResourceManager myHumanResourceManager;
    private TaskManager myTaskManager;

    @Test
    public void testAssignmentDisappearOnResourceDeletion() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        res1.delete();
        Set<HumanResource> resources = extractResources(task);
        assertTrue(resources.isEmpty(), "It is expected that after resource deletion assignments disappear");
    }

    // See https://github.com/bardsoftware/ganttproject/issues/612
    @Test
    public void testAssignmentUpdateAndDelete() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        ResourceAssignment assignment = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentMutator mutator = task.getAssignmentCollection().createMutator();
        assignment.delete();
        assignment = mutator.addAssignment(res1);
        assignment.setLoad(50);
        assignment.delete();
        mutator.commit();

        Set<HumanResource> resources = extractResources(task);
        assertTrue(
            resources.isEmpty(),
            "It is expected that assignment is removed after sequential update+delete via mutator"
        );
    }

    @Test
    public void testAssignmentsDisappearOnSummaryTaskDeletion() {
        HumanResource res1 = getResourceManager().getById(1);
        TaskManager taskManager = getTaskManager();
        Task summaryTask = taskManager.createTask();
        summaryTask.getAssignmentCollection().addAssignment(res1);

        Task childTask = taskManager.newTaskBuilder().withParent(summaryTask).build();
        childTask.getAssignmentCollection().addAssignment(res1);

        taskManager.deleteTask(summaryTask);
        ResourceAssignment[] assignments = res1.getAssignments();
        assertTrue(
            assignments.length == 0,
            "Resource is expected to have no assignments after summary task deletion"
        );
    }

    @Test
    public void testAssignmentsDisappearOnTaskDeletion() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        task.delete();
        ResourceAssignment[] assignments = res1.getAssignments();
        assertTrue(
            assignments.length == 0,
            "Resource is expected to have no assignments after task deletion"
        );
    }

    @Test
    public void testResourceAppearsInListAfterCreation() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        HumanResource res2 = getResourceManager().getById(2);
        task.getAssignmentCollection().addAssignment(res1);
        task.getAssignmentCollection().addAssignment(res2);
        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
            Arrays.asList(res1, res2));
        assertEquals(expectedResources,
                     actualResources, "Unexpected set of resources assigned to task=" + task
        );
    }

    @Test
    public void testResourceDisappearsFromListAfterAssignmentDeletion() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        HumanResource res2 = getResourceManager().getById(2);
        task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignment asgn2 = task.getAssignmentCollection()
                                       .addAssignment(res2);

        asgn2.delete();

        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
            Arrays.asList(res1));
        assertEquals(expectedResources,
                     actualResources, "Unexpected set of resources assigned to task=" + task
        );
    }

    @Test
    public void testResourceIsNotAssignedTwice() {
        TaskManager taskManager = getTaskManager();
        Task task = taskManager.createTask();
        HumanResource res1 = getResourceManager().getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        task.getAssignmentCollection().addAssignment(res1);
        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
            Arrays.asList(res1));
        assertEquals(expectedResources,
                     actualResources, "Unexpected set of resources assigned to task=" + task
        );
    }

    @BeforeEach
    protected void setUp() {
        myHumanResourceManager = new HumanResourceManager(RoleManager.Access
                                                              .getInstance().getDefaultRole(), null);
        getResourceManager().create("test resource#1", 1);
        getResourceManager().create("test resource#2", 2);
        myTaskManager = newTaskManager();
    }

    private Set<HumanResource> extractResources(Task task) {
        Set<HumanResource> result = new HashSet<HumanResource>();
        ResourceAssignment[] assignments = task.getAssignments();
        for (int i = 0; i < assignments.length; i++) {
            ResourceAssignment next = assignments[i];
            result.add(next.getResource());
            assertEquals(task, next.getTask(), "Unexpected task is owning resource assignment="
                                               + next);
        }
        return result;
    }

    private HumanResourceManager getResourceManager() {
        return myHumanResourceManager;
    }

    private TaskManager getTaskManager() {
        return myTaskManager;
    }

    private TaskManager newTaskManager() {
        return TaskManager.Access.newInstance(null, new TaskManagerConfig() {

            @Override
            public GPCalendarCalc getCalendar() {
                return new AlwaysWorkingTimeCalendarImpl();
            }

            @Override
            public Color getDefaultColor() {
                return null;
            }

            @Override
            public ColorOption getDefaultColorOption() {
                return null;
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
                return null;
            }

            @Override
            public TimeUnitStack getTimeUnitStack() {
                return new GPTimeUnitStack();
            }
        });
    }
}
