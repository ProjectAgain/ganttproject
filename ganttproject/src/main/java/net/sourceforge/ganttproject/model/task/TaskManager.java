/*
GanttProject is an opensource project management tool.
Copyright (C) 2003-2011 GanttProject Team

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

import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.GanttTask;
import net.sourceforge.ganttproject.model.ProjectEventListener;
import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.calendar.GPCalendarListener;
import net.sourceforge.ganttproject.model.resource.HumanResource;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.task.Task.Priority;
import net.sourceforge.ganttproject.model.task.algorithm.AlgorithmCollection;
import net.sourceforge.ganttproject.model.task.algorithm.DependencyGraph;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyCollection;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.model.task.event.TaskListener;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.model.time.TimeUnit;
import net.sourceforge.ganttproject.ui.viewmodel.option.ColorOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.EnumerationOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.StringOption;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author bard
 */
public interface TaskManager {
    abstract class TaskBuilder {
        Boolean isExpanded;
        boolean isLegacyMilestone;
        Color myColor;
        Integer myCompletion;
        BigDecimal myCost;
        TimeDuration myDuration;
        Date myEndDate;
        Integer myId;
        String myName;
        String myNotes;
        Task myParent;
        Task myPrevSibling;
        Priority myPriority;
        Task myPrototype;
        Date myStartDate;
        String myWebLink;

        public abstract Task build();

        public TaskBuilder withColor(Color color) {
            myColor = color;
            return this;
        }

        public TaskBuilder withCompletion(int pctgCompletion) {
            myCompletion = pctgCompletion;
            return this;
        }

        public TaskBuilder withCost(BigDecimal value) {
            myCost = value;
            return this;
        }

        public TaskBuilder withDuration(TimeDuration duration) {
            myDuration = duration;
            return this;
        }

        public TaskBuilder withEndDate(Date date) {
            myEndDate = date;
            return this;
        }

        public TaskBuilder withExpansionState(boolean isExpanded) {
            this.isExpanded = isExpanded;
            return this;
        }

        public TaskBuilder withId(int id) {
            myId = id;
            return this;
        }

        public TaskBuilder withLegacyMilestone() {
            isLegacyMilestone = true;
            return this;
        }

        public TaskBuilder withName(String name) {
            myName = name;
            return this;
        }

        public TaskBuilder withNotes(String notes) {
            myNotes = notes;
            return this;
        }

        public TaskBuilder withParent(Task parent) {
            myParent = parent;
            return this;
        }

        public TaskBuilder withPrevSibling(Task sibling) {
            myPrevSibling = sibling;
            return this;
        }

        public TaskBuilder withPriority(Priority priority) {
            myPriority = priority;
            return this;
        }

        public TaskBuilder withPrototype(Task prototype) {
            myPrototype = prototype;
            return this;
        }

        public TaskBuilder withStartDate(Date startDate) {
            myStartDate = startDate;
            return this;
        }

        public TaskBuilder withWebLink(String value) {
            myWebLink = value;
            return this;
        }
    }

    class Access {
        public static TaskManager newInstance(
            TaskContainmentHierarchyFacade.Factory containmentFacadeFactory,
            TaskManagerConfig config
        ) {
            return new TaskManagerImpl(containmentFacadeFactory, config);
        }
    }

    void addTaskListener(TaskListener listener);

    TaskDependencyConstraint createConstraint(TaskDependencyConstraint.Type constraintType);

    TimeDuration createLength(String lengthAsString);

    TimeDuration createLength(long length);

    TimeDuration createLength(TimeUnit unit, float length);

    TimeDuration createLength(TimeUnit timeUnit, Date startDate, Date endDate);

    GanttTask createTask();

    @Deprecated
    GanttTask createTask(int taskId);

    void deleteTask(Task tasktoRemove);

    TaskManager emptyClone();

    String encode(TimeDuration duration);

    AlgorithmCollection getAlgorithmCollection();

    GPCalendarCalc getCalendar();

    GPCalendarListener getCalendarListener();

    CustomPropertyManager getCustomPropertyManager();

    TaskDependencyCollection getDependencyCollection();

    DependencyGraph getDependencyGraph();

    EnumerationOption getDependencyHardnessOption();

    int getProjectCompletion();

    Date getProjectEnd();

    TimeDuration getProjectLength();

    ProjectEventListener getProjectListener();

    Date getProjectStart();

    Task getRootTask();

    GanttTask getTask(int taskId);

    StringOption getTaskCopyNamePrefixOption();

    int getTaskCount();

    ColorOption getTaskDefaultColorOption();

    TaskContainmentHierarchyFacade getTaskHierarchy();

    StringOption getTaskNamePrefixOption();

    Task[] getTasks();

    void importAssignments(
        TaskManager importedTaskManager, HumanResourceManager hrManager,
        Map<Task, Task> original2importedTask, Map<HumanResource, HumanResource> original2importedResource
    );

    Map<Task, Task> importData(
        TaskManager taskManager,
        Map<CustomPropertyDefinition, CustomPropertyDefinition> customPropertyMapping
    );

    Boolean isZeroMilestones();

    TaskBuilder newTaskBuilder();

    /**
     * Processes the critical path finding on <code>root</code> tasks.
     *
     * @param root The root of the tasks to consider in the critical path finding.
     */
    void processCriticalPath(Task root);

    void registerTask(Task task);

    void setZeroMilestones(Boolean b);

    Date shift(Date original, TimeDuration duration);
}
