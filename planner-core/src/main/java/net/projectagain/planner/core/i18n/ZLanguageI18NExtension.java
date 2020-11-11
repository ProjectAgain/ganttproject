package net.projectagain.planner.core.i18n;

import net.projectagain.planner.core.annotations.ExtensionComponent;
import org.springframework.context.annotation.Profile;

import java.util.*;

/**
 * I18NExtension to test if all your strings could be translated. It just replaces every letter through "z" or "Z"
 *
 */
@Profile("!production")
@ExtensionComponent
public class ZLanguageI18NExtension implements I18NExtension {
  @Override
  public int order() {
    return 0;
  }

  @Override
  public String translate(String toTranslate, final Object... args) {
    return toTranslate.replaceAll("[A-Z]", "Z")
                      .replaceAll("[a-z]", "z");
  }

  @Override
  public Collection<Locale> getSupportedLocales() {
    return Collections.singletonList(new Locale("zz_ZZ"));
  }
}
