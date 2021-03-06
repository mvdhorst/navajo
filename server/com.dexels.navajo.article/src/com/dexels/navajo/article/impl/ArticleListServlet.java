package com.dexels.navajo.article.impl;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.article.ArticleContext;
import com.dexels.navajo.article.ArticleException;

public class ArticleListServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = -6895324256139435015L;

	private final static Logger logger = LoggerFactory
			.getLogger(ArticleListServlet.class);

	private ArticleContext context;

	public ArticleListServlet() {

	}

	// public void activate() {
	// logger.info("Activating acticle component");
	// }
	//
	// public void deactivate() {
	// logger.info("Deactivating acticle component");
	// }

	public ArticleContext getContext() {
		return context;
	}

	public void setArticleContext(ArticleContext context) {
		this.context = context;
	}

	public void clearArticleContext(ArticleContext context) {
		this.context = null;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.setContentType("application/json; charset=utf-8");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		String requestedArticle = req.getParameter("article");
		if (requestedArticle != null) {
			try {
				context.writeArticleMeta(requestedArticle, rootNode, mapper);
			} catch (Throwable e) {
				logger.error("Error generating metadata for article: "
						+ requestedArticle, e);
			}
		} else {
			List<String> articles = context.listArticles();
			for (String article : articles) {
				try {
					context.writeArticleMeta(article, rootNode, mapper);
				} catch (Throwable e) {
					logger.error("Error generating metadata for article: "
							+ article, e);
				}
				// context.getArticleMeta(article);
			}
		}
		// mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		try {
			writer.writeValue(resp.getWriter(), rootNode);
			if (resp.getWriter() != null) {
				resp.getWriter().close();
			}
		} catch (JsonGenerationException e) {
			throw new ServletException("Error generating JSON", e);
		} catch (JsonMappingException e) {
			throw new ServletException("Error generating JSON", e);
		} catch (IOException e) {
			throw new ServletException("Error writing JSON", e);
		}
	}

}
