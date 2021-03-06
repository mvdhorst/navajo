package com.dexels.navajo.server;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.loader.NavajoClassLoader;
import com.dexels.navajo.loader.NavajoLegacyClassLoader;
import com.dexels.navajo.parser.DefaultExpressionEvaluator;
import com.dexels.navajo.persistence.PersistenceManager;
import com.dexels.navajo.persistence.PersistenceManagerFactory;
import com.dexels.navajo.script.api.NavajoClassSupplier;
import com.dexels.navajo.script.api.SystemException;
import com.dexels.navajo.server.descriptionprovider.DescriptionProviderInterface;
import com.dexels.navajo.server.enterprise.integrity.WorkerFactory;
import com.dexels.navajo.server.enterprise.integrity.WorkerInterface;
import com.dexels.navajo.server.enterprise.monitoring.AgentFactory;
import com.dexels.navajo.server.enterprise.scheduler.TaskRunnerFactory;
import com.dexels.navajo.server.enterprise.statistics.StatisticsRunnerFactory;
import com.dexels.navajo.server.enterprise.statistics.StatisticsRunnerInterface;
import com.dexels.navajo.sharedstore.SharedFileStore;
import com.dexels.navajo.sharedstore.SharedStoreInterface;

/*
 * The default NavajoConfig class.
 * 
 */
public final class NavajoConfig extends FileNavajoConfig implements NavajoConfigInterface {

	
	public String adapterPath;
	public String compiledScriptPath;
	public String hibernatePath;
	public String scriptPath;
	
	private String repositoryClass = "com.dexels.navajo.server.SimpleRepository";
	private String sharedStoreClass;
	
	private String auditLevel;
	private HashMap<String,Object> dbProperties = new HashMap<String,Object>();
	public String store;
	
	private String resourcePath;
	public int dbPort = -1;
	public boolean compileScripts = false;
	protected HashMap<String,String> properties = new HashMap<String,String>();
	private String configPath;
	protected NavajoClassLoader betaClassloader;
	protected NavajoClassSupplier adapterClassloader;
	protected volatile com.dexels.navajo.server.Repository repository = null;
	protected Navajo configuration;
    public int maxAccessSetSize = MAX_ACCESS_SET_SIZE;
    
    private Message body;
    private boolean statisticsRunnerStarted = false;
    
    /**
     * Several supporting threads.
     */
    protected DescriptionProviderInterface myDescriptionProvider = null;
        
    public String rootPath;
    private PersistenceManager persistenceManager;
    private String betaUser;
    private String classPath = "";
    private boolean enableAsync = true;
    private boolean enableIntegrityWorker = true;
    private boolean enableLockManager = true;
    private boolean enableStatisticsRunner = true;
    private float asyncTimeout;
	private File rootFile;

    private static volatile NavajoConfig instance = null;
	private File jarFolder;
	private String instanceName;
	private String instanceGroup;
	
	private OperatingSystemMXBean myOs = null;

	private String compilationLanguage;

	private File contextRoot;
	
	

	private final static Logger logger = LoggerFactory
			.getLogger(NavajoConfig.class);


    
	/**
	 * Creates a fresh NavajoConfig object.
	 * 
	 * @param inputStreamReader, the reader object to be used to read the config inputstream is
	 * @param ncs, the classloader to be used for the scripts.
	 * @param in, the inpustream containing the xml configuration.
	 * @param externalRootPath, optionally an external rootpath can be supplied (e.g. from a Servlet Context).
	 * 
	 * @throws SystemException
	 */
	public NavajoConfig(NavajoClassSupplier ncs, InputStream in, String externalRootPath, String servletContextRootPath)  throws SystemException {

		classPath = System.getProperty("java.class.path");
		adapterClassloader = ncs;
		// BIG NOTE: instance SHOULD be set at this point since instance needs to be known by classes
		// called during loadConfig()!!
		instance = this;
		loadConfig(in, externalRootPath,servletContextRootPath);
		myOs = ManagementFactory.getOperatingSystemMXBean();

	}

	
	@SuppressWarnings("unchecked")
	private void loadConfig(InputStream in, String externalRootPath, String servletContextPath)  throws SystemException{
    	
   	if(servletContextPath!=null) {
   		contextRoot = new File(servletContextPath);
   	}

    	configuration = NavajoFactory.getInstance().createNavajo(in);
    	
    	body = configuration.getMessage("server-configuration");
    	if (body == null) {
    		throw new SystemException(-1, "Could not read configuration file server.xml");
    	}
    	
    	try {
    		
    		String r = ( body.getProperty("paths/root") != null ? body.getProperty("paths/root").getValue() : null);
    		// in Old Skool situation, passed rootPath is null.
    		if(externalRootPath==null) {
    			System.err.println("Old skool configuration (null rootPath), get path from serverXml: "+r);
    			if ( r == null ) {
    				throw new SystemException(-1, "Server root was not specified, try defining root in server.xml");
    			}
    			rootPath = properDir(r);
    		} else {
    			rootPath = externalRootPath;
    			
    		}
            
    		// Get the instance name.
    		instanceName = ( body.getProperty("instance_name") != null ? body.getProperty("instance_name").getValue() : null );
    		if ( instanceName == null ) {
    			throw new IllegalArgumentException("instance_name in server.xml not found.");
    		}
    		
    		// Get the instance group.
    		instanceGroup = ( body.getProperty("instance_group") != null ? body.getProperty("instance_group").getValue() : null );
    		if ( instanceGroup == null ) {
    			throw new IllegalArgumentException("instance_group in server.xml not found.");
    		}
    		
    		rootFile = new File(rootPath);
    		if (!rootFile.exists()) {
				throw new IllegalArgumentException("Rootpath in server.xml not found.");
			}
    		
    		configPath = properDir(rootPath +
    				body.getProperty("paths/configuration").getValue());
    		adapterPath = properDir(rootPath +
    				body.getProperty("paths/adapters").getValue());
    		scriptPath = properDir(rootPath +
    				body.getProperty("paths/scripts").getValue());
    		
    		// changed to more defensive behaviour
    		Property resourceProperty = body.getProperty("paths/resource");
    		if (resourceProperty!=null) {
        		resourcePath = properDir(rootPath +
        				resourceProperty.getValue());				
			}

    		compiledScriptPath = (body.getProperty("paths/compiled-scripts") != null ?
    				properDir(rootPath +
    						body.getProperty("paths/compiled-scripts").
    						getValue()) : "");
    		
    		persistenceManager = PersistenceManagerFactory.getInstance("com.dexels.navajo.persistence.impl.PersistenceManagerImpl", configPath);
    		
    		if(adapterClassloader == null) {
    			if(!navajocore.Version.osgiActive()) {
        			adapterClassloader = new NavajoLegacyClassLoader(adapterPath, compiledScriptPath, getClass().getClassLoader());
        			logger.warn("Setting non-OSGi legacy adapter classloader: " + adapterClassloader);
    			} else {
    				adapterClassloader = new NavajoClassLoader(adapterPath, compiledScriptPath, getClass().getClassLoader());
    			}
    		}

    		if(betaClassloader==null) {
    			if(!navajocore.Version.osgiActive()) {
        			betaClassloader = new NavajoLegacyClassLoader(adapterPath, compiledScriptPath, true, getClass().getClassLoader());
    			} else {
    				adapterClassloader = new NavajoClassLoader(adapterPath, compiledScriptPath, getClass().getClassLoader());
    			}
    		}
    		
    		// Read monitoring configuration options
    		Property monitoringAgentClass = body.getProperty("monitoring-agent/class");
    		Property monitoringAgentProperties = body.getProperty("monitoring-agent/properties");
    		// Set properties.
    		if  ( monitoringAgentProperties != null ) {
    			String [] properties = monitoringAgentProperties.getValue().split(";");
    			for ( int i = 0; i < properties.length; i++ ) {
    				String [] keyValue = properties[i].split("=");
    				String key = ( keyValue.length > 1) ? keyValue[0] : "";
    				String value = ( keyValue.length > 1) ? keyValue[1] : "";
    				System.setProperty(key, value);
    			}
    		}
    		if ( monitoringAgentClass != null ) {
    			AgentFactory.getInstance(monitoringAgentClass.getValue()).start();
    		}
    		
    		Message descriptionMessage = body.getMessage("description-provider");
    		Property descriptionProviderProperty = body.getProperty("description-provider/class");
			String descriptionProviderClass = null;
			if (descriptionProviderProperty!=null) {
				descriptionProviderClass = descriptionProviderProperty.getValue();
				if (descriptionProviderClass!=null) {
					try {
					Class<? extends DescriptionProviderInterface> cc = (Class<? extends DescriptionProviderInterface>) Class.forName(descriptionProviderClass);
					System.err.println("Descriptionprovider is: " + descriptionProviderClass);
					if (cc!=null) {
//						System.err.println("Setting description provider. config hash: "+hashCode());
						if (myDescriptionProvider==null) {
							myDescriptionProvider = cc.newInstance();
							myDescriptionProvider.setDescriptionConfigMessage(descriptionMessage);
						} else {
							System.err.println("Warning: Resetting description provider.");
						}
					}
					} catch (Throwable e) {
						logger.warn("DescriptionProvider not available (normal in OSGi)");
					}
				}
			} 
					
			if ( body.getProperty("repository/class") != null ) {
				repositoryClass = body.getProperty("repository/class").getValue();
			}
			
			if ( body.getProperty("sharedstore/class") != null ) {
				sharedStoreClass = body.getProperty("sharedstore/class").getValue();
			}
			
    		// Read navajostore parameters.
    		Message navajostore = body.getMessage("navajostore");
    		if (navajostore != null) {
    			String p = (navajostore.getProperty("dbport") != null ? navajostore.getProperty("dbport").getValue() : null);
    			store = (navajostore.getProperty("store") != null ? navajostore.getProperty("store").getValue() : null);
    			if (p != null) {
    				dbPort = Integer.parseInt(p);
    			}
    			auditLevel = ( navajostore.getProperty("auditlevel") != null ? navajostore.getProperty("auditlevel").getValue() : Level.WARNING.getName() );
    			dbProperties.put("auditlevel", auditLevel);
    		}
    		
    		if (dbPort != -1) {
    			dbProperties.put("port", new Integer(dbPort));
    		}
    		
    		enableStatisticsRunner = (body.getProperty("parameters/enable_statistics") == null ||
    				body.getProperty("parameters/enable_statistics").getValue().equals("true"));
    		
	
    		//System.err.println("USing repository = " + repository);
//    		Message maintenance = body.getMessage("maintenance-services");
//    		
//    		if ( maintenance != null ) {
//    			List<Property> propertyList = maintenance.getAllProperties();
//    			for (int i = 0; i < propertyList.size(); i++) {
//    				Property prop = propertyList.get(i);
//    				properties.put(prop.getName(), scriptPath + prop.getValue());
//    			}
//    		}
    		
//    		Message security = body.getMessage("security");
//    		if (security != null) {
//    			Property matchCn = security.getProperty("match_cn");
//    			if (matchCn != null)
//    				DispatcherFactory.getInstance().matchCN = matchCn.getValue().equals("true");
//    		}
    		
    		Property s = body.getProperty("parameters/async_timeout");
    		asyncTimeout = 3600 * 1000; // default 1 hour.
    		if (s != null) {
    			asyncTimeout = Float.parseFloat(s.getValue()) * 1000;
    		}
    		
    		enableAsync = (body.getProperty("parameters/enable_async") == null ||
    				body.getProperty("parameters/enable_async").getValue().
    				equals("true"));
    		
    		enableIntegrityWorker = (body.getProperty("parameters/enable_integrity") == null ||
    				body.getProperty("parameters/enable_integrity").getValue().equals("true"));
    		
    		enableLockManager = (body.getProperty("parameters/enable_locks") == null ||
    				body.getProperty("parameters/enable_locks").getValue().equals("true"));
    		
    		
    		if (body.getProperty("paths/jar_folder") != null) {
				Property fold = body.getProperty("paths/jar_folder");
				if (fold!=null) {
					String ss = fold.getValue();
					if (ss!=null) {
						if (rootFile!=null) {
							jarFolder = new File(rootFile,ss);
						} else {
							jarFolder = new File(ss);
						}
					} else {
						jarFolder = null;
					}
				}
			} else {
				jarFolder = new File(contextRoot,"WEB-INF/lib/");				
			}	
		    					
    		maxAccessSetSize = (body.getProperty("parameters/max_webservices") == null ? MAX_ACCESS_SET_SIZE :
    			                   Integer.parseInt(body.getProperty("parameters/max_webservices").getValue()) );
    		
    		try {
    			betaUser = body.getProperty("special-users/beta").getValue();
    			if ( betaUser == null || betaUser.equals("") ) {
    				betaUser = "_beta";
    			}
    		}
    		catch (Throwable e) {
    			//System.out.println("No beta user specified");
    			betaUser = "_beta";
    		}
    		//System.err.println("Betauser suffix is: " + betaUser);
    		
    		s = body.getProperty("parameters/compile_scripts");
    		if (s != null) {
    			//System.out.println("s.getValue() = " + s.getValue());
    			compileScripts = (s.getValue().equals("true"));
    		}
    		else {
    			compileScripts = false;
    		}

    		// Get compilation class.
    		// TODO refactor into intelligent discovery
    		compilationLanguage = ( body.getProperty("parameters/compilation_language") != null ? body.getProperty("parameters/compilation_language").getValue() : null );

    		
    		// Get document class implementation.
    		String documentClass = ( body.getProperty("documentClass") != null ? 
    				body.getProperty("documentClass").getValue() : null );
    		
    		if ( documentClass != null ) {
    			System.setProperty("com.dexels.navajo.DocumentImplementation", documentClass);
    			NavajoFactory.resetImplementation();
    			NavajoFactory.getInstance();
    			NavajoFactory.getInstance().setExpressionEvaluator(new DefaultExpressionEvaluator());
    		} 

    		
    	} catch (Throwable t) {
    		throw new SystemException(-1, "Error reading server.xml configuration", t);
    	}
    	//System.out.println("COMPILE SCRIPTS: " + compileScripts);
    }

    /*
     * Startup the Navajo TaskRunner instance. If no instance exists, it is started.
     */
    @Override
	public void startTaskRunner() {
    	// Bootstrap task scheduler
    	try {
			TaskRunnerFactory.getInstance();
		} catch (RuntimeException e) {
			System.err.println("No taskrunner found.");
		}  
    }
    
    /*
     * Check whether the integrity module is operational.
     */
    @Override
	public boolean isIntegrityWorkerEnabled() {
    	return enableIntegrityWorker;
    }
    
    /*
     * Check whether the service lock module is operational.
     */
    @Override
	public boolean isLockManagerEnabled() {
    	return enableLockManager;
    }
    
    /*
     * Function to enable/disable statistics runner on the fly.
     * 
     * @param b
     */
    @Override
	public synchronized void setStatisticsRunnerEnabled(boolean b) {
    	
    	if ( getStatisticsRunner() != null ) {
    		getStatisticsRunner().setEnabled(b);
    	}
 
    }
    
    /*
     * Check whether the statistics runner is enabled.
     */
    @Override
	public boolean isStatisticsRunnerEnabled() {
    	if ( getStatisticsRunner() != null ) {
    		return getStatisticsRunner().isEnabled();
    	} else {
    		return false;
    	}
    }
    
    /*
     * Gets the Integrity Worker instance (if enabled).
     * If it does not exist, the Integrity Worker is started.
     */
    @Override
	public WorkerInterface getIntegrityWorker() {
    	
    	if ( !enableIntegrityWorker ) {
    		return null;
    	}
    	return WorkerFactory.getInstance();
   
    }
    
    /*
     * Check whether asynchronous services are enabled.
     */
    @Override
    public final boolean isAsyncEnabled() {
      return enableAsync;
    }

    /*
     * Gets the class path to be used for the compiling scripts.
     */
    @Override
	@Deprecated
    public final String getClassPath() {
      return this.classPath;
    }

    /*
     * Gets the path where the compiled scripts are stored.
     */
    @Override
	public final String getCompiledScriptPath() {
        return compiledScriptPath;
    }

    /*
     * Gets the path to the user adapter library.
     */
    @Override
	public final String getAdapterPath() {
        return adapterPath;
    }

    /*
     * Gets the path where the scripts are located.
     */
    @Override
	public final String getScriptPath() {
        return scriptPath;
    }

    @Override
	public final String getResourcePath() {
        return resourcePath;
    }
    
//    public final HashMap<String,String> getProperties() {
//        return properties;
//    }


    /*
     * Gets the configuration path to the Navajo Instance.
     */
    @Override
	public final String getConfigPath() {
        return configPath;
    }

    public final NavajoClassLoader getBetaClassLoader() {
    	return betaClassloader;
    }

    // Added a cast, because I changed the type of classloader to generic class loader, so I can just use the system class loader as well...
    @Override
	public final NavajoClassSupplier getClassloader() {
    	return adapterClassloader;
    }

    /*
     * Gets the BETA user post fix for scripts.
     */
    public final String getBetaUser() {
    	return betaUser;
    }

    /*
     * Sets the Authentication/Authorization module that will be used by the Navajo Instance.
     */
    public final void setRepository(com.dexels.navajo.server.Repository newRepository) {
        repository = newRepository;
    }

    /*
     * Gets the Authentication/Authorization module that will be used by the Navajo Instance.
     */
    @Override
	public final com.dexels.navajo.server.Repository getRepository() {

    	if ( repository != null ) {
    		return repository;
    	}

    	synchronized (instance) {
    		if ( repository == null ) {
    			RepositoryFactory r = RepositoryFactoryImpl.getInstance();
    			if(r==null) {
    				// no instance, means no OSGi, so go legacy:
        			repository = RepositoryFactoryImpl.getRepository(repositoryClass, this);
        			return repository;
    			}
    			this.repository = r.getRepository(repositoryClass);
    		} 
    	}

    	return repository;
    }

    @Override
	public final SharedStoreInterface getSharedStore() {
    	try {
    		if ( sharedStoreClass != null ) {
    			Class<?> c = Class.forName(sharedStoreClass);
    			SharedStoreInterface ssi = (SharedStoreInterface) c.newInstance();
    			return ssi;
    		} else {
    			logger.warn("No SharedStore implementation defined, using default: SharedFileStore");
    			return new SharedFileStore();
    		}
    	} catch (Exception e) {
    		logger.error("Could not instantiate SharedStore implementation.");
    		return null;
    	}
    }
    
    /*
     * Returns the instance of the Persistence Manager.
     */
    @Override
	public final PersistenceManager getPersistenceManager() {
      return persistenceManager;
    }
    
    /*
     * Get the root path for the Navajo Installation.
     */
    @Override
	public final String getRootPath() {
        return this.rootPath;
    }
    
    /*
     * Gets the asynchronous service store instance.
     */
    @Override
	public final com.dexels.navajo.mapping.AsyncStore getAsyncStore() {
    	if ( isEnableAsync() ) {
    		return com.dexels.navajo.mapping.AsyncStore.getInstance(asyncTimeout);
    	} else {
    		return null;
    	}
    }
 
    /*
     * Start statistics runner.
     */
    @Override
	public void startStatisticsRunner() {
    	if ( !statisticsRunnerStarted ) {
    		StatisticsRunnerInterface statisticsRunner = getStatisticsRunner();
    		if ( statisticsRunner != null ) {
    			statisticsRunner.setEnabled(isEnableStatisticsRunner());
    		}
    		statisticsRunnerStarted = true;
    	}
    }
    
    /*
     * Gets the statistics runnner instance.
     */
    @Override
    public final StatisticsRunnerInterface getStatisticsRunner() {
       return StatisticsRunnerFactory.getInstance(null, dbProperties, store);
   }

    @Override
    public File getContextRoot() {
   	 return contextRoot;
    }




    private final String properDir(String in) {
        String result = in + (in.endsWith("/") ? "" : "/");
        return result;
    }

    /*
     * Clears all NavajoClassLoaders instances. Both the general classloader as well as each instantiated script
     * classloader.
     */
    @Override
    public final synchronized void doClearCache() {

    	if(!navajocore.Version.osgiActive()) {
    		adapterClassloader = new NavajoLegacyClassLoader(adapterPath, null, getClass().getClassLoader());
    		betaClassloader = new NavajoLegacyClassLoader(adapterPath, null, true, getClass().getClassLoader());
    		GenericHandler.doClearCache();
    	}

    }
    
    /*
     * Clears all script classloaders.
     */
    @Override
    public final synchronized void doClearScriptCache() {
    	GenericHandler.doClearCache();
    }


    

	/**
	 * @param classloader The classloader to set.
	 */
    @Override
	public void setClassloader(NavajoClassSupplier classloader) {
		this.adapterClassloader = classloader;
	}
	
    @Override
	public File getJarFolder() {
		return jarFolder;
	}

    @Override
	public String getInstanceName() {
		return instanceName;
	}

    @Override
	public DescriptionProviderInterface getDescriptionProvider() {
//		System.err.println("Getting description provider. Config hash: "+hashCode());
		return myDescriptionProvider;
	}
	
    @Override
	public double getCurrentCPUload() {
		if ( myOs != null ) {
			return ( myOs.getSystemLoadAverage() / myOs.getAvailableProcessors() );
		}
		else return 1.0;
	}

    @Override
	public String getInstanceGroup() {
		return instanceGroup;
	}
	
    @Override
	public float getAsyncTimeout() {
		return asyncTimeout;
	}

	private boolean isEnableAsync() {
		return enableAsync;
	}

    @Override
	public boolean isEnableStatisticsRunner() {
		return enableStatisticsRunner;
	}

    @Override
	public boolean isCompileScripts() {
		return compileScripts;
	}

    @Override
	public int getMaxAccessSetSize() {
		return maxAccessSetSize;
	}

	public void setMaxAccessSetSize(int maxAccessSetSize) {
		this.maxAccessSetSize = maxAccessSetSize;
	}

	private Message getMessage(String msg) {
		if ( body != null ) {
			return body.getMessage(msg);
		} else {
			return null;
		}
	}

    @Override
	public String getCompilationLanguage() {
		return compilationLanguage;
	}



	@Override
	public Object getParameter(String name) {
		final Message message = getMessage("parameters");
		if(message==null) {
			return null;
		}
		Property p = message.getProperty(name);
		if(p==null) {
			return null;
		}
		return p.getTypedValue();
	}

}