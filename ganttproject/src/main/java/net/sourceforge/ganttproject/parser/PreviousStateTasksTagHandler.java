/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2010 Dmitry Barashev

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
package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.model.GanttPreviousState;
import net.sourceforge.ganttproject.model.GanttPreviousStateTask;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author nbohn
 */
public class PreviousStateTasksTagHandler extends DefaultHandler implements TagHandler {
  private final Logger log = getLogger(getClass());
  private final List<GanttPreviousState> myPreviousStates;
  private String myName = "";
  private GanttPreviousState previousState;
  private ArrayList<GanttPreviousStateTask> tasks = new ArrayList<GanttPreviousStateTask>();

  public PreviousStateTasksTagHandler() {
    this(null);
  }

  public PreviousStateTasksTagHandler(List<GanttPreviousState> previousStates) {
    myPreviousStates = previousStates;
  }

  public void appendCdata(String cdata) {
  }

  @Override
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("previous-tasks") && myPreviousStates != null) {
      try {
        previousState = new GanttPreviousState(myName, tasks);
        previousState.init();
        previousState.saveFile();
        myPreviousStates.add(previousState);
      } catch (IOException e) {
        log.error("Exception", e);
      }
    }
  }

  public String getName() {
    return myName;
  }

  private void setName(String name) {
    myName = name;
  }

  public ArrayList<GanttPreviousStateTask> getTasks() {
    return tasks;
  }

  public boolean hasCdata() {
    return false;
  }

  @Override
  public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
    if (qName.equals("previous-tasks")) {
      setName(attrs.getValue("name"));
      tasks = new ArrayList<GanttPreviousStateTask>();
    } else if (qName.equals("previous-task")) {
      loadPreviousTask(attrs);
    }
  }

  private void loadPreviousTask(Attributes attrs) {

    String id = attrs.getValue("id");

    boolean meeting = Boolean.parseBoolean(attrs.getValue("meeting"));

    String start = attrs.getValue("start");

    String duration = attrs.getValue("duration");

    boolean nested = Boolean.parseBoolean(attrs.getValue("super"));

    GanttPreviousStateTask task = new GanttPreviousStateTask(new Integer(id).intValue(),
                                                             GanttCalendar.parseXMLDate(start),
                                                             new Integer(duration).intValue(), meeting, nested
    );
    tasks.add(task);
  }
}
