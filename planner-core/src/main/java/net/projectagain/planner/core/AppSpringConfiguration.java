package net.projectagain.planner.core;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Configuration
public class AppSpringConfiguration {
  @Bean
  public SpringPluginManager springPluginManager() {
    return new SpringPluginManager();
  }
}
