package net.projectagain.planner.core.plugins;

import org.pf4j.PluginManager;

public interface Plugin {
  /**
   * This method is called by the application when the plugin is started.
   * See {@link PluginManager#startPlugin(String)}.
   */
  default void start() {}

  /**
   * This method is called by the application when the plugin is stopped.
   * See {@link PluginManager#stopPlugin(String)}.
   */
  default void stop() {  }
}
