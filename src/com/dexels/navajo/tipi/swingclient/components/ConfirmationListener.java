package com.dexels.navajo.tipi.swingclient.components;

import com.dexels.navajo.document.Message;

/**
 * <p>Title: Seperate project for Navajo Swing client</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dexels</p>
 * @author not attributable
 * @version 1.0
 */

public interface ConfirmationListener {
  public void confirmationNeeded(Message m, String type);
}
