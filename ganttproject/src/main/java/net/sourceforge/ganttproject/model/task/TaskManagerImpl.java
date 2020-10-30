/*
 * Created on 05.07.2003
 *
 */
package net.sourceforge.ganttproject.model.task;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.CustomPropertyListener;
import net.sourceforge.ganttproject.model.GanttTask;
import net.sourceforge.ganttproject.model.ProjectEventListener;
import net.sourceforge.ganttproject.model.calendar.AlwaysWorkingTimeCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.calendar.GPCalendarListener;
import net.sourceforge.ganttproject.model.resource.HumanResource;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.task.algorithm.*;
import net.sourceforge.ganttproject.model.task.dependency.*;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency.Hardness;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishFinishConstraintImpl;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishStartConstraintImpl;
import net.sourceforge.ganttproject.model.task.dependency.constraint.StartFinishConstraintImpl;
import net.sourceforge.ganttproject.model.task.dependency.constraint.StartStartConstraintImpl;
import net.sourceforge.ganttproject.model.task.event.*;
import net.sourceforge.ganttproject.model.task.hierarchy.TaskHierarchyManagerImpl;
import net.sourceforge.ganttproject.model.time.*;
import net.sourceforge.ganttproject.ui.chart.scene.BarChartActivity;
import net.sourceforge.ganttproject.ui.chart.scene.gantt.ChartBoundsAlgorithm;
import net.sourceforge.ganttproject.ui.chart.scene.gantt.ChartBoundsAlgorithm.Result;
import net.sourceforge.ganttproject.ui.gui.NotificationChannel;
import net.sourceforge.ganttproject.ui.gui.NotificationItem;
import net.sourceforge.ganttproject.ui.gui.NotificationManager;
import net.sourceforge.ganttproject.ui.gui.options.model.GP1XOptionConverter;
import net.sourceforge.ganttproject.ui.viewmodel.option.*;
import net.sourceforge.ganttproject.util.collect.Pair;
import org.slf4j.Logger;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author bard
 */
public class TaskManagerImpl implements TaskManager {
  private static class TaskMap {
    private final Map<Integer, Task> myId2task = new HashMap<Integer, Task>();
    private final TaskManagerImpl myManager;
    private boolean isModified = true;
    private Task[] myArray;
    private final TaskDocumentOrderComparator myComparator;

    TaskMap(TaskManagerImpl taskManager) {
      myComparator = new TaskDocumentOrderComparator(taskManager);
      myManager = taskManager;
    }

    public void clear() {
      myId2task.clear();
      isModified = true;
    }

    public Task[] getTasks() {
      if (isModified) {
        myArray = myId2task.values().toArray(new Task[myId2task.size()]);
        Arrays.sort(myArray, myComparator);
        isModified = false;
      }
      return myArray;
    }

    public boolean isEmpty() {
      return myId2task.isEmpty();
    }

    public void removeTask(Task task) {
      myId2task.remove(new Integer(task.getTaskID()));
      Task[] nestedTasks = myManager.getTaskHierarchy().getNestedTasks(task);
      for (int i = 0; i < nestedTasks.length; i++) {
        removeTask(nestedTasks[i]);
      }
      isModified = true;
    }

    public int size() {
      return myId2task.size();
    }

    void addTask(Task task) {
      myId2task.put(new Integer(task.getTaskID()), task);
      isModified = true;
    }

    Task getTask(int id) {
      return myId2task.get(new Integer(id));
    }

    void setDirty() {
      isModified = true;
    }
  }

  private static class TaskNamePrefixOption extends DefaultStringOption implements GP1XOptionConverter {
    public TaskNamePrefixOption() {
      super("taskNamePrefix");
      resetValue(GanttLanguage.getInstance().getText("defaultTaskPrefix"), true);
    }

    @Override
    public String getAttributeName() {
      return "prefix";
    }

    @Override
    public String getTagName() {
      return "task-name";
    }

    @Override
    public void loadValue(String legacyValue) {
      resetValue(legacyValue, true);
    }
  }

  private final class FacadeImpl implements TaskContainmentHierarchyFacade {
    // private final Task myRoot;

    private final List<Task> myPathBuffer = new ArrayList<Task>();

    // public FacadeImpl(Task root) {
    // myRoot = root;
    // }

    @Override
    public boolean areUnrelated(Task first, Task second) {
      if (first.equals(second)) {
        return false;
      }
      myPathBuffer.clear();
      for (Task container = getContainer(first); container != null; container = getContainer(container)) {
        myPathBuffer.add(container);
      }
      if (myPathBuffer.contains(second)) {
        return false;
      }
      myPathBuffer.clear();
      for (Task container = getContainer(second); container != null; container = getContainer(container)) {
        myPathBuffer.add(container);
      }
      return !myPathBuffer.contains(first);
    }

    @Override
    public void breadthFirstSearch(Task root, Predicate<Pair<Task, Task>> predicate) {
      Preconditions.checkNotNull(root);
      Queue<Task> queue = Queues.newArrayDeque();
      if (predicate.apply(Pair.create(null, root))) {
        queue.add(root);
      }
      while (!queue.isEmpty()) {
        Task head = queue.poll();
        for (Task child: head.getNestedTasks()) {
          if (predicate.apply(Pair.create(head, child))) {
            queue.add(child);
          }
        }
      }
    }

    @Override
    public List<Task> breadthFirstSearch(Task root, final boolean includeRoot) {
      final Task _root = (root == null) ? getRootTask() : root;
      final List<Task> result = Lists.newArrayList();
      breadthFirstSearch(_root, new Predicate<Pair<Task, Task>>() {
        public boolean apply(Pair<Task, Task> parent_child) {
          if (includeRoot || parent_child.first() != null) {
            result.add(parent_child.second());
          }
          return true;
        }
      });
      return result;
    }

    @Override
    public int compareDocumentOrder(Task task1, Task task2) {
      if (task1 == task2) {
        return 0;
      }
      List<Task> buffer1 = new ArrayList<Task>();
      for (Task container = task1; container != null; container = getContainer(container)) {
        buffer1.add(0, container);
      }
      List<Task> buffer2 = new ArrayList<Task>();
      for (Task container = task2; container != null; container = getContainer(container)) {
        buffer2.add(0, container);
      }
      if (buffer1.get(0) != getRootTask() && buffer2.get(0) == getRootTask()) {
        return -1;
      }
      if (buffer1.get(0) == getRootTask() && buffer2.get(0) != getRootTask()) {
        return 1;
      }

      int i = 0;
      Task commonRoot = null;
      while (true) {
        if (i == buffer1.size()) {
          return -1;
        }
        if (i == buffer2.size()) {
          return 1;
        }
        Task root1 = buffer1.get(i);
        Task root2 = buffer2.get(i);
        if (root1 != root2) {
          assert commonRoot != null : "Failure comparing task=" + task1 + " and task=" + task2 + "\n. Path1=" + buffer1
                                      + "\nPath2=" + buffer2;
          Task[] nestedTasks = commonRoot.getNestedTasks();
          for (int j = 0; j < nestedTasks.length; j++) {
            if (nestedTasks[j] == root1) {
              return -1;
            }
            if (nestedTasks[j] == root2) {
              return 1;
            }
          }
          throw new IllegalStateException("We should not be here");
        }
        i++;
        commonRoot = root1;
      }
    }

    @Override
    public boolean contains(Task task) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Task getContainer(Task nestedTask) {
      return nestedTask.getSupertask();
    }

    @Override
    public Task[] getDeepNestedTasks(Task container) {
      ArrayList<Task> result = new ArrayList<Task>();
      addDeepNestedTasks(container, result);
      return result.toArray(new Task[result.size()]);
    }

    @Override
    public int getDepth(Task task) {
      int depth = 0;
      while (task != myRoot) {
        task = task.getSupertask();
        depth++;
      }
      return depth;
    }

    @Override
    public Task[] getNestedTasks(Task container) {
      return container.getNestedTasks();
    }

    @Override
    public Task getNextSibling(Task nestedTask) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getOutlinePath(Task task) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Task getPreviousSibling(Task nestedTask) {
      int pos = getTaskIndex(nestedTask);
      return pos == 0 ? null : nestedTask.getSupertask().getNestedTasks()[pos - 1];
    }

    @Override
    public Task getRootTask() {
      return TaskManagerImpl.this.getRootTask();
    }

    @Override
    public int getTaskIndex(Task nestedTask) {
      Task container = nestedTask.getSupertask();
      if (container == null) {
        return 0;
      }
      return Arrays.asList(container.getNestedTasks()).indexOf(nestedTask);
    }

    @Override
    public List<Task> getTasksInDocumentOrder() {
      List<Task> result = Lists.newArrayList();
      LinkedList<Task> deque = new LinkedList<>();
      deque.addFirst(getRootTask());
      while (!deque.isEmpty()) {
        Task head = deque.poll();
        result.add(head);
        deque.addAll(0, Arrays.asList(head.getNestedTasks()));
      }
      result.remove(0);
      return result;
    }

    @Override
    public boolean hasNestedTasks(Task container) {
      return container.getNestedTasks().length > 0;
    }

    @Override
    public void move(Task whatMove, Task whereMove) {
      whatMove.move(whereMove);
    }

    @Override
    public void move(Task whatMove, Task whereMove, int index) {
      whatMove.move(whereMove);
    }

    @Override
    public void sort(Comparator<Task> comparator) {
      throw new UnsupportedOperationException("Sort is not available int this implementation. It is stateless!");
    }

    private void addDeepNestedTasks(Task container, ArrayList<Task> result) {
      Task[] nested = container.getNestedTasks();
      result.addAll(Arrays.asList(nested));
      for (int i = 0; i < nested.length; i++) {
        addDeepNestedTasks(nested[i], result);
      }
    }
  }

  private class FacadeFactoryImpl implements TaskContainmentHierarchyFacade.Factory {
    // private final Task myRoot;
    //
    // FacadeFactoryImpl(Task root) {
    // myRoot = root;
    // }

    @Override
    public TaskContainmentHierarchyFacade createFacade() {
      return new FacadeImpl();
    }
  }
  private static final GPCalendarCalc RESTLESS_CALENDAR = new AlwaysWorkingTimeCalendarImpl();
  private final Logger log = getLogger(getClass());
  private final AlgorithmCollection myAlgorithmCollection;
  private final TaskManagerConfig myConfig;
  private final CustomColumnsManager myCustomColumnsManager;
  private final CustomPropertyListenerImpl myCustomPropertyListener;
  private final TaskDependencyCollectionImpl myDependencyCollection;
  private final EnumerationOption myDependencyHardnessOption = new DefaultEnumerationOption<Object>(
    "dependencyDefaultHardness", new String[]{"Strong", "Rubber"})
  {
    {
      resetValue("Strong", true);
    }
  };

  private final TaskContainmentHierarchyFacade.Factory myFacadeFactory;
  private final TaskHierarchyManagerImpl myHierarchyManager;
  private final Supplier<TaskContainmentHierarchyFacade> myHierarchySupplier =
    new Supplier<TaskContainmentHierarchyFacade>() {
      @Override
      public TaskContainmentHierarchyFacade get() {
        return getTaskHierarchy();
      }
    };
  private final DependencyGraph myDependencyGraph =
    new DependencyGraph(myHierarchySupplier, new DependencyGraph.Logger() {
      @Override
      public void logDependencyLoop(String title, String message) {
        if (myConfig.getNotificationManager() != null) {
          myConfig.getNotificationManager().addNotifications(
            NotificationChannel.WARNING,
            ImmutableList.of(
              new NotificationItem("Dependency loop detected", message, NotificationManager.DEFAULT_HYPERLINK_LISTENER))
          );
        }
        log.info(title + "\n" + message);
      }
    });
  private final List<TaskListener> myListeners = new ArrayList<TaskListener>();
  private final Task myRoot;
  private final SchedulerImpl myScheduler = new SchedulerImpl(myDependencyGraph, myHierarchySupplier);
  private final StringOption myTaskCopyNamePrefixOption =
    new DefaultStringOption("taskCopyNamePrefix", GanttLanguage.getInstance().getText("task.copy.prefix"));
  private final TaskMap myTaskMap = new TaskMap(this);
  private final TaskNamePrefixOption myTaskNamePrefixOption = new TaskNamePrefixOption();
  private boolean areEventsEnabled = true;
  private Boolean isZeroMilestones = true;
  private final AtomicInteger myMaxID = new AtomicInteger(0);

  TaskManagerImpl(TaskContainmentHierarchyFacade.Factory containmentFacadeFactory, TaskManagerConfig config) {
    myCustomPropertyListener = new CustomPropertyListenerImpl(this);
    myCustomColumnsManager = new CustomColumnsManager();
    myCustomColumnsManager.addListener(getCustomPropertyListener());

    myConfig = config;
    myHierarchyManager = new TaskHierarchyManagerImpl();
    EventDispatcher dispatcher = new EventDispatcher() {
      @Override
      public void fireDependencyAdded(TaskDependency dep) {
        TaskManagerImpl.this.fireDependencyAdded(dep);
      }

      @Override
      public void fireDependencyChanged(TaskDependency dep) {
        TaskManagerImpl.this.fireDependencyChanged(dep);
      }

      @Override
      public void fireDependencyRemoved(TaskDependency dep) {
        TaskManagerImpl.this.fireDependencyRemoved(dep);
      }
    };
    myDependencyCollection = new TaskDependencyCollectionImpl(containmentFacadeFactory, dispatcher) {
      @Override
      protected Hardness getDefaultHardness() {
        String optionValue = getDependencyHardnessOption().getValue();
        return optionValue == null ? super.getDefaultHardness() : TaskDependency.Hardness.parse(optionValue);
      }

      @Override
      protected TaskContainmentHierarchyFacade getTaskHierarchy() {
        return TaskManagerImpl.this.getTaskHierarchy();
      }
    };
    myFacadeFactory = containmentFacadeFactory == null ? new FacadeFactoryImpl() : containmentFacadeFactory;
    // clear();
    myRoot = createRootTask();

    FindPossibleDependeesAlgorithm alg1 = new FindPossibleDependeesAlgorithmImpl() {
      @Override
      protected TaskContainmentHierarchyFacade createContainmentFacade() {
        return TaskManagerImpl.this.getTaskHierarchy();
      }
    };
    AdjustTaskBoundsAlgorithm alg3 = new AdjustTaskBoundsAlgorithm() {
      @Override
      protected TaskContainmentHierarchyFacade createContainmentFacade() {
        return TaskManagerImpl.this.getTaskHierarchy();
      }
    };
    RecalculateTaskScheduleAlgorithm alg2 = new RecalculateTaskScheduleAlgorithm(alg3) {
      @Override
      protected TaskContainmentHierarchyFacade createContainmentFacade() {
        return TaskManagerImpl.this.getTaskHierarchy();
      }
    };
    RecalculateTaskCompletionPercentageAlgorithm alg4 = new RecalculateTaskCompletionPercentageAlgorithm() {
      @Override
      protected TaskContainmentHierarchyFacade createContainmentFacade() {
        return TaskManagerImpl.this.getTaskHierarchy();
      }
    };
    ChartBoundsAlgorithm alg5 = new ChartBoundsAlgorithm();
    CriticalPathAlgorithm alg6 = new CriticalPathAlgorithmImpl(this, getCalendar());
    myAlgorithmCollection = new AlgorithmCollection(this, alg1, alg2, alg3, alg4, alg5, alg6, myScheduler);
    addTaskListener(myScheduler.getTaskModelListener());
  }

  private static Iterable<BarChartActivity<?>> tasksToActivities(Task[] tasks) {
    return Iterables.transform(Arrays.asList(tasks), new Function<Task, BarChartActivity<?>>() {
      @Override
      public BarChartActivity<?> apply(final Task task) {
        return new BarChartActivity<Task>() {
          @Override
          public TimeDuration getDuration() {
            return task.getDuration();
          }

          @Override
          public Date getEnd() {
            return task.getEnd().getTime();
          }

          @Override
          public Task getOwner() {
            return task;
          }

          @Override
          public Date getStart() {
            return task.getStart().getTime();
          }
        };
      }
    });
  }

  @Override
  public void addTaskListener(TaskListener listener) {
    myListeners.add(listener);
  }

  @Override
  public TaskDependencyConstraint createConstraint(final TaskDependencyConstraint.Type type) {
    TaskDependencyConstraint result;
    switch (type) {
      case finishstart:
        result = new FinishStartConstraintImpl();
        break;
      case finishfinish:
        result = new FinishFinishConstraintImpl();
        break;
      case startfinish:
        result = new StartFinishConstraintImpl();
        break;
      case startstart:
        result = new StartStartConstraintImpl();
        break;
      default:
        throw new IllegalArgumentException("Unknown constraint type=" + type);
    }
    return result;
  }

  @Override
  public TimeDuration createLength(String lengthAsString) throws DurationParsingException {
    int state = 0;
    StringBuffer valueBuffer = new StringBuffer();
    Integer currentValue = null;
    TimeDuration currentLength = null;
    lengthAsString += " ";
    for (int i = 0; i < lengthAsString.length(); i++) {
      char nextChar = lengthAsString.charAt(i);
      if (Character.isDigit(nextChar)) {
        switch (state) {
          case 0:
            if (currentValue != null) {
              throw new DurationParsingException();
            }
            state = 1;
            valueBuffer.setLength(0);
          case 1:
            valueBuffer.append(nextChar);
            break;
          case 2:
            TimeUnit timeUnit = findTimeUnit(valueBuffer.toString());
            if (timeUnit == null) {
              throw new DurationParsingException(valueBuffer.toString());
            }
            assert currentValue != null;
            TimeDuration localResult = createLength(timeUnit, currentValue.floatValue());
            if (currentLength == null) {
              currentLength = localResult;
            } else {
              if (currentLength.getTimeUnit().isConstructedFrom(timeUnit)) {
                float recalculatedLength = currentLength.getLength(timeUnit);
                currentLength = createLength(timeUnit, localResult.getValue() + recalculatedLength);
              } else {
                throw new DurationParsingException();
              }
            }
            state = 1;
            currentValue = null;
            valueBuffer.setLength(0);
            valueBuffer.append(nextChar);
            break;
        }
      } else if (Character.isWhitespace(nextChar)) {
        switch (state) {
          case 0:
            break;
          case 1:
            currentValue = Integer.valueOf(valueBuffer.toString());
            state = 0;
            break;
          case 2:
            TimeUnit timeUnit = findTimeUnit(valueBuffer.toString());
            if (timeUnit == null) {
              throw new DurationParsingException(valueBuffer.toString());
            }
            assert currentValue != null;
            TimeDuration localResult = createLength(timeUnit, currentValue.floatValue());
            if (currentLength == null) {
              currentLength = localResult;
            } else {
              if (currentLength.getTimeUnit().isConstructedFrom(timeUnit)) {
                float recalculatedLength = currentLength.getLength(timeUnit);
                currentLength = createLength(timeUnit, localResult.getValue() + recalculatedLength);
              } else {
                throw new DurationParsingException();
              }
            }
            state = 0;
            currentValue = null;
            break;
        }
      } else {
        switch (state) {
          case 1:
            currentValue = Integer.valueOf(valueBuffer.toString());
          case 0:
            if (currentValue == null) {
              throw new DurationParsingException("Failed to parse value=" + lengthAsString);
            }
            state = 2;
            valueBuffer.setLength(0);
          case 2:
            valueBuffer.append(nextChar);
            break;
        }
      }
    }
    if (currentValue != null) {
      currentValue = Integer.valueOf(valueBuffer.toString());
      TimeUnit dayUnit = findTimeUnit("d");
      currentLength = createLength(dayUnit, currentValue.floatValue());
    }
    return currentLength;
  }

  @Override
  public TimeDuration createLength(TimeUnit unit, float length) {
    return new TimeDurationImpl(unit, length);
  }

  @Override
  public TimeDuration createLength(long count) {
    return new TimeDurationImpl(getConfig().getTimeUnitStack().getDefaultTimeUnit(), count);
  }

  @Override
  public TimeDuration createLength(TimeUnit timeUnit, Date startDate, Date endDate) {
    return getConfig().getTimeUnitStack().createDuration(timeUnit, startDate, endDate);
  }

  @Override
  public GanttTask createTask() {
    return (GanttTask) newTaskBuilder().build();
  }

  @Override
  public GanttTask createTask(int id) {
    return (GanttTask) newTaskBuilder().withId(id).build();
  }

  @Override
  public void deleteTask(Task tasktoRemove) {
    Task[] nestedTasks = getTaskHierarchy().getDeepNestedTasks(tasktoRemove);
    for (Task t: nestedTasks) {
      t.delete();
    }
    Task container = getTaskHierarchy().getContainer(tasktoRemove);
    myTaskMap.removeTask(tasktoRemove);
    tasktoRemove.delete();
    fireTaskRemoved(container, tasktoRemove);
  }

  @Override
  public TaskManager emptyClone() {
    TaskManagerImpl result = new TaskManagerImpl(null, myConfig);
    result.myDependencyHardnessOption.setValue(this.myDependencyHardnessOption.getValue());
    return result;
  }

  @Override
  public String encode(TimeDuration taskLength) {
    StringBuffer result = new StringBuffer(String.valueOf(taskLength.getLength()));
    result.append(myConfig.getTimeUnitStack().encode(taskLength.getTimeUnit()));
    return result.toString();
  }

  public Date findClosestWorkingTime(Date time) {
    return getCalendar().findClosestWorkingTime(time);
  }

  public void fireTaskProgressChanged(Task changedTask) {
    if (areEventsEnabled) {
      TaskPropertyEvent e = new TaskPropertyEvent(changedTask);
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.taskProgressChanged(e);
      }
    }
  }

  @Override
  public AlgorithmCollection getAlgorithmCollection() {
    return myAlgorithmCollection;
  }

  @Override
  public GPCalendarCalc getCalendar() {
    return getConfig().getCalendar();
  }

  public GPCalendarListener getCalendarListener() {
    return new GPCalendarListener() {
      @Override
      public void onCalendarChange() {
        for (Task t: getTasks()) {
          t.setEnd(null);
        }
        myScheduler.run();
      }
    };
  }

  public TaskManagerConfig getConfig() {
    return myConfig;
  }

  @Override
  public CustomPropertyManager getCustomPropertyManager() {
    return myCustomColumnsManager;
  }

  @Override
  public TaskDependencyCollection getDependencyCollection() {
    return myDependencyCollection;
  }

  @Override
  public DependencyGraph getDependencyGraph() {
    return myDependencyGraph;
  }

  @Override
  public EnumerationOption getDependencyHardnessOption() {
    return myDependencyHardnessOption;
  }

  public TaskHierarchyManagerImpl getHierarchyManager() {
    return myHierarchyManager;
  }

  @Override
  public int getProjectCompletion() {
    return myRoot.getCompletionPercentage();
  }

  public URL getProjectDocument() {
    return myConfig.getProjectDocumentURL();
  }

  @Override
  public Date getProjectEnd() {
    if (myTaskMap.isEmpty()) {
      return myRoot.getStart().getTime();
    }
    Result result =
      getAlgorithmCollection().getProjectBoundsAlgorithm().getBounds(tasksToActivities(myTaskMap.getTasks()));
    return result.upperBound;
  }

  @Override
  public TimeDuration getProjectLength() {
    if (myTaskMap.isEmpty()) {
      return createLength(getConfig().getTimeUnitStack().getDefaultTimeUnit(), 0);
    }
    Result result =
      getAlgorithmCollection().getProjectBoundsAlgorithm().getBounds(tasksToActivities(myTaskMap.getTasks()));
    return createLength(getConfig().getTimeUnitStack().getDefaultTimeUnit(), result.lowerBound, result.upperBound);
  }

  public ProjectEventListener getProjectListener() {
    return new ProjectEventListener.Stub() {
      @Override
      public void projectClosed() {
        TaskManagerImpl.this.projectClosed();
      }

      @Override
      public void projectOpened() {
        TaskManagerImpl.this.projectOpened();
      }
    };
  }

  @Override
  public Date getProjectStart() {
    if (myTaskMap.isEmpty()) {
      return myRoot.getStart().getTime();
    }
    Result result =
      getAlgorithmCollection().getProjectBoundsAlgorithm().getBounds(tasksToActivities(myTaskMap.getTasks()));
    return result.lowerBound;
  }

  @Override
  public Task getRootTask() {
    return myRoot;
  }

  @Override
  public GanttTask getTask(int taskId) {
    return (GanttTask) myTaskMap.getTask(taskId);
  }

  @Override
  public StringOption getTaskCopyNamePrefixOption() {
    return myTaskCopyNamePrefixOption;
  }

  @Override
  public int getTaskCount() {
    return myTaskMap.size();
  }

  @Override
  public ColorOption getTaskDefaultColorOption() {
    return myConfig.getDefaultColorOption();
  }

  @Override
  public TaskContainmentHierarchyFacade getTaskHierarchy() {
    // if (myTaskContainment==null) {
    return myFacadeFactory.createFacade();
    // }
    // return myTaskContainment;
  }

  @Override
  public StringOption getTaskNamePrefixOption() {
    return myTaskNamePrefixOption;
  }

  @Override
  public Task[] getTasks() {
    return myTaskMap.getTasks();
  }

  @Override
  public void importAssignments(
    TaskManager importedTaskManager, HumanResourceManager hrManager,
    Map<Task, Task> original2importedTask, Map<HumanResource, HumanResource> original2importedResource
  ) {
    Task[] tasks = importedTaskManager.getTasks();
    for (int i = 0; i < tasks.length; i++) {
      ResourceAssignment[] assignments = tasks[i].getAssignments();
      for (int j = 0; j < assignments.length; j++) {
        Task task = getTask(original2importedTask.get(tasks[i]).getTaskID());
        ResourceAssignment assignment = task.getAssignmentCollection().addAssignment(
          original2importedResource.get(assignments[j].getResource()));
        assignment.setLoad(assignments[j].getLoad());
        assignment.setCoordinator(assignments[j].isCoordinator());
      }
    }
  }

  @Override
  public Map<Task, Task> importData(
    TaskManager taskManager,
    Map<CustomPropertyDefinition, CustomPropertyDefinition> customPropertyMapping
  ) {
    Task importRoot = taskManager.getRootTask();
    Map<Task, Task> original2imported = new LinkedHashMap<Task, Task>();
    importData(importRoot, getRootTask(), customPropertyMapping, original2imported);
    TaskDependency[] deps = taskManager.getDependencyCollection().getDependencies();
    for (int i = 0; i < deps.length; i++) {
      Task nextDependant = deps[i].getDependant();
      Task nextDependee = deps[i].getDependee();
      Task importedDependant = original2imported.get(nextDependant);
      Task importedDependee = original2imported.get(nextDependee);
      try {
        TaskDependency dependency = getDependencyCollection().createDependency(importedDependant, importedDependee,
                                                                               new FinishStartConstraintImpl()
        );
        dependency.setConstraint(deps[i].getConstraint());
        dependency.setDifference(deps[i].getDifference());
        dependency.setHardness(deps[i].getHardness());
      } catch (TaskDependencyException e) {
        log.error("Exception", e);
      }
    }
    return original2imported;
  }

  @Override
  public Boolean isZeroMilestones() {
    return isZeroMilestones;
  }

  @Override
  public TaskBuilder newTaskBuilder() {
    return new TaskBuilder() {
      @Override
      public Task build() {

        if (myId == null || myTaskMap.getTask(myId) != null) {
          myId = getAndIncrementId();
        }

        TaskImpl task = myPrototype == null
                        ? new GanttTask("", CalendarFactory.createGanttCalendar(), 1, TaskManagerImpl.this, myId)
                        : new GanttTask(TaskManagerImpl.this, (TaskImpl) myPrototype);

        if (myPrototype == null) {
          String name = myName == null
                        ? getTaskNamePrefixOption().getValue() + "_" + task.getTaskID() : myName;
          task.setName(name);
        } else if (myName != null) {
          task.setName(myName);
        }
        if (myStartDate != null) {
          GanttCalendar cal = CalendarFactory.createGanttCalendar(myStartDate);
          task.setStart(cal);
        }
        TimeDuration duration;
        if (myDuration != null) {
          duration = myDuration;
        } else if (myPrototype != null) {
          duration = myPrototype.getDuration();
        } else {
          duration = (myEndDate == null)
                     ? createLength(getTimeUnitStack().getDefaultTimeUnit(), 1.0f)
                     : createLength(getTimeUnitStack().getDefaultTimeUnit(), myStartDate, myEndDate);
        }
        task.setDuration(duration);

        if (myColor != null) {
          task.setColor(myColor);
        }
        if (myPriority != null) {
          task.setPriority(myPriority);
        }
        if (isExpanded != null) {
          task.setExpand(isExpanded);
        }
        if (myNotes != null) {
          task.setNotes(myNotes);
        }
        if (myWebLink != null) {
          task.setWebLink(myWebLink);
        }
        if (myCompletion != null) {
          task.setCompletionPercentage(myCompletion);
        }
        if (myCost != null) {
          task.getCost().setCalculated(false);
          task.getCost().setValue(myCost);
        }
        registerTask(task);

        if (myPrevSibling != null && myPrevSibling != getRootTask()) {
          int position = getTaskHierarchy().getTaskIndex(myPrevSibling) + 1;
          Task parentTask = getTaskHierarchy().getContainer(myPrevSibling);
          getTaskHierarchy().move(task, parentTask, position);
        } else {
          Task parentTask = myParent == null ? getRootTask() : myParent;
          getTaskHierarchy().move(task, parentTask);
        }

        if (isLegacyMilestone) {
          task.setMilestone(isLegacyMilestone);
        }
        fireTaskAdded(task);
        return task;
      }
    };
  }

  @Override
  public void processCriticalPath(Task root) {
    try {
      myAlgorithmCollection.getRecalculateTaskScheduleAlgorithm().run();
    } catch (TaskDependencyException e) {
      log.error("Exception", e);
    }
    Task[] tasks = myAlgorithmCollection.getCriticalPathAlgorithm().getCriticalTasks();
    resetCriticalPath();
    for (int i = 0; i < tasks.length; i++) {
      tasks[i].setCritical(true);
    }
  }

  @Override
  public void registerTask(Task task) {
    int taskID = task.getTaskID();
    assert myTaskMap.getTask(taskID) == null : "There is a task that already has the ID " + taskID;
    myTaskMap.addTask(task);
    myMaxID.set(Math.max(taskID + 1, myMaxID.get()));
    myDependencyGraph.addTask(task);
  }

  public void setEventsEnabled(boolean enabled) {
    areEventsEnabled = enabled;
  }

  @Override
  public void setZeroMilestones(Boolean b) {
    isZeroMilestones = b;
    if (Boolean.TRUE == isZeroMilestones) {
      List<Task> milestones = Lists.newArrayList();
      for (Task t: getTasks()) {
        if (t.isMilestone()) {
          t.setMilestone(true);
          milestones.add(t);
        }
      }
      getAlgorithmCollection().getAdjustTaskBoundsAlgorithm().run(milestones);
      try {
        getAlgorithmCollection().getRecalculateTaskScheduleAlgorithm().run(milestones);
      } catch (TaskDependencyException e) {
        log.error("Exception", e);
      }
    }
  }

  @Override
  public Date shift(Date original, TimeDuration duration) {
    GPCalendarCalc calendar = RESTLESS_CALENDAR;
    return calendar.shiftDate(original, duration);
  }

  protected TimeUnitStack getTimeUnitStack() {
    return getConfig().getTimeUnitStack();
  }

  boolean areEventsEnabled() {
    return areEventsEnabled;
  }

  void fireTaskPropertiesChanged(Task task) {
    if (areEventsEnabled) {
      TaskPropertyEvent e = new TaskPropertyEvent(task);
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.taskPropertiesChanged(e);
      }
    }
  }

  void fireTaskScheduleChanged(Task changedTask, GanttCalendar oldStartDate, GanttCalendar oldFinishDate) {
    myScheduler.run();
    if (areEventsEnabled) {
      TaskScheduleEvent e = new TaskScheduleEvent(changedTask, oldStartDate, oldFinishDate, changedTask.getStart(),
                                                  changedTask.getEnd()
      );
      // List copy = new ArrayList(myListeners);
      // myListeners.clear();
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.taskScheduleChanged(e);
      }
    }
  }

  int getAndIncrementId() {
    return myMaxID.getAndIncrement();
  }

  boolean isRegistered(TaskImpl task) {
    return myTaskMap.getTask(task.getTaskID()) != null;
  }

  void onTaskMoved(TaskImpl task) {
    if (!isRegistered(task)) {
      registerTask(task);
    }
    myDependencyGraph.move(task, getTaskHierarchy().getContainer(task));
    myTaskMap.setDirty();
  }

  private Task createRootTask() {
    Calendar c = CalendarFactory.newCalendar();
    Date today = c.getTime();
    Task root = new GanttTask(null, CalendarFactory.createGanttCalendar(today), 1, this, -1);
    root.setStart(CalendarFactory.createGanttCalendar(today));
    root.setDuration(createLength(getConfig().getTimeUnitStack().getDefaultTimeUnit(), 1));
    root.setExpand(true);
    root.setName("root");
    return root;
  }

  private TimeUnit findTimeUnit(String code) {
    return myConfig.getTimeUnitStack().findTimeUnit(code);
  }

  private void fireDependencyAdded(TaskDependency newDependency) {
    myDependencyGraph.addDependency(newDependency);
    if (areEventsEnabled) {
      TaskDependencyEvent e = new TaskDependencyEvent(getDependencyCollection(), newDependency);
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.dependencyAdded(e);
      }
    }
  }

  private void fireDependencyChanged(TaskDependency dep) {
    TaskDependencyEvent e = new TaskDependencyEvent(getDependencyCollection(), dep);
    for (int i = 0; i < myListeners.size(); i++) {
      TaskListener next = myListeners.get(i);
      next.dependencyChanged(e);
    }
  }

  private void fireDependencyRemoved(TaskDependency dep) {
    myDependencyGraph.removeDependency(dep);
    TaskDependencyEvent e = new TaskDependencyEvent(getDependencyCollection(), dep);
    for (int i = 0; i < myListeners.size(); i++) {
      TaskListener next = myListeners.get(i);
      next.dependencyRemoved(e);
    }
  }

  private void fireTaskAdded(Task task) {
    if (areEventsEnabled) {
      TaskHierarchyEvent e = new TaskHierarchyEvent(this, task, null, getTaskHierarchy().getContainer(task));
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.taskAdded(e);
      }
    }
  }

  private void fireTaskModelReset() {
    if (areEventsEnabled) {
      for (int i = 0; i < myListeners.size(); i++) {
        TaskListener next = myListeners.get(i);
        next.taskModelReset();
      }
    }
  }

  private void fireTaskRemoved(Task container, Task task) {
    myDependencyGraph.removeTask(task);
    if (areEventsEnabled) {
      TaskHierarchyEvent e = new TaskHierarchyEvent(this, task, container, null);
      for (TaskListener l: myListeners) {
        l.taskRemoved(e);
      }
    }
  }

  private CustomPropertyListener getCustomPropertyListener() {
    return myCustomPropertyListener;
  }

  private void importData(
    Task importRoot, Task root,
    Map<CustomPropertyDefinition, CustomPropertyDefinition> customPropertyMapping, Map<Task, Task> original2imported
  ) {
    Task[] nested = importRoot.getManager().getTaskHierarchy().getNestedTasks(importRoot);
    for (int i = 0; i < nested.length; i++) {
      TaskManager.TaskBuilder builder = newTaskBuilder();
      GanttTask that = (GanttTask) nested[i];
      if (getTask(that.getTaskID()) == null) {
        builder = builder.withId(that.getTaskID());
      }
      Task nextImported = builder
        .withName(that.getName())
        .withStartDate(that.getStart().getTime())
        .withDuration(that.getDuration())
        .withColor(that.getColor())
        .withNotes(that.getNotes())
        .withWebLink(that.getWebLink())
        .withPriority(that.getPriority())
        .withParent(root).build();

      nextImported.setShape(nested[i].getShape());
      nextImported.setCompletionPercentage(nested[i].getCompletionPercentage());
      nextImported.setTaskInfo(nested[i].getTaskInfo());
      nextImported.setExpand(nested[i].getExpand());
      nextImported.setMilestone(nested[i].isMilestone());
      nextImported.getCost().setValue(that.getCost());
      if (nested[i].getThird() != null) {
        nextImported.setThirdDate(nested[i].getThird().clone());
        nextImported.setThirdDateConstraint(nested[i].getThirdDateConstraint());
      }

      CustomColumnsValues customValues = nested[i].getCustomValues();
      for (CustomPropertyDefinition thatDef: importRoot.getManager().getCustomPropertyManager().getDefinitions()) {
        CustomPropertyDefinition thisDef = customPropertyMapping.get(thatDef);
        Object value = customValues.getValue(thatDef);
        if (value != null) {
          try {
            nextImported.getCustomValues().setValue(thisDef, value);
          } catch (CustomColumnsException e) {
            log.error("Exception", e);
          }
        }
      }
      original2imported.put(nested[i], nextImported);
      importData(nested[i], nextImported, customPropertyMapping, original2imported);
    }
  }

  private void projectClosed() {
    myDependencyGraph.clear();
    myTaskMap.clear();
    myMaxID.set(0);
    myDependencyCollection.clear();
    // createRootTask();
    fireTaskModelReset();
  }

  private void projectOpened() {
    processCriticalPath(getRootTask());
    myAlgorithmCollection.getRecalculateTaskCompletionPercentageAlgorithm().run();
  }

  private void resetCriticalPath() {
    Task[] allTasks = getTasks();
    for (int i = 0; i < allTasks.length; i++) {
      allTasks[i].setCritical(false);
    }
  }
}
