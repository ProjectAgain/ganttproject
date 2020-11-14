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
package net.sourceforge.ganttproject.model.task.algorithm;

import net.sourceforge.ganttproject.model.task.TaskManagerImpl;
import net.sourceforge.ganttproject.ui.chart.scene.gantt.ChartBoundsAlgorithm;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class AlgorithmCollection {
  private final AdjustTaskBoundsAlgorithm myAdjustTaskBoundsAlgorithm;
  private final RecalculateTaskCompletionPercentageAlgorithm myCompletionPercentageAlgorithm;
  private final CriticalPathAlgorithm myCriticalPathAlgorithm;
  private final FindPossibleDependeesAlgorithm myFindPossibleDependeesAlgorithm;
  private final ChartBoundsAlgorithm myProjectBoundsAlgorithm;
  private final RecalculateTaskScheduleAlgorithm myRecalculateTaskScheduleAlgorithm;
  private final SchedulerImpl myScheduler;
  private final ShiftTaskTreeAlgorithm myShiftTaskTreeAlgorithm;

  public AlgorithmCollection(
    TaskManagerImpl taskManager,
    FindPossibleDependeesAlgorithm myFindPossibleDependeesAlgorithm,
    RecalculateTaskScheduleAlgorithm recalculateTaskScheduleAlgorithm,
    AdjustTaskBoundsAlgorithm adjustTaskBoundsAlgorithm,
    RecalculateTaskCompletionPercentageAlgorithm completionPercentageAlgorithm,
    ChartBoundsAlgorithm projectBoundsAlgorithm, CriticalPathAlgorithm criticalPathAlgorithm, SchedulerImpl scheduler
  ) {
    myScheduler = scheduler;
    this.myFindPossibleDependeesAlgorithm = myFindPossibleDependeesAlgorithm;
    myRecalculateTaskScheduleAlgorithm = recalculateTaskScheduleAlgorithm;
    myAdjustTaskBoundsAlgorithm = adjustTaskBoundsAlgorithm;
    myCompletionPercentageAlgorithm = completionPercentageAlgorithm;
    myProjectBoundsAlgorithm = projectBoundsAlgorithm;
    myShiftTaskTreeAlgorithm = new ShiftTaskTreeAlgorithm(taskManager, recalculateTaskScheduleAlgorithm);
    myCriticalPathAlgorithm = criticalPathAlgorithm;
  }

  public AdjustTaskBoundsAlgorithm getAdjustTaskBoundsAlgorithm() {
    return myAdjustTaskBoundsAlgorithm;
  }

  public CriticalPathAlgorithm getCriticalPathAlgorithm() {
    return myCriticalPathAlgorithm;
  }

  public FindPossibleDependeesAlgorithm getFindPossibleDependeesAlgorithm() {
    return myFindPossibleDependeesAlgorithm;
  }

  public ChartBoundsAlgorithm getProjectBoundsAlgorithm() {
    return myProjectBoundsAlgorithm;
  }

  public RecalculateTaskCompletionPercentageAlgorithm getRecalculateTaskCompletionPercentageAlgorithm() {
    return myCompletionPercentageAlgorithm;
  }

  public RecalculateTaskScheduleAlgorithm getRecalculateTaskScheduleAlgorithm() {
    return myRecalculateTaskScheduleAlgorithm;
  }

  public AlgorithmBase getScheduler() {
    return myScheduler;
  }

  public ShiftTaskTreeAlgorithm getShiftTaskTreeAlgorithm() {
    return myShiftTaskTreeAlgorithm;
  }
}