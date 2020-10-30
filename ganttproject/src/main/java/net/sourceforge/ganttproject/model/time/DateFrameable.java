/*
 LICENSE:

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.model.time;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 *
 * @author bard
 */
public interface DateFrameable {
  Date adjustLeft(Date baseDate);

  Date adjustRight(Date baseDate);

  Date jumpLeft(Date baseDate);
}
