/*
 * Created on 06.03.2005
 */
package net.sourceforge.ganttproject.core.chart.text;

import net.sourceforge.ganttproject.core.chart.grid.Offset;
import net.sourceforge.ganttproject.core.chart.text.TimeFormatters.LocaleApi;
import net.sourceforge.ganttproject.core.time.DateFrameable;
import net.sourceforge.ganttproject.core.time.TimeUnit;

import java.util.Date;
import java.util.HashMap;

/**
 * @author bard
 */
public abstract class CachingTextFormatter {
  private final HashMap<Date, TimeUnitText[]> myTextCache = new HashMap<Date, TimeUnitText[]>();

  protected CachingTextFormatter() {
  }

  public TimeUnitText[] format(Offset curOffset) {
    return format(curOffset.getOffsetUnit(), curOffset.getOffsetStart());
  }

  public TimeUnitText[] format(TimeUnit timeUnit, Date baseDate) {
    TimeUnitText[] result = null;
    Date adjustedLeft = ((DateFrameable) timeUnit).adjustLeft(baseDate);
    result = getCachedText(adjustedLeft);
    if (result == null) {
      result = createTimeUnitText(adjustedLeft);
      myTextCache.put(adjustedLeft, result);
    }

    return result;
  }

  protected TimeUnitText[] getCachedText(Date startDate) {
    return myTextCache.get(startDate);
  }

  public void setLocale(LocaleApi locale) {
    myTextCache.clear();
  }

  public int getTextCount() {
    return 1;
  }
  protected abstract TimeUnitText[] createTimeUnitText(Date adjustedLeft);

}
