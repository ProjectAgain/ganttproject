/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 GanttProject Team

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
package net.sourceforge.ganttproject.ui.gui;

import net.sourceforge.ganttproject.ui.viewmodel.option.DefaultEnumerationOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.GPOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.GPOptionGroup;
import net.sourceforge.ganttproject.ui.viewmodel.option.IntegerOption;
import net.sourceforge.ganttproject.ui.action.zoom.ZoomActionSet;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.ui.gui.scrolling.ScrollingManager;
import net.sourceforge.ganttproject.ui.gui.zoom.ZoomManager;
import net.sourceforge.ganttproject.model.task.TaskSelectionManager;
import net.sourceforge.ganttproject.model.task.TaskView;
import net.sourceforge.ganttproject.model.undo.GPUndoManager;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Locale;

/**
 * @author bard
 */
public interface UIFacade {
  ImageIcon DEFAULT_LOGO = new ImageIcon(UIFacade.class.getResource("/icons/big.png"));

   interface Dialog {
    void show();

    void hide();

    void layout();

    void center(Centering centering);
    //void resize();
  }

  public enum Centering {
    SCREEN, WINDOW
  };

  public enum Choice {
    YES, NO, CANCEL, OK
  };

  int DEFAULT_DPI = 96;

  int GANTT_INDEX = 0;

  int RESOURCES_INDEX = 1;

  IntegerOption getDpiOption();
  GPOption<String> getLafOption();

  ScrollingManager getScrollingManager();

  ZoomManager getZoomManager();

  /** @returns an object containing the zoom related actions */
  ZoomActionSet getZoomActionSet();

  GPUndoManager getUndoManager();

  void setLookAndFeel(GanttLookAndFeelInfo laf);

  GanttLookAndFeelInfo getLookAndFeel();

  Choice showConfirmationDialog(String message, String title);

  void showPopupMenu(Component invoker, Action[] actions, int x, int y);

  void showPopupMenu(Component invoker, Collection<Action> actions, int x, int y);

  void showOptionDialog(int messageType, String message, Action[] actions);

  Dialog createDialog(Component content, Action[] buttonActions, String title);

  void setStatusText(String text);

  void showErrorDialog(String errorMessage);

  void showNotificationDialog(NotificationChannel channel, String message);

  void showSettingsDialog(String pageID);
  /**
   * Shows the given exception in an error dialog and also puts it into the log
   * file
   *
   * @param e
   *          the exception to show (and log)
   */
  void showErrorDialog(Throwable e);

  NotificationManager getNotificationManager();

  GanttChart getGanttChart();

  TimelineChart getResourceChart();

  Chart getActiveChart();

  /** @return the index of the displayed tab. */
  int getViewIndex();

  void setViewIndex(int viewIndex);

  int getGanttDividerLocation();

  void setGanttDividerLocation(int location);

  int getResourceDividerLocation();

  void setResourceDividerLocation(int location);

  /** Refreshes the UI (ie repaints all tasks in the chart) */
  void refresh();

  Frame getMainFrame();

  Image getLogo();

  void setWorkbenchTitle(String title);

  TaskView getCurrentTaskView();

  TaskTreeUIFacade getTaskTree();

  ResourceTreeUIFacade getResourceTree();

  TaskSelectionManager getTaskSelectionManager();

  TaskSelectionContext getTaskSelectionContext();

  DefaultEnumerationOption<Locale> getLanguageOption();

  GPOptionGroup[] getOptions();

  void addOnUpdateComponentTreeUi(Runnable callback);
}