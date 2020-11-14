package net.projectagain.planner.core.plugins;

import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PluginManager {
  private static final Logger log = getLogger(PluginManager.class);

  final SpringPluginManager springPluginManager;
  final List<Plugin> plugins;
  final ApplicationContext applicationContext;

  public PluginManager(
    SpringPluginManager springPluginManager,
    List<Plugin> plugins,
    ApplicationContext applicationContext
  ) {
    this.springPluginManager = springPluginManager;
    this.plugins = plugins;
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  protected void init() {
    plugins.forEach(Plugin::start);
  }

  @Component
  class ClosingListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
      log.info("Stopping plugins...");
      plugins.forEach(Plugin::stop);
    }
  }

}
