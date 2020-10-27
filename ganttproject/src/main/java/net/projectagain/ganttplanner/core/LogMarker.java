package net.projectagain.ganttplanner.core;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface LogMarker {
  Marker CLIPBOARD = MarkerFactory.getMarker("CLIPBOARD");
  Marker TASKS = MarkerFactory.getMarker("TASKS");
  Marker APP_LIFECYCLE = MarkerFactory.getMarker("APP_LIFECYCLE");
}
