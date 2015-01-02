package com.dexels.navajo.tipi.vaadin.bridged;

import java.util.Hashtable;

import javax.servlet.ServletException;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.vaadin.application.servlet.TipiVaadinServlet;

public class VaadinComponent {
	private static final String VAADIN_DIR_PREFIX = "/VAADIN";
	private static final String SERVLET_ALIAS = "/";
	private HttpService httpService;
	
	private final static Logger logger = LoggerFactory
			.getLogger(VaadinComponent.class);
	private TipiVaadinServlet servlet;
	private HttpContext commonContext;

	public void setHttpService(HttpService httpService) {
		if(this.httpService!=null) {
			logger.warn("Multiple httpServices present? Odd. Might be trouble.");
		}
		logger.info("Attaching to HTTP Service");
		this.httpService = httpService;

	}

	/**
	 * @param httpService The httpservice to remove
	 */
	public void removeHttpService(HttpService httpService) {
		logger.info("Disconnecting http.");
	}

	protected void startup() {
		try {
		logger.info("Vaadin has everything it needs, starting up...");
		servlet = new TipiVaadinServlet();
		commonContext = httpService.createDefaultHttpContext();
		Hashtable<String,String> ht = new Hashtable<String,String>();
		ht.put("application", "com.dexels.navajo.tipi.vaadin.application.TipiVaadinApplication");
		VaadinFileServlet vfs = new VaadinFileServlet();
		httpService.registerServlet(SERVLET_ALIAS, servlet, ht, commonContext);
		httpService.registerServlet(VAADIN_DIR_PREFIX, vfs, null, commonContext);
	} catch (ServletException e) {
		logger.error("Error: ",e);
	} catch (NamespaceException e) {
		logger.error("Error: ",e);
	}
	}

	protected void shutdown() {
		logger.info("SHUTTIN DOWN!");

	}
}