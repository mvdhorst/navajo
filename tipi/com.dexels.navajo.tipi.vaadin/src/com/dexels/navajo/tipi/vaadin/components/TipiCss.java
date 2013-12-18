package com.dexels.navajo.tipi.vaadin.components;

import com.dexels.navajo.tipi.vaadin.components.base.TipiVaadinComponentImpl;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;

public class TipiCss extends TipiVaadinComponentImpl {

	private static final long serialVersionUID = -8463738923426799145L;
	
	
	@Override
	public Object createContainer() {
		return new CssLayout();
	}

	@Override
	protected void addToVaadinContainer(ComponentContainer currentContainer, Component component, Object constraints) {
//		CssLayout hl = (CssLayout)currentContainer;
		super.addToVaadinContainer(currentContainer, component, constraints);

	}

	
}
