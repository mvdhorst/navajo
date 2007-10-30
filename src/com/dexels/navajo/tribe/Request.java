package com.dexels.navajo.tribe;

import java.io.Serializable;

import com.dexels.navajo.server.Dispatcher;
import com.dexels.navajo.util.Util;

public abstract class Request implements Serializable {

	public String owner = null;
	public Answer predefined = null;
	private String guid = null;
	
	public Request() {
		owner = Dispatcher.getInstance().getNavajoConfig().getInstanceName();
		guid = hashCode() + "-" + System.currentTimeMillis();
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setPredefinedAnswer(Answer a) {
		predefined = a;
	}
	
	public Answer getPredefinedAnswer() {
		return predefined;
	}
	
	public abstract Answer getAnswer();

	public String getGuid() {
		return guid;
	}
}
