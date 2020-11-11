package net.projectagain.planner.core;

public interface Extension {
  int DEFAULT_PRIORITY = 1000000;

  default int order() {
    return Extension.DEFAULT_PRIORITY;
  }
}
