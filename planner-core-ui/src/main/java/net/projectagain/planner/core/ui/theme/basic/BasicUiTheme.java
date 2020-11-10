package net.projectagain.planner.core.ui.theme.basic;

import net.projectagain.planner.core.ui.theme.UiTheme;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class BasicUiTheme implements UiTheme {

  final ResourceLoader resourceLoader;

  public BasicUiTheme(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public String getName() {
    return "BasicTheme";
  }

  @Override
  public Resource getResource(String id) {
    return resourceLoader.getResource("classpath:/theme/default/" + id);
  }

}
