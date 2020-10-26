/*
 * Created on 25.04.2005
 */
package net.sourceforge.ganttproject.application;

import net.sourceforge.ganttproject.GanttProject;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author bard
 */
public class MainApplication implements IPlatformRunnable {
  private static final Logger log = getLogger(MainApplication.class);

  private Object myLock = new Object();

  // The hack with waiting is necessary because when you
  // launch Runtime Workbench in Eclipse, it exists as soon as
  // GanttProject.main() method exits
  // without Eclipse, Swing thread continues execution. So we wait until main
  // window closes
  @Override
  public Object run(Object args) throws Exception {
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    String[] cmdLine = (String[]) args;
    Runnable onApplicationQuit = new Runnable() {
      public void run() {
        synchronized(myLock) {
          myLock.notify();
        }
      }
    };
    GanttProject.setApplicationQuitCallback(onApplicationQuit);
    if (GanttProject.main(cmdLine)) {
      synchronized (myLock) {
        log.trace("Waiting until main window closes");
        myLock.wait();
        log.trace("Main window has closed");
      }
    }
    log.info("Program terminated");
    System.exit(0);
    return null;
  }

}
