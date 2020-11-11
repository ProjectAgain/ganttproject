package net.projectagain.planner.core.i18n;

import net.projectagain.planner.core.annotations.ExtensionComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@ExtensionComponent
public class CoreI18NExtension implements I18NExtension {
  @Override
  public String translate(String toTranslate, final Object... args) {
    return null;
  }

  @Override
  public Collection<Locale> getSupportedLocales() {
    return Arrays.asList(Locale.ENGLISH, Locale.GERMAN);
  }
}
