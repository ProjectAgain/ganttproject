package biz.ganttproject.lib.fx

import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.swing.SwingUtilities

/**
 * @author dbarashev@bardsoftware.com
 */
fun openInBrowser(url: String) {
  SwingUtilities.invokeLater {
    try {
      Desktop.getDesktop().browse(URI(url))
    } catch (e: IOException) {
      LoggerFactory.getLogger("biz.ganttproject.lib.fx.openInBrowser").error("{}", e)
    } catch (e: URISyntaxException) {
      LoggerFactory.getLogger("biz.ganttproject.lib.fx.openInBrowser").error("{}", e)
    }
  }
}

fun isBrowseSupported(): Boolean = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
