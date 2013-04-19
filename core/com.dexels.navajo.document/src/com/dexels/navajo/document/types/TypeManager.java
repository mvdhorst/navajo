package com.dexels.navajo.document.types;

import java.util.Set;

public interface TypeManager {

	public abstract void addNavajoType(String typeId, Class<?> clz);

	public abstract String getNavajoType(Class<?> c);

	public abstract Class<?> getJavaType(String p);

	public abstract Set<String> getNavajoTypes();

	/**
	 * Already deprecated
	 * @throws Exception 
	 */
	public void readTypes() throws Exception;
}