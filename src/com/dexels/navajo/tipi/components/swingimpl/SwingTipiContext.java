package com.dexels.navajo.tipi.components.swingimpl;

import java.lang.reflect.*;
import java.awt.*;
import javax.swing.*;
import com.dexels.navajo.tipi.*;
import com.dexels.navajo.tipi.components.swingimpl.swing.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class SwingTipiContext
    extends TipiContext {
  private TipiSwingSplash splash;

  private final Set threadSet = Collections.synchronizedSet(new HashSet());

  public SwingTipiContext() {
    setDefaultTopLevel(new TipiScreen());
    getDefaultTopLevel().setContext(this);
  }

  public synchronized void setWaiting(boolean b) {
    myThreadPool.write("Setting wait to: "+b);
    for (int i = 0; i < rootPaneList.size(); i++) {
      TipiComponent tc = (TipiComponent) rootPaneList.get(i);
      ( (Container) tc.getContainer()).setCursor(b ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }
    for (int j = 0; j < myActivityListeners.size(); j++) {
      TipiActivityListener tal = (TipiActivityListener) myActivityListeners.get(j);
      tal.setActive(b);
    }
  }

  public void clearTopScreen() {
    ( (TipiScreen) topScreen).clearTopScreen();
  }

  public void setSplashInfo(final String info) {
    if (splash != null) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            splash.setInfoText(info);
          }
        });
      }
      catch (InvocationTargetException ex) {
        ex.printStackTrace();
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void setSplashVisible(boolean b) {
    if (splash != null) {
      splash.setVisible(b);
    }
  }

  public void setSplash(Object s) {
    splash = (TipiSwingSplash) s;
  }
  public void threadStarted(Thread workThread) {
    if (threadSet==null) {
      return;
    }
    super.threadStarted(workThread);
    threadSet.add(workThread);
    setActiveThreads(threadSet.size());
    if (!threadSet.isEmpty()) {
          setWaiting(true);
    }
  }

  public void threadEnded(Thread workThread) {
    super.threadEnded(workThread);
    if (threadSet==null) {
      return;
    }
    threadSet.remove(workThread);
    setActiveThreads(threadSet.size());
    if (threadSet.isEmpty()) {
      setWaiting(false);
    }
  }


}
