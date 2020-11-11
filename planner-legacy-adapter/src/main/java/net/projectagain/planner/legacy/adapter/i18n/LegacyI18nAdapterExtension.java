package net.projectagain.planner.legacy.adapter.i18n;

import net.projectagain.planner.core.Extension;
import net.projectagain.planner.core.annotations.ExtensionComponent;
import net.projectagain.planner.core.i18n.I18NExtension;
import net.sourceforge.ganttproject.language.GanttLanguage;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Locale;

@ExtensionComponent
public class LegacyI18nAdapterExtension implements I18NExtension {
  private GanttLanguage ganttLanguage;

  @Override
  public String translate(String toTranslate, final Object... args) {
    return null;
  }

  @Override
  public Collection<Locale> getSupportedLocales() {
    return ganttLanguage.getAvailableLocales();
  }

  @Override
  public int order() {
    return Extension.DEFAULT_PRIORITY - 1;
  }

  @PostConstruct
  private void postConstruct() {
    this.ganttLanguage = GanttLanguage.getInstance();
  }

}
