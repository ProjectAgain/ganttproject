package net.projectagain.ganttplanner.core.domain;

import net.sourceforge.ganttproject.model.calendar.GPCalendarCalc;

public interface MProjectBase {
  String getDescription();

  void setDescription(String description);

  String getOrganization();

  void setOrganization(String organization);

  String getProjectName();

  void setProjectName(String projectName);

  String getWebLink();

  void setWebLink(String webLink);

  GPCalendarCalc getActiveCalendar();
}
