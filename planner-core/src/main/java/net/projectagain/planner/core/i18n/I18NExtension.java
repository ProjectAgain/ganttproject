package net.projectagain.planner.core.i18n;

import net.projectagain.planner.core.Extension;

import java.util.Collection;
import java.util.Locale;

public interface I18NExtension extends Extension {

  String translate(String toTranslate, final Object... args);

  Collection<Locale> getSupportedLocales();
}
