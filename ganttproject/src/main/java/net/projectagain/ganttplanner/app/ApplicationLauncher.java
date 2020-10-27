package net.projectagain.ganttplanner.app;

import net.projectagain.ganttplanner.core.ui.UiManager;
import org.pf4j.PluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationLauncher {

  public static void main(String[] args) {
    // retrieves the Spring application context
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);

    // retrieves automatically the extensions for the Greeting.class extension point
    UiManager uiManager = applicationContext.getBean(UiManager.class);
    uiManager.startUiApp(ganttProject -> null);

    // stop plugins
    PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
        /*
        // retrieves manually the extensions for the Greeting.class extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println("greetings.size() = " + greetings.size());
        */
    pluginManager.stopPlugins();
  }
}
