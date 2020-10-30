/*
 LICENSE:

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.task.algorithm.RecalculateTaskCompletionPercentageAlgorithm;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class TestTaskCompletionPercentage extends TaskTestCase {
  @Test
  public void testCompletionIs0WhenAllNestedTasksNotStarted() {
    TaskManager taskManager = getTaskManager();
    Task supertask = taskManager.createTask();
    supertask.setCompletionPercentage(50);
    Task task1 = taskManager.createTask();
    Task task2 = taskManager.createTask();
    Task task3 = taskManager.createTask();
    //
    GanttCalendar commonStart = CalendarFactory.createGanttCalendar(2000, 01, 01);
    GanttCalendar commonEnd = CalendarFactory.createGanttCalendar(2000, 01, 05);
    task1.setStart(commonStart);
    task1.setEnd(commonEnd);
    task2.setStart(commonStart);
    task2.setEnd(commonEnd);
    task3.setStart(commonStart);
    task3.setEnd(commonEnd);
    //
    task1.move(supertask);
    task2.move(supertask);
    task3.move(supertask);
    //
    RecalculateTaskCompletionPercentageAlgorithm alg = taskManager.getAlgorithmCollection()
                                                                  .getRecalculateTaskCompletionPercentageAlgorithm();
    alg.run();
    assertEquals(
      0,
      supertask.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask
    );
  }

  @Test
  public void testCompletionIs100WhenAllNestedTasksCompleted() {
    TaskManager taskManager = getTaskManager();
    Task supertask = taskManager.createTask();
    supertask.setCompletionPercentage(50);
    Task task1 = taskManager.createTask();
    Task task2 = taskManager.createTask();
    Task task3 = taskManager.createTask();
    //
    GanttCalendar commonStart = CalendarFactory.createGanttCalendar(2000, 01, 01);
    GanttCalendar commonEnd = CalendarFactory.createGanttCalendar(2000, 01, 05);
    task1.setStart(commonStart);
    task1.setEnd(commonEnd);
    task2.setStart(commonStart);
    task2.setEnd(commonEnd);
    task3.setStart(commonStart);
    task3.setEnd(commonEnd);
    //
    task1.move(supertask);
    task2.move(supertask);
    task3.move(supertask);
    //
    task1.setCompletionPercentage(100);
    task2.setCompletionPercentage(100);
    task3.setCompletionPercentage(100);
    //
    RecalculateTaskCompletionPercentageAlgorithm alg = taskManager.getAlgorithmCollection()
                                                                  .getRecalculateTaskCompletionPercentageAlgorithm();
    alg.run();
    assertEquals(
      100,
      supertask.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask
    );
  }

  @Test
  public void testCompletionIs50WhenAllNestedTasksHalfCompleted() {
    TaskManager taskManager = getTaskManager();
    Task supertask = taskManager.createTask();
    supertask.setCompletionPercentage(50);
    Task task1 = taskManager.createTask();
    Task task2 = taskManager.createTask();
    Task task3 = taskManager.createTask();
    //
    GanttCalendar commonStart = CalendarFactory.createGanttCalendar(2000, 01, 01);
    GanttCalendar commonEnd = CalendarFactory.createGanttCalendar(2000, 01, 05);
    task1.setStart(commonStart);
    task1.setEnd(commonEnd);
    task2.setStart(commonStart);
    task2.setEnd(commonEnd);
    task3.setStart(commonStart);
    task3.setEnd(commonEnd);
    //
    task1.move(supertask);
    task2.move(supertask);
    task3.move(supertask);
    //
    task1.setCompletionPercentage(50);
    task2.setCompletionPercentage(50);
    task3.setCompletionPercentage(50);
    //
    RecalculateTaskCompletionPercentageAlgorithm alg = taskManager.getAlgorithmCollection()
                                                                  .getRecalculateTaskCompletionPercentageAlgorithm();
    alg.run();
    assertEquals(
      50,
      supertask.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask
    );
  }

  @Test
  public void testCompletionWithMilestones() {
    TaskManager taskManager = getTaskManager();
    Task supertask = taskManager.createTask();
    Task task1 = taskManager.createTask();
    Task task2 = taskManager.createTask();
    task2.setMilestone(true);
    //
    task1.move(supertask);
    task2.move(supertask);
    //
    task1.setCompletionPercentage(50);
    task2.setCompletionPercentage(100);
    //
    RecalculateTaskCompletionPercentageAlgorithm alg = taskManager.getAlgorithmCollection()
                                                                  .getRecalculateTaskCompletionPercentageAlgorithm();
    alg.run();
    assertEquals(
      50,
      supertask.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask
    );
  }

  @Test
  public void testCompletionWithNestedTasksDepthGreaterThan1AndGapsInCalender() {
    TaskManager taskManager = getTaskManager();

    Task project = taskManager.createTask();
    project.setProjectTask(true);
    project.move(taskManager.getRootTask());

    Task supertask = taskManager.newTaskBuilder().withParent(project).build();

    Task supertask_0 = taskManager.newTaskBuilder().withParent(supertask).build();
    taskManager.newTaskBuilder()
               .withStartDate(TestSetupHelper.newMonday().getTime())
               .withDuration(taskManager.createLength(1))
               .withCompletion(100)
               .withParent(supertask_0)
               .build();
    taskManager.newTaskBuilder()
               .withStartDate(TestSetupHelper.newFriday().getTime())
               .withDuration(taskManager.createLength(1))
               .withParent(supertask_0)
               .build();

    Task supertask_1 = taskManager.newTaskBuilder().withParent(supertask).build();
    taskManager.newTaskBuilder()
               .withStartDate(TestSetupHelper.newMonday().getTime())
               .withDuration(taskManager.createLength(1))
               .withCompletion(50)
               .withParent(supertask_1)
               .build();
    taskManager.newTaskBuilder()
               .withStartDate(TestSetupHelper.newWednesday().getTime())
               .withDuration(taskManager.createLength(1))
               .withParent(supertask_1)
               .build();
    //
    RecalculateTaskCompletionPercentageAlgorithm alg = taskManager.getAlgorithmCollection()
                                                                  .getRecalculateTaskCompletionPercentageAlgorithm();
    alg.run();

    // half a day of two
    assertEquals(
      25,
      supertask_1.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask_1
    );
    // one day of two
    assertEquals(
      50,
      supertask_0.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask_0
    );
    // 1.5 days of 4 = 3/8 = 37%
    assertEquals(
      37,
      supertask.getCompletionPercentage(),
      "Unexpected completion percentage of supertask=" + supertask
    );
    assertEquals(
      37,
      project.getCompletionPercentage(),
      "Unexpected completion percentage of project=" + project
    );
  }
}
