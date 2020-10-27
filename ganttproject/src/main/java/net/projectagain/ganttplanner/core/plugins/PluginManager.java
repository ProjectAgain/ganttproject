package net.projectagain.ganttplanner.core.plugins;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
@DependsOn("springPluginManager")
public class PluginManager {
  final ApplicationContext applicationContext;
  final SpringPluginManager delegate;

  PluginManager(
    final SpringPluginManager delegate, final ApplicationContext applicationContext
  ) {
    this.delegate = delegate;
    this.applicationContext = applicationContext;
  }

  public <T> Collection<T> getExtensions(Class<T> extensionPoint) {
    return applicationContext.getBeansOfType(extensionPoint).values();
  }
}
