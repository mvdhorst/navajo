package com.dexels.navajo.server;

import com.dexels.navajo.script.api.Access;

public class HandlerFactory {

	public static ServiceHandler createHandler(String handler, NavajoConfigInterface navajoConfig, Access access) {

		ServiceHandler sh = new GenericHandler(navajoConfig);
		sh.setInput(access);
		return sh;
	}
	
}
