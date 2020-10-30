/*
 * Created on 24.10.2004
 */
package net.sourceforge.ganttproject.model.task.dependency;

import net.sourceforge.ganttproject.model.calendar.AlwaysWorkingTimeCalendarImpl;
import net.sourceforge.ganttproject.model.calendar.CalendarActivityImpl;
import net.sourceforge.ganttproject.model.calendar.GPCalendar;
import net.sourceforge.ganttproject.model.calendar.GPCalendarActivity;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishStartConstraintImpl;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bard
 */
public class TestDependencyActivityBinding extends TaskTestCase {

    private Date myJanuaryFirst;

    private Date myJanuarySecond;
    private GPCalendar myJanuaryFirstIsHolidayCalendar = new AlwaysWorkingTimeCalendarImpl() {
        @Override
        public List<GPCalendarActivity> getActivities(Date startDate, Date endDate) {
            List<GPCalendarActivity> result = new ArrayList<GPCalendarActivity>();
            if (endDate.before(myJanuaryFirst)
                || startDate.after(myJanuarySecond))
            {
                result.add(new CalendarActivityImpl(startDate, endDate, true));
                return result;
            }
            if (startDate.after(myJanuaryFirst)
                && endDate.before(myJanuarySecond))
            {
                result.add(new CalendarActivityImpl(startDate, endDate, false));
                return result;
            }
            if (startDate.before(myJanuaryFirst)
                && endDate.after(myJanuarySecond))
            {
                result.add(new CalendarActivityImpl(myJanuaryFirst,
                                                    myJanuarySecond, false
                ));
            }
            if (startDate.before(myJanuaryFirst)) {
                result.add(new CalendarActivityImpl(startDate, myJanuaryFirst,
                                                    true
                ));
            } else {
                result.add(new CalendarActivityImpl(startDate, myJanuarySecond,
                                                    false
                ));
            }
            if (endDate.after(myJanuarySecond)) {
                result.add(new CalendarActivityImpl(myJanuarySecond, endDate,
                                                    true
                ));
            } else {
                result.add(new CalendarActivityImpl(myJanuaryFirst, endDate,
                                                    false
                ));
            }
            if (result.size() == 0) {
                throw new RuntimeException("Noactivities for start date="
                                           + startDate + " and end date=" + endDate);
            }
            return result;
        }
    };

    public GPCalendar getCalendar() {
        return myJanuaryFirstIsHolidayCalendar;
    }

    @Test
    public void testFinishStartBindings() throws Exception {
        Task dependant = getTaskManager().createTask();
        Task dependee = getTaskManager().createTask();
        dependant.setStart(CalendarFactory.createGanttCalendar(1999, Calendar.DECEMBER, 30));
        dependant.setEnd(CalendarFactory.createGanttCalendar(2000, Calendar.JANUARY, 3));
        dependee.setStart(CalendarFactory.createGanttCalendar(1999, Calendar.NOVEMBER, 15));
        dependee.setEnd(CalendarFactory.createGanttCalendar(1999, Calendar.NOVEMBER, 16));

        TaskDependency dep = getTaskManager().getDependencyCollection()
                                             .createDependency(dependant, dependee,
                                                               new FinishStartConstraintImpl()
                                             );
        TaskDependency.ActivityBinding binding = dep.getActivityBinding();
        assertEquals(
            binding.getDependantActivity(),
            dependant.getActivities().get(0)
        );
        assertEquals(binding.getDependeeActivity(), dependee.getActivities().get(0));

        dependant.setStart(CalendarFactory.createGanttCalendar(2000, Calendar.JANUARY, 4));
        dependant.setEnd(CalendarFactory.createGanttCalendar(2000, Calendar.JANUARY, 5));
        dependee.setStart(CalendarFactory.createGanttCalendar(1999, Calendar.DECEMBER, 30));
        dependee.setEnd(CalendarFactory.createGanttCalendar(2000, Calendar.JANUARY, 3));
        binding = dep.getActivityBinding();
        assertEquals(
            binding.getDependantActivity(),
            dependant.getActivities().get(0)
        );
        assertEquals(binding.getDependeeActivity(), dependee.getActivities().get(0));
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        Calendar c = (Calendar) GregorianCalendar.getInstance().clone();
        c.clear();
        c.set(Calendar.YEAR, 2000);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        myJanuaryFirst = c.getTime();
        //
        c.add(Calendar.DAY_OF_MONTH, 1);
        myJanuarySecond = c.getTime();
        super.setUp();
    }
}
