/*
GanttProject is an opensource project management tool.
Copyright (C) 2009 Dmitry Barashev

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
package net.sourceforge.ganttproject.export.htmlpdf.fonts;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import net.projectagain.ganttplanner.app.LegacyApp;
import net.projectagain.ganttplanner.core.LogMarker;
import net.sourceforge.ganttproject.export.htmlpdf.itext.FontSubstitutionModel;
import net.sourceforge.ganttproject.export.htmlpdf.itext.FontSubstitutionModel.FontSubstitution;
import net.sourceforge.ganttproject.language.GanttLanguage;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class collects True Type fonts from .ttf files in the registered
 * directories and provides mappings of font family names to plain AWT fonts and
 * iText fonts.
 *
 * @author dbarashev
 */
public class TTFontCache {
  private static final Logger log = getLogger(TTFontCache.class);

  private static String FALLBACK_FONT_PATH = "/fonts/LiberationSans-Regular.ttf";
  private Map<String, AwtFontSupplier> myMap_Family_RegularFont = new TreeMap<String, AwtFontSupplier>();
  private final Map<FontKey, com.itextpdf.text.Font> myFontCache = new HashMap<FontKey, com.itextpdf.text.Font>();
  private Map<String, Function<String, BaseFont>> myMap_Family_ItextFont = new HashMap<String, Function<String, BaseFont>>();
  private Properties myProperties;
  private BaseFont myFallbackFont;

  public void registerDirectory(String path) {
    log.trace(LogMarker.FONTS, "scanning directory=" + path);
    File dir = new File(path);
    if (dir.exists() && dir.isDirectory()) {
      registerFonts(dir);
    } else {
      log.warn(LogMarker.FONTS, "directory " + path + " is not readable");
    }
  }

  public List<String> getRegisteredFamilies() {
    return new ArrayList<String>(myMap_Family_RegularFont.keySet());
  }

  public Font getAwtFont(String family) {
    Supplier<Font> supplier = myMap_Family_RegularFont.get(family);
    return supplier == null ? null : supplier.get();
  }

  private void registerFonts(File dir) {
    final File[] files = dir.listFiles();
    for (File f : files) {
      if (!f.canRead()) {
        continue;
      }
      if (f.isDirectory()) {
        registerFonts(f);
        continue;
      }
      String filename = f.getName().toLowerCase().trim();
      if (!filename.endsWith(".ttf") && !filename.endsWith(".ttc")) {
        continue;
      }
      try {
        registerFontFile(f);
      } catch (Throwable e) {
        log.info(LogMarker.FONTS, "Failed to register font from " + f.getAbsolutePath(), e);
      }
    }
  }

  private static Font createAwtFont(File fontFile) throws IOException, FontFormatException {
    try (FileInputStream istream = new FileInputStream(fontFile)) {
      return Font.createFont(Font.TRUETYPE_FONT, istream);
    }
  }

  private static class AwtFontSupplier implements Supplier<Font> {
    private final List<File> myFiles = Lists.newArrayList();
    private Font myFont;

    void addFile(File f) {
      myFiles.add(f);
    }

    @Override
    public Font get() {
      if (myFont == null) {
        myFont = createFont();
      }
      return myFont;
    }

    private Font createFont() {
      Font result = null;
      for (File f : myFiles) {
        Font font = createFont(f);
        if (result == null || result.getStyle() > font.getStyle()) {
          result = font;
        }
      }
      return result;
    }

    private Font createFont(File fontFile) {
      try {
        return createAwtFont(fontFile);
      } catch (IOException | FontFormatException e) {
        log.error(LogMarker.FONTS, "Exception", e);
      }
      return null;
    }
  }

  private void registerFontFile(final File fontFile) throws FontFormatException,
    IOException {
    // FontFactory.register(fontFile.getAbsolutePath());
    Font awtFont = createAwtFont(fontFile);
    log.trace(LogMarker.FONTS, "Trying font file: " + fontFile.getAbsolutePath());

    final String family = awtFont.getFontName().toLowerCase();
    AwtFontSupplier awtSupplier = myMap_Family_RegularFont.get(family);

    try {
      myMap_Family_ItextFont.put(family, createFontSupplier(fontFile, BaseFont.EMBEDDED));
    } catch (DocumentException e) {
      if (e.getMessage().indexOf("cannot be embedded") < 0) {
        log.error(LogMarker.FONTS, "Exception", e);
        return;
      }
    }
    try {
      myMap_Family_ItextFont.put(family, createFontSupplier(fontFile, BaseFont.NOT_EMBEDDED));
    } catch (DocumentException e) {
      log.error(LogMarker.FONTS, "Exception", e);
      return;
    }
    log.trace(LogMarker.FONTS, "registering font: " + family);
    if (awtSupplier == null) {
      awtSupplier = new AwtFontSupplier();
      myMap_Family_RegularFont.put(family, awtSupplier);
    }
    awtSupplier.addFile(fontFile);
  }

  private Function<String, BaseFont> createFontSupplier(final File fontFile, final boolean isEmbedded)
    throws DocumentException, IOException {
    try {
      BaseFont.createFont(fontFile.getAbsolutePath(), GanttLanguage.getInstance().getCharSet(), isEmbedded);
    } catch (DocumentException e) {
      if (!e.getMessage().contains("is not recognized")
        || !e.getMessage().contains(GanttLanguage.getInstance().getCharSet())) {
        throw e;
      }
    } finally {
      BaseFontPublicMorozov.clearCache();
    }
    return new Function<String, BaseFont>() {
      @Override
      public BaseFont apply(String charset) {
        try {
          if (fontFile.getName().toLowerCase().endsWith(".ttc")) {
            return BaseFont.createFont(fontFile.getAbsolutePath() + ",0", charset, isEmbedded);
          } else {
            return BaseFont.createFont(fontFile.getAbsolutePath(), charset, isEmbedded);
          }
        } catch (DocumentException | IOException e) {
          log.error(LogMarker.FONTS, "Exception", e);
        }
        return null;
      }
    };
  }

  private static class FontKey {
    private String family;
    private int style;
    private float size;
    private String charset;

    FontKey(String family, String charset, int style, float size) {
      this.family = family;
      this.charset = charset;
      this.style = style;
      this.size = size;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((charset == null) ? 0 : charset.hashCode());
      result = prime * result + ((family == null) ? 0 : family.hashCode());
      result = prime * result + Float.floatToIntBits(size);
      result = prime * result + style;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FontKey other = (FontKey) obj;
      if (charset == null) {
        if (other.charset != null)
          return false;
      } else if (!charset.equals(other.charset))
        return false;
      if (family == null) {
        if (other.family != null)
          return false;
      } else if (!family.equals(other.family))
        return false;
      if (Float.floatToIntBits(size) != Float.floatToIntBits(other.size))
        return false;
      if (style != other.style)
        return false;
      return true;
    }
  }

  public com.itextpdf.text.Font getFont(String family, String charset, int style, float size) {
    FontKey key = new FontKey(family, charset, style, size);
    com.itextpdf.text.Font result = myFontCache.get(key);
    if (result == null) {
      Function<String, BaseFont> f = myMap_Family_ItextFont.get(family);
      BaseFont bf = f == null ? getFallbackFont(charset) : f.apply(charset);
      if (bf != null) {
        result = new com.itextpdf.text.Font(bf, size, style);
        myFontCache.put(key, result);
      } else {
        log.error(LogMarker.FONTS, "Font with family={} not found. Also tried fallback font", family);
      }
    }
    return result;

  }

  public FontMapper getFontMapper(final FontSubstitutionModel substitutions, final String charset) {
    return new FontMapper() {
      private Map<Font, BaseFont> myFontCache = new HashMap<Font, BaseFont>();

      @Override
      public BaseFont awtToPdf(Font awtFont) {
        if (myFontCache.containsKey(awtFont)) {
          return myFontCache.get(awtFont);
        }

        String family = awtFont.getFamily().toLowerCase();
        Function<String, BaseFont> f = myMap_Family_ItextFont.get(family);
        if (f != null) {
          BaseFont result = f.apply(charset);
          myFontCache.put(awtFont, result);
          return result;
        }

        family = family.replace(' ', '_');
        if (myProperties.containsKey("font." + family)) {
          family = String.valueOf(myProperties.get("font." + family));
        }
        FontSubstitution substitution = substitutions.getSubstitution(family);
        if (substitution != null) {
          family = substitution.getSubstitutionFamily();
        }
        f = myMap_Family_ItextFont.get(family);
        if (f != null) {
          BaseFont result = f.apply(charset);
          myFontCache.put(awtFont, result);
          return result;
        }
        BaseFont result = getFallbackFont(charset);
        if (result == null) {
          log.error(LogMarker.FONTS, "Font with family={} not found. Also tried family={} and fallback font", awtFont.getFamily(), family);
        }
        return result;
      }

      @Override
      public Font pdfToAwt(BaseFont itextFont, int size) {
        return null;
      }

    };
  }

  protected BaseFont getFallbackFont(String charset) {
    if (myFallbackFont == null) {
      try {
        myFallbackFont = BaseFont.createFont(
                LegacyApp.getInstance().getResource(FALLBACK_FONT_PATH).getFile().getAbsolutePath(),
                charset,
                BaseFont.EMBEDDED
        );
      } catch (DocumentException | IOException e) {
        log.error(LogMarker.FONTS, "Exception FONT", e);
      }
    }
    return myFallbackFont;
  }

  public void setProperties(Properties properties) {
    myProperties = properties;
  }

  // BaseFont.fontCache is a static map which caches font objects. Since we scan all
  // fonts in this code, we may cache a few hundreds of objects, and retained size of each object
  // can be up to a few megabytes. Here we use so-called "Public Morozov" anti-pattern
  // which discloses protected fields of its parent class
  // See description of this pattern in English here:
  // http://jamesdolan.blogspot.com/2011/05/pavlik-morozov-anti-pattern.html
  private static abstract class BaseFontPublicMorozov extends BaseFont {
    static void clearCache() {
      BaseFont.fontCache.clear();
    }
  }
}
