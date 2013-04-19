package com.dexels.navajo.document.types.impl;

import com.dexels.navajo.document.types.Type;

public class TypeImpl implements Type {
	private String name;
	private String type;
	private Class<?> clazz;

	TypeImpl(String name, String type, Class<?> clazz) {
		this.name = name;
		this.type = type;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public Class<?> getClazz() {
		return clazz;
	}

	
}
