package com.dexels.navajo.tipi.dev.server.appmanager.operations.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.repository.api.RepositoryInstance;
import com.dexels.navajo.tipi.dev.server.appmanager.AppStoreOperation;

public class CacheBuild extends BaseOperation implements AppStoreOperation {

	private static final long serialVersionUID = 4675519591066489420L;
	private final static Logger logger = LoggerFactory
			.getLogger(CacheBuild.class);
	
	
	public void cachebuild(String name) throws IOException {
		RepositoryInstance as = applications.get(name);
		build(as);
	}
	
	public void cachebuild() throws IOException {
		for (RepositoryInstance a: applications.values()) {
			build(a);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String val = req.getParameter("app");
		if(val!=null) {
			cachebuild(val);
		} else {
			cachebuild();
		}
		writeValueToJsonArray(resp.getOutputStream(),"cache build ok");

	}
	
	public void build() throws IOException {
		for (RepositoryInstance a: applications.values()) {
			build(a);
		}
	}
	
	@Override
	public void build(RepositoryInstance a) throws IOException {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			createDigestFor(messageDigest, "tipi",a.getRepositoryFolder());
			createDigestFor(messageDigest, "resource",a.getRepositoryFolder());
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error: ", e);
		}

	}

	private void createDigestFor(MessageDigest digest,String resourceName, File appFolder) throws IOException {
		File inputFolder = new File(appFolder,resourceName);
		File digestOutput = new File(inputFolder,"remotedigest.properties");
		Properties properties = new Properties();
		scan(digest,inputFolder,inputFolder,properties);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(digestOutput);
			properties.store(fos, "Generated by Tipi Store");
		} catch (IOException e) {
			logger.error("Error: ", e);
		} finally {
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}


	private void scan(MessageDigest digest,File root, File current, Properties properties) throws IOException {
		File[] elements = current.listFiles();
		if(elements!=null) {
			for (File file : elements) {
				if(file.isDirectory()) {
					if(!"CVS".equals(file.getName())) {
						scan(digest,root,file,properties);
					}
				} else {
					String mdigest = createDigest(digest,file).trim();
					properties.put(relativePath(root, file), mdigest);
				}
			}
		}
		
	}
	
	private String createDigest(MessageDigest digest, File file) throws IOException {
		OutputStream nullOutput = new OutputStream(){

			@Override
			public void write(int b) throws IOException {
			}};
		DigestOutputStream dos = new DigestOutputStream(nullOutput, digest);
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			copyResource(dos, fis);
		} catch (FileNotFoundException e) {
			logger.error("Error: ", e);
		} finally {
			if(fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		
		final byte[] bytes = digest.digest();
		return new String(Base64.encodeBase64(bytes));
	}
	private String relativePath(File base, File path) {
		return base.toURI().relativize(path.toURI()).getPath();
	}




	private final void copyResource(OutputStream out, InputStream in)
			throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in);
		BufferedOutputStream bout = new BufferedOutputStream(out);
		byte[] buffer = new byte[1024];
		int read;
		while ((read = bin.read(buffer)) > -1) {
			bout.write(buffer, 0, read);
		}
		bin.close();
		bout.flush();
		bout.close();
	}

	public String convertPath(String location) {
		return location.replaceAll("/", "_");
	}



}
