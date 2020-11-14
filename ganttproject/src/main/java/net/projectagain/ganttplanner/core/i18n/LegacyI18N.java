package net.projectagain.ganttplanner.core.i18n;

import net.sourceforge.ganttproject.language.GanttLanguage;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class LegacyI18N {
  private static LegacyI18N instance;
  private GanttLanguage ganttLanguage;

  LegacyI18N() {
  }

  public static String __(final String toTranslate) {
    return getInstance().translate(toTranslate);
  }

  public static LegacyI18N getInstance() {
    return instance;
  }

  public String translate(final String toTranslate) {
    return ganttLanguage.getText(toTranslate);
  }

  @PostConstruct
  private void postConstruct() {
    this.ganttLanguage = GanttLanguage.getInstance();
    instance = this;
  }
}
