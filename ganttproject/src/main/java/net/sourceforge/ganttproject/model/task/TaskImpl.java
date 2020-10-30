/*
GanttProject is an opensource project management tool.
Copyright (C) 2004-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.model.task;

import com.google.common.collect.ImmutableList;
import net.sourceforge.ganttproject.chart.MilestoneTaskFakeActivity;
import net.sourceforge.ganttproject.model.calendar.AlwaysWorkingTimeCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.GPCalendar.DayMask;
import net.sourceforge.ganttproject.model.calendar.GPCalendar.DayType;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc.MoveDirection;
import net.sourceforge.ganttproject.model.document.AbstractURLDocument;
import net.sourceforge.ganttproject.model.document.Document;
import net.sourceforge.ganttproject.model.task.algorithm.AlgorithmCollection;
import net.sourceforge.ganttproject.model.task.algorithm.AlgorithmException;
import net.sourceforge.ganttproject.model.task.algorithm.CostAlgorithmImpl;
import net.sourceforge.ganttproject.model.task.algorithm.ShiftTaskTreeAlgorithm;
import net.sourceforge.ganttproject.model.task.dependency.*;
import net.sourceforge.ganttproject.model.task.hierarchy.TaskHierarchyItem;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.model.time.TimeDurationImpl;
import net.sourceforge.ganttproject.model.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.ui.chart.render.ShapePaint;
import net.sourceforge.ganttproject.util.collect.Pair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author bard
 */
public class TaskImpl implements Task {
  private static class FieldChange {
    EventSender myEventSender;
    Object myFieldValue;
    Object myOldValue;

    public void setOldValue(Object oldValue) {
      myOldValue = oldValue;
    }

    void setValue(Object newValue) {
      myFieldValue = newValue;
      myEventSender.enable();
    }
  }

  private interface EventSender {
    void enable();

    void fireEvent();
  }

  class MutatorException extends RuntimeException {
    public MutatorException(String msg) {
      super(msg);
    }
  }

  private class ProgressEventSender implements EventSender {
    private boolean myEnabled;

    @Override
    public void enable() {
      myEnabled = true;
    }

    @Override
    public void fireEvent() {
      if (myEnabled) {
        myManager.fireTaskProgressChanged(TaskImpl.this);
      }
      myEnabled = false;
    }
  }

  private class PropertiesEventSender implements EventSender {
    private boolean myEnabled;

    @Override
    public void enable() {
      myEnabled = true;
    }

    @Override
    public void fireEvent() {
      if (myEnabled) {
        myManager.fireTaskPropertiesChanged(TaskImpl.this);
      }
      myEnabled = false;
    }
  }

  private class MutatorImpl implements TaskMutator {
    public final Exception myException = new Exception();
    private final List<Runnable> myCommands = new ArrayList<Runnable>();
    private List<TaskActivity> myActivities;
    private FieldChange myCompletionPercentageChange;
    private FieldChange myDurationChange;
    private FieldChange myEndChange;
    private int myIsolationLevel;
    private final EventSender myProgressEventSender = new ProgressEventSender();
    private final EventSender myPropertiesEventSender = new PropertiesEventSender();
    private Pair<FieldChange, FieldChange> myShiftChange;
    private FieldChange myStartChange;
    private FieldChange myThirdChange;

    @Override
    public void addNotes(final String notes) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.addNotes(notes);
        }
      });
    }

    @Override
    public void commit() {
      try {
        if (myStartChange != null) {
          GanttCalendar start = getStart();
          TaskImpl.this.setStart(start);
        }
        if (myDurationChange != null) {
          TimeDuration duration = getDuration();
          TaskImpl.this.setDuration(duration);
          myEndChange = null;
        }
        if (myCompletionPercentageChange != null) {
          int newValue = getCompletionPercentage();
          TaskImpl.this.setCompletionPercentage(newValue);
        }
        if (myEndChange != null) {
          GanttCalendar end = getEnd();
          if (end.getTime().compareTo(TaskImpl.this.getStart().getTime()) > 0) {
            TaskImpl.this.setEnd(end);
          }
        }
        if (myThirdChange != null) {
          GanttCalendar third = getThird();
          TaskImpl.this.setThirdDate(third);
        }
        for (Runnable command: myCommands) {
          command.run();
        }
        myCommands.clear();
        myPropertiesEventSender.fireEvent();
        myProgressEventSender.fireEvent();
      } finally {
        TaskImpl.this.myMutator = null;
      }
      if (myStartChange != null && TaskImpl.this.isSupertask()) {
        TaskImpl.this.adjustNestedTasks();
      }
      if ((myStartChange != null || myEndChange != null || myDurationChange != null || myShiftChange != null ||
           myThirdChange != null) && areEventsEnabled())
      {
        GanttCalendar oldStart;
        if (myStartChange != null) {
          oldStart = (GanttCalendar) myStartChange.myOldValue;
        } else if (myShiftChange != null) {
          oldStart = (GanttCalendar) myShiftChange.first().myOldValue;
        } else {
          oldStart = TaskImpl.this.getStart();
        }
        GanttCalendar oldEnd;
        if (myEndChange != null) {
          oldEnd = (GanttCalendar) myEndChange.myOldValue;
        } else if (myShiftChange != null) {
          oldEnd = (GanttCalendar) myShiftChange.second().myOldValue;
        } else {
          oldEnd = TaskImpl.this.getEnd();
        }
        myManager.fireTaskScheduleChanged(TaskImpl.this, oldStart, oldEnd);
      }
    }

    public List<TaskActivity> getActivities() {
      if (myActivities == null && (myStartChange != null) || (myDurationChange != null)) {
        myActivities = new ArrayList<TaskActivity>();
        TaskImpl.recalculateActivities(myManager.getConfig().getCalendar(), TaskImpl.this, myActivities,
                                       getStart().getTime(), TaskImpl.this.getEnd().getTime()
        );
      }
      return myActivities;
    }

    @Override
    public int getCompletionPercentage() {
      return myCompletionPercentageChange == null ? TaskImpl.this.myCompletionPercentage
                                                  : ((Integer) myCompletionPercentageChange.myFieldValue).intValue();
    }

    @Override
    public void setCompletionPercentage(final int percentage) {
      if (myCompletionPercentageChange == null) {
        myCompletionPercentageChange = new FieldChange();
        myCompletionPercentageChange.myEventSender = myProgressEventSender;
      }
      myCompletionPercentageChange.setValue(new Integer(percentage));
    }

    public GanttCalendar getThird() {
      return myThirdChange == null ? TaskImpl.this.myThird : (GanttCalendar) myThirdChange.myFieldValue;
    }

    @Override
    public void setColor(final Color color) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setColor(color);
        }
      });
    }

    @Override
    public void setCritical(final boolean critical) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setCritical(critical);
        }
      });
    }

    @Override
    public void setExpand(final boolean expand) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setExpand(expand);
        }
      });
    }

    @Override
    public void setIsolationLevel(int level) {
      myIsolationLevel = level;
    }

    @Override
    public void setMilestone(final boolean milestone) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setMilestone(milestone);
        }
      });
    }

    @Override
    public void setName(final String name) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setName(name);
        }
      });
    }

    @Override
    public void setNotes(final String notes) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setNotes(notes);
        }
      });
    }

    @Override
    public void setPriority(final Priority priority) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setPriority(priority);
        }
      });
    }

    @Override
    public void setProjectTask(final boolean projectTask) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setProjectTask(projectTask);
        }
      });
    }

    @Override
    public void setShape(final ShapePaint shape) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setShape(shape);
        }
      });
    }

    @Override
    public void setTaskInfo(TaskInfo taskInfo) {
      myTaskInfo = taskInfo;
    }

    @Override
    public void setThird(final GanttCalendar third, final int thirdDateConstraint) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setThirdDateConstraint(thirdDateConstraint);
        }
      });
      if (myThirdChange == null) {
        myThirdChange = new FieldChange();
        myThirdChange.myEventSender = myPropertiesEventSender;
      }
      myThirdChange.setValue(third);
      myActivities = null;
    }

    @Override
    public void setWebLink(final String webLink) {
      myCommands.add(new Runnable() {
        @Override
        public void run() {
          TaskImpl.this.setWebLink(webLink);
        }
      });
    }

    @Override
    public void shift(float unitCount) {
      Task result = getPrecomputedShift(unitCount);
      if (result == null) {
        result = TaskImpl.this.shift(unitCount);
        cachePrecomputedShift(result, unitCount);
      }

      setStart(result.getStart());
      setDuration(result.getDuration());
      setEnd(result.getEnd());
    }

    @Override
    public void shift(TimeDuration shift) {
      if (myShiftChange == null) {
        myShiftChange = Pair.create(new FieldChange(), new FieldChange());
        myShiftChange.first().setOldValue(TaskImpl.this.myStart);
        myShiftChange.second().setOldValue(TaskImpl.this.myEnd);
      }
      ShiftTaskTreeAlgorithm shiftAlgorithm = new ShiftTaskTreeAlgorithm(myManager, null);
      try {
        shiftAlgorithm.run(TaskImpl.this, shift, ShiftTaskTreeAlgorithm.DEEP);
      } catch (AlgorithmException e) {
        log.error("Exception", e);
      }
    }

    TimeDuration getDuration() {
      return myDurationChange == null ? TaskImpl.this.myLength : (TimeDuration) myDurationChange.myFieldValue;
    }

    @Override
    public void setDuration(final TimeDuration length) {
      // If duration of task was set to 0 or less do not change it
      if (length.getLength() <= 0) {
        return;
      }

      if (myDurationChange == null) {
        myDurationChange = new FieldChange();
        myDurationChange.myEventSender = myPropertiesEventSender;
        myDurationChange.setValue(length);
      } else {
        TimeDuration currentLength = (TimeDuration) myDurationChange.myFieldValue;
        if (currentLength.getLength() - length.getLength() == 0) {
          return;
        }
      }

      myDurationChange.setValue(length);
      Date shifted = TaskImpl.this.shiftDate(getStart().getTime(), length);
      GanttCalendar newEnd = CalendarFactory.createGanttCalendar(shifted);
      setEnd(newEnd);
      myActivities = null;
    }

    GanttCalendar getEnd() {
      return myEndChange == null ? null : (GanttCalendar) myEndChange.myFieldValue;
    }

    @Override
    public void setEnd(final GanttCalendar end) {
      if (myEndChange == null) {
        myEndChange = new FieldChange();
        myEndChange.myEventSender = myPropertiesEventSender;
      }
      myEndChange.setOldValue(TaskImpl.this.myEnd);
      myEndChange.setValue(end);
      myActivities = null;
    }

    GanttCalendar getStart() {
      return myStartChange == null ? TaskImpl.this.myStart : (GanttCalendar) myStartChange.myFieldValue;
    }

    @Override
    public void setStart(final GanttCalendar start) {
      assert start != null;
      GanttCalendar currentStart = getStart();
      if (currentStart != null && start.equals(currentStart)) {
        return;
      }
      if (myStartChange == null) {
        myStartChange = new FieldChange();
        myStartChange.myEventSender = myPropertiesEventSender;
      }
      myStartChange.setOldValue(TaskImpl.this.myStart);
      myStartChange.setValue(start);
      myActivities = null;
    }

    private void cachePrecomputedShift(Task result, float unitCount) {
      // TODO Implement cache
    }

    private Task getPrecomputedShift(float unitCount) {
      // TODO Use cache to grab value
      return null;
    }
  }

  private class CostImpl implements Cost {
    private boolean isCalculated = true;
    private BigDecimal myValue = BigDecimal.ZERO;

    @Override
    public BigDecimal getCalculatedValue() {
      return new CostAlgorithmImpl().getCalculatedCost(TaskImpl.this);
    }

    @Override
    public BigDecimal getManualValue() {
      return myValue;
    }

    @Override
    public BigDecimal getValue() {
      return (isCalculated) ? getCalculatedValue() : getManualValue();
    }

    @Override
    public void setValue(BigDecimal value) {
      myValue = value;
    }

    public void setValue(Cost copy) {
      myValue = copy.getValue();
      isCalculated = copy.isCalculated();
    }

    @Override
    public boolean isCalculated() {
      return isCalculated;
    }

    @Override
    public void setCalculated(boolean calculated) {
      isCalculated = calculated;
    }
  }
  public final static int EARLIESTBEGIN = 1;
  public final static int NONE = 0;
  private static final TimeDuration EMPTY_DURATION = new TimeDurationImpl(GPTimeUnitStack.DAY, 0);
  private static final GPCalendarCalc RESTLESS_CALENDAR = new AlwaysWorkingTimeCalendarImpl();
  private final CustomColumnsValues customValues;
  private final Logger log = getLogger(getClass());
  private final List<TaskActivity> myActivities = new ArrayList<TaskActivity>();
  // private final TaskDependencyCollection myDependencies = new
  // TaskDependencyCollectionImpl();
  private final ResourceAssignmentCollectionImpl myAssignments;
  private final CostImpl myCost = new CostImpl();
  private final TaskDependencySlice myDependencySlice;
  private final TaskDependencySlice myDependencySliceAsDependant;
  private final TaskDependencySlice myDependencySliceAsDependee;
  private final int myID;
  private final TaskManagerImpl myManager;
  private final TaskHierarchyItem myTaskHierarchyItem;
  boolean isProjectTask;
  private boolean bExpand;
  private boolean critical;
  private boolean isMilestone;
  private boolean isUnplugged = false;
  private Color myColor;
  private int myCompletionPercentage;
  private GanttCalendar myEnd;
  private boolean myEventsEnabled;
  private TimeDuration myLength;
  private List<TaskActivity> myMilestoneActivity;
  private MutatorImpl myMutator;
  private String myName;
  private String myNotes;
  private Priority myPriority;
  private ShapePaint myShape;
  private GanttCalendar myStart;
  private TaskInfo myTaskInfo;
  private GanttCalendar myThird;
  private int myThirdDateConstraint;
  private String myWebLink = "";

  protected TaskImpl(TaskManagerImpl taskManager, int taskID) {
    myManager = taskManager;
    myID = taskID;

    myAssignments = new ResourceAssignmentCollectionImpl(this, myManager.getConfig().getResourceManager());
    myDependencySlice =
      new TaskDependencySliceImpl(this, myManager.getDependencyCollection(), TaskDependencySlice.COMPLETE_SLICE_FXN);
    myDependencySliceAsDependant = new TaskDependencySliceAsDependant(this, myManager.getDependencyCollection());
    myDependencySliceAsDependee = new TaskDependencySliceAsDependee(this, myManager.getDependencyCollection());
    myPriority = DEFAULT_PRIORITY;
    myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
    myNotes = "";
    bExpand = true;
    myColor = null;

    customValues = new CustomColumnsValues(myManager.getCustomPropertyManager());
  }

  protected TaskImpl(TaskManagerImpl manager, TaskImpl copy, boolean isUnplugged) {
    this.isUnplugged = isUnplugged;
    myManager = manager;
    // Use a new (unique) ID for the cloned task
    myID = myManager.getAndIncrementId();

    if (!isUnplugged) {
      myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
    } else {
      myTaskHierarchyItem = copy.myTaskHierarchyItem;
    }
    myAssignments = new ResourceAssignmentCollectionImpl(this, myManager.getConfig().getResourceManager());
    myAssignments.importData(copy.getAssignmentCollection());
    myName = copy.myName;
    myWebLink = copy.myWebLink;
    isMilestone = copy.isMilestone;
    isProjectTask = copy.isProjectTask;
    myPriority = copy.myPriority;
    myStart = copy.myStart;
    myEnd = copy.myEnd;
    myThird = copy.myThird;
    myThirdDateConstraint = copy.myThirdDateConstraint;
    myCompletionPercentage = copy.myCompletionPercentage;
    myLength = copy.myLength;
    myShape = copy.myShape;
    myColor = copy.myColor;
    myNotes = copy.myNotes;
    bExpand = copy.bExpand;
    myCost.setValue(copy.myCost);

    myDependencySlice =
      new TaskDependencySliceImpl(this, myManager.getDependencyCollection(), TaskDependencySlice.COMPLETE_SLICE_FXN);
    myDependencySliceAsDependant = new TaskDependencySliceAsDependant(this, myManager.getDependencyCollection());
    myDependencySliceAsDependee = new TaskDependencySliceAsDependee(this, myManager.getDependencyCollection());

    customValues = (CustomColumnsValues) copy.getCustomValues().clone();

    recalculateActivities();
  }

  private static void recalculateActivities(
    GPCalendarCalc calendar, Task task, List<TaskActivity> output, Date startDate,
    Date endDate
  ) {
    TaskActivitiesAlgorithm alg = new TaskActivitiesAlgorithm(calendar);
    alg.recalculateActivities(task, output, startDate, endDate);
  }

  @Override
  public void addNotes(String notes) {
    myNotes += notes;
  }

  // TODO: implementation of this method has no correlation with algorithms
  // recalculating schedules,
  // doesn't affect subtasks and supertasks. It is necessary to call this
  // method explicitly from other
  // parts of code to be sure that constraint fulfills
  @Override
  public void applyThirdDateConstraint() {
//    if (getThird() != null)
//      switch (getThirdDateConstraint()) {
//      case EARLIESTBEGIN:
//        if (getThird().after(getStart())) {
//          shift(myManager.getTimeUnitStack().createDuration(getDuration().getTimeUnit(), getStart().getTime(), getThird().getTime()));
//        }
//        break;
//      }
  }

  /**
   * Determines whether a special color is defined for this task.
   *
   * @return true, if this task has its own color defined.
   */
  public boolean colorDefined() {
    return (myColor != null);
  }

  @Override
  public TaskMutator createMutator() {
    if (myMutator != null) {
      return myMutator;
    }
    myMutator = new MutatorImpl();
    return myMutator;
  }

  @Override
  public TaskMutator createMutatorFixingDuration() {
    if (myMutator != null) {
      throw new MutatorException("Two mutators have been requested for task=" + getName());
    }
    myMutator = new MutatorImpl() {
      @Override
      public void setStart(GanttCalendar start) {
        super.setStart(start);
        TaskImpl.this.myEnd = null;
      }
    };
    return myMutator;
  }

  @Override
  public void delete() {
    getDependencies().clear();
    getAssignmentCollection().clear();
  }

  @Override
  public List<TaskActivity> getActivities() {
    if (isMilestone) {
      return myMilestoneActivity;
    }
    List<TaskActivity> activities = myMutator == null ? null : myMutator.getActivities();
    if (activities == null) {
      activities = myActivities;
    }
    return activities;
  }

  @Override
  public ResourceAssignmentCollection getAssignmentCollection() {
    return myAssignments;
  }

  @Override
  public ResourceAssignment[] getAssignments() {
    return myAssignments.getAssignments();
  }

  @Override
  public List<Document> getAttachments() {
    if (getWebLink() != null && !"".equals(getWebLink())) {
      return Collections.singletonList(new AbstractURLDocument() {
        @Override
        public boolean canRead() {
          return true;
        }

        @Override
        public IStatus canWrite() {
          return Status.CANCEL_STATUS;
        }

        @Override
        public String getFileName() {
          return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
          return null;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
          return null;
        }

        @Override
        public String getPath() {
          return null;
        }

        @Override
        public URI getURI() {
          try {
            return new URI(new URL(getWebLink()).toString());
          } catch (URISyntaxException e) {
            // Do nothing
          } catch (MalformedURLException e) {
            File f = new File(getWebLink());
            if (f.exists()) {
              return f.toURI();
            }
          }
          try {
            URL context = myManager.getProjectDocument();
            if (context == null) {
              return null;
            }
            URL relative = new URL(context, getWebLink());
            return new URI(URLEncoder.encode(relative.toString(), StandardCharsets.UTF_8));
          } catch (URISyntaxException e) {
            // Do nothing
          } catch (MalformedURLException e) {
            // Do nothing
          }
          return null;
        }

        @Override
        public boolean isLocal() {
          return false;
        }

        @Override
        public boolean isValidForMRU() {
          return false;
        }

        @Override
        public void write() throws IOException {
        }
      });
    }
    return Collections.emptyList();
  }

  @Override
  public Color getColor() {
    Color result = myColor;
    if (result == null) {
      if (isMilestone() || myManager.getTaskHierarchy().hasNestedTasks(this)) {
        result = Color.BLACK;
      } else {
        result = myManager.getConfig().getDefaultColor();
      }
    }
    return result;
  }

  @Override
  public void setColor(Color color) {
    myColor = color;
  }

  @Override
  public int getCompletionPercentage() {
    return (myMutator != null && myMutator.myIsolationLevel == TaskMutator.READ_UNCOMMITED)
           ? myMutator.getCompletionPercentage()
           : myCompletionPercentage;
  }

  @Override
  public void setCompletionPercentage(int percentage) {
    if (percentage != myCompletionPercentage) {
      myCompletionPercentage = percentage;
      EventSender progressEventSender = new ProgressEventSender();
      progressEventSender.enable();
      progressEventSender.fireEvent();
    }
  }

  @Override
  public Cost getCost() {
    return myCost;
  }

  /**
   * @return The CustomColumnValues.
   */
  @Override
  public CustomColumnsValues getCustomValues() {
    return customValues;
  }

  @Override
  public TaskDependencySlice getDependencies() {
    return myDependencySlice;
  }

  @Override
  public TaskDependencySlice getDependenciesAsDependant() {
    return myDependencySliceAsDependant;
  }

  @Override
  public TaskDependencySlice getDependenciesAsDependee() {
    return myDependencySliceAsDependee;
  }

  @Override
  public GanttCalendar getDisplayEnd() {
    GanttCalendar modelEnd = getEnd();
    if (modelEnd.equals(getStart())) {
      boolean allMilestones = true;
      Task[] deepNestedTasks = getManager().getTaskHierarchy().getDeepNestedTasks(this);
      for (Task t: deepNestedTasks) {
        if (!t.isSupertask() && !t.isMilestone()) {
          allMilestones = false;
          break;
        }
      }
      if (!allMilestones) {
        log.warn(String.format(
          "This is probably a bug. Task #%d (%s) has end date=%s equal to start date." +
          "It could be possible if all child tasks were milestones, however they are not. Child tasks: %s",
          getTaskID(), getName(), modelEnd, Arrays.asList(deepNestedTasks)
        ));
      }
      return modelEnd;
    }
    return isMilestone ? modelEnd : modelEnd.getDisplayValue();
  }

  @Override
  public TimeDuration getDuration() {
    if (isMilestone()) {
      return EMPTY_DURATION;
    }
    return (myMutator != null && myMutator.myIsolationLevel == TaskMutator.READ_UNCOMMITED) ? myMutator.getDuration()
                                                                                            : myLength;
  }

  @Override
  public void setDuration(TimeDuration length) {
    assert length.getLength() >= 0 : "An attempt to set length=" + length + " to task=" + this;

    myLength = length;
    myEnd = null;
    recalculateActivities();
  }

  @Override
  public GanttCalendar getEnd() {
    GanttCalendar result = null;
    if (myMutator != null && myMutator.myIsolationLevel == TaskMutator.READ_UNCOMMITED) {
      result = myMutator.getEnd();
    }
    if (result == null) {
      if (myEnd == null) {
        myEnd = calculateEnd();
      }
      result = myEnd;
    }
    return result;
  }

  @Override
  public void setEnd(GanttCalendar end) {
    myEnd = end;
    recalculateActivities();
  }

  @Override
  public boolean getExpand() {
    return bExpand;
  }

  @Override
  public void setExpand(boolean expand) {
    bExpand = expand;
  }

  @Override
  public TaskManager getManager() {
    return myManager;
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public void setName(String name) {
    myName = (name == null ? null : name.trim());
  }

  @Override
  public Task[] getNestedTasks() {
    TaskHierarchyItem[] nestedItems = myTaskHierarchyItem.getNestedItems();
    Task[] result = new Task[nestedItems.length];
    for (int i = 0; i < nestedItems.length; i++) {
      result[i] = nestedItems[i].getTask();
    }
    return result;
  }

  @Override
  public String getNotes() {
    return myNotes;
  }

  @Override
  public void setNotes(String notes) {
    myNotes = notes;
  }

  @Override
  public Priority getPriority() {
    return myPriority;
  }

  @Override
  public void setPriority(Priority priority) {
    myPriority = priority;
  }

  @Override
  public ShapePaint getShape() {
    return myShape;
  }

  @Override
  public void setShape(ShapePaint shape) {
    myShape = shape;
  }

  @Override
  public GanttCalendar getStart() {
    if (myMutator != null && myMutator.myIsolationLevel == TaskMutator.READ_UNCOMMITED) {
      return myMutator.getStart();
    }
    return myStart;
  }

  @Override
  public void setStart(GanttCalendar start) {
    Date closestWorkingStart = myManager.findClosestWorkingTime(start.getTime());
    start.setTime(closestWorkingStart);
    myStart = start;
    recalculateActivities();
    adjustNestedTasks();
  }

  @Override
  public Task getSupertask() {
    TaskHierarchyItem container = myTaskHierarchyItem.getContainerItem();
    return container.getTask();
  }

  // main properties
  @Override
  public int getTaskID() {
    return myID;
  }

  @Override
  public TaskInfo getTaskInfo() {
    return myTaskInfo;
  }

  @Override
  public void setTaskInfo(TaskInfo taskInfo) {
    myTaskInfo = taskInfo;
  }

  @Override
  public GanttCalendar getThird() {
    if (myMutator != null && myMutator.myIsolationLevel == TaskMutator.READ_UNCOMMITED) {
      return myMutator.getThird();
    }
    return myThird;
  }

  @Override
  public int getThirdDateConstraint() {
    return myThirdDateConstraint;
  }

  @Override
  public void setThirdDateConstraint(int thirdDateConstraint) {
    myThirdDateConstraint = thirdDateConstraint;
  }

  public String getWebLink() {
    return myWebLink;
  }

  @Override
  public void setWebLink(String webLink) {
    myWebLink = webLink;
  }

  @Override
  public boolean isCritical() {
    return this.critical;
  }

  @Override
  public void setCritical(boolean critical) {
    this.critical = critical;
  }

  public boolean isLegacyMilestone() {
    return isMilestone;
  }

  @Override
  public boolean isMilestone() {
    return isMilestone && Boolean.TRUE == myManager.isZeroMilestones();
  }

  @Override
  public void setMilestone(boolean milestone) {
    isMilestone = milestone;
    if (milestone) {
      setEnd(null);
    }
  }

  @Override
  public boolean isProjectTask() {
    return isProjectTask;
  }

  @Override
  public void setProjectTask(boolean projectTask) {
    isProjectTask = projectTask;
  }

  @Override
  public boolean isSupertask() {
    return myManager.getTaskHierarchy().hasNestedTasks(this);
  }

  public boolean isUnplugged() {
    return this.isUnplugged;
  }

  @Override
  public void move(Task targetSupertask) {
    move(targetSupertask, -1);
  }

  @Override
  public void move(Task targetSupertask, int position) {
    TaskImpl supertaskImpl = (TaskImpl) targetSupertask;
    TaskHierarchyItem targetItem = supertaskImpl.myTaskHierarchyItem;
    myTaskHierarchyItem.delete();
    targetItem.addNestedItem(myTaskHierarchyItem, position);
    myManager.onTaskMoved(this);
  }

  @Override
  public void setThirdDate(GanttCalendar third) {
    Date closestWorkingStart = myManager.findClosestWorkingTime(third.getTime());
    third.setTime(closestWorkingStart);
    myThird = third;
  }

  /**
   * Determines whether a special shape is defined for this task.
   *
   * @return true, if this task has its own shape defined.
   */
  public boolean shapeDefined() {
    return (myShape != null);
  }

  @Override
  public void shift(TimeDuration shift) {
    float unitCount = shift.getLength(myLength.getTimeUnit());
    if (unitCount != 0f) {
      Task resultTask = shift(unitCount);
      GanttCalendar oldStart = myStart;
      GanttCalendar oldEnd = myEnd;
      myStart = resultTask.getStart();
      myLength = resultTask.getDuration();
      myEnd = resultTask.getEnd();
      if (areEventsEnabled()) {
        myManager.fireTaskScheduleChanged(this, oldStart, oldEnd);
      }
      recalculateActivities();
    }
  }

  public Task shift(float unitCount) {
    Task clone = unpluggedClone();
    if (unitCount != 0) {
      Date newStart;
      if (unitCount > 0) {
        TimeDuration length = myManager.createLength(myLength.getTimeUnit(), unitCount);
        // clone.setDuration(length);
        newStart = RESTLESS_CALENDAR.shiftDate(myStart.getTime(), length);
        if (0 == (getManager().getCalendar().getDayMask(newStart) & DayMask.WORKING)) {
          newStart = getManager()
            .getCalendar()
            .findClosest(newStart, myLength.getTimeUnit(), MoveDirection.FORWARD, DayType.WORKING);
        }
      } else {
        newStart = RESTLESS_CALENDAR.shiftDate(
          clone.getStart().getTime(),
          getManager().createLength(clone.getDuration().getTimeUnit(), (long) unitCount)
        );
        if (0 == (getManager().getCalendar().getDayMask(newStart) & DayMask.WORKING)) {
          newStart = getManager()
            .getCalendar()
            .findClosest(newStart, myLength.getTimeUnit(), MoveDirection.BACKWARD, DayType.WORKING);
        }
      }
      clone.setStart(CalendarFactory.createGanttCalendar(newStart));
      clone.setDuration(myLength);
    }
    return clone;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public TimeDuration translateDuration(TimeDuration duration) {
    return myManager.createLength(myLength.getTimeUnit(), translateDurationValue(duration));
  }

  @Override
  public Task unpluggedClone() {
    TaskImpl result = new TaskImpl(myManager, this, true) {
      @Override
      public boolean isSupertask() {
        return false;
      }
    };
    return result;
  }

  protected boolean areEventsEnabled() {
    return myEventsEnabled && myManager.areEventsEnabled();
  }

  protected void enableEvents(boolean enabled) {
    myEventsEnabled = enabled;
  }

  GanttCalendar calculateEnd() {
    GanttCalendar result = getStart().clone();
    Date newEnd = shiftDate(result.getTime(), getDuration());
    result.setTime(newEnd);
    return result;
  }

  private void adjustNestedTasks() {
    assert myManager != null;
    try {
      AlgorithmCollection algorithmCollection = myManager.getAlgorithmCollection();
      if (algorithmCollection != null) {
        algorithmCollection.getAdjustTaskBoundsAlgorithm().adjustNestedTasks(this);
      }
    } catch (TaskDependencyException e) {
      log.error("Exception", e);
    }
  }

  private void recalculateActivities() {
    if (myLength == null || myManager == null) {
      return;
    }
    if (isMilestone) {
      myMilestoneActivity = ImmutableList.of(new MilestoneTaskFakeActivity(this));
      return;
    }

    final Date startDate = myStart.getTime();
    final Date endDate = getEnd().getTime();

    myActivities.clear();
    if (startDate.equals(endDate)) {
      myActivities.add(new MilestoneTaskFakeActivity(this));
      return;
    }

    recalculateActivities(myManager.getConfig().getCalendar(), this, myActivities, startDate, endDate);
    int length = 0;
    for (TaskActivity activity: myActivities) {
      if (activity.getIntensity() > 0) {
        length += activity.getDuration().getLength(getDuration().getTimeUnit());
      }
    }
    myLength = getManager().createLength(myLength.getTimeUnit(), length);
  }

  private Date shiftDate(Date input, TimeDuration duration) {
    return myManager.getConfig().getCalendar().shiftDate(input, duration);
  }

  private float translateDurationValue(TimeDuration duration) {
    if (myLength.getTimeUnit().equals(duration.getTimeUnit())) {
      return duration.getValue();
    }
    if (myLength.getTimeUnit().isConstructedFrom(duration.getTimeUnit())) {
      return duration.getValue() / myLength.getTimeUnit().getAtomCount(duration.getTimeUnit());
    }
    if (duration.getTimeUnit().isConstructedFrom(myLength.getTimeUnit())) {
      return duration.getValue() * duration.getTimeUnit().getAtomCount(myLength.getTimeUnit());
    }
    throw new RuntimeException("Can't translate duration=" + duration + " into units=" + myLength.getTimeUnit());
  }
}
