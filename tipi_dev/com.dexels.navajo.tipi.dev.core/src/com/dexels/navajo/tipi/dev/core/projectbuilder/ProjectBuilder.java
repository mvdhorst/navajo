package com.dexels.navajo.tipi.dev.core.projectbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectBuilder {
	
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProjectBuilder.class);

	public static String getCurrentDeploy(File projectPath) throws IOException {
		File path = new File(projectPath, "settings/tipi.properties");
		Map<String,String> map = parsePropertyFile(path);
		return map.get("deploy");
	}
	
	public static Map<String,String> assembleTipi(File projectPath) throws IOException {
		logger.info("Project patH: "+projectPath.getAbsoluteFile());
//		FileInputStream is = new FileInputStream(new File(projectPath,"settings/tipi.properties"));
//		PropertyResourceBundle tipiProperties = new PropertyResourceBundle(is);		
//		is.close();	
		File path = new File(projectPath, "settings/tipi.properties");
		Map<String,String> tipiPropertyMap = parsePropertyFile(path);


		


		File deploymentFolder = new File(projectPath,"settings/deploy/");
		Set<String> deployments = new HashSet<String>();

		logger.info("Deployment folder detected: "+deploymentFolder.getAbsolutePath());
		if(deploymentFolder.exists() && deploymentFolder.isDirectory()) {
			File[] deployCandidates = deploymentFolder.listFiles();
			String selectedDeploy = tipiPropertyMap.get("deploy");
			if(selectedDeploy!=null) {
				for (File candidate : deployCandidates) {
					logger.info("Current: "+candidate.getName());
					if(candidate.getName().endsWith(".properties") && candidate.isFile()) {
						String currentDeploymentName = candidate.getName().substring(0,candidate.getName().length()-".properties".length());
						deployments.add(currentDeploymentName);
						logger.info("     : "+currentDeploymentName);
						if(currentDeploymentName.equals(selectedDeploy)) {
							logger.info("Deployment found!");
							Map<String,String> res = parsePropertyFile(candidate);
							tipiPropertyMap.putAll(res);
						}
					}
				}
			}
		}	
		return tipiPropertyMap;
		
	}
	
	public static String buildTipiProject(File projectPath, String codebase, String deployment) throws IOException {
		
		Map<String,String> tipiPropertyMap = assembleTipi(projectPath);
		
		String extensions = tipiPropertyMap.get("extensions").trim();
		String repository = tipiPropertyMap.get("repository").trim();
		String extensionRepository = repository; //+"Extensions/";
		String developmentRepository = repository+"Development/";

		
		List<String> profiles = getProfiles(projectPath);

		boolean clean = false;
		if(codebase==null) {
			codebase = "$$codebase";
		}
		logger.info("PRofiles: "+profiles);
//		String keystore = null;
//		keystore = tipiPropertyMap.get("keystore");
//		boolean resign = keystore!=null;
		
		if(deployment==null) {
			deployment = tipiPropertyMap.get("deploy");			
		}

	
		
		String postProcessAnt = null;
		if(profiles==null || profiles.isEmpty()) {
			postProcessAnt = buildProfileDescriptor(null,clean,  tipiPropertyMap,deployment,projectPath, codebase, extensions, extensionRepository,developmentRepository, true);
		} else {
			postProcessAnt = buildProfileDescriptor(profiles,clean,tipiPropertyMap,deployment, projectPath, codebase, extensions , extensionRepository, developmentRepository,true);
		}

		return postProcessAnt;
	}

	public static List<String> getProfiles(File projectPath) {
		List<String> profiles = new LinkedList<String>();
		File profileFolder = new File(projectPath,"settings/profiles/");
		logger.info("Profile folder detected: "+profileFolder.getAbsolutePath());
		if(profileFolder.exists() && profileFolder.isDirectory()) {
			File[] profileCandidates = profileFolder.listFiles();
			for (File candidate : profileCandidates) {
				logger.info("Current: "+candidate.getName());
				if(candidate.getName().endsWith(".properties") && candidate.isFile()) {
					// ewwwww
					String currentProfileName = candidate.getName().substring(0,candidate.getName().length()-".properties".length());
					
					profiles.add(currentProfileName);
					logger.info("     : "+currentProfileName);
				}
			}
		}
		return profiles;
	}


	private static Map<String, String> parsePropertyFile(File path) throws FileNotFoundException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		FileInputStream fr = new FileInputStream(path);

		PropertyResourceBundle p = new PropertyResourceBundle(fr);
		fr.close();
		Enumeration<String> eb = p.getKeys();
		while (eb.hasMoreElements()) {
			String string = eb.nextElement();
			params.put(string, p.getString(string));
		}
		logger.info("params: " + params);
		return params;
	}

	private static String buildProfileDescriptor(List<String> profiles,  boolean clean,Map<String,String> tipiProperties, String deployment, File projectPath, String projectUrl, String extensions,
			String repository,String developmentRepository,boolean useVersioning) throws IOException {
		
		String postProcessAnt = null;

			LocalJnlpBuilder l = new LocalJnlpBuilder();
			postProcessAnt = l.build(repository,developmentRepository, extensions,tipiProperties,deployment,projectPath, projectUrl,profiles,useVersioning);
		return postProcessAnt;
	}


}