package net.sourceforge.ganttproject.model.calendar;

import net.sourceforge.ganttproject.ui.viewmodel.option.DefaultEnumerationOption;

public class ImportCalendarOption extends DefaultEnumerationOption<ImportCalendarOption.Values> {
  public enum Values {
    NO, REPLACE, MERGE;

    @Override
    public String toString() {
      return "importCalendar_" + name().toLowerCase();
    }
  }

  public ImportCalendarOption() {
    super("impex.importCalendar", Values.values());
  }

  public ImportCalendarOption(Values initialValue) {
    super("impex.importCalendar", Values.values());
    setSelectedValue(initialValue);
  }
}
