package net.projectagain.planner.core.i18n;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class I18N {
  private static I18N instance;

  private final SortedSet<I18NExtension> extensions;

  protected I18N(List<I18NExtension> extensions) {
    this.extensions = new TreeSet<>(Comparator.comparingInt(I18NExtension::order));
    this.extensions.addAll(extensions);
  }

  public static String __(final String toTranslate, final Object... args) {
    return instance.translate(toTranslate, args);
  }

  public String translate(String toTranslate, final Object... args) {
    for (I18NExtension extension : extensions) {
      String translated = extension.translate(toTranslate, args);
      if (translated != null)
        return translated;
    }
    return toTranslate;
  }

  @PostConstruct
  private void postConstruct() {
    instance = this;
  }
}
