package com.dexels.navajo.tipi.appmanager.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.appmanager.ApplicationManager;
import com.dexels.navajo.tipi.appmanager.ApplicationStatus;

public class ApplicationManagerImpl implements ApplicationManager {
	
	
	private final static Logger logger = LoggerFactory
			.getLogger(ApplicationManagerImpl.class);
	private File appsFolder;
	List<ApplicationStatus> applications;
	private String currentApplication;
	private String documentationRepository;
	

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getDocumentationRepository()
	 */
	@Override
	public String getDocumentationRepository() {
		return documentationRepository;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#setCurrentApplication(java.lang.String)
	 */
	@Override
	public void setCurrentApplication(String currentApplication) {
		this.currentApplication = currentApplication;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getCurrentApplication()
	 */
	@Override
	public String getCurrentApplication() {
		return currentApplication;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getApplication()
	 */
	@Override
	public ApplicationStatus getApplication() {
		for (ApplicationStatus a : applications) {
			if(a.getApplicationName().equals(currentApplication)) {
				return a;
			}
		}
		return null;
	}
private ServletContext context = null;
	
	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getContext()
	 */
	@Override
	public ServletContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#setContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setContext(ServletContext context) throws IOException {
		this.context = context;
		documentationRepository = context.getInitParameter("documentationRepository");
		String appFolder = context.getInitParameter("appFolder"); 
		File ff = null;
		if(appFolder==null) {
			ff = new File("/Users/frank/Documents/workspace42/"); //context.getRealPath("."));
//			ff = new File(contextFolder, "DefaultApps");
		} else {
			File suppliedFolder = new File(appFolder);
			if(suppliedFolder.isAbsolute()) {
				ff = suppliedFolder;
			} else {
				ff = new File(context.getRealPath(appFolder));
			}
		}
		setAppsFolder(ff);

	}
	
	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#setApplications(java.util.List)
	 */
	@Override
	public void setApplications(List<ApplicationStatus> applications) {
		this.applications = applications;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getAppsFolder()
	 */
	@Override
	public File getAppsFolder() {
		return appsFolder;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#setAppsFolder(java.io.File)
	 */
	@Override
	public void setAppsFolder(File appsFolder) throws IOException {
		
		this.appsFolder = appsFolder;
		File[] apps = appsFolder.listFiles();
		List<ApplicationStatus> appStats = new LinkedList<ApplicationStatus>();
		this.applications = appStats;
		if(apps==null) {
			return;
		}
		for (File file : apps) {
			if(file.getName().equals("WEB-INF")) {
				continue;
			}
			if(file.getName().equals("META-INF")) {
				continue;
			}
			if(!file.isDirectory()) {
				continue;
			}
			if(!isTipiAppDir(file)) {
				continue;
			}
			ApplicationStatus appStatus = new ApplicationStatus();
			appStatus.setManager(this);
			appStatus.load(file);
			appStats.add(appStatus);
			Collections.sort(appStats);
		}
	}

	private boolean isTipiAppDir(File tipiRoot) {
		File tipiDir = new File(tipiRoot,"tipi");
		File settingsProp = new File(tipiRoot,"settings/tipi.properties");
		return tipiDir.exists() && settingsProp.exists();
//		return false;
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.tipi.appmanager.impl.ApplicationManager#getApplications()
	 */
	@Override
	public  List<ApplicationStatus> getApplications()  {
//		logger.info("Getting applications: "+applications);
		return applications;
	}
	
	public static void main(String[] args) throws IOException {
		ApplicationManager m = new ApplicationManagerImpl();
		m.setAppsFolder(new File("WebContent"));
		List<ApplicationStatus> apps = m.getApplications();
		logger.info("Appcount: "+apps.size());
	}
}
