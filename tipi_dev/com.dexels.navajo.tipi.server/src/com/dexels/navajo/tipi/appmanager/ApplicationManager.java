package com.dexels.navajo.tipi.appmanager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import com.dexels.navajo.tipi.appmanager.ApplicationStatus;

public interface ApplicationManager {

	public abstract String getDocumentationRepository();

	public abstract void setCurrentApplication(String currentApplication);

	public abstract String getCurrentApplication();

	public abstract ApplicationStatus getApplication();

	public abstract ServletContext getContext();

	public abstract void setContext(ServletContext context) throws IOException;

	public abstract void setApplications(List<ApplicationStatus> applications);

	public abstract File getAppsFolder();

	public abstract void setAppsFolder(File appsFolder) throws IOException;

	public abstract List<ApplicationStatus> getApplications();

}