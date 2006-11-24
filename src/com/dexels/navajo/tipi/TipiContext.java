package com.dexels.navajo.tipi;

import java.io.*;
import java.net.*;
import java.util.*;


import com.dexels.navajo.client.*;
import com.dexels.navajo.document.*;
import com.dexels.navajo.parser.*;
import com.dexels.navajo.tipi.components.core.*;
import com.dexels.navajo.tipi.internal.*;
import com.dexels.navajo.tipi.studio.*;
import com.dexels.navajo.tipi.tipixml.*;
import com.dexels.navajo.client.impl.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public abstract class TipiContext
    implements ActivityController {

  protected final Map tipiInstanceMap = new HashMap();
  protected final Map tipiComponentMap = new HashMap();
  protected final Map tipiClassMap = new HashMap();
  protected final Map tipiClassDefMap = new HashMap();
  protected final Map tipiActionDefMap = new HashMap();
  
  private boolean contextShutdown = false;
  
  protected final Map lazyMap = new HashMap();
  protected final ArrayList includeList = new ArrayList();
  protected TipiErrorHandler eHandler;
  protected String errorHandler;
  protected final ArrayList rootPaneList = new ArrayList();
  private final ArrayList screenList = new ArrayList();
  private final TipiActionManager myActionManager = new TipiActionManager();


  protected final List myActivityListeners = new ArrayList();
  private final ArrayList activityListenerList = new ArrayList();

  protected final HashMap clientConfigMap = new HashMap();
  protected boolean studioMode = false;
  protected TipiThreadPool myThreadPool;
  protected TipiComponent topScreen = null;
  protected List myThreadsToServer = new ArrayList();
  protected int maxToServer = 1;
  protected int poolSize = 2;
  protected boolean singleThread = true;
   private String currentDefinition = null;
  private final Map parserInstanceMap = new HashMap();
  private final Map resourceReferenceMap = new HashMap();

  private final List resourceReferenceList = new ArrayList();

  protected TipiStorageManager myStorageManager = null;

  private final List packageList = new ArrayList();
  private final Map packageMap = new HashMap();
//  private final List packageReferenceList = new ArrayList();
//  private final Map packageReferenceMap = new HashMap();

  private final Map globalMap = new HashMap();

  protected final long startTime = System.currentTimeMillis();
  
  protected File resourceBaseDirectory = null;
  protected File tipiBaseDirectory = null;
  
  protected boolean allowLazyIncludes = true;
  private boolean isSwitching = false;
  private ClassLoader tipiClassLoader = null;
  private ClassLoader resourceClassLoader = null;
  private final ArrayList shutdownListeners = new ArrayList();

  public TipiContext() {
//    myThreadPool = new TipiThreadPool(this);
    NavajoFactory.getInstance().setExpressionEvaluator(new DefaultExpressionEvaluator());
    // Use the null implementation as default (Will always return null, will ignore all saves)
    setStorageManager(new TipiNullStorageManager());
  }
  protected void clearLogFile() {
   }

  public void setResourceClassLoader(ClassLoader c) {
      resourceClassLoader = c;
  }
  public void setResourceBaseDirectory(File f) {
      resourceBaseDirectory = f;
  }
  
  public void setTipiBaseDirectory(File f) {
	  tipiBaseDirectory = f;
  }

  
  public void debugLog(String category, String event) {
   }

  public void handleException(Exception e) {
    if (eHandler != null) {
      eHandler.showError(e);
    }
  }

  public int getPoolSize() {
    return poolSize;
  }

  public void setStudioMode(boolean b) {
    studioMode = b;
  }

  public void setClassLoader(ClassLoader cl) {
      tipiClassLoader = cl;
  }

  public ClassLoader getClassLoader() {
      if (tipiClassLoader!=null) {
        return tipiClassLoader;
    } else {
        return getClass().getClassLoader();
    }
  }
  
  public Object getGlobalValue(String name) {
    return globalMap.get(name);
  }

  public void setGlobalValue(String name, Object value) {
    globalMap.put(name,value);
  }

  public abstract void setSplash(Object s);

//  public void setToplevel(RootPaneContainer tl) {
//    myTopLevel = tl;
//  }
  public void parseFile(File location, boolean studioMode, String dir) throws IOException, XMLParseException, TipiException {
    parseStream(new FileInputStream(location), dir, studioMode);
  }

  public boolean isStudioMode() {
    return studioMode;
//    return false;
  }


  public void parseURL(URL location, boolean studioMode) throws IOException, XMLParseException,
      TipiException {
      try {
          parseStream(location.openStream(), location.toString(), studioMode);
      } catch (IOException e) {
          throw new TipiException("Can not resolve URL: "+location+" or other IO error.",e);
      }
  }

  public void parseURL(URL location, boolean studioMode, String definitionName) throws IOException, XMLParseException,
      TipiException {
    parseStream(location.openStream(), location.toString(),definitionName, studioMode);
  }


  public Map getTipiClassDefMap() {
    return tipiClassDefMap;
  }

//  public Map getTipiDefinitionMap() {
//    return tipiComponentMap;
//  }

  protected void clearResources() {
    tipiInstanceMap.clear();
    tipiComponentMap.clear();
    tipiClassMap.clear();
    tipiClassDefMap.clear();
    clearTopScreen();
    includeList.clear();
//    System.err.println("clearResources: clearing includeList. ");

    eHandler = null;
    errorHandler = null;
    rootPaneList.clear();
    screenList.clear();
    Runtime runtimeObject = Runtime.getRuntime();
    runtimeObject.traceInstructions(false);
    runtimeObject.traceMethodCalls(false);
    runtimeObject.runFinalization();
    runtimeObject.gc();
  }

  public abstract void clearTopScreen();

  private final void configureTipi(XMLElement config) throws TipiException {
    /** @todo Implement configuration of tipi setup
     * For example, the threading model, the amount of event threads, the # of allowed
     * connections.
     *  */
//    System.err.println("CONFIGURING TIPI WITH: "+config);
      
     // TODO WATCH OUT IN THE STANDALONE
    maxToServer = config.getIntAttribute("maxtoserver", 5);
    System.setProperty("tipi.client.maxthreadstoserver", "" + maxToServer);
    poolSize = config.getIntAttribute("poolsize", 1);
    System.setProperty("tipi.client.threadpoolsize", "" + poolSize);
  }

  protected final void setSystemProperty(String name, String value, boolean overwrite) {
    if (System.getProperty(name) != null) {
      if (overwrite) {
        System.setProperty(name, value);
      }
    }
    else {
      System.setProperty(name, value);
    }
  }

  protected void createClient(XMLElement config) throws TipiException {
//    System.err.println("**************CREATING CLIENT:***************************");
//    System.err.println(config.toString());
//    System.err.println("*********************************************************");
	  String name = config.getStringAttribute("name");
//    clientConfig = config;
    String impl = (String)attemptGenericEvaluate(config.getStringAttribute("impl", "'indirect'"));
    setSystemProperty("tipi.client.impl", impl, false);
    String cfg = (String)attemptGenericEvaluate(config.getStringAttribute("config", "'server.xml'"));
    setSystemProperty("tipi.client.config", cfg, false);
    boolean secure = ((Boolean)attemptGenericEvaluate(config.getStringAttribute("secure", "false"))).booleanValue();
    setSystemProperty("tipi.client.secure", ""+secure, false);

    String locale = (String)attemptGenericEvaluate(config.getStringAttribute("locale", "'en'"));
    setSystemProperty("tipi.client.locale", cfg, false);

    Object keystore = attemptGenericEvaluate(config.getStringAttribute("keystore", ""));


    String storepass = (String)attemptGenericEvaluate(config.getStringAttribute("storepass", ""));
    setSystemProperty("tipi.client.storepass", storepass, false);
    String navajoServer = (String)attemptGenericEvaluate(config.getStringAttribute("server", ""));
    setSystemProperty("tipi.client.server", navajoServer, false);
    String navajoUsername = (String)attemptGenericEvaluate(config.getStringAttribute("username", ""));
    setSystemProperty("tipi.client.username", navajoUsername, false);
    String navajoPassword = (String)attemptGenericEvaluate(config.getStringAttribute("password", ""));
    setSystemProperty("tipi.client.password", navajoPassword, false);
    if (!impl.equals("direct")) {
        
//      System.err.println("Using INDIRECT. Username = " + navajoUsername);
        if(impl.equals("socket")) {
            NavajoClientFactory.resetClient();
            ClientInterface ci =  NavajoClientFactory.createClient("com.dexels.navajo.client.NavajoSocketClient",null);
            NavajoClientFactory.getClient().setServerUrl(navajoServer);
            NavajoClientFactory.getClient().setUsername(navajoUsername);
            NavajoClientFactory.getClient().setPassword(navajoPassword);
        } else {
            NavajoClientFactory.resetClient();
            NavajoClientFactory.createDefaultClient();
            NavajoClientFactory.getClient().setServerUrl(navajoServer);
            NavajoClientFactory.getClient().setUsername(navajoUsername);
            NavajoClientFactory.getClient().setPassword(navajoPassword);
        }
        if (name!=null) {
			clientConfigMap.put(name, NavajoClientFactory.getClient());
		}
    }
    else {
        NavajoClientFactory.resetClient();
      NavajoClientFactory.createClient("com.dexels.navajo.client.impl.DirectClientImpl", getClass().getClassLoader().getResource(cfg));
    }
    
    NavajoClientFactory.getClient().setLocaleCode(locale);
    if (secure) {
      if (storepass != null && keystore != null) {
        try {
          if (keystore instanceof URL) {
            NavajoClientFactory.getClient().setSecure(((URL)keystore).openStream(), storepass, true);

          } else {
            String keys = ""+keystore;
            NavajoClientFactory.getClient().setSecure(keys, storepass, true);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
          throw new TipiException("Could not locate keystore: " + keystore);
        }
      }
    }
  }


  public void parseStream(InputStream in, String sourceName, boolean studioMode) throws IOException, XMLParseException, TipiException {
    parseStream(in, sourceName,"init",studioMode);
  }

  public void parseStream(InputStream in, String sourceName, String definitionName, boolean studioMode) throws IOException, XMLParseException, TipiException {
//    clearResources();
    XMLElement doc = new CaseSensitiveXMLElement();
    InputStreamReader isr = new InputStreamReader(in, "UTF-8");
    doc.parseFromReader(isr);
    isr.close();
    parseXMLElement(doc, studioMode, sourceName);
    Class initClass = (Class) tipiClassMap.get(definitionName);
    try {
      if (initClass != null) {
        //System.err.println("---- Found init class");
        TipiInitInterface tii = (TipiInitInterface) initClass.newInstance();
        tii.init(this);
      }
    }
    catch (IllegalAccessException ex) {
      ex.printStackTrace();
    }
    catch (InstantiationException ex) {
      ex.printStackTrace();
    }
    catch (ClassCastException ex) {
      ex.printStackTrace();
    }
    switchToDefinition(definitionName, null);
    if (errorHandler != null) {
      try {
        Class c = getTipiClass(errorHandler);
        if (c != null) {
          eHandler = (TipiErrorHandler) c.newInstance();
          eHandler.setContext(this);
        }
      }
      catch (Exception e) {
        System.err.println("Error instantiating TipiErrorHandler!");
        e.printStackTrace();
      }
    }
  }

  protected void parseXMLElement(XMLElement elm, boolean studioMode, String dir) throws TipiException {
    String elmName = elm.getName();
    setSplashInfo("Loading user interface");
    if (!elmName.equals("tid")) {
      throw new TipiException("TID Rootnode not found!, found " + elmName +
                              " instead.");
    }
    errorHandler = (String) elm.getAttribute("errorhandler", null);
    Vector children = elm.getChildren();
    XMLElement startScreenDef = null;
    for (int i = 0; i < children.size(); i++) {
      XMLElement child = (XMLElement) children.elementAt(i);
//      System.err.println("Parsing::: "+child.getName());
      parseChild(child,studioMode, dir);
    }
  }

  protected void parseChild(XMLElement child, boolean studioMode,String dir) throws TipiException {
    String childName = child.getName();
    if (childName.equals("client-config")) {
      if (!"__ignore".equals(dir)) {
        createClient(child);
      }
    }
    if (childName.equals("tipi-config")) {
      configureTipi(child);
      return;
    }
    if (childName.equals("component") || childName.equals("tipi")) {
      parseDefinition(child);
      return;
    }
    if (childName.equals("tipiclass")) {
      addTipiClassDefinition(child);
      return;
    }
    if (childName.equals("tipiaction")) {
      addActionDefinition(child);
      return;
    }
    if (childName.equals("tipi-include")) {
      if (!"__ignore".equals(dir)) {
        parseLibrary(child,  dir);
      }
      return;
    }
    if (childName.equals("tipi-parser")) {
      parseParser(child);
      return;
    }
    if (childName.equals("tipi-resource")) {
      parseResource(child);
      return;
    }
    if (childName.equals("tipi-package")) {
      parsePackage(child);
      return;
    }

    if (childName.equals("tipi-storage")) {
        parseStorage(child);
        return;
      }
    
//    if (childName.equals("tipi-package-reference")) {
//      parsePackageReference(child);
//      return;
//    }
  }

  private void parseStorage(XMLElement child) {
      String type = child.getStringAttribute("type");
      if ("asp".equals(type)) {
        String instanceId = child.getStringAttribute("instanceId");
        String scriptPrefix = child.getStringAttribute("scriptPrefix");
        setStorageManager(new TipiGeneralAspManager(scriptPrefix,instanceId));
    }
      if ("file".equals(type)) {
          String basePath = child.getStringAttribute("dir");
          String instanceId = child.getStringAttribute("instanceId");
          
          File baseDir = null;
          if (basePath!=null) {
              baseDir= new File(basePath);
              if (instanceId!=null) {
                  baseDir = new File(baseDir,instanceId);
            }
          }
          setStorageManager(new TipiFileStorageManager(baseDir));
      }
      if ("null".equals(type)) {
          setStorageManager(new TipiNullStorageManager());
      }
      
  }
public void parseDefinition(XMLElement child) throws TipiException {
    String childName = child.getName();
    if (childName.equals("tipi") || childName.equals("component")) {
      if (getTipiDefinition(childName)!=null) {
		System.err.println(">>>>>>>>>>>>>>>>> SKIPPING ALREADY DEFINED: "+childName);
	} else {
	      testDefinition(child);
	      addComponentDefinition(child);

	}
    }
  }

  private final void testDefinition(XMLElement xe) {
    if (xe.getAttribute("name") == null) {
      throw new RuntimeException("Tipi/component definition found without name at: " + xe.getLineNr());
    }
    if (xe.getAttribute("class") == null) {
      throw new RuntimeException("Tipi/component definition found without class at: " + xe.getLineNr());
    }
  }

  public URL getResourceURL(String location) {
      return getResourceURL(location,resourceClassLoader);
  }  
  
  public URL getResourceURL(String location, ClassLoader cl) {
	  // Try the classloader first, the 
//	  System.err.println("Classloader: "+cl);
	  if (cl==null) {
          cl = getClass().getClassLoader();
      }
      URL u = cl.getResource(location);
    if (u==null) {
   //   System.err.println("getResourceURL: "+location+" not found in classpath, continuing");
    } else {
    	return u;
    }
    // case 2: strip the path, keep only the filename:
    if (location.startsWith("tipi/")) {
		location = location.substring(5);
	} else {
	    if (location.startsWith("resource/")) {
			location = location.substring(9);
		}
	}
    u = cl.getResource(location);
    if (u==null) {
        System.err.println("getResourceURL: "+location+" not found in classpath, continuing");
      } else {
      	return u;
      }
    
    if (resourceBaseDirectory!=null) {
//        System.err.println("ResourceDir found: "+resourceBaseDirectory.getAbsolutePath());
        File locationFile = new File(resourceBaseDirectory.getAbsolutePath()+"/"+location);
        if (!locationFile.exists()) {
           // System.err.println(".. but it did not exist: "+locationFile);
        	// ignore and continue
        } else {
	        try {
	            return locationFile.toURL();
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }
        }
    }
    if (tipiBaseDirectory!=null) {
//      System.err.println("ResourceDir found: "+resourceBaseDirectory.getAbsolutePath());
      File locationFile = new File(tipiBaseDirectory.getAbsolutePath()+"/"+location);
      if (!locationFile.exists()) {
          System.err.println(".. but it did not exist either: "+locationFile);
      } else {
	        try {
	            return locationFile.toURL();
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }
      }
  }
  //      System.err.println("ResourceDir found: "+resourceBaseDirectory.getAbsolutePath());
    // Finally, try the working dir
      File locationFile = new File(location);
//      System.err.println("Abs: "+locationFile.getAbsolutePath());
      if (!locationFile.exists()) {
          System.err.println(".. but it did not exist either: "+locationFile);
      } else {
	        try {
	            return locationFile.toURL();
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }
      }

    return null;
  }

  
  public final void parseLibraryFromClassPath(String location) {
    parseLibrary(location,false,null,null,false);
  }

  private final void parseLibrary(XMLElement lib,  String dir) throws TipiException {

      String location = (String) lib.getAttribute("location");
       String lazy = (String) lib.getAttribute("lazy");
    boolean isLazy = "true".equals(lazy);
    String componentName = (String)lib.getAttribute("definition");
    if (componentName==null && isLazy) {
        throw new IllegalArgumentException("Lazy include, but no definition found. Location: "+location);
    }
    if (isLazy && getTipiDefinition(componentName)!=null) {
    	// already present.
    	System.err.println("Not reparsing lazy: "+componentName);
    	return;
	}
    parseLibrary(location,true,dir,componentName,isLazy);
  }

  public void processRequiredIncludes() {
      for (Iterator iter = includeList.iterator(); iter.hasNext();) {
        String element = (String) iter.next();
//        System.err.println("Parsing element: "+element);
        parseLibrary(element, false, null, null, false);
    }
  }
  private final void parseLibrary(String location, boolean addToInclude, String dir, String definition, boolean isLazy) {
//    System.err.println("Parsing library: "+location+" dir: "+dir);
    if (isLazy) {
        if (definition==null) {
            throw new IllegalArgumentException("Lazy include, but no definition found. Location: "+location);
        }
        lazyMap.put(definition,location);
        return;
    }
    try {
//       if (addToInclude) {
//       System.err.println("parseLibrary: adding to includeList: "+location);
//
  // HACK!
        
//        includeList.add(location);
//      }
      if (location != null) {
        InputStream in = resolveInclude(location, dir);
        if (in == null) {
          System.err.println("Could not resolve: "+location);
          return;
        }
//        System.err.println("=============================\nPARSING LIBRARY: "+location+"\n=============================");
        XMLElement doc = new CaseSensitiveXMLElement();
        try {
          InputStreamReader isr = new InputStreamReader(in, "UTF-8");
          doc.parseFromReader(isr);
          isr.close();
//              System.err.println("Parsed successfully");;
        }
        /** @todo Throw these exceptions */
        catch (XMLParseException ex) {
          System.err.println("XML parse exception while parsing file: " + location + " at line: " + ex.getLineNr());
          ex.printStackTrace();
          return;
        }
        catch (IOException ex) {
          System.err.println("IO exception while parsing file: " + location);
          ex.printStackTrace();
          return;
        }
        parseXMLElement(doc, studioMode, dir);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private InputStream resolveInclude(String location, String dir) throws IOException {
    File tipiDir = null; // only studio related, for now.
    // first, try to resolve the include by checking the classpath:
    URL loc = getResourceURL(location);
    if (loc != null) {
      try {
        InputStream in = loc.openStream();
          return in;
    } catch (IOException e) {
        // classload failed. Continuing.
    }
    }
    //System.err.println("Resolving: " + location);
    File currentDir = new File(dir);
    File ff = new File(currentDir, location);
    if (ff.exists()) {
      FileInputStream fis = new FileInputStream(ff);
      return fis;
    }
    
    // now try it without the dir:
    
    ff = new File(location);
    if (ff.exists()) {
        FileInputStream fis = new FileInputStream(ff);
        return fis;
	}
    
    // finally, try the project directory:
    if (tipiDir == null) {
      String td = System.getProperty("tipi.project.dir");
      //System.err.println("FOUND PROP: " + td);
      if (td != null) {
        tipiDir = new File(td);
        //System.err.println("CREATED: " + tipiDir.toString());
      }
    }
    if (tipiDir != null) {
      location = location.replace('/', System.getProperty("file.separator").charAt(0));
      File f = new File(tipiDir, location);
      //System.err.println("Constructed file: " + f.toString());
      if (!f.exists()) {
        System.err.println("OH DEAR!!!");
        throw new FileNotFoundException("Could not resolve");
      }
      else {
        FileInputStream fis = new FileInputStream(f);
        return fis;
      }
    }
    else {
      System.err.println("Resource not found in classpath, and filedir not set: " + location);
      return null;
    }
  }

  public TipiActionBlock instantiateTipiActionBlock(XMLElement definition, TipiComponent parent) throws TipiException {
    TipiActionBlock c = createTipiActionBlockCondition();
    c.load(definition, parent);
    return c;
  }

  public TipiActionBlock instantiateDefaultTipiActionBlock(TipiComponent parent) throws TipiException {
    TipiActionBlock c = createTipiActionBlockCondition();
    return c;
  }

  public TipiAction instantiateTipiAction(XMLElement definition, TipiComponent parent) throws TipiException {
    String type = (String) definition.getAttribute("type");
    if (type == null) {
      throw new TipiException("Undefined action type in: " + definition.toString());
    }
    return myActionManager.instantiateAction(definition, parent);
  }

  public TipiActionManager getActionManager() {
    return myActionManager;
  }
  public TipiLayout instantiateLayout(XMLElement instance, TipiComponent cc) throws TipiException {
    String type = (String) instance.getAttribute("type");
    TipiLayout tl = (TipiLayout) instantiateClass(type, null, instance);
    if (tl == null) {
      System.err.println("Null layout!!!!!!!!!!!!");
    }
    XMLElement xx = (XMLElement) getTipiClassDefMap().get(type);
    tl.setComponent(cc);
    tl.setName(type);
    tl.setClassDef(xx);
    tl.initializeLayout(instance);
    tl.loadClassDef();
    return tl;
  }

//  /**
//   * @deprecated
//   */
//  public TipiLayout instantiateLayout(XMLElement instance) throws TipiException {
//    return instantiateLayout(instance,null);
//  }

  protected TipiComponent instantiateComponentByDefinition(XMLElement definition, XMLElement instance) throws TipiException {
    String clas = definition.getStringAttribute("class", "");
    String name = instance.getStringAttribute("name");
    if (name==null) {
		System.err.println("Error instantiating component: "+clas+". No name supplied. instance: "+instance);
	}
    if (!clas.equals("")) {
      Class cc = getTipiClass(clas);
      TipiComponent tc = (TipiComponent) instantiateClass(clas, name, instance);
//      System.err.println("Instantiating component by definition: "+clas);
      XMLElement classDef = (XMLElement) tipiClassDefMap.get(clas);

      /** @todo  think  these two can be removed, because they are invoked in the instantiateClass method*/
      tc.loadEventsDefinition(this, definition, classDef);
      tc.loadMethodDefinitions(this, definition, classDef);
      //-----------------------------
      tc.loadStartValues(definition);
//      boolean se = Boolean.getBoolean(definition.getStringAttribute("studioelement", "false"));
      boolean se = definition.getAttribute("studioelement") != null;
//      System.err.println("Is studio element? " + se + " (class is:" + tc.getClass() + ")");
//      System.err.println("Definition is: " + definition);
//      System.err.println("Definition built: "+name);
//      System.err.println("Childcount: "+tc.getChildCount());
      tc.setStudioElement(se);
      tc.commitToUi();
      return tc;
    }
    else {
      throw new TipiException("Problems instantiating TipiComponent class: " + definition.toString());
    }
  }

  public TipiComponent reloadComponent(TipiComponent comp, XMLElement definition, XMLElement instance) throws TipiException {
    String clas = definition.getStringAttribute("class", "");
    String name = instance.getStringAttribute("name");
    if (!clas.equals("")) {
//      Class cc = getTipiClass(clas);
//      TipiComponent tc = (TipiComponent) instantiateClass(clas, name, instance);
      comp.load(definition,instance,this);
      XMLElement classDef = (XMLElement) tipiClassDefMap.get(clas);
      comp.loadEventsDefinition(this, definition, classDef);
      comp.loadMethodDefinitions(this, definition, classDef);
      comp.loadStartValues(definition);
//      boolean se = Boolean.getBoolean(definition.getStringAttribute("studioelement", "false"));
      boolean se = definition.getAttribute("studioelement") != null;
//      System.err.println("Is studio element? " + se + " (class is:" + tc.getClass() + ")");
//      System.err.println("Definition is: " + definition);
      comp.setStudioElement(se);
      return comp;
    }
    else {
      throw new TipiException("Problems reloading TipiComponent class: " + definition.toString());
    }
  }


  public TipiComponent instantiateComponent(XMLElement instance) throws TipiException {
    String name = (String) instance.getAttribute("name");
    String clas = instance.getStringAttribute("class", "");
    TipiComponent tc = null;
    if (clas.equals("") && name!=null && !"".equals(name)) {
      // No class provided. Must be instantiating from a definition.
      XMLElement xx = getComponentDefinition(name);
      if (xx==null) {
        throw new TipiException("Definition based instance, but no definition found. Definition: "+name);
      }
      tc = instantiateComponentByDefinition(xx, instance);

    }
    else {
        // Class provided. Not instantiating from a definition, name is irrelevant.
      tc = (TipiComponent) instantiateClass(clas, null, instance);
    }
    tc.parseStyle(instance.getStringAttribute("style"));
    tc.processStyles();
    if (tc.getContainer() != null) {
//      if (RootPaneContainer.class.isInstance(tc.getContainer())) {
      if (tc.isTopLevel()) {
        rootPaneList.add(tc);
      }
    }
    tc.loadStartValues(instance);
//    fireTipiStructureChanged(tc);
     tc.componentInstantiated();
    if (tc.getId() == null) {
      System.err.println("NULL ID: component: " + tc.store().toString());
    }
    return tc;
  }

  private final void killComponent(TipiComponent comp) {
    comp.disposeComponent();
  }

  public void disposeTipiComponent(TipiComponent comp) {
//    System.err.println("Disposing tipicomponent: "+comp.getPath());
    if (comp == null) {
      System.err.println("Can not dispose null tipi!");
      return;
    }
    if (comp.getTipiParent() == null) {
      System.err.println("Can not dispose tipi: It has no parent!");
      return;
    }
    TipiComponent parent = comp.getTipiParent();
    parent.removeChild(comp);
    if (comp instanceof TipiDataComponent) {
      removeTipiInstance(comp);
    } else {
//        System.err.println("Ignoring non-data component");
    }
    killComponent(comp);
  }

  private Object instantiateClass(String className, String defname, XMLElement instance) throws TipiException {
//    System.err.println("Instantiating class: " + className);
    XMLElement tipiDefinition = null;
    Class c = getTipiClass(className);
    // AAAAAAP
    if (defname!=null ) {
        tipiDefinition = getComponentDefinition(defname);
	}
    XMLElement classDef = (XMLElement) tipiClassDefMap.get(className);
//    System.err.println("Classes in map: "+tipiClassDefMap.size());
    if (c == null) {
      throw new TipiException("Error retrieving class definition. Looking for class: " + defname + ", classname: " + className);
    }
    Object o;
    try {
      o = c.newInstance();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new TipiException("Error instantiating class:"+className+"/"+defname+". Class may not have a public default contructor, or be abstract, or an interface");
    }
    if (TipiComponent.class.isInstance(o)) {
      TipiComponent tc = (TipiComponent) o;
      tc.setContext(this);
//      tc.setContainer(tc.createContainer());
      tc.setPropertyComponent(classDef.getBooleanAttribute("propertycomponent", "true", "false", false));
      tc.setStudioElement(instance.getBooleanAttribute("studioelement", "true", "false", false));
      tc.initContainer();
      tc.instantiateComponent(instance, classDef);
      if (tipiDefinition != null) {
        tc.load(tipiDefinition, instance, this);
      }
      else {
        tc.load(instance, instance, this);
      }
      // Moved from the previous else clause
      tc.loadEventsDefinition(this, instance, classDef);
      tc.loadMethodDefinitions(this, instance, classDef);
//      System.err.println("Instantiating class: "+className+" def: "+defname);
//      tc.setContainerVisible(true);
      return tc;
    }
    else {
//      System.err.println("Not a TIPICOMPONENT!!");
    }
    if (TipiLayout.class.isInstance(o)) {
      TipiLayout tl = (TipiLayout) o;
      tl.setContext(this);
      return tl;
    }
    throw new TipiException("INSTANTIATING UNKOWN SORT OF CLASS THING.");
  }

  public Class getTipiClass(String name) throws TipiException {
    return (Class) tipiClassMap.get(name);
  }

  private final void addTipiClassDefinition(XMLElement xe) throws TipiException {
    String pack = (String) xe.getAttribute("package");
    String name = (String) xe.getAttribute("name");
    String clas = (String) xe.getAttribute("class");
    String fullDef = pack + "." + clas;
    setSplashInfo("Adding: " + fullDef);
//    System.err.println("Loading class: "+fullDef+" with classloader: "+getClass().getClassLoader());
//    System.err.println("Adding class " + pack + "." + clas + " as " + name);
    try {
      Class c = Class.forName(fullDef,true,getClassLoader());
      tipiClassMap.put(name, c);
      tipiClassDefMap.put(name, xe);
    }
    catch (ClassNotFoundException ex) {
      ex.printStackTrace();
 //     throw new TipiException("Trouble loading class. Name: " + clas + " in package: " + pack);
    }
  }
  

  public Iterator getTipiClassDefIterator() {
    return tipiClassDefMap.keySet().iterator();
  }

  public void addActionDefinition(XMLElement xe) throws TipiException {
    myActionManager.addAction(xe, this);
  }

  public void addTipiInstance(String service, TipiDataComponent instance) {
    if (tipiInstanceMap.containsKey(service)) {
      ArrayList al = (ArrayList) tipiInstanceMap.get(service);
      al.add(instance);
    }
    else {
      ArrayList al = new ArrayList();
      al.add(instance);
      tipiInstanceMap.put(service, al);
    }
  }

  public void removeTipiInstance(TipiComponent instance) {
    Iterator c = tipiInstanceMap.values().iterator();
//    System.err.println("Removing tipi: "+instance.getPath());
    while (c.hasNext()) {
      ArrayList current = (ArrayList) c.next();
//      System.err.println("Checking for removal:");
      for(int i=current.size()-1; i>=0; i--){
        TipiComponent element = (TipiComponent) current.get(i);
//        System.err.println("Element with path: "+element.getPath());
        if (instance.getPath().equals(element.getPath())) {
//            System.err.println("pathmatch found. removing");
            current.remove(i);
            continue;
        }
        if (element.getPath().startsWith(instance.getPath()+"/")) {
//            System.err.println("partial pathmatch found. removing");
            current.remove(i);
        }
      }
    }
  }

  public void printTipiInstanceMap() {
      System.err.println("=================================Instance map:");
    Iterator c = tipiInstanceMap.keySet().iterator();
    while (c.hasNext()) {
      String currentKey = (String) c.next();
      ArrayList current = (ArrayList) tipiInstanceMap.get(currentKey);
      System.err.println("   Current service: " + currentKey);
      for (int i = 0; i < current.size(); i++) {
        TipiComponent tc = (TipiComponent) current.get(i);
        System.err.println("      Tipi with path: " + tc.getPath()+" hash: "+tc.hashCode());
      }
      System.err.println("   End of Current service: " + currentKey);
    }
    System.err.println("=================================End of print of tipi instance map:");
  }

  protected XMLElement getTipiDefinition(String name) throws TipiException {
      XMLElement xe = (XMLElement) tipiComponentMap.get(name);
      return xe;
  }

  public ArrayList getTipiInstancesByService(String service) throws TipiException {
    return (ArrayList) tipiInstanceMap.get(service);
  }
  
  public ArrayList getListeningServices() {
      return new ArrayList(tipiInstanceMap.keySet());
  }

  public XMLElement getComponentDefinition(String componentName) throws TipiException {
    XMLElement xe = getTipiDefinition(componentName);
    if(xe!=null) {
        return xe;
    }
    String location = (String)lazyMap.get(componentName);
    if (location==null) {
//    	System.err.println("No lazy location found: "+componentName);
//    	Thread.dumpStack();
    	return null;
    }
    System.err.println("Parsing lib location: "+location);
    parseLibrary(location, true,"dir", componentName, false);
    xe = getTipiDefinition(componentName);

    return xe;
  }

  protected void addComponentDefinition(XMLElement elm) {
    String defname = (String) elm.getAttribute("name");
    setSplashInfo("Loading: " + defname);
    tipiComponentMap.put(defname, elm);
//    tipiMap.put(defname, elm);
  }

  public void addTipiDefinition(XMLElement elm) {
    String tipiName = (String) elm.getAttribute("name");
    addComponentDefinition(elm);
  }

  private TipiActionBlock createTipiActionBlockCondition() {
    return new TipiActionBlock(this);
  }

  public ArrayList getScreens() {
    return screenList;
  }

  public Object getTopLevel() {
    return ( (TipiComponent) getDefaultTopLevel()).getContainer();
  }

  public TipiComponent getDefaultTopLevel() {
    return topScreen;
  }

  public void setDefaultTopLevel(TipiComponent tc) {
    topScreen = tc;
  }

  public void closeAll() {
    screenList.clear();
//    screenDefList.clear();
//    tipiMap.clear();
    tipiComponentMap.clear();
//    tipiServiceMap.clear();
    tipiInstanceMap.clear();
    tipiClassMap.clear();
    tipiClassDefMap.clear();
//    System.err.println("closeAll: clearing includeList");
    includeList.clear();
  }


  public void switchToDefinition(String name, TipiEvent event) throws TipiException {
    setSwitching(true);
    clearTopScreen();
    setSplashInfo("Starting application");
//    System.err.println("Switching to: "+name);
    
    XMLElement componentDefinition = getComponentDefinition(name);
    if (componentDefinition==null) {
        throw new TipiException("Fatal tipi error: Can not switch. Unknown definition: "+name);
    }
    TipiComponent tc = instantiateComponent(componentDefinition);
    tc.commitToUi();
    ( (TipiComponent) getDefaultTopLevel()).addComponent(tc, this, null);
    ( (TipiComponent) getDefaultTopLevel()).addToContainer(tc.getContainer(), null);
//    if (TipiDataComponent.class.isInstance(tc)) {
//      ( (TipiDataComponent) tc).autoLoadServices(this, event);
//    }
    setSplashVisible(false);
//    ( (TipiDataComponent) getDefaultTopLevel()).autoLoadServices(this, event);

    currentDefinition = name;
    setSwitching(false);
    fireTipiStructureChanged(tc);
  }




  protected synchronized boolean isSwitching() {
//    System.err.println("Setting switching to: "+b);
    return isSwitching;
  }

  protected synchronized void setSwitching(boolean b) {
//    System.err.println("Setting switching to: "+b);
    isSwitching = b;
  }

  public String getCurrentDefinition() {
    return currentDefinition;
  }

  public void setCurrentDefinition(String name) {
    currentDefinition = name;
  }

  public abstract void setSplashVisible(boolean b);

  public abstract void setSplashInfo(String s);

  public TipiComponent getTipiComponentByPath(String path) {
    if (path.indexOf("/") == 0) {
      path = path.substring(1);
    }
    return ( (TipiComponent) getDefaultTopLevel()).getTipiComponentByPath(path);
  }

  public TipiDataComponent getTipiByPath(String path) {
    TipiComponent tc = getTipiComponentByPath(path);
    if (!TipiDataComponent.class.isInstance(tc)) {
//      System.err.println("Object referred to by path: " + path + " is a TipiComponent, not a Tipi");
      return null;
    }
    return (TipiDataComponent) tc;
  }

  private final void enqueueAsyncSend(Navajo n, String tipiDestinationPath, String method, ConditionErrorHandler ch, boolean breakOnError, TipiEvent event) throws TipiBreakException {
  }

  private final void logServicePerformance(String service, long time) {
  }


//  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch)  {
//    try {
//      return doSimpleSend(n, service, ch, -1, false);
//    }
//    catch (TipiBreakException ex) {
//      // will not happen.
//      ex.printStackTrace();
//      return null;
//    }
//  }

  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, boolean breakOnError) throws TipiBreakException {
    return doSimpleSend(n, service, ch, -1, breakOnError);
  }
  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, long expirtationInterval, boolean breakOnError) throws TipiBreakException  {
    return doSimpleSend(n,service,ch,expirtationInterval,null,null,null,breakOnError);
  }

  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, long expirtationInterval, String hosturl, String username, String password, boolean breakOnError) throws TipiBreakException{
    return doSimpleSend(n,service,ch,expirtationInterval,hosturl,username,password,null,null,breakOnError);
  }

//  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, String clientName, boolean breakOnError) throws TipiBreakException {
//	return doSimpleSend(n, service, ch, -1, null,null,null, null, null, breakOnError);
//  }

  public Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, long expirtationInterval, String hosturl, String username, String password,String keystore,String keypass, boolean breakOnError) throws TipiBreakException {
	    return doSimpleSend(n,service,ch,expirtationInterval,hosturl,username,password,null,null,breakOnError,null);
 }
  
  private Navajo doSimpleSend(Navajo n, String service, ConditionErrorHandler ch, long expirtationInterval, String hosturl, String username, String password,String keystore,String keypass, boolean breakOnError, String clientName) throws TipiBreakException {
      Navajo reply = null;
    if (myThreadPool == null) {
      myThreadPool = new TipiThreadPool(this, getPoolSize());
    }
    try {
      String oldhost = null;
      if (hosturl!=null &&!"".equals(hosturl)) {
        if (NavajoClientFactory.getClient() instanceof DirectClientImpl) {
          ClientInterface ci = NavajoClientFactory.createDefaultClient();
          ci.setServerUrl(hosturl);
//          System.err.println("Specifically sending to: "+hosturl);
          ci.setUsername(username);
          ci.setPassword(password);
          if (keystore!=null && keypass!=null && !"".equals(keystore)) {
//            System.err.println("Setting secure. Keystore: "+keystore+" keypass: "+keypass);
            ci.setSecure(keystore,keypass,true);
          }

          reply = ci.doSimpleSend(n, service, ch, expirtationInterval);
          debugLog("data","simpleSend to host (diverted from directclient): "+hosturl+" username: "+username+" password: "+password+" method: "+service);
        } else {
          /** @todo This situation is untested */
          String url = NavajoClientFactory.getClient().getServerUrl();
          NavajoClientFactory.getClient().setServerUrl(hosturl);
          NavajoClientFactory.getClient().setUsername(username);
          NavajoClientFactory.getClient().setPassword(password);
          reply = NavajoClientFactory.getClient().doSimpleSend(n, service, ch, expirtationInterval);
          NavajoClientFactory.getClient().setServerUrl(url);
          debugLog("data","simpleSend to host: "+hosturl+" username: "+username+" password: "+password+" method: "+service);
        }

      } else {
        reply = NavajoClientFactory.getClient().doSimpleSend(n, service, ch, expirtationInterval);
        debugLog("data","simpleSend client: "+NavajoClientFactory.getClient().getClientName()+" method: "+service);
      }
    }
    catch (Throwable ex) {
      if (eHandler != null && Exception.class.isInstance(ex)) {
        debugLog("data","send error occurred:"+ex.getMessage()+" method: "+service);
        eHandler.showError( (Exception) ex);
        if (breakOnError) {
          throw new TipiBreakException(TipiBreakException.BREAK_EVENT);
        }
      }
      ex.printStackTrace();
    }
    if (reply == null) {
      reply = NavajoFactory.getInstance().createNavajo();
    }
    return reply;
  }

  public void performTipiMethod(TipiDataComponent t, Navajo n, String tipiDestinationPath, String method, boolean breakOnError, TipiEvent event, long expirationInterval, String hosturl, String username, String password) throws TipiException, TipiBreakException {
    performTipiMethod(t,n,tipiDestinationPath,method,breakOnError,event,expirationInterval,hosturl,username,password,null,null);
  }

  public void performTipiMethod(TipiDataComponent t, Navajo n, String tipiDestinationPath, String method, boolean breakOnError, TipiEvent event, long expirationInterval, String hosturl, String username, String password, String keystore, String keypass) throws TipiException, TipiBreakException {
	    performTipiMethod(t,n,tipiDestinationPath,method,breakOnError,event,expirationInterval,hosturl,username,password,keystore,keypass,null);
  }

  public void performTipiMethod(TipiDataComponent t, Navajo n, String tipiDestinationPath, String method, boolean breakOnError, TipiEvent event, long expirationInterval, String hosturl, String username, String password, String keystore, String keypass, String server) throws TipiException, TipiBreakException {

     ConditionErrorHandler ch = t;

    Navajo reply = doSimpleSend(n, method, ch, expirationInterval,hosturl,username,password,keystore,keypass,breakOnError);
    loadNavajo(reply,method,tipiDestinationPath,event,breakOnError,server);

//    long x2 = System.currentTimeMillis() - xx;
//    logServicePerformance(method, x2);
  }

  public void loadNavajo(Navajo reply, String method, String server)  throws TipiException, TipiBreakException {
    loadNavajo(reply,method,"*",null,false,server);
  }


  public void loadNavajo(Navajo reply, String method, String tipiDestinationPath, TipiEvent event, boolean breakOnError, String server) throws TipiException, TipiBreakException  {
    if (reply != null) {
   if (eHandler != null) {
     if (eHandler.hasErrors(reply)) {
       boolean hasUserDefinedErrorHandler = false;
       try {
//         System.err.println("Error found. Looking for instances listening to: "+method);
         ArrayList tipis = getTipiInstancesByService(method);
         if (tipis != null) {
           for (int i = 0; i < tipis.size(); i++) {
             TipiDataComponent current = (TipiDataComponent) tipis.get(i);

             if (current.hasPath(tipiDestinationPath, event)) {
               boolean hasHandler = false;
               debugLog("data    ","delivering error from method: "+method+" to tipi: "+current.getId());
               hasHandler = current.loadErrors(reply,method);
               if (hasHandler) {
                 hasUserDefinedErrorHandler = true;
               }
             } else {
               //System.err.println("HASPATH RETURNED FALSE!!!!");
             }
           }
         } else {
//           System.err.println("No tipis found");
         }
       }
       catch (TipiException ex1) {
         ex1.printStackTrace();
       }
       if (!hasUserDefinedErrorHandler) {
         eHandler.showError();
       }
//       System.err.println("Error found in service... Checking for break");
       if (breakOnError) {
         //System.err.println("breakOnError set. Breaking event...");
         throw new TipiBreakException( -1);
       }
       return;
     }
   }
   try {
     if (studioMode) {
       storeTemplateNavajo(method, reply);
       fireNavajoLoaded(method, reply);
     }
     loadTipiMethod(reply, tipiDestinationPath, method,server);
   }
   catch (TipiException ex) {
     ex.printStackTrace();
   }
 }

  }
  public void parseStudio() throws IOException, XMLParseException, TipiException{
     // do nothing
   }

  protected void storeTemplateNavajo(String method, Navajo reply) {
    // do nothing
  }

  protected void fireNavajoLoaded(String method, Navajo reply) {
    // do nothing
  }
  public void fireTipiStructureChanged(TipiComponent tc) {
     // do nothing
   }


  protected final void loadTipiMethod(Navajo reply, String tipiDestinationPath, String method,String server) throws TipiException {
    TipiDataComponent tt;
    ArrayList tipiList;
    //System.err.println("Loading method");
    if ("-".equals(tipiDestinationPath)) {
      System.err.println("Destination blocked");
      return;
    }
    tipiList = getTipiInstancesByService(method);
    if (tipiList == null) {
      return;
    }
    for (int i = 0; i < tipiList.size(); i++) {
      TipiDataComponent t = (TipiDataComponent) tipiList.get(i);
      debugLog("data    ","delivering data from method: "+method+" to tipi: "+t.getId());
      try {
		t.loadData(reply, this, method,server);
	} catch (TipiBreakException e) {
		System.err.println("Data refused by component");
	}
      if (t.getContainer() != null) {
        t.tipiLoaded();
      }
    }
  }


  protected Object attemptGenericEvaluate(String expression) {
//    System.err.println("\n\nAttempting to evaluate: "+expression);
    Operand o = null;
    try {
      o = evaluate(expression, getDefaultTopLevel(), null);
    }
    catch (Throwable ex) {
//      ex.printStackTrace();
//      System.err.println("Trouble: "+ex.getMessage()+" Returning original: "+expression);
      return expression;
    }
    if (o==null) {
//      System.err.println("Null. Returning original: "+expression);
      return expression;
    }
    if (o.value==null) {
//      System.err.println("Null evaluation. Returning original: "+expression);
      return expression;
    }
//    System.err.println("returning evaluation: "+o.value);
    return o.value;
  }

  public void resetConditionRuleById(String id) {
    for (int i = 0; i < screenList.size(); i++) {
      // Instances
      TipiComponent current = (TipiComponent) screenList.get(i);
      current.resetComponentValidationStateByRule(id);
    }
  }

  public Operand evaluate(String expr, TipiComponent tc, TipiEvent event) {
    return evaluate(expr,tc,event,null);
  }

  public Operand evaluate(String expr, TipiComponent tc, TipiEvent event, Message currentMessage) {

    return evaluate(expr,tc,event,tc.getNearestNavajo(),currentMessage);
  }

  public Operand evaluate(String expr, TipiComponent tc, TipiEvent event, Navajo n, Message currentMessage) {
    Operand o = null;
    if (expr==null) {
     return null;
    }
//    System.err.println("Evaluating: "+expr);
//    if(n != null){
//      System.err.println("Navajo: " + n.toString().length());
//    }
//    System.err.println("Message null? "+currentMessage == null);
    try {
        synchronized (tc) {
          tc.setCurrentEvent(event);
          o = Expression.evaluate(expr, n, null, currentMessage, null, tc);
          if (o==null) {
            System.err.println("Expression evaluated to null operand!");
        }
        }
    }
    catch (Exception ex) {
      System.err.println("Not happy while evaluating expression: " + expr + " message: " + ex.getMessage());
      Operand op = new Operand(expr, Property.STRING_PROPERTY, "");
//      ex.printStackTrace();
      return o;
    }
    catch (Error ex) {
      System.err.println("Not happy while evaluating expression: " + expr + " message: " + ex.getMessage());
//        ex.printStackTrace();
        Operand op = new Operand(expr, Property.STRING_PROPERTY, "");
      return o;
    }
    if (o.type.equals(Property.STRING_PROPERTY)) {
      if (o.value != null) {
        String s = (String) o.value;
        if (s.length() > 1) {
          if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'') {
            o.value = s.substring(1, s.length() - 2);
          }
        }
      }
    }
    return o;
  }


  public Object evaluateExpression(String expression, TipiComponent tc, TipiEvent event) throws Exception {
      Object obj = null;
    if (expression.startsWith("{") && expression.endsWith("}")) {
      String path = expression.substring(1, expression.length() - 1);
      // Bad bad and evil. The exist operator should be outside the curlies.
      // That would require some extensions to the expression mechanism.
      // The current construction is a bit stupid, but it kind of works for now.
      if (path.startsWith("?")) {
        obj = new Boolean(exists(tc, path.substring(1)));
      }
      else if (path.startsWith("!?")) {
        obj = new Boolean(!exists(tc, path.substring(2)));
      }
      else {
        StringTokenizer str = new StringTokenizer(path, ":/");
        String protocol = null;
        String rest = null;
        if (str.hasMoreTokens()) {
          protocol = str.nextToken();
        }
        rest = path.substring(protocol.length() + 2);
        obj = parse(tc, protocol, rest, event);
 //        if (true) {
        return obj;
//        }
      }
    }
    else {
      System.err.println("Trying to evaluate an expression that is not a tipiexpression.\n I.e. It is not in placed in curly brackets: " + expression);
      Thread.dumpStack();
      return expression;
    }
    return obj;
  }

  public InputStream getResource(TipiComponent source, String id, TipiEvent event) throws IOException {
    TipiResourceReference trr = getResourceReference(id);
    if (trr != null) {
      return trr.getStream(source, event);
    }
    throw new IOException("Resource: " + id + " unknown with tipi context");
  }

  public TipiResourceReference getResourceReference(String id) {
    return (TipiResourceReference) resourceReferenceMap.get(id);
  }

  public List getResourceList() {
    return resourceReferenceList;
  }

  public void addResourceReference(String id, String description, String path, String type, boolean local, boolean eager) {
    resourceReferenceList.add(id);
    resourceReferenceMap.put(id, new TipiResourceReference(this, id, description, path, type, local, eager));
  }
  public void addResourceReference(TipiResourceReference trr) {
    resourceReferenceList.add(trr.getId());
    resourceReferenceMap.put(trr.getId(),trr);
  }

  public void clearResourceReference() {
    resourceReferenceMap.clear();
    resourceReferenceList.clear();
  }

  public void removeResourceReference(String id) {
    resourceReferenceList.remove(id);
    resourceReferenceMap.remove(id);
  }

  private final void parseParser(XMLElement xe) {
    String name = xe.getStringAttribute("name");
    String parserClass = xe.getStringAttribute("parser");
    String classType = xe.getStringAttribute("type");
    Class pClass = null;
    try {
      pClass = Class.forName(parserClass,true,getClassLoader());
    }
    catch (ClassNotFoundException ex) {
      System.err.println("Error loading class for parser: " + parserClass);
      return;
    }
    TipiTypeParser ttp = null;
    try {
      ttp = (TipiTypeParser) pClass.newInstance();
    }
    catch (IllegalAccessException ex1) {
      System.err.println("Error instantiating class for parser: " + parserClass);
      ex1.printStackTrace();
      return;
    }
    catch (InstantiationException ex1) {
      System.err.println("Error instantiating class for parser: " + parserClass);
      ex1.printStackTrace();
      return;
    }
    ttp.setContext(this);
    try {
      Class cc = Class.forName(classType,true,getClassLoader());
      ttp.setReturnType(cc);
    }
    catch (ClassNotFoundException ex) {
      System.err.println("Error verifying return type class for parser: " + classType);
      return;
    }
    parserInstanceMap.put(name, ttp);
  }

  public Object parse(TipiComponent source, String name, String expression, TipiEvent te) {
    TipiTypeParser ttp = (TipiTypeParser) parserInstanceMap.get(name);
    if (ttp == null) {
      System.err.println("Unknown type: " + name);
      return null;
    }
    Object o = ttp.parse(source, expression, te);
    Class c = ttp.getReturnType();
    if (o != null && !c.isInstance(o)) {
      throw new IllegalArgumentException("Wrong type returned. Expected: " + c + "\nfound: " + o.getClass() + "\nWas parsing expression: " + expression + "\nUsing parser: " + name);
    }
    return o;
  }

  public boolean isValidType(String name) {
    return parserInstanceMap.containsKey(name);
  }

  public String toString(TipiComponent source, String name, Object o) {
    TipiTypeParser ttp = (TipiTypeParser) parserInstanceMap.get(name);
    if (ttp == null) {
      System.err.println("Unknown type: " + name);
      return null;
    }
    Class c = ttp.getReturnType();
    if (o == null) {
      return null;
    }
    if (!c.isInstance(o)) {
//      System.err.println("PRocessing source: " + source.getPath());
      throw new IllegalArgumentException("Wrong type: Need type: " + name + " (being of class: " + c.toString() + ") but found: " + o.getClass());
    }
    return ttp.toString(o, source);
  }

  private boolean exists(TipiComponent source, String path) {
    if (source != null) {
      try {
        Object p = source.evaluateExpression(path);
        return p != null;
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return false;
      }
    }
    else {
      Object p = evaluate(path, null, null);
      return p != null;
    }
  }

  public void setWaiting(boolean b) {
  }

  public void setActiveThreads(int i) {
    for (int j = 0; j < myActivityListeners.size(); j++) {
      TipiActivityListener tal = (TipiActivityListener) myActivityListeners.get(j);
      tal.setActiveThreads(i);
    }
  }

  public boolean isDefined(TipiComponent comp) {
    if (comp != null) {
      try {
		if (getTipiDefinition(comp.getName()) != null) {
		    return true;
		  }
		  else {
		    return false;
		  }
	} catch (TipiException e) {
		e.printStackTrace();
		return false;
	}
    }
    else {
      return true;
    }
  }


  public XMLElement getComponentTree() {
    try {
      XMLElement root = new CaseSensitiveXMLElement();
      root.setName("tid");
      root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      root.setAttribute("xsi:noNamespaceSchemaLocation", "tipiscript.xsd");
      root.setAttribute("errorhandler", "error");
      Set s = new TreeSet(includeList);
      Iterator iter = s.iterator();
      while (iter.hasNext()) {
//      for (int j = 0; j < s.size(); j++) {
        String location = (String) iter.next();
        XMLElement inc = new CaseSensitiveXMLElement();
        inc.setName("tipi-include");
        inc.setAttribute("location", location);
        root.addChild(inc);
      }
//      if (clientConfig != null) {
//        root.addChild(clientConfig);
//      }
      Iterator it = tipiComponentMap.keySet().iterator();
      while (it.hasNext()) {
        String name = (String) it.next();
        XMLElement current = (XMLElement) tipiComponentMap.get(name);
        boolean se = current.getAttribute("studioelement") != null;
        if (!se) {
          root.addChild(current);
        }
      }
      return root;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

//  public void addDefinition(XMLElement xe) {
//    addTipiDefinition(xe);
//    fireTipiDefinitionAdded(xe.getStringAttribute("name"));
//  }
  public void deleteDefinition(String definition) {
    tipiComponentMap.remove(definition);
  }


  public void replaceDefinition(XMLElement xe) {
    String name = xe.getStringAttribute("name");
    tipiComponentMap.put(name, xe);
  }

  public void storeComponentTree(String name) {
    try {
//      System.err.println("NAME: " + name);
      FileWriter fw = new FileWriter(name);
      getComponentTree().write(fw);
      fw.flush();
      fw.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  //EOF
  public void addActivityListener(ActivityController al) {
    activityListenerList.add(al);
  }

  public void removeActivityListener(ActivityController al) {
    activityListenerList.remove(al);
  }

  private boolean isStudioElement(TipiComponent tc) {
    if (tc.isStudioElement()) {
      return true;
    }
    return tc.getPath().indexOf("//studio") != -1;
  }

  public void performedEvent(TipiComponent tc, TipiEvent e) throws BlockActivityException {
    boolean blocked = false;
    for (int i = 0; i < activityListenerList.size(); i++) {
      try {
        ActivityController current = (ActivityController) activityListenerList.get(i);
        current.performedEvent(tc, e);
      }
      catch (BlockActivityException ex) {
        blocked = true;
      }
    }
    if (blocked && !isStudioElement(tc)) {
//      System.err.println("Event blocked. Component: " + tc.getPath() + " is studio? " + tc.isStudioElement());
      throw new BlockActivityException();
    }
  }

  public void performedBlock(TipiComponent tc, TipiActionBlock tab, String expression, String exprPath, boolean passed, TipiEvent te) throws BlockActivityException {
    boolean blocked = false;
    for (int i = 0; i < activityListenerList.size(); i++) {
      try {
        ActivityController current = (ActivityController) activityListenerList.get(i);
        current.performedBlock(tc, tab, expression, exprPath, passed,te);
      }
      catch (BlockActivityException ex) {
        blocked = true;
      }
      if (blocked && !isStudioElement(tc)) {
        System.err.println("Block blocked. Component: " + tc.getPath() + " is studio? " + tc.isStudioElement());
        throw new BlockActivityException();
      }
    }
  }

  public void performedAction(TipiComponent tc, TipiAction ta, TipiEvent te) throws BlockActivityException {
    boolean blocked = false;
    for (int i = 0; i < activityListenerList.size(); i++) {
      try {
        ActivityController current = (ActivityController) activityListenerList.get(i);
        current.performedAction(tc, ta,te);
      }
      catch (BlockActivityException ex) {
        blocked = true;
      }
    }
    if (blocked && !isStudioElement(tc)) {
      System.err.println("Action blocked. Component: " + tc.getPath() + " is studio? " + tc.isStudioElement());
      throw new BlockActivityException();
    }
  }


// note that the TipiException will only be thrown in sync mode (== poolsize 0)
 // UPDATE: NOT ANYMORE
  public void performAction(final TipiEvent te, TipiEventListener listener)  {
    debugLog("event   ","enqueueing async event: "+te.getEventName());
    if (myThreadPool == null) {
      myThreadPool = new TipiThreadPool(this, getPoolSize());
    }
    try {
        myThreadPool.performAction(te, listener);
    } catch (Throwable e) {
        e.printStackTrace();
    }
  }

  public void threadStarted(Thread workThread) {
  }

  public void threadEnded(Thread workThread) {
  }

//  public void createStartupFile(File startupDir, Set jarList, XMLElement projectSettings) throws IOException {
//    System.err.println("This implementation can not create a startup file!");
//  }
//

  public void loadServerSettingsFromProperties() {
    String impl = System.getProperty("tipi.client.impl");
    if ("direct".equals(impl)) {
      System.err.println("********* FOR NOW: Only supports indirect client *******");
    }
    String cfg = System.getProperty("tipi.client.config");
//     String secure = System.getProperty("tipi.client.impl");
    String keystore = System.getProperty("tipi.client.keystore");
    String storepass = System.getProperty("tipi.client.storepass");
    String navajoServer = System.getProperty("tipi.client.server");
    String navajoUsername = System.getProperty("tipi.client.username");
    String navajoPassword = System.getProperty("tipi.client.password");
    NavajoClientFactory.getClient().setUsername(navajoUsername);
    NavajoClientFactory.getClient().setPassword(navajoPassword);
    NavajoClientFactory.getClient().setServerUrl(navajoServer);
  }

  private final void parseResource(XMLElement xe) {
    TipiResourceReference trr = null;
    try {
      trr = new TipiResourceReference(this, xe);
    }
    catch (IOException ex) {
      System.err.println("ERROR LOADING EAGER Resource: " + xe);
    }
    resourceReferenceMap.put(trr.getId(), trr);
    resourceReferenceList.add(trr.getId());
  }

//  private final void parsePackageReference(XMLElement xe) {
//    TipiPackageReference trr = null;
//    trr = new TipiPackageReference(this, xe);
//    packageReferenceMap.put(trr.getId(), trr);
//    packageReferenceList.add(trr.getId());
//  }

  private final void parsePackage(XMLElement xe) {
    TipiPackage tp = null;
    tp = new TipiPackage(this, xe);
    packageMap.put(tp.getId(), tp);
    packageList.add(tp.getId());
  }

  public Iterator getPackageIterator() {
    return packageList.iterator();
  }

  public Iterator getPackageReferenceIterator() {
//    return packageReferenceList.iterator();
      throw new IllegalAccessError("Sorry, deploy no longer available");
  }

  public TipiPackage getTipiPackage(String id) {
    return (TipiPackage) packageMap.get(id);
  }

//  public void addPackageReference(String id) {
//    TipiPackageReference trr = null;
//    trr = new TipiPackageReference(this, id);
//    packageReferenceMap.put(trr.getId(), trr);
//    packageReferenceList.add(trr.getId());
//  }
//
//  public void removePackageReference(String id) {
//    TipiPackageReference trr = (TipiPackageReference) packageReferenceMap.get(id);
//    if (trr == null) {
//      return;
//    }
//    packageReferenceList.remove(trr);
//    packageReferenceMap.remove(id);
//  }
//
//  public void clearPackageReference() {
//    packageReferenceList.clear();
//    packageReferenceMap.clear();
//  }
//  
//  
  public void addTipiActivityListener(TipiActivityListener listener) {
    myActivityListeners.add(listener);
  }

  public void removeTipiActivityListener(TipiActivityListener listener) {
    myActivityListeners.remove(listener);
  }
/**
 * @return
 */
public Set getRequiredIncludes() {
    Set s = new HashSet();
    s.add("com/dexels/navajo/tipi/classdef.xml");
    return s;
}
/**
 * 
 */
public void parseRequiredIncludes() {
    for (Iterator iter = getRequiredIncludes().iterator(); iter.hasNext();) {
        String element = (String) iter.next();
        includeList.add(element);
    }
}

public void enqueueExecutable(TipiExecutable te) throws  TipiBreakException, TipiException {
    myThreadPool.enqueueExecutable(te);
}

public TipiStorageManager getStorageManager() {
    return myStorageManager;
    
    
}

public void setStorageManager(TipiStorageManager tsm) {
    if (tsm==null) {
        throw new IllegalArgumentException("setStorageManager: Can not be null");
    }
    System.err.println("Storage manager class: "+tsm.getClass());
    myStorageManager = tsm;
}

public Navajo retrieveDocument(String id)  {
    if (getStorageManager()!=null) {
        try {
            return getStorageManager().getStorageDocument(id);
        } catch (TipiException e) {
             e.printStackTrace();
        }
    }
    return null;
}
public void storeDocument(String id, Navajo n)  {
    if (getStorageManager()!=null) {
        try {
            getStorageManager().setStorageDocument(id,n);
        } catch (TipiException e) {
            e.printStackTrace();
        }
    }
}

public static void debugTipiComponentTree(TipiComponent c, int indent) {
    printIndent(indent, "Debugging component"+c.toString() );
    printIndent(indent, "Childlist"+((TipiComponentImpl)c).childDump() );
    
      for (int i = 0; i < c.getChildCount(); i++) {
          TipiComponent ccc = c.getTipiComponent(i);
          debugTipiComponentTree(ccc, indent+3);
      }
    printIndent(indent, "End of debug component: "+c.getId()+" class: "+c.getClass());
    
}
protected static void printIndent(int indent, String text) {
    for (int i = 0; i < indent; i++) {
      System.err.print(" ");
  }
    System.err.println(text);
}

public void exit() {
	System.exit(0); 
}

public void addShutdownListener(ShutdownListener sl) {
	  shutdownListeners.add(sl);
}

public void removeShutdownListener(ShutdownListener sl) {
	  shutdownListeners.remove(sl);
}

public void shutdown() {
	// prevent multishutdown
	if (contextShutdown) {
		return;
	}
    if (myThreadPool!=null) {
        myThreadPool.shutdown();
      }
	  for (int i = 0; i < shutdownListeners.size(); i++) {
		ShutdownListener s = (ShutdownListener)shutdownListeners.get(i);
		s.contextShutdown();
	}
	  contextShutdown = true;
}

}