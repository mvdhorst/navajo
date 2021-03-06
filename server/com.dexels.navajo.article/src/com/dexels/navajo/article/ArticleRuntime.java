package com.dexels.navajo.article;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.dexels.navajo.document.Navajo;
import com.dexels.oauth.api.Token;

public interface ArticleRuntime {
	public String resolveArgument(String name) throws ArticleException;

	public void execute(ArticleContext articleServlet) throws ArticleException, DirectOutputThrowable;

	public void pushNavajo(String name,Navajo res);

	public Navajo getNavajo(String name);

	public String getPassword();
	public String getUsername();
	public void setPassword(String password);
	public void setUsername(String username);
	
	public void setMimeType(String mime);
	
	public Writer getOutputWriter() throws IOException;

	public Navajo getNavajo();

	public String getArticleName();
	
	public ObjectNode getMetadataRootNode();

	public void commit() throws IOException;

	public void writeNode(JsonNode node) throws IOException;

	public ObjectMapper getObjectMapper();

	public Map<String, String[]> getParameterMap();

	public ObjectNode getGroupNode(String name) throws ArticleException;

	public Set<String> getRequiredScopes();

	public Object resolveScope(String name) throws ArticleException;

	public String getInstance();

	public ObjectNode getRootNode();

	public Set<String> getSuppliedScopes();

	public Map<String, Object> getUserAttributes();

	public Token getToken();
	
}
