package net.projectagain.planner.app;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import net.projectagain.planner.core.event.impl.PlannerApplicationStartedEvent;
import net.projectagain.planner.core.event.impl.PlannerApplicationStoppingEvent;
import net.projectagain.planner.core.ui.event.internal.PrimaryStageReady;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class FxApplication extends Application {

  private static final Logger log = getLogger(FxApplication.class);

  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() throws Exception {
    super.init();

    ApplicationContextInitializer<GenericApplicationContext> initializer =
      applicationContext -> {
        applicationContext.registerBean(Application.class, () -> FxApplication.this);
        applicationContext.registerBean(Parameters.class, this::getParameters);
        applicationContext.registerBean(HostServices.class, this::getHostServices);
      };
    // retrieves the Spring application context
    applicationContext = new SpringApplicationBuilder()
      .sources(SpringApplication.class)
      .initializers(initializer)
      .run(getParameters().getRaw().toArray(new String[0]));

    applicationContext.publishEvent(new PlannerApplicationStartedEvent(applicationContext));
  }

  @Override
  public void start(Stage stage) {
    applicationContext.publishEvent(new PrimaryStageReady(stage));
  }

  @Override
  public void stop() throws Exception {
    applicationContext.publishEvent(new PlannerApplicationStoppingEvent(applicationContext));

    // stop plugins
    PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
    pluginManager.stopPlugins();

    applicationContext.close();
    log.info("FX App stopped");
    super.stop();
  }

}
