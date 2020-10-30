/*
 LICENSE:

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.time.TimeUnitStack;
import net.sourceforge.ganttproject.ui.gui.NotificationManager;
import net.sourceforge.ganttproject.ui.viewmodel.option.ColorOption;

import java.awt.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public interface TaskManagerConfig {
  GPCalendarCalc getCalendar();

  Color getDefaultColor();

  ColorOption getDefaultColorOption();

  NotificationManager getNotificationManager();

  URL getProjectDocumentURL();

  HumanResourceManager getResourceManager();

  TimeUnitStack getTimeUnitStack();
}
