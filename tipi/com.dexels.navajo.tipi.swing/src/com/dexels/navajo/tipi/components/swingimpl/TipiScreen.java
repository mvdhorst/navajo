package com.dexels.navajo.tipi.components.swingimpl;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JApplet;
import javax.swing.JInternalFrame;
import javax.swing.RootPaneContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.TipiComponent;
import com.dexels.navajo.tipi.TipiContext;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class TipiScreen extends TipiSwingDataComponentImpl{
	private static final long serialVersionUID = -4453008939836688032L;

	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiScreen.class);
	
	public TipiScreen() {
		setId("init");
	}

	@Override
	public Object createContainer() {
		return null;
	}

	@Override
	public Object getContainer() {
		return getTopLevel();
	}

	
	@Override
	public void addToContainer(final Object c, final Object constraints) {
		final Component current = (Component) c;
		runSyncInEventThread(new Runnable() {
			@Override
			public void run() {

				if (current != null && Window.class.isInstance(current)) {
					// logger.debug("Not null, and window");
					current.setVisible(true);
				} else {
					if (c instanceof JApplet) {
						// ok
					} else {
						logger.error("**************** SHOULD NOT REALLY BE HERE: "
										+ current);
						Thread.dumpStack();
					}
				}
			}
		});

	}

	@Override
	public void removeFromContainer(Object c) {
		final Component current = (Component) c;
		if (current == null) {
			return;
		}
		if (Window.class.isInstance(current)
				|| JInternalFrame.class.isInstance(current)) {
			current.setVisible(false);
		} else {
			logger.debug("NOT studio mode, but not of type window: "
					+ current.getClass());
		}

	}

	@Override
	public TipiComponent getTipiComponentByPath(String path) {
		if (path.equals(".")) {
			return this;
		}
		if (path.equals("..")) {
			return null;
		}
		if (path.startsWith("..")) {
			return null;
		}
		if (path.indexOf("/") == 0) {
			path = path.substring(1);
		}
		int s = path.indexOf("/");
		if (s == -1) {
			if (path.equals("")) {
				return myContext.getDefaultTopLevel();
			}
			return getTipiComponent(path, false);
		} else {
			String name = path.substring(0, s);
			String rest = path.substring(s);
			TipiComponent t = getTipiComponent(name, false);
			if (t == null) {
				throw new NullPointerException("Did not find Tipi: " + name);
			}
			return t.getTipiComponentByPath(rest);
		}
	}

	// For now, always return the first frame. Maybe enhance later or something
	public RootPaneContainer getTopLevel() {
		for (int i = 0; i < getChildCount(); i++) {
			TipiComponent current = getTipiComponent(i);
			if (RootPaneContainer.class.isInstance(current.getContainer())) {
				return (RootPaneContainer) current.getContainer();
			}
		}
		logger.debug("RETURNING NULL. OH DEAR");
		return null;
	}

	@Override
	public String getPath(String typedef) {
		return typedef;
	}

	@Override
	public String toString() {
		super.toString();
		return "screen";
	}

	public void clearTopScreen() {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			TipiComponent current = getTipiComponent(i);
			myContext.disposeTipiComponent(current);
		}
	}

	@Override
	public void addComponent(TipiComponent tc, TipiContext context,
			Object constraints) {

		if (tc == null) {
			logger.error("And I thought that this would never happen. Nice.");
			Thread.dumpStack();
			return;
		}
		super.addComponent(tc, context, constraints);
	}
}
