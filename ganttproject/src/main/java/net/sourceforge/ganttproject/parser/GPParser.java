/*
 * Created on 12.03.2005
 */
package net.sourceforge.ganttproject.parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author bard
 */
public interface GPParser {
  void addParsingListener(ParsingListener listener);

  void addTagHandler(TagHandler handler);

  ParsingContext getContext();

  TagHandler getDefaultTagHandler();

  TagHandler getTimelineTagHandler();

  boolean load(InputStream inStream) throws IOException;
}
