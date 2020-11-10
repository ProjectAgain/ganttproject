package net.projectagain.planner.core.ui.config;

import net.projectagain.planner.core.ui.theme.UiTheme;
import net.projectagain.planner.core.ui.theme.basic.BasicUiTheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class CoreUiSpringConfiguration {

  @Bean
  @Primary
  private UiTheme defaultTheme(ResourceLoader resourceLoader) {
    return new BasicUiTheme(resourceLoader);
  }
}
