package net.projectagain.planner.app;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@SpringBootApplication(
  scanBasePackages = {
    "net.projectagain.planner.core",
    "net.projectagain.planner.legacy"
  }
)
public class SpringApplication {
  public static void main(String[] args) {
    Application.launch(FxApplication.class);
  }
}
