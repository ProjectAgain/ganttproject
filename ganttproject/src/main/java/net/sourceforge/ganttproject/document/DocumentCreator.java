/*
 * Created on 20.08.2003
 *
 */
package net.sourceforge.ganttproject.document;

import net.sourceforge.ganttproject.core.option.DefaultStringOption;
import net.sourceforge.ganttproject.core.option.GPOption;
import net.sourceforge.ganttproject.core.option.GPOptionGroup;
import net.sourceforge.ganttproject.core.option.StringOption;
import net.sourceforge.ganttproject.core.table.ColumnList;
import net.sourceforge.ganttproject.core.time.CalendarFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.GanttOptions;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.options.model.GP1XOptionConverter;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.parser.ParserFactory;
import net.sourceforge.ganttproject.storage.DocumentKt;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This is a helper class, to create new instances of Document easily. It
 * chooses the correct implementation based on the given path.
 *
 * @author Michael Haeusler (michael at akatose.de)
 */
public class DocumentCreator implements DocumentManager {
  private static final Logger log = getLogger(DocumentCreator.class);

  private final IGanttProject myProject;

  private final UIFacade myUIFacade;

  private final ParserFactory myParserFactory;

  private final StringOption myWorkingDirectory = new StringOptionImpl("working-dir", "working-dir", "dir");

  private final GPOptionGroup myOptionGroup;

  /** List containing the Most Recent Used documents */
  private final DocumentsMRU myMRU = new DocumentsMRU(5);
  private final File myDocumentsFolder;

  public DocumentCreator(IGanttProject project, UIFacade uiFacade, ParserFactory parserFactory) {
    myProject = project;
    myUIFacade = uiFacade;
    myParserFactory = parserFactory;
    myOptionGroup = new GPOptionGroup("", myWorkingDirectory);
    myDocumentsFolder = DocumentKt.getDefaultLocalFolder();
  }

  /**
   * Creates an HttpDocument if path starts with "http://" or "https://";
   * creates a FileDocument otherwise.
   *
   * @param path
   *          path to the document
   * @return an implementation of the interface Document
   */
  private Document createDocument(String path) {
    return createDocument(path, null, null);
  }

  /**
   * Creates an HttpDocument if path starts with "http://" or "https://";
   * creates a FileDocument otherwise.
   *
   * @param path
   *          path to the document
   * @param user
   *          username
   * @param pass
   *          password
   * @return an implementation of the interface Document
   * @throws Exception when the specified protocol is not supported
   */
  private Document createDocument(String path, String user, String pass) {
    assert path != null;
    path = path.trim();
    String lowerPath = path.toLowerCase();
    if (lowerPath.startsWith("ftp:")) {
      return new FtpDocument(path, myFtpUserOption, myFtpPasswordOption);
    } else if (!lowerPath.startsWith("file://") && path.contains("://")) {
      // Generate error for unknown protocol
      throw new RuntimeException("Unknown protocol: " + path.substring(0, path.indexOf("://")));
    }
    File file = new File(path);
    if (file.toPath().isAbsolute()) {
      return new FileDocument(file);
    }
    File relativeFile = new File(myDocumentsFolder, path);
    return new FileDocument(relativeFile);
  }

  @Override
  public Document getDocument(String path) {
    Document physicalDocument = createDocument(path);
    Document proxyDocument = new ProxyDocument(this, physicalDocument, myProject, myUIFacade, getVisibleFields(),
        getResourceVisibleFields(), getParserFactory());
    return proxyDocument;
  }

  @Override
  public Document getProxyDocument(Document physicalDocument) {
    Document proxyDocument = new ProxyDocument(this, physicalDocument, myProject, myUIFacade, getVisibleFields(),
        getResourceVisibleFields(), getParserFactory());
    return proxyDocument;
  }

  @Override
  public Document newUntitledDocument() throws IOException {
    for (int i = 1;; i++) {
      String filename = GanttLanguage.getInstance().formatText("document.storage.untitledDocument", i);
      File untitledFile = new File(myDocumentsFolder, filename);
      if (untitledFile.exists()) {
        continue;
      }
      return getDocument(untitledFile.getAbsolutePath());
    }
  }

  @Override
  public Document newDocument(String path) throws IOException {
    return createDocument(path);
  }

  @Override
  public Document newAutosaveDocument() throws IOException {
    File tempFile = File.createTempFile("_ganttproject_autosave", ".gan", getTempDir());
    return getDocument(tempFile.getAbsolutePath());
  }

  public static Runnable createAutosaveCleanup() {
    long now = CalendarFactory.newCalendar().getTimeInMillis();
    final File tempDir = getTempDir();
    final long cutoff;
    try {
      File optionsFile = GanttOptions.getOptionsFile();
      if (!optionsFile.exists()) {
        return null;
      }
      log.info("Options file:" + optionsFile.getAbsolutePath());
      BasicFileAttributes attrs = Files.readAttributes(optionsFile.toPath(), BasicFileAttributes.class);
      FileTime accessTime = attrs.lastAccessTime();
      FileTime modifyTime = attrs.lastModifiedTime();
      long lastFileTime = Math.max(accessTime.toMillis(), modifyTime.toMillis());
      cutoff = Math.min(lastFileTime, now);
    } catch (IOException e) {
      log.error("Exception", e);
      return null;
    }
    return new Runnable() {
      @Override
      public void run() {
        log.info("Deleting old auto-save files");
        deleteAutosaves();
      }

      private void deleteAutosaves() {
        // Let's find autosaves created before launch of this GP instance
        File[] previousAutosaves = tempDir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File file) {
            return file.getName().startsWith("_ganttproject_autosave") && file.lastModified() < cutoff;
          }
        });
        for (File f : previousAutosaves) {
          f.deleteOnExit();
        }
      }
    };
  }

  private FileSystem getAutosaveZipFs() {
    try {
      File tempDir = getTempDir();
      if (tempDir == null) {
        return null;
      }
      File autosaveFile = new File(tempDir, "_ganttproject_autosave.zip");
      if (autosaveFile.exists() && !autosaveFile.canWrite()) {
        log.warn(String.format(
            "Autosave file %s is not writable", autosaveFile.getAbsolutePath()));
        return null;
      }
      URI uri = new URI("jar:file:" + autosaveFile.toURI().getPath());
      return FileSystems.newFileSystem(uri, ImmutableMap.<String, Object>of("create", "true"));
    } catch (Throwable e) {
      log.error("Failure when creating ZIP FS for autosaves", e);
      return null;
    }
  }

  private static File getTempDir() {
    File tempDir;
    if (SystemUtils.IS_OS_LINUX ){
      tempDir = new File("/var/tmp");
      if (tempDir.exists() && tempDir.isDirectory() && tempDir.canWrite()) {
        return tempDir;
      }
    }
    tempDir = new File(System.getProperty("java.io.tmpdir"));
    if (tempDir.exists() && tempDir.isDirectory() && tempDir.canWrite()) {
      return tempDir;
    }
    try {
      File tempFile = File.createTempFile("_ganttproject_autosave", ".empty");
      tempDir = tempFile.getParentFile();
      if (tempDir.exists() && tempDir.isDirectory() && tempDir.canWrite()) {
        return tempDir;
      }
    } catch (IOException e) {
      log.warn("Can't get parent of the temp file", e);
    }
    log.warn("Failed to find temporary directory");
    return null;
  }

  @Override
  public Document getLastAutosaveDocument(Document priorTo) throws IOException {
    File f = File.createTempFile("tmp", "", getTempDir());
    File directory = f.getParentFile();
    File files[] = directory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File f, String arg1) {
        return arg1.startsWith("_ganttproject_autosave");
      }
    });
    Arrays.sort(files, new Comparator<File>() {
      @Override
      public int compare(File left, File right) {
        return Long.valueOf(left.lastModified()).compareTo(Long.valueOf(right.lastModified()));
      }
    });
    if (files.length == 0) {
      return null;
    }
    if (priorTo == null) {
      return getDocument(files[files.length - 1].getAbsolutePath());
    }
    for (int i = files.length - 1; i >= 0; i--) {
      if (files[i].getName().equals(priorTo.getFileName())) {
        return i > 0 ? getDocument(files[i - 1].getAbsolutePath()) : null;
      }
    }
    return null;
  }

  protected ColumnList getVisibleFields() {
    return null;
  }

  protected ColumnList getResourceVisibleFields() {
    return null;
  }

  protected ParserFactory getParserFactory() {
    return myParserFactory;
  }

  String createTemporaryFile() throws IOException {
    return getWorkingDirectoryFile().getAbsolutePath();
  }

  @Override
  public void changeWorkingDirectory(File directory) {
    assert directory.isDirectory();
    myWorkingDirectory.lock();
    myWorkingDirectory.setValue(directory.getAbsolutePath());
    myWorkingDirectory.commit();
  }

  @Override
  public String getWorkingDirectory() {
    return myWorkingDirectory.getValue();
  }


  private File getWorkingDirectoryFile() {
    return new File(getWorkingDirectory());
  }

  @Override
  public GPOptionGroup getOptionGroup() {
    return myOptionGroup;
  }

  @Override
  public FTPOptions getFTPOptions() {
    return myFtpOptions;
  }

  @Override
  public GPOptionGroup[] getNetworkOptionGroups() {
    return new GPOptionGroup[] { myFtpOptions, myOptionGroup };
  }

  private final StringOption myFtpUserOption = new StringOptionImpl("user-name", "ftp", "ftpuser");
  private final StringOption myFtpServerNameOption = new StringOptionImpl("server-name", "ftp", "ftpurl");
  private final StringOption myFtpDirectoryNameOption = new StringOptionImpl("directory-name", "ftp", "ftpdir");
  private final StringOption myFtpPasswordOption = new StringOptionImpl("password", "ftp", "ftppwd");
  private final FTPOptions myFtpOptions = new FTPOptions("ftp", new GPOption[] { myFtpUserOption,
      myFtpServerNameOption, myFtpDirectoryNameOption, myFtpPasswordOption }) {
    @Override
    public StringOption getDirectoryName() {
      return myFtpDirectoryNameOption;
    }

    @Override
    public StringOption getPassword() {
      return myFtpPasswordOption;
    }

    @Override
    public StringOption getServerName() {
      return myFtpServerNameOption;
    }

    @Override
    public StringOption getUserName() {
      return myFtpUserOption;
    }

  };

  static final String USERNAME_OPTION_ID = "user-name";
  static final String SERVERNAME_OPTION_ID = "server-name";
  static final String DIRECTORYNAME_OPTION_ID = "directory-name";
  static final String PASSWORD_OPTION_ID = "password";

  private static class StringOptionImpl extends DefaultStringOption implements GP1XOptionConverter {
    private final String myLegacyTagName;
    private final String myLegacyAttrName;

    private StringOptionImpl(String optionName, String legacyTagName, String legacyAttrName) {
      super(optionName);
      myLegacyTagName = legacyTagName;
      myLegacyAttrName = legacyAttrName;
    }

    @Override
    public String getTagName() {
      return myLegacyTagName;
    }

    @Override
    public String getAttributeName() {
      return myLegacyAttrName;
    }

    @Override
    public void loadValue(String legacyValue) {
      loadPersistentValue(legacyValue);
    }
  }

  @Override
  public List<String> getRecentDocuments() {
    return Lists.newArrayList(myMRU.iterator());
  }

  @Override
  public void addListener(DocumentMRUListener listener) {
    myMRU.addListener(listener);
  }

  @Override
  public void addToRecentDocuments(Document document) {
    myMRU.add(document.getPath(), true);
  }

  @Override
  public void addToRecentDocuments(String value) {
    myMRU.add(value, false);
  }

  @Override
  public void clearRecentDocuments() {
    myMRU.clear();
  }
}
