package net.projectagain.ganttplanner.core.domain;

import net.sourceforge.ganttproject.model.ProjectEventListener;

public interface MProjectEventSource {
  void addProjectEventListener(ProjectEventListener listener);

  void removeProjectEventListener(ProjectEventListener listener);
}
