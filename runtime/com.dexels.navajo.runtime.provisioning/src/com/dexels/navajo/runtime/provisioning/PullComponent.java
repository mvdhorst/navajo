package com.dexels.navajo.runtime.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.obr.Repository;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resolver;
import org.osgi.service.obr.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.osgi.runtime.ContextIdentifier;

public class PullComponent {

	private RepositoryAdmin myRepositoryAdmin = null;
	private ConfigurationAdmin myConfigurationAdmin = null;
	private final Set<ContextIdentifier> contextIdentifiers = new HashSet<ContextIdentifier>();
	private BundleContext bundleContext = null;
	private EventAdmin eventAdmin = null;
	
	private final static Logger logger = LoggerFactory
			.getLogger(PullComponent.class);
	private long total;
	private int current;

	public void activate(Map<String,Object> properties, BundleContext bundleContext) {
		long stamp;
		this.bundleContext  = bundleContext;
		try {
			stamp = System.currentTimeMillis();
			logger.info("Obr Component activated.");
			String contextPath = (String) properties.get("contextPath");
			String deployment = (String) properties.get("deployment");
			String profile = (String) properties.get("profile");

			try {
				setupTipi(contextPath, deployment, profile);
			} catch (Exception e) {
				logger.error("Setting up tipi failed: ", e);
			}

			try {
				debugRepositories();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("Activating OBR took: "
					+ (System.currentTimeMillis() - stamp) + " millis. ");
		} catch (Throwable e) {
			logger.error("Activation of pull had a problem", e);
		}

	}

//	private boolean hasContext(String contextName) {
//		for (ContextIdentifier c : contextIdentifiers) {
//			String con = c.getContextPath();
//			String ctxName = contextName;
//			if (ctxName.equals(con)) {
//				return true;
//			}
//			if (con != null && con.startsWith("/")) {
//				con = con.substring(1);
//			}
//			if (ctxName.startsWith("/")) {
//				ctxName = ctxName.substring(1);
//			}
//			if (ctxName.equals(con)) {
//				return true;
//			}
//
//		}
//		return false;
//	}

	private void setupTipi(String contextPath, String deployment, String profile)
			throws Exception {
		logger.info("Setting up tipi!");
		File tipiHome = new File(contextPath);
		File settings = new File(tipiHome, "settings/tipi.properties");
		InputStream fis = new FileInputStream(settings);
		PropertyResourceBundle prp = new PropertyResourceBundle(fis);
		fis.close();
		// "https://source.dexels.com/nexus/content/shadows/navajo_snapshot_obr/.meta/obr.xml"
		String[] dependencies = prp.getString("dependencies").split(",");
		Repository[] reps = myRepositoryAdmin.listRepositories();
		for (Repository repository : reps) {
			myRepositoryAdmin.removeRepository(repository.getURL());
		}
		reps = myRepositoryAdmin.listRepositories();
		if (reps.length > 0) {
			logger.warn("Weird, not all repositories were removed!");
		}
		// boolean localFound = installDependency(dependencies,false);
		// if(localFound) {
		// logger.info("Local dependencies satisfied, leaving as-is");
		// return;
		// }

		String[] obrRepos = prp.getString("obrRepositories").split(",");
		addObrRepository(obrRepos);
		installDependency(dependencies, true);

		Dictionary<String, String> s = new Hashtable<String, String>();
		s.put("tipi.context", contextPath);
		s.put("deployment", deployment);
		s.put("profile", profile);
		Configuration cc = myConfigurationAdmin.getConfiguration(
				"com.dexels.navajo.tipi.swing.application", null);
		cc.update(s);

		// String contextPath = (String) properties.get("contextPath");
		// String resourceType = (String) properties.get("resourceType");
		// String deployment = (String) properties.get("deployment");
		// String profile = (String) properties.get("profile");

	}

	@SuppressWarnings("rawtypes")
	private boolean installDependency(String[] dependencies,
			boolean performDeploy) {
		
		BundleListener bl = new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent be) {
				logger.info("OBR delta: "+be.getBundle()+" phase:"+ be.getType());
			}
		};
		bundleContext.addBundleListener(bl);
		Resolver res = myRepositoryAdmin.resolver();
		total = 0;
		for (String dep : dependencies) {
			Resource[] result = myRepositoryAdmin.discoverResources(dep);
			logger.info("Adding dep: " + dep + " # of results: "
					+ result.length);

			for (Resource resource : result) {
				res.add(resource);
				logger.info("Added resource: "+resource.getSymbolicName());
				final Map properties = resource.getProperties();
				long size = (Long) properties.get("size");
				total+= size;
				logger.info("Properties: "+properties); 
			}
			logger.info("End of dep: "+dep);
		}
		boolean resolved = res.resolve();
		logger.info("Dependencies resolve: " + resolved);
		logger.info("Total bytes to install: "+total);
		try {
		if (!resolved) {
			Requirement[] req = res.getUnsatisfiedRequirements();
			for (Requirement requirement : req) {
				logger.warn("Requirement missing: " + requirement.getName() +" comment: "+ requirement.getComment()
						+ " filter: " + requirement.getFilter());
			}
			return false;
		} else {
			if (performDeploy) {
				logger.info("Added resources: "+res.getAddedResources().length+" req: "+res.getRequiredResources().length);
				final Map<String,Long> bundleSizes = new HashMap<String, Long>();
				Resource[] add = res.getAddedResources();
				for (Resource resource : add) {
					bundleSizes.put(resource.getSymbolicName(), (Long) resource.getProperties().get("size"));
				}
				BundleListener monitor = null;
				try {
					monitor = new BundleListener() {
						
						@Override
						public void bundleChanged(BundleEvent be) {
							final String name = be.getBundle().getSymbolicName();
							Long size = bundleSizes.get(name);
							if(size!=null) {
								addProgress(name,size);
							} else {
								logger.warn("No size for bundle: "+name);
							}
						}
					};
					bundleContext.addBundleListener(monitor);
					res.deploy(true);
				} catch (Exception e) {
					logger.error("Deployment failed: ",e);
					return false;
				} finally {
					if(monitor!=null) {
						bundleContext.removeBundleListener(monitor);
					}
				}
			}
			return true;
		} 
			
		}finally {
			if(bundleContext!=null) {
				bundleContext.removeBundleListener(bl);
			}
		}
	}

	protected void addProgress(String name, long size) {
		logger.info("Progress: "+name+" size: "+size);
		this.current+=size;
		final double frac = current/total;
		logger.info("Current progress = "+current+" / "+total+" = "+frac);
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("current",current);
		props.put("total",total);
		props.put("progress",frac);
		props.put("name",name);

		if(eventAdmin!=null) {
			Event e = new Event("obr",props);
			eventAdmin.postEvent(e);
		}
	}

	public void deactivate() {
		logger.info("Obr Component deactivated");
		this.bundleContext = null;
	}

	private void debugRepositories() throws Exception {
		Repository[] repositories = myRepositoryAdmin.listRepositories();
		// if (repositories.length == 0) {
		// logger.info("No repositories found, adding one.");
		//
		// // addObrRepository(new
		// //
		// String[]{"https://source.dexels.com/nexus/content/shadows/navajo_snapshot_obr/.meta/obr.xml"});
		// Resolver res = myRepositoryAdmin.resolver();
		// Resource[] result =
		// myRepositoryAdmin.discoverResources("(symbolicname=com.dexels.navajo.tipi.swing.application)");
		// if (result.length > 0) {
		// res.add(result[0]);
		// }
		// boolean resolved = res.resolve();
		// logger.info("Resolved: " + resolved);
		// Resource[] req = res.getRequiredResources();
		//
		// for (Resource resource : req) {
		// logger.info("Required resource: " + resource.getSymbolicName()
		// + " - " + resource.getId() + " pres: "
		// + resource.getPresentationName());
		// }
		// Requirement[] reqq = res.getUnsatisfiedRequirements();
		// for (Requirement requirement : reqq) {
		// logger.info("missing requ: " + requirement.getName());
		// }
		// res.deploy(true);
		// debugRepositories();
		// }
		for (Repository repository : repositories) {
			debugRepository(repository);
		}
	}

	private void addObrRepository(String[] obrRepo) throws Exception,
			MalformedURLException {
		for (String repo : obrRepo) {
			myRepositoryAdmin.addRepository(new URL(repo));
		}
	}

	private void debugRepository(Repository repository) {
		logger.info("Repository: " + repository.getName() + " url: "
				+ repository.getURL());
		// Resource[] r = repository.getResources();

		// for (Resource resource : r) {
		// //
		// logger.info("Resource: "+resource.getSymbolicName()+" - "+resource.getId()+" pres: "+resource.getPresentationName());
		// }
	}

	public void addRepositoryAdmin(RepositoryAdmin admin) {
		this.myRepositoryAdmin = admin;
	}

	/**
	 * the RepositoryAdmin to remove
	 * 
	 * @param admin
	 */
	public void clearRepositoryAdmin(RepositoryAdmin admin) {
		this.myRepositoryAdmin = null;
	}
	
	public void setEventAdmin(EventAdmin admin) {
		this.eventAdmin = admin;
	}

	/**
	 * the EventAdmin to remove
	 * 
	 * @param admin
	 */
	public void clearEventAdmin(EventAdmin admin) {
		this.eventAdmin = null;
	}

	public void addConfigurationAdmin(ConfigurationAdmin admin) {
		this.myConfigurationAdmin = admin;
	}

	/**
	 * the ConfigurationAdmin to remove
	 * 
	 * @param admin
	 */
	public void clearConfigurationAdmin(ConfigurationAdmin admin) {
		this.myConfigurationAdmin = null;
	}

	// TODO, refine this to support multiple context identifiers
	public void removeContextIdentifier(ContextIdentifier ci) {
		contextIdentifiers.remove(ci);
	}

	public void addContextIdentifier(ContextIdentifier ci) {
		contextIdentifiers.add(ci);
	}

}
