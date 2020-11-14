package net.projectagain.planner.legacy.adapter;

import net.projectagain.planner.core.plugins.Plugin;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class LegacyPlugin implements Plugin {

  private static final Logger log = getLogger(LegacyPlugin.class);

  @Override
  public void start() {
    log.trace("Started Plugin {}",this.getClass().getSimpleName());
  }

  @Override
  public void stop() {
    log.trace("Stopped Plugin {}",this.getClass().getSimpleName());
  }
}
