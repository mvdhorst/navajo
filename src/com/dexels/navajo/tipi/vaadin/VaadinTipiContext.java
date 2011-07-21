package com.dexels.navajo.tipi.vaadin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tipi.TipiApplicationInstance;

import com.dexels.navajo.tipi.TipiBreakException;
import com.dexels.navajo.tipi.TipiContext;
import com.dexels.navajo.tipi.internal.FileResourceLoader;
import com.dexels.navajo.tipi.vaadin.application.TipiVaadinApplication;
import com.dexels.navajo.tipi.vaadin.cookie.BrowserCookieManager;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class VaadinTipiContext extends TipiContext {
 
	private static final long serialVersionUID = -5277282822136332687L;

	private static Logger logger = LoggerFactory.getLogger(VaadinTipiContext.class);
	
	private Window mainWindow;


	private String contextName = null;
	
	public VaadinTipiContext(TipiApplicationInstance myApplication) {
		super(myApplication, null);
		
		
		File install = getVaadinApplication().getInstallationFolder();
		logger.info("Using install: "+install.getAbsolutePath());
		File tipi = new File(install,"tipi");
		File resource = new File(install,"resource");
		setTipiResourceLoader(new FileResourceLoader(tipi));
		setGenericResourceLoader(new FileResourceLoader(resource));
		setCookieManager(new BrowserCookieManager());
		TipiScreen ts = new TipiScreen(this);
		setDefaultTopLevel(ts);
	}



	/**
	 * Maybe we can loosen up this constraint, at some point, but for now: GAE doesn't like it.
	 */
	@Override
	public int getPoolSize() {
		return 0;
	}



	@Override
	public void exit() {
		// TODO Auto-generated method stub
		super.exit();
		getVaadinApplication().close();
	}



	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		super.shutdown();
	}



	public String getContextName() {
		return contextName;
	}


	public void setContextName(String contextName) {
		this.contextName = contextName;
	}


	@Override
	public void runSyncInEventThread(Runnable r) {
		r.run();
	}

	@Override
	public void runAsyncInEventThread(Runnable r) {
		r.run();

	}

	@Override
	public void setSplash(Object s) {
		logger.info("Splash {}",s);
	}

	@Override
	public void clearTopScreen() {

	}

	@Override
	public void setSplashVisible(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSplashInfo(String s) {
//		logger.info("Splash {}",s);

	}

	@Override
	public void showInfo(String text, String title) {
		Notification not = new Notification(text, title);
		mainWindow.showNotification(not);
	}

	@Override
	public void showQuestion(String text, String title, String[] options)
			throws TipiBreakException {

	}

	public void setMainWindow(Window mainWindow) {
		this.mainWindow = mainWindow;
	}

	public TipiVaadinApplication getVaadinApplication() {
		return (TipiVaadinApplication) this.getApplicationInstance();
	}

	
}
