package com.dexels.navajo.tipi.vaadin.touch.components;

import com.dexels.navajo.tipi.vaadin.components.base.TipiVaadinComponentImpl;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class TipiNavigationView extends TipiVaadinComponentImpl {

	private static final long serialVersionUID = -5043895594246857632L;
	private NavigationView navigationView;

	@Override
	public Object createContainer() {
		navigationView = new NavigationView() {
			
			private static final long serialVersionUID = 6897448332261629281L;

			@Override
			protected void onBecomingVisible() {
				String constr = (String) getConstraints();
				if("noback".equals(constr)) {
					getNavigationBar().getLeftComponent().setVisible(false);
				}
			}
		};
		navigationView.getNavigationBar().setVisible(true);
		//		VerticalComponentGroup componentGroup = new VerticalComponentGroup();
		
		return navigationView;
	}

	@Override
	public void setComponentValue(final String name, final Object object) {
	    super.setComponentValue(name, object);
	        if (name.equals("title")) {
	        	navigationView.getNavigationBar().setCaption( (String) object);
	        }
	}

	@Override
	protected void addToVaadinContainer(ComponentContainer currentContainer,
			Component component, Object constraints) {
		if("right".equals(constraints)) {
			navigationView.setRightComponent(component);
		} else if("toolbar".equals(constraints)) {
			navigationView.setToolbar(component);
		} else {
			navigationView.setContent(component);;
//			super.addToVaadinContainer(currentContainer, component, constraints);
		}
	}
	
	
}
