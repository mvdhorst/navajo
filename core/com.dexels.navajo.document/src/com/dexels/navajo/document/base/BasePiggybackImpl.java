package com.dexels.navajo.document.base;

import java.util.*;

public class BasePiggybackImpl extends BaseNode {

	private static final long serialVersionUID = -4285472929344591538L;

	private final Map<String,String> piggyData;
	public BasePiggybackImpl(Map<String,String> init) {
		piggyData = init;
	}
	@Override
	public Map<String,String> getAttributes() {
		return piggyData;
	}

	@Override
	public List<BaseNode> getChildren() {
		return null;
	}

	@Override
	public String getTagName() {
		return "piggyback";
	}

}
