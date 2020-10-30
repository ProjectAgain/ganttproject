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

import net.projectagain.ganttplanner.core.plugins.ExtensionComponent;
import net.sourceforge.ganttproject.export.WebPublisher;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.ui.action.OkAction;
import net.sourceforge.ganttproject.ui.gui.options.OptionPageProviderBase;
import net.sourceforge.ganttproject.ui.gui.options.OptionsPageBuilder;
import net.sourceforge.ganttproject.ui.gui.options.OptionsPageBuilder.I18N;
import net.sourceforge.ganttproject.ui.viewmodel.option.ChangeValueEvent;
import net.sourceforge.ganttproject.ui.viewmodel.option.ChangeValueListener;
import net.sourceforge.ganttproject.ui.viewmodel.option.DefaultStringOption;
import net.sourceforge.ganttproject.ui.viewmodel.option.GPOptionGroup;
import org.eclipse.core.runtime.IStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

@ExtensionComponent
public class NetworkOptionPageProvider extends OptionPageProviderBase {

  public NetworkOptionPageProvider() {
    super("impex.ftp");
  }

  @Override
  public Component buildPageComponent() {
    OptionsPageBuilder builder = new OptionsPageBuilder();
    final GPOptionGroup ftpGroup = getProject().getDocumentManager().getNetworkOptionGroups()[0];
    ftpGroup.setTitled(false);
    I18N i18n = new OptionsPageBuilder.I18N();

    final DefaultStringOption usernameOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.USERNAME_OPTION_ID);
    ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(usernameOption), "ftpuser");

    final DefaultStringOption servernameOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.SERVERNAME_OPTION_ID);
    ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(servernameOption), "ftpserver");

    final DefaultStringOption dirnameOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.DIRECTORYNAME_OPTION_ID);
    ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(dirnameOption), "ftpdirectory");

    final DefaultStringOption passwordOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.PASSWORD_OPTION_ID);
    ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(passwordOption), "ftppwd");

    final JComponent optionsPane = builder.buildPage(new GPOptionGroup[]{ftpGroup}, getPageID());
    final Action testConnectionAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        WebPublisher.Ftp ftp = new WebPublisher.Ftp();
        try {
          IStatus status = ftp.loginAndChangedir(getProject().getDocumentManager().getFTPOptions());
          if (status.isOK()) {
            getUiFacade().showOptionDialog(
              JOptionPane.INFORMATION_MESSAGE,
              GanttLanguage.getInstance().getText("successFTPConnection"),
              new Action[]{OkAction.createVoidAction("ok")}
            );
          } else {
            getUiFacade().showErrorDialog(status.getMessage());
          }
        } catch (IOException e1) {
          getUiFacade().showErrorDialog(e1);
        }
      }

      {
        putValue(Action.NAME, GanttLanguage.getInstance().getText("testFTPConnection"));
        setEnabled(canEnableTestAction(ftpGroup));
      }
    };
    ChangeValueListener listener = new ChangeValueListener() {
      @Override
      public void changeValue(ChangeValueEvent event) {
        testConnectionAction.setEnabled(canEnableTestAction(ftpGroup));
      }
    };
    servernameOption.addChangeValueListener(listener);
    usernameOption.addChangeValueListener(listener);
    passwordOption.addChangeValueListener(listener);
    JPanel result = new JPanel(new BorderLayout());
    result.setBorder(new EmptyBorder(5, 5, 5, 5));
    result.add(optionsPane, BorderLayout.NORTH);
    JButton testConnectionButton = new JButton(testConnectionAction);
    testConnectionButton.setAlignmentX(SwingConstants.RIGHT);

    JPanel connectionWrapper = new JPanel(new BorderLayout());
    connectionWrapper.add(testConnectionButton, BorderLayout.NORTH);

    result.add(connectionWrapper, BorderLayout.CENTER);
    return result;
  }

  @Override
  public GPOptionGroup[] getOptionGroups() {
    return new GPOptionGroup[]{getProject().getDocumentManager().getFTPOptions()};
  }

  @Override
  public boolean hasCustomComponent() {
    return true;
  }

  private boolean canEnableTestAction(GPOptionGroup ftpGroup) {
    final DefaultStringOption usernameOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.USERNAME_OPTION_ID);
    final DefaultStringOption servernameOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.SERVERNAME_OPTION_ID);
    final DefaultStringOption passwordOption =
      (DefaultStringOption) ftpGroup.getOption(DocumentCreator.PASSWORD_OPTION_ID);
    return servernameOption.getValue() != null && usernameOption.getValue() != null
           && passwordOption.getValue() != null;
  }
}
