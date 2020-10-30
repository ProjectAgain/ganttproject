/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 GanttProject team

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
package net.sourceforge.ganttproject.model.document;

import net.sourceforge.ganttproject.ui.viewmodel.option.GPOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.GPOptionGroup;
import net.sourceforge.ganttproject.ui.viewmodel.option.StringOption;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author bard
 */
public interface DocumentManager {
  abstract class FTPOptions extends GPOptionGroup {
    public FTPOptions(String id, GPOption<?>[] options) {
      super(id, options);
    }

    public abstract StringOption getDirectoryName();

    public abstract StringOption getPassword();

    public abstract StringOption getServerName();

    public abstract StringOption getUserName();
  }

  void addListener(DocumentMRUListener listener);

  void addToRecentDocuments(Document document);

  void addToRecentDocuments(String value);

  void changeWorkingDirectory(File parentFile);

  void clearRecentDocuments();

  Document getDocument(String path);

  FTPOptions getFTPOptions();

  Document getLastAutosaveDocument(Document priorTo) throws IOException;

  GPOptionGroup[] getNetworkOptionGroups();

  GPOptionGroup getOptionGroup();

  Document getProxyDocument(Document physicalDocument);

  List<String> getRecentDocuments();

  String getWorkingDirectory();

  Document newAutosaveDocument() throws IOException;

  Document newDocument(String path) throws IOException;

  Document newUntitledDocument() throws IOException;
}
