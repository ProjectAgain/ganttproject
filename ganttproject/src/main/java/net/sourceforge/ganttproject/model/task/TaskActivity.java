/*
 */
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.ui.chart.scene.BarChartActivity;

/**
 * @author bard
 */
public interface TaskActivity extends BarChartActivity<Task> {
  float getIntensity();

  boolean isFirst();

  boolean isLast();
}
