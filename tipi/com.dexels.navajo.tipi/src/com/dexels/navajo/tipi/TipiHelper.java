package com.dexels.navajo.tipi;

import java.io.Serializable;

import com.dexels.navajo.tipi.internal.TipiEvent;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public interface TipiHelper extends Serializable {
	public void initHelper(TipiComponent tc);

	public void setComponentValue(String name, Object object);

	public Object getComponentValue(String name);

	public void registerEvent(TipiEvent te);

	public void deregisterEvent(TipiEvent te);
}