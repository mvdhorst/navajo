package com.dexels.navajo.server.statistics;

import com.dexels.navajo.server.jmx.GenericThreadMXBean;

public interface StatisticsRunnerMXBean extends GenericThreadMXBean {

	 public int getTodoCount();
	 public void setEnabled(boolean b);
	 public boolean isEnabled();
	 public String getStoreClass();
	 
}
