/*
 * Created on 31.10.2004
 */
package net.sourceforge.ganttproject.model.task.calendar;

import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.time.impl.GregorianTimeUnitStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bard
 */
public class TestSetLength extends TestWeekendCalendar {
  @Test
  public void testTaskStartingOnFridayLastingTwoDaysEndsOnTuesday() {
    Task t = getTaskManager().newTaskBuilder()
                             .withStartDate(TestSetupHelper.newFriday().getTime())
                             .build();
    t.setDuration(getTaskManager().createLength(
      GregorianTimeUnitStack.DAY,
      2
    ));
    assertEquals(
      TestSetupHelper.newTuesday(),
      t.getEnd(), "unXpected end of task which starts on friday and is 2 days long"
    );
  }

  @Test
  public void testTaskStartingOnSaturdayLastingOneDayEndsOnTuesday() {
    Task t = getTaskManager().newTaskBuilder()
                             .withStartDate(TestSetupHelper.newSaturday().getTime())
                             .build();
    t.setDuration(getTaskManager().createLength(
      GregorianTimeUnitStack.DAY,
      1
    ));
    assertEquals(
      TestSetupHelper.newTuesday(),
      t.getEnd(), "unXpected end of task which starts on saturday and is 1 day long"
    );
  }

  @Test
  public void testTaskStartingOnSundayLastingOneDayEndsOnTuesday() {
    Task t = getTaskManager().newTaskBuilder()
                             .withStartDate(TestSetupHelper.newSunday().getTime())
                             .build();
    t.setDuration(getTaskManager().createLength(
      GregorianTimeUnitStack.DAY,
      1
    ));
    assertEquals(
      TestSetupHelper.newTuesday(),
      t.getEnd(), "unXpected end of task which starts on sunday and is 1 day long"
    );
  }
}
