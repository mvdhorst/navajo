package com.dexels.navajo.client.nql.internal.command;

import com.dexels.navajo.client.ClientException;
import com.dexels.navajo.client.nql.NQLCommand;
import com.dexels.navajo.client.nql.OutputCallback;
import com.dexels.navajo.client.nql.internal.NQLContext;
import com.dexels.navajo.document.NavajoException;

public class CallCommand implements NQLCommand {

	private String service = null;
	@Override
	public void execute(NQLContext context, OutputCallback callback) throws ClientException,NavajoException {
		context.call(service,true);
	}

	@Override
	public void parse(String raw) {
		String[] parts = raw.split(":");
		service = parts[1];
	}

}
