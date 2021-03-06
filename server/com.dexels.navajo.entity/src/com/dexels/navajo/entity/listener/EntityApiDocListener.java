package com.dexels.navajo.entity.listener;


import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.NavajoLaszloConverter;
import com.dexels.navajo.document.Operation;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.json.JSONTML;
import com.dexels.navajo.document.json.JSONTMLFactory;
import com.dexels.navajo.entity.Entity;
import com.dexels.navajo.entity.EntityManager;
import com.dexels.navajo.entity.Key;


public class EntityApiDocListener extends HttpServlet  implements ResourceMapping {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2642151786192206338L;

	private final static Logger logger = LoggerFactory.getLogger(EntityApiDocListener.class);

	private EntityManager myManager;

	private final DefaultResourceMapping resourceMapping = new DefaultResourceMapping();

	
	
	public void activate() {
		resourceMapping.setAlias("/entityApi");
		resourceMapping.setPath("entityApi");

	}

	@Override
	public String getAlias() {
		return resourceMapping.getAlias();
	}

	@Override
	public String getHttpContextId() {
		return resourceMapping.getHttpContextId();
	}

	@Override
	public String getPath() {
		return resourceMapping.getPath();
	}
	
	
	
	public void setEntityManager(EntityManager em) {
		myManager = em;
	}

	public void clearEntityManager(EntityManager em) {
		myManager = null;
	}


	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String path = request.getPathInfo();
		String basePath = "";

		if (path != null && !path.equals("/")) {
			for (String subPath : path.split("/")) {
				basePath += subPath + ".";
			}
			basePath = basePath.substring(1);
		}
		
		
		String out = "";
		out += "<!DOCTYPE html>";
		out += "<html>";
		out += "<head>";
		out += " <title>Navajo Entity API documentation</title>";
		out += " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /> ";
		out += " <link rel=\"stylesheet\" href=\"/entityApi/css/style.css\"> ";
		out += "<script type=\"text/javascript\" src=\"/entityApi/jquery-1.9.1.min.js\" ></script>";
		out += "<script type=\"text/javascript\" src=\"/entityApi/run_prettify.js\" ></script>";
		out += "<script type=\"text/javascript\" src=\"/entityApi/entity.js\" ></script>";
		out +=  "</head>";

		out += "<body class=\"bodycenter\">";	
		out += "<h1>Entity API Documentation</h1>";
		
		List<String> entityNames = myManager.getRegisteredEntities(basePath);
		for (String entityName : entityNames) {
			Entity e = myManager.getEntity(entityName);
			
			Map<String, Operation> ops = myManager.getOperations(entityName);
			String entityNameUrl = entityName.replace(".", "/");

			Navajo n = NavajoFactory.getInstance().createNavajo();
			n.addMessage(e.getMessage());
			
			for (String op : ops.keySet()) {
				out += "<ul class=\"operations\">";
				out += "<li class=\"operation " + op + "\" >";
				out += "<a href=\"#\" > ";
				out += "<div class=\"operationHeader " + op + "\">";
				out += "<div class=\"method http" + op + "\">" + op + "</div>";
				out += "<div class=\"url\" > /" + entityNameUrl + "</div>";
				out += "<div class=\"descrption " + op + "\" > " + operationDescription(op)
						+ entityNameUrl + "</div>";
				out += "</div>"; // operationHeader
				out += "</a>";
				out += "<div class=\"entityDescription\" style=\"display: none \">  ";
				if ((op.equals(Operation.GET) || op.equals(Operation.DELETE))
						&& e.getKeys().size() > 0) {
					out += printRequestKeysDefinition(e);
				} else {
					// Writing entire object
					out += "<h2> Request </h2>";
					out += "<pre class=\"prettyprint\">";
					out += writeEntityJson(n, "request");
					out += "</pre>";
				}

				out += "<h2> Response </h2>";
				out += "<small> <a href=\"#\" class=\"outputFormatJSON\">JSON</a></small> ";
				out += "<small> |  <a href=\"#\" class=\"outputFormatXML\">XML</a> </small>";
				
				
				out += "<div class=\"JSON\">";
				out += "<pre class=\"prettyprint\">";
				out += writeEntityJson(n, "response");
				out += "</pre>";
				out += "</div>"; //; JSON div
				
				out += "<div class=\"XML\" style=\"display: none; \" \">";
				out += "<pre class=\"prettyprint lang-xml\">";
				out +=  StringEscapeUtils.escapeHtml(writeEntityXml(n));
				out += "</pre>";
				out += "</div>"; //; XML div
								
				out += printPropertiesDescription(e);

				out += "</div>";// description div
				out += "</li></ul>"; 
			}			
		}
		
		out += "</body>";
		out += "</html>";
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getOutputStream().write(out.getBytes());
		
	}

	private String printPropertiesDescription(Entity e) {
		String result = "<h2> Description </h2>";
		boolean hasDescriptions = false;
		
		// Check entity message
		for (Property p : e.getMessage().getAllProperties()) {
			if (p.getDescription() != null && ! p.getDescription().equals("")) {
				hasDescriptions = true;
				result += "<b>" + p.getName() + "</b>: " + p.getDescription();
				result += "<br>";
			}
		}
		
		// And other submessages
		for (Message m : e.getMessage().getAllMessages()) {
			for (Property p : m.getAllProperties()) {
				if (p.getDescription() != null && ! p.getDescription().equals("")) {
					hasDescriptions = true;
					result += "<b>" + p.getName() + "</b>: " + p.getDescription();
					result += "<br>";
				}
			}
			
		}
		
		if (hasDescriptions) {
			return result;
		}
		return "";
	}

	private String printRequestKeysDefinition(Entity e) throws ServletException {
		String result = "";
		
		result += "<h2> Request </h2>";
		for (Key key : e.getKeys()) {
			// Get all properties for this key, put them in a temp Navajo and use the JSONTML to print it
			Set<Property> properties = key.getKeyProperties();
			Set<Property> optionalProps = new HashSet<Property>();
			
			Navajo nkey = NavajoFactory.getInstance().createNavajo();
			Message mkey = NavajoFactory.getInstance().createMessage(nkey,"keys");
			nkey.addMessage(mkey);
			
			for (Property prop : properties) {
				if (! Key.isAutoKey(prop.getKey())) {
					mkey.addProperty(prop.copy(nkey));
				}
				if (Key.isOptionalKey(prop.getKey())) {
					optionalProps.add(prop);
				}
			}
			
			// Printing result.
			result += "<pre class=\"prettyprint\">";
			result += writeEntityJson(nkey, "request");
			result += "</pre>";
			
			if (optionalProps.size() > 0 ){
				result+= "<b>Optional: </b> ";
			}
			result += "<code> ";
			for (Property optProp : optionalProps) {
				result += optProp.getName() +"; ";
			}
			result += "</code> ";
		}
		return result;
	}

	private String writeEntityJson(Navajo n, String method) throws ServletException {
		StringWriter writer = new StringWriter();
		JSONTML json = JSONTMLFactory.getInstance();
		Navajo masked = n.copy().mask(n, method);
		try {
			json.formatDefinition(masked, writer, true);
		} catch (Exception ex) {
			logger.error("Error in writing entity output in JSON!",ex);
			throw new ServletException("Error producing output");
		}
		return StringEscapeUtils.escapeHtml(writer.toString());
	}
	
	private String writeEntityXmlBirt(Navajo n) throws ServletException {
		StringWriter writer = new StringWriter();
		NavajoLaszloConverter.writeBirtXml(n, writer);
		String xml = writer.toString();
		return xml.replace(">",">\n");
	}
	
	private String writeEntityXml(Navajo n) throws ServletException {
		StringWriter writer = new StringWriter();
		n.write(writer);
		return writer.toString();
	}
	
	
	
	
	private String operationDescription(String op) {
		if (op.equals(Operation.GET)) {
			return "Get ";
		}
		if (op.equals(Operation.POST)) {
			return "Create ";
		}
		if (op.equals(Operation.PUT)) {
			return "Update ";
		}
		if (op.equals(Operation.DELETE)) {
			return "Delete ";
		}
		return "";
	}


}
