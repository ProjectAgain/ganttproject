/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 Dmitry Barashev, GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sourceforge.ganttproject.language;

import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.ui.InternationalizationKt;
import net.sourceforge.ganttproject.ui.viewmodel.option.GPAbstractOption;
import net.sourceforge.ganttproject.util.PropertiesUtil;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class for the language
 */
public class GanttLanguage {
  private static class CalendarFactoryImpl extends CalendarFactory implements CalendarFactory.LocaleApi {
    static void setLocaleImpl() {
      CalendarFactory.setLocaleApi(new CalendarFactoryImpl());
    }

    @Override
    public Locale getLocale() {
      return GanttLanguage.getInstance().getLocale();
    }

    @Override
    public DateFormat getShortDateFormat() {
      return GanttLanguage.getInstance().getShortDateFormat();
    }
  }

  public interface Listener extends EventListener {
    void languageChanged(Event event);
  }

  public class Event extends EventObject {
    public Event(GanttLanguage language) {
      super(language);
    }

    public GanttLanguage getLanguage() {
      return (GanttLanguage) getSource();
    }
  }

  private static final GanttLanguage ganttLanguage = new GanttLanguage();
  private final Logger log = getLogger(getClass());
  private final CharSetMap myCharSetMap;
  private final Properties myExtraLocales = new Properties();
  private final ArrayList<Listener> myListeners = new ArrayList<Listener>();
  private final SimpleDateFormat myRecurringDateFormat = new SimpleDateFormat("MMM dd");
  private SimpleDateFormat currentDateFormat = null;
  private DateFormat currentDateTimeFormat = null;
  private Locale currentLocale = null;
  private DateFormat currentTimeFormat = null;
  private Locale myDateFormatLocale;
  private List<String> myDayShortNames;
  private SimpleDateFormat myLongFormat;
  private SimpleDateFormat shortCurrentDateFormat = null;

  private GanttLanguage() {
    new GPAbstractOption.I18N() {
      @Override
      protected String i18n(String key) {
        return getText(key);
      }

      {
        setI18N(this);
      }
    };
    Properties charsets = new Properties();
    PropertiesUtil.loadProperties(charsets, "/charsets.properties");
    myCharSetMap = new CharSetMap(charsets);
    setLocale(Locale.getDefault());
    PropertiesUtil.loadProperties(myExtraLocales, "/language/extra.properties");
  }

  public static GanttLanguage getInstance() {
    return ganttLanguage;
  }

  private static List<String> getShortDayNames(Locale locale) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", locale);
    List<String> result = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      GregorianCalendar day = new GregorianCalendar(2000, 1, 1);
      while (day.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        day.add(Calendar.DATE, 1);
      }
      day.add(Calendar.DATE, i);

      StringBuffer formattedDay = new StringBuffer();
      formattedDay = dateFormat.format(day.getTime(), formattedDay, new FieldPosition(DateFormat.DAY_OF_WEEK_FIELD));
      result.add(formattedDay.toString());
    }
    return result;
  }

  public void addListener(Listener listener) {
    myListeners.add(listener);
  }

  /**
   * @return label with the $ removed from it (if it was included)
   */
  public String correctLabel(String label) {
    if (label == null) {
      return null;
    }

    int index = label.indexOf('$');
    if (index != -1 && label.length() - index > 1) {
      label = label.substring(0, index).concat(label.substring(++index));
    }
    return label;
  }

  public SimpleDateFormat createDateFormat(String string) {
    return new SimpleDateFormat(string, myDateFormatLocale);
  }

  public String formatDate(Calendar date) {
    return currentDateFormat.format(date.getTime());
  }

  public String formatDateTime(Date date) {
    return currentDateTimeFormat.format(date);
  }

  public String formatLanguageAndCountry(Locale locale) {
    String englishName = locale.getDisplayLanguage(Locale.US);
    String localName = locale.getDisplayLanguage(locale);
    String currentLocaleName = locale.getDisplayLanguage(getLocale());
    if ("en".equals(locale.getLanguage()) || "zh".equals(locale.getLanguage()) || "pt".equals(locale.getLanguage())) {
      if (!locale.getCountry().isEmpty()) {
        englishName += " - " + locale.getDisplayCountry(Locale.US);
        localName += " - " + locale.getDisplayCountry(locale);
      }
    }
    if (localName.equals(englishName) && currentLocaleName.equals(englishName)) {
      return englishName;
    }
    StringBuilder builder = new StringBuilder(englishName);
    builder.append(" (");
    boolean hasLocal = false;
    if (!localName.equals(englishName)) {
      builder.append(localName);
      hasLocal = true;
    }
    if (!currentLocaleName.equals(localName) && !currentLocaleName.equals(englishName)) {
      if (hasLocal) {
        builder.append(", ");
      }
      builder.append(currentLocaleName);
    }
    builder.append(")");
    return builder.toString();
  }

  public String formatShortDate(Calendar date) {
    return shortCurrentDateFormat.format(date.getTime());
  }

  public String formatText(String key, Object... values) {
    return InternationalizationKt.getRootLocalizer().formatText(key, values);
  }

  public String formatTime(Calendar date) {
    return currentTimeFormat.format(date.getTime());
  }

  public List<Locale> getAvailableLocales() {
    return InternationalizationKt.getAvailableTranslations();
  }

  public String getCharSet() {
    return myCharSetMap.getCharSet(getLocale());
  }

  public ComponentOrientation getComponentOrientation() {
    return ComponentOrientation.getOrientation(currentLocale);
  }

  /**
   * @return the text suitable for labels in the current language for the given
   * key (all $ characters are removed from the original text)
   */
  public String getCorrectedLabel(String key) {
    String label = getText(key);
    return label == null ? null : correctLabel(label);
  }

  /**
   * @return The current DateFormat
   */
  public DateFormat getDateFormat() {
    return currentDateFormat;
  }

  public Locale getDateFormatLocale() {
    return myDateFormatLocale;
  }

  public String getDay(int day) {
    return myDayShortNames.get(day);
  }

  /**
   * @return The current Locale
   */
  public Locale getLocale() {
    return currentLocale;
  }

  public void setLocale(Locale locale) {
    currentLocale = locale;
    CalendarFactoryImpl.setLocaleImpl();
    Locale.setDefault(locale);
    int defaultTimezoneOffset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();

    TimeZone utc = TimeZone.getTimeZone("UTC");
    utc.setRawOffset(defaultTimezoneOffset);
    TimeZone.setDefault(utc);

    applyDateFormatLocale(getDateFormatLocale(locale));
    InternationalizationKt.setLocale(locale);
    fireLanguageChanged();
  }

  public SimpleDateFormat getLongDateFormat() {
    return myLongFormat;
  }

  public SimpleDateFormat getMediumDateFormat() {
    return currentDateFormat;
  }

  public SimpleDateFormat getRecurringDateFormat() {
    return myRecurringDateFormat;
  }

  public SimpleDateFormat getShortDateFormat() {
    return shortCurrentDateFormat;
  }

  public void setShortDateFormat(SimpleDateFormat dateFormat) {
    shortCurrentDateFormat = dateFormat;
    UIManager.put("JXDatePicker.shortFormat", shortCurrentDateFormat.toPattern());
    fireLanguageChanged();
  }

  /**
   * @return the text in the current language for the given key
   */
  public String getText(String key) {
    return InternationalizationKt.getRootLocalizer().formatTextOrNull(key);
  }

  public Date parseDate(String dateString) {
    if (dateString == null) {
      return null;
    }
    try {
      Date parsed = getShortDateFormat().parse(dateString);
      if (getShortDateFormat().format(parsed).equals(dateString)) {
        return parsed;
      }
    } catch (ParseException e) {
      log.error("Exception", e);
    }
    return null;
  }

  private void applyDateFormatLocale(Locale locale) {
    myDateFormatLocale = locale;
    setShortDateFormat((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale));
    currentDateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    currentTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
    currentDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    myLongFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
    UIManager.put("JXDatePicker.longFormat", myLongFormat.toPattern());
    UIManager.put("JXDatePicker.mediumFormat", currentDateFormat.toPattern());
    UIManager.put("JXDatePicker.numColumns", new Integer(10));
    myDayShortNames = getShortDayNames(locale);
    UIManager.put("JXMonthView.daysOfTheWeek", myDayShortNames.toArray(new String[7]));
  }

  private void fireLanguageChanged() {
    Event event = new Event(this);
    for (Listener next: myListeners) {
      next.languageChanged(event);
    }
  }

  private Locale getDateFormatLocale(Locale baseLocale) {
    String dateFormatLocale = myExtraLocales.getProperty(baseLocale.getLanguage() + ".dateFormatLocale", null);
    if (dateFormatLocale == null) {
      return baseLocale;
    }
    return new Locale(dateFormatLocale);
  }
}
