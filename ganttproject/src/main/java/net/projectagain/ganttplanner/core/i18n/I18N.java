package net.projectagain.ganttplanner.core.i18n;

import net.sourceforge.ganttproject.language.GanttLanguage;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class I18N {
  private static I18N instance;
  private GanttLanguage ganttLanguage;

  I18N() {
  }

  public static String __(final String toTranslate) {
    return getInstance().translate(toTranslate);
  }

  public static I18N getInstance() {
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
