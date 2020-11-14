/*
Copyright (C) 2004-2012 GanttProject Team

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
package net.sourceforge.ganttproject.ui.chart.scene;

import net.sourceforge.ganttproject.ui.chart.canvas.Canvas;
import net.sourceforge.ganttproject.ui.chart.canvas.Canvas.TextGroup;
import net.sourceforge.ganttproject.ui.chart.canvas.TextMetrics;
import net.sourceforge.ganttproject.ui.chart.canvas.TextSelector;
import net.sourceforge.ganttproject.ui.chart.grid.Offset;
import net.sourceforge.ganttproject.ui.chart.grid.OffsetList;
import net.sourceforge.ganttproject.ui.chart.text.TimeFormatter;
import net.sourceforge.ganttproject.ui.chart.text.TimeUnitText;
import net.sourceforge.ganttproject.ui.chart.text.TimeUnitText.Position;
import net.sourceforge.ganttproject.model.time.TimeUnit;

import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * Renders chart timeline.
 */
public class TimelineSceneBuilder extends AbstractSceneBuilder {

  private Canvas myTimelineContainer;
  private final InputApi myInputApi;
  private final BottomUnitSceneBuilder myBottomUnitLineRenderer;

  public static interface InputApi {
    int getViewportWidth();
    int getTopLineHeight();
    int getTimelineHeight();
    Color getTimelineBackgroundColor();
    Color getTimelineBorderColor();

    Date getViewportStartDate();
    OffsetList getTopUnitOffsets();
    OffsetList getBottomUnitOffsets();

    TimeFormatter getFormatter(TimeUnit timeUnit, TimeUnitText.Position position);
  }

  public TimelineSceneBuilder(InputApi inputApi) {
    myInputApi = inputApi;
    getCanvas().newLayer();
    getCanvas().newLayer();
    getCanvas().newLayer();
    getCanvas().newLayer();
    myTimelineContainer = getCanvas().newLayer();
    myBottomUnitLineRenderer = new BottomUnitSceneBuilder(
        myTimelineContainer, new BottomUnitSceneBuilder.InputApi() {
          @Override
          public int getTopLineHeight() {
            return myInputApi.getTopLineHeight();
          }
          @Override
          public OffsetList getBottomUnitOffsets() {
            return myInputApi.getBottomUnitOffsets();
          }
          @Override
          public TimeFormatter getFormatter(TimeUnit offsetUnit, Position position) {
            return myInputApi.getFormatter(offsetUnit, position);
          }
        });
  }

  public Canvas getTimelineContainer() {
    return myTimelineContainer;
  }

  @Override
  public void reset(int sceneHeight) {
    super.reset(sceneHeight);
    myBottomUnitLineRenderer.reset(getHeight());
  }

  /**
   * Draws the timeline box
   */
  private void renderUnderlay() {
    int sizex = myInputApi.getViewportWidth();
    final int spanningHeaderHeight = myInputApi.getTopLineHeight();
    final int headerHeight = myInputApi.getTimelineHeight();
    Canvas container = myTimelineContainer;
    Canvas.Rectangle headerRectangle = container.createRectangle(0, 0, sizex, headerHeight);
    headerRectangle.setStyle("timeline.area");
    //headerRectangle.setBackgroundColor(myInputApi.getTimelineBackgroundColor());

    Canvas.Line timeunitHeaderBorder = container.createLine(0, spanningHeaderHeight,
        sizex - 1, spanningHeaderHeight);
    timeunitHeaderBorder.setStyle("timeline.lineSplitter");
    //timeunitHeaderBorder.setForegroundColor(myInputApi.getTimelineBorderColor());
    Canvas.Line bottomBorder = getTimelineContainer().createLine(0, headerHeight - 1, sizex - 2,
        headerHeight - 1);
    bottomBorder.setStyle("timeline.borderBottom");
  }

  @Override
  public void build() {
    renderUnderlay();
    renderTopUnits();
    renderBottomUnits();
  }

  /**
   * Draws cells of the top line in the time line
   */
  private void renderTopUnits() {
    Date curDate = myInputApi.getViewportStartDate();
    List<Offset> topOffsets = myInputApi.getTopUnitOffsets();
    int curX = topOffsets.get(0).getOffsetPixels();
    if (curX > 0) {
      curX = 0;
    }
    final int topUnitHeight = myInputApi.getTopLineHeight();
    TextGroup textGroup = myTimelineContainer.createTextGroup(0, 0, topUnitHeight, "timeline.top");
    for (Offset nextOffset : topOffsets) {
      if (curX >= 0) {
        TimeUnitText[] texts = myInputApi.getFormatter(nextOffset.getOffsetUnit(), TimeUnitText.Position.UPPER_LINE)
            .format(nextOffset.getOffsetUnit(), curDate);
        final int maxWidth = nextOffset.getOffsetPixels() - curX - 5;
        final TimeUnitText timeUnitText = texts[0];
        textGroup.addText(curX + 5, 0, new TextSelector() {
          @Override
          public Canvas.Label[] getLabels(TextMetrics textLengthCalculator) {
            return timeUnitText.getLabels(maxWidth, textLengthCalculator);
          }
        });
        getTimelineContainer().createLine(curX, topUnitHeight - 10, curX, topUnitHeight);
      }
      curX = nextOffset.getOffsetPixels();
      curDate = nextOffset.getOffsetEnd();
    }
  }

  /**
   * Draws cells of the bottom line in the time line
   */
  private void renderBottomUnits() {
    myBottomUnitLineRenderer.build();
  }
}