/*
 * Created on 03.05.2005
 */
package net.sourceforge.ganttproject.chart;

import net.sourceforge.ganttproject.model.resource.HumanResource;

/**
 * @author bard
 */
public interface ResourceChart extends Chart {
  boolean isExpanded(HumanResource resource);
}
