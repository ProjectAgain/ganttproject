/*
 * Created on 09.11.2004
 */
package net.sourceforge.ganttproject.model.time;

/**
 * @author bard
 */
public class TimeUnitPair {
  private final TimeUnit myBottomTimeUnit;
  /**
   * Used scale for this TimeUnit
   */
  private final int myDefaultUnitWidth;
  private final TimeUnitStack myTimeUnitStack;
  private final TimeUnit myTopTimeUnit;

  public TimeUnitPair(TimeUnit topUnit, TimeUnit bottomUnit, TimeUnitStack timeUnitStack, int defaultUnitWidth) {
    myTopTimeUnit = topUnit;
    myBottomTimeUnit = bottomUnit;
    myTimeUnitStack = timeUnitStack;
    myDefaultUnitWidth = defaultUnitWidth;
  }

  public TimeUnit getBottomTimeUnit() {
    return myBottomTimeUnit;
  }

  /**
   * @return the scale for this TimeUnit
   */
  public int getDefaultUnitWidth() {
    return myDefaultUnitWidth;
  }

  public TimeUnitStack getTimeUnitStack() {
    return myTimeUnitStack;
  }

  public TimeUnit getTopTimeUnit() {
    return myTopTimeUnit;
  }
}
