package net.projectagain.ganttplanner.core.settings;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SettingsManager {
  private final Logger log = getLogger(getClass());
  private ResourceBundle resourceBundle;

  public SettingsManager() {
    this.resourceBundle = getResourceBundle();
  }

  public String getSetting(String key) {
    resourceBundle = getResourceBundle();
    return getResourceBundle().containsKey(key) ? resourceBundle.getString(key) : null;
  }

  private ResourceBundle getResourceBundle() {
    ResourceBundle resourceBundle = null;
    try {
      ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
      resourceBundle = ResourceBundle.getBundle("settings", control);
    } catch (MissingResourceException ex) {
      log.error("Exception", ex);
    }
    return resourceBundle;
  }

}
