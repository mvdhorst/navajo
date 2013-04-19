package com.dexels.navajo.document.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.types.impl.TypeManagerImpl;

import navajodocument.Version;

public class TypeManagerFactory {

	private static TypeManager instance;
	
	private final static Logger logger = LoggerFactory
			.getLogger(TypeManagerFactory.class);
	
	public static synchronized TypeManager getInstance() {
		if(instance!=null) {
			return instance;
		}
		if(Version.osgiActive()) {
			logger.warn("No TypeManager found in OSGi mode");
			return null;
		}
		// legacy mode:
		instance = new TypeManagerImpl();
		try {
			instance.readTypes();
		} catch (Exception e) {
			logger.warn("Trouble reading types in legacy (non-OSGi) mode",e);
		}
		return instance;
	}
	
	public static synchronized void setInstance(TypeManager instance) {
		TypeManagerFactory.instance = instance;
	}

}
