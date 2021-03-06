package com.dexels.navajo.tipi.components.echoimpl;

import nextapp.echo2.app.TextField;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class TipiTextField extends TipiEchoComponentImpl {
	private static final long serialVersionUID = 5579720008151004727L;

	public TipiTextField() {
    }

    public Object createContainer() {
        TextField b = new TextField();
//        b.setStyleName("Default");
      
                return b;
    }

    protected void setComponentValue(String name, Object object) {
        TextField b = (TextField) getContainer();
        if ("text".equals(name)) {
            b.setText("" + object);
        }
        if ("enabled".equals(name)) {
            b.setEnabled("true".equals(object));
        }
        super.setComponentValue(name, object);
    }

}
