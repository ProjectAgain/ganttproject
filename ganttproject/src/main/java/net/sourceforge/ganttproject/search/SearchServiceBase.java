/*
Copyright 2003-2012 GanttProject Team

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
package net.sourceforge.ganttproject.search;

import net.sourceforge.ganttproject.model.IGanttProject;
import net.sourceforge.ganttproject.ui.gui.TreeUiFacade;
import net.sourceforge.ganttproject.ui.gui.UIFacade;

import java.util.List;

/**
 * Base class for task and resource search services.
 *
 * @param <SR> search result object type
 * @param <SO> target search object type
 *
 * @author dbarashev (Dmitry Barashev)
 */
abstract class SearchServiceBase<SR extends SearchResult<SO>, SO> implements SearchService<SR, SO> {
  private final int myViewIndex;
  private IGanttProject myProject;
  private TreeUiFacade<SO> myTreeUiFacade;
  private UIFacade myUiFacade;

  protected SearchServiceBase(int viewIndex) {
    myViewIndex = viewIndex;
  }

  protected static boolean isNotEmptyAndContains(String doc, String query) {
    return doc != null && doc.toLowerCase().contains(query);
  }

  @Override
  public void select(List<SR> results) {
    myTreeUiFacade.clearSelection();
    for (SearchResult<SO> r: results) {
      myTreeUiFacade.setSelected(r.getObject(), false);
      myTreeUiFacade.makeVisible(r.getObject());
    }
    myUiFacade.setViewIndex(myViewIndex);
    myTreeUiFacade.getTreeComponent().requestFocusInWindow();
  }

  protected IGanttProject getProject() {
    return myProject;
  }

  protected void init(IGanttProject project, TreeUiFacade<SO> treeUiFacade, UIFacade uiFacade) {
    myProject = project;
    myTreeUiFacade = treeUiFacade;
    myUiFacade = uiFacade;
  }
}
