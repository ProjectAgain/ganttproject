/*
GanttProject is an opensource project management tool.
Copyright (C) 2009-2012 Dmitry Barashev, GanttProject Team

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
package net.sourceforge.ganttproject.export.htmlpdf.itext;

import biz.ganttproject.core.option.GPOptionGroup;
import net.projectagain.ganttplanner.app.App;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.export.ExportException;
import net.sourceforge.ganttproject.export.ExporterBase;
import net.sourceforge.ganttproject.export.ExporterBase.ExporterJob;
import net.sourceforge.ganttproject.export.htmlpdf.AbstractEngine;
import net.sourceforge.ganttproject.export.htmlpdf.ExporterToPDF;
import net.sourceforge.ganttproject.export.htmlpdf.Stylesheet;
import net.sourceforge.ganttproject.export.htmlpdf.fonts.TTFontCache;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ITextEngine extends AbstractEngine {
  private static final Logger log = getLogger(ITextEngine.class);

  private ITextStylesheet myStylesheet;
  private final TTFontCache myFontCache;
  private FontSubstitutionModel mySubstitutionModel;
  private Object myFontsMutex = new Object();
  private boolean myFontsReady = false;
  private ExporterToPDF myExporter;

  public ITextEngine(ExporterToPDF exporter) {
    myExporter = exporter;
    myFontCache = new TTFontCache();
    registerFonts();
  }

  public List<GPOptionGroup> getSecondaryOptions() {
    return Arrays.asList(getSecondaryOptionsArray());
  }

  private GPOptionGroup[] getSecondaryOptionsArray() {
    return ((ThemeImpl) myStylesheet).getOptions();
  }

  public Component getCustomOptionsUI() {
    waitRegisterFonts();
    JPanel result = new JPanel(new BorderLayout());
    result.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    OptionsPageBuilder builder = new OptionsPageBuilder();

    List<GPOptionGroup> options = new ArrayList<GPOptionGroup>();
    options.addAll(myExporter.getSecondaryOptions());
    result.add(builder.buildPlanePage(options.toArray(new GPOptionGroup[0])), BorderLayout.NORTH);
    result.add(createFontPanel(), BorderLayout.CENTER);
    return result;
  }

  public String[] getCommandLineKeys() {
    return new String[]{"itext"};
  }

  private Component createFontPanel() {
    return new FontSubstitutionPanel(mySubstitutionModel).getComponent();
  }

  public void setContext(IGanttProject project, UIFacade uiFacade, Preferences preferences, Stylesheet stylesheet) {
    super.setContext(project, uiFacade, preferences);
    setSelectedStylesheet(stylesheet);
  }

  public void setSelectedStylesheet(Stylesheet stylesheet) {
    waitRegisterFonts();
    myStylesheet = (ITextStylesheet) stylesheet;
    if (getPreferences() != null) {
      Preferences node = getPreferences().node("/configuration/net.sourceforge.ganttproject.export.htmlpdf/font-substitution");
      mySubstitutionModel = new FontSubstitutionModel(myFontCache, myStylesheet, node);
      myStylesheet.setFontSubstitutionModel(mySubstitutionModel);
    }
  }

  public void setStylesheet(Stylesheet stylesheet) {
    myStylesheet = (ITextStylesheet) stylesheet;
  }

  public List<Stylesheet> getStylesheets() {
    List<Stylesheet> result = new ArrayList<>();
    try {
      URL url = App.getInstance().getResource("html-exporter/itext-export-themes/sortavala.txt").getURL();
      result.add(new ThemeImpl(url, "Sortavala", getExporter(), myFontCache));
    } catch (IOException e) {
      log.error("Exception", e);
    }
    return result;
  }

  private ExporterBase getExporter() {
    return myExporter;
  }

  private void registerFonts() {
    Thread fontReadingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // Random waiting seems silly, depending on the available
          // resources (CPU speed, number of processes running etc)
          // this might take longer or shorter...
          // FIXME Add some better way of determining whether the fonts can be
          // read already
          Thread.sleep(10000);
          log.info("Scanning font directories...");
        } catch (InterruptedException e) {
          log.error("Exception", e);
        }
        registerFontDirectories();
        synchronized (ITextEngine.this.myFontsMutex) {
          myFontsReady = true;
          myFontsMutex.notifyAll();
        }
        log.info("Scanning font directories completed");
      }
    });
    fontReadingThread.setPriority(Thread.MIN_PRIORITY);
    fontReadingThread.start();
  }

  private void waitRegisterFonts() {
    while (myFontsMutex != null) {
      synchronized (myFontsMutex) {
        if (myFontsReady) {
          break;
        }
        try {
          myFontsMutex.wait();
        } catch (InterruptedException e) {
          log.error("Exception", e);
          break;
        }
      }
    }
  }

  protected void registerFontDirectories() {
    myFontCache.registerDirectory(System.getProperty("java.home") + "/lib/fonts");
    Map<String, Boolean> l = Map.of(
      "C:/windows/fonts", true,
      "/usr/share/fonts/truetype", true,
      "/System/Library/Fonts", true,
      "fonts", false
    );

    for (Map.Entry<String, Boolean> x : l.entrySet()) {
      final String dirName = x.getKey();
      if (x.getValue()) {
        myFontCache.registerDirectory(dirName);
      } else {
        Resource resource = App.getInstance().applicationContext.getResource(dirName);
        if (!resource.exists()) {
          log.warn("Failed to find directory '{}'", dirName);
          continue;
        }

        try {
          final URL dirUrl = resource.getURL();
          myFontCache.registerDirectory(dirUrl.getPath());
        } catch (IOException e) {
          log.warn(e.getMessage(), e);
        }
      }
    }
  }

  public ExporterJob[] createJobs(File outputFile, List<File> resultFiles) {
    waitRegisterFonts();
    return new ExporterJob[]{createTransformationJob(outputFile)};
  }

  private ExporterJob createTransformationJob(final File outputFile) {
    ExporterJob result = new ExporterJob("Generating PDF") {
      @Override
      protected IStatus run() {
        assert myStylesheet != null;
        OutputStream out = null;
        try {
          out = new FileOutputStream(outputFile);
          ((ThemeImpl) myStylesheet).run(getProject(), getUiFacade(), out);
        } catch (ExportException e) {
          throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        } finally {
        }
        return Status.OK_STATUS;
      }
    };
    return result;
  }
}
