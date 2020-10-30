/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2010 Alexandre Thomas, Dmitry Barashev

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
package net.sourceforge.ganttproject.model.undo;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.language.GanttLanguage.Event;
import net.sourceforge.ganttproject.model.IGanttProject;
import net.sourceforge.ganttproject.model.document.DocumentManager;
import net.sourceforge.ganttproject.parser.ParserFactory;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * UndoManager implementation, it manages the undoable edits in GanttProject
 *
 * @author bard
 */
public class UndoManagerImpl implements GPUndoManager {
  private final Logger log = getLogger(getClass());
  private final DocumentManager myDocumentManager;
  private final ParserFactory myParserFactory;
  private final IGanttProject myProject;
  private final UndoManager mySwingUndoManager;
  private final UndoableEditSupport myUndoEventDispatcher;
  private UndoableEditImpl swingEditImpl;

  public UndoManagerImpl(IGanttProject project, ParserFactory parserFactory, DocumentManager documentManager) {
    myProject = project;
    myParserFactory = parserFactory;
    myDocumentManager = documentManager;
    mySwingUndoManager = new UndoManager();
    myUndoEventDispatcher = new UndoableEditSupport();
    GanttLanguage.getInstance().addListener(new GanttLanguage.Listener() {
      public void languageChanged(Event event) {
        UIManager.getDefaults().put("AbstractUndoableEdit.undoText", GanttLanguage.getInstance().getText("undo"));
        UIManager.getDefaults().put("AbstractUndoableEdit.redoText", GanttLanguage.getInstance().getText("redo"));
      }
    });
  }

  @Override
  public void addUndoableEditListener(GPUndoListener listener) {
    myUndoEventDispatcher.addUndoableEditListener(listener);
  }

  @Override
  public boolean canRedo() {
    return mySwingUndoManager.canRedo();
  }

  @Override
  public boolean canUndo() {
    return mySwingUndoManager.canUndo();
  }

  @Override
  public void die() {
    if (swingEditImpl != null) {
      swingEditImpl.die();
    }
    if (mySwingUndoManager != null) {
      mySwingUndoManager.discardAllEdits();
    }
  }

  @Override
  public String getRedoPresentationName() {
    return mySwingUndoManager.getRedoPresentationName();
  }

  @Override
  public String getUndoPresentationName() {
    return mySwingUndoManager.getUndoPresentationName();
  }

  @Override
  public void redo() throws CannotRedoException {
    mySwingUndoManager.redo();
    fireUndoOrRedoHappened();
  }

  @Override
  public void removeUndoableEditListener(GPUndoListener listener) {
    myUndoEventDispatcher.removeUndoableEditListener(listener);
  }

  @Override
  public void undo() throws CannotUndoException {
    mySwingUndoManager.undo();
    fireUndoOrRedoHappened();
  }

  @Override
  public void undoableEdit(String localizedName, Runnable editImpl) {

    try {
      swingEditImpl = new UndoableEditImpl(localizedName, editImpl, this);
      mySwingUndoManager.addEdit(swingEditImpl);
      fireUndoableEditHappened(swingEditImpl);
    } catch (IOException e) {
      log.error("Exception", e);
    }
  }

  protected ParserFactory getParserFactory() {
    return myParserFactory;
  }

  DocumentManager getDocumentManager() {
    return myDocumentManager;
  }

  IGanttProject getProject() {
    return myProject;
  }

  private void fireUndoOrRedoHappened() {
    UndoableEditListener[] listeners = myUndoEventDispatcher.getUndoableEditListeners();
    for (int i = 0; i < listeners.length; i++) {
      ((GPUndoListener) listeners[i]).undoOrRedoHappened();
    }
  }

  private void fireUndoableEditHappened(UndoableEditImpl swingEditImpl) {
    myUndoEventDispatcher.postEdit(swingEditImpl);
  }
}
