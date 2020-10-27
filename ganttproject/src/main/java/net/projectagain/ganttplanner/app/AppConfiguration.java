package net.projectagain.ganttplanner.app;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"net.projectagain.ganttplanner", "net.sourceforge.ganttproject"})
public class AppConfiguration {

    @Bean
    public SpringPluginManager springPluginManager() {
      return new SpringPluginManager();
    }

}
