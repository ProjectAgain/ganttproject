/*
Copyright 2003-2012 Dmitry Barashev, GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ganttproject.model.document;

import net.sourceforge.ganttproject.ui.viewmodel.option.StringOption;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

public class FtpDocument extends AbstractURLDocument {
  private static final Object EMPTY_STRING = "";
  private final Logger log = getLogger(getClass());
  private final URI myURI;

  FtpDocument(String urlAsString, StringOption ftpUser, StringOption ftpPassword) {
    assert urlAsString != null;
    try {
      URI url = new URI(urlAsString);
      String userInfo = url.getUserInfo();
      if (userInfo == null || EMPTY_STRING.equals(userInfo)) {
        StringBuffer buf = new StringBuffer();
        if (ftpUser.getValue() != null) {
          buf.append(ftpUser.getValue());
        }
        if (ftpPassword.getValue() != null) {
          buf.append(':').append(ftpPassword.getValue());
        }
        myURI = new URI("ftp", buf.toString(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
                        url.getFragment()
        );
      } else {
        myURI = url;
      }
      urlAsString = myURI.toString();
      myURI.toURL().openConnection().connect();
    } catch (URISyntaxException e) {
      log.error("Exception", e);
      throw new RuntimeException("Failed to create FTP document addressed by URL=" + urlAsString, e);
    } catch (MalformedURLException e) {
      log.error("Exception", e);
      throw new RuntimeException("Failed to create FTP document addressed by URL=" + urlAsString, e);
    } catch (IOException e) {
      log.error("Exception", e);
      throw new RuntimeException("Failed to create FTP document addressed by URL=" + urlAsString, e);
    }
  }

  @Override
  public boolean canRead() {
    return true;
  }

  @Override
  public IStatus canWrite() {
    return Status.OK_STATUS;
  }

  @Override
  public String getFileName() {
    // TODO return filename instead of complete URI?
    return myURI.toString();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return myURI.toURL().openConnection().getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return myURI.toURL().openConnection().getOutputStream();
  }

  @Override
  public String getPath() {
    return myURI.toString();
  }

  @Override
  public URI getURI() {
    return myURI;
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public boolean isValidForMRU() {
    return true;
  }

  @Override
  public void write() throws IOException {
    throw new UnsupportedOperationException();
  }
}