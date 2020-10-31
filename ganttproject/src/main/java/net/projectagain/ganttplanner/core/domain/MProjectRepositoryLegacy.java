package net.projectagain.ganttplanner.core.domain;

import net.sourceforge.ganttproject.model.GanttPreviousState;
import net.sourceforge.ganttproject.model.document.Document;

import java.io.IOException;
import java.util.List;

public interface MProjectRepositoryLegacy {
  void close();

  List<GanttPreviousState> getBaselines();

  Document getDocument();

  void setDocument(Document document);

  boolean isModified();

  void setModified(boolean modified);

  void open(Document document) throws IOException, Document.DocumentException;

  void setModified();
}
