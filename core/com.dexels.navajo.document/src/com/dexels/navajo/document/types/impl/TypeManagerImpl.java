package com.dexels.navajo.document.types.impl;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.nanoimpl.CaseSensitiveXMLElement;
import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.types.Type;
import com.dexels.navajo.document.types.TypeManager;

public class TypeManagerImpl implements TypeManager {

	
	private final Map<String, Class<?>> toJavaType = new HashMap<String, Class<?>>();
	private final Map<Class<?>, String> toNavajoType = new HashMap<Class<?>, String>();
	
	private final Map<String,ServiceRegistration<Type>> typeRegistrations = new HashMap<String, ServiceRegistration<Type>>();
	
	private BundleContext bundleContext = null;
	 
	
	private final static Logger logger = LoggerFactory
			.getLogger(TypeManagerImpl.class);
	
	public void activate(BundleContext bundleContext) throws Exception {
		this.bundleContext  = bundleContext;
		readStandardOSGiTypes();
	}
	
	public void deactivate() {
		deregisterTypes();
		this.bundleContext = null;
	}
	
	public void addType(Type t) {
		addNavajoType(t.getName(),t.getClazz());
	}

	public void removeType(Type t) {
		removeNavajoType(t.getName());
	}

	@Override
	public void readTypes() throws Exception {
		  ClassLoader cl = getClass().getClassLoader();
		  if(cl==null) {
			  logger.info("Bootstrap classloader detected!");
			  cl = ClassLoader.getSystemClassLoader();
		  }
		 InputStream is = cl.getResourceAsStream("navajotypes.xml");
		 CaseSensitiveXMLElement types = new CaseSensitiveXMLElement();
		 types.parseFromStream(is);
		 is.close();
		 
		 Vector<XMLElement> children = types.getChildren();
		 for (int i = 0; i < children.size(); i++) {
			 XMLElement child = children.get(i);
			 String navajotype = (String) child.getAttribute("name");
			 String javaclass = (String) child.getAttribute("type");
			 Class<?> c = Class.forName(javaclass);
			 toJavaType.put(navajotype, c);
			 toNavajoType.put(c, navajotype);
		 }
	  }
	  
	public void readStandardOSGiTypes() throws Exception {
		 InputStream is = getClass().getClassLoader().getResourceAsStream("navajotypes.xml");
		 CaseSensitiveXMLElement types = new CaseSensitiveXMLElement();
		 types.parseFromStream(is);
		 is.close();
		 
		 Vector<XMLElement> children = types.getChildren();
		 for (int i = 0; i < children.size(); i++) {
			 XMLElement child = children.get(i);
			 String navajotype = (String) child.getAttribute("name");
			 String javaclass = (String) child.getAttribute("type");
			 Class<?> c = Class.forName(javaclass);
			 Type t = new TypeImpl(navajotype, javaclass, c);
			 registerNavajoType(t);
			 addNavajoType(navajotype, c);
		 }
	  }

	public void registerNavajoType(Type type) {
//		Dictionary<String, Object> data = new HashMap<String,Object>();
		Dictionary<String, Object> d = new Hashtable<String,Object>();
		d.put("type", type.getType());
		d.put("name", type.getName());
		ServiceRegistration<Type> registration = bundleContext.registerService(Type.class, type, d);
		typeRegistrations.put(type.getName(), registration);
	}
	
	private void deregisterTypes() {
		for(Entry<String,ServiceRegistration<Type>> entry : typeRegistrations.entrySet()) {
			entry.getValue().unregister();
		}
	}

	  /* (non-Javadoc)
	 * @see com.dexels.navajo.document.types.impl.TipeManager#addNavajoType(java.lang.String, java.lang.Class)
	 */
	@Override
	public void addNavajoType(String typeId, Class<?> clz) {
		  toJavaType.put(typeId, clz);
		  toNavajoType.put(clz, typeId);
	  }
	  
	private void removeNavajoType(String name) {
		Class<?> c = toJavaType.get(name);
		toNavajoType.remove(c);
		toJavaType.remove(name);
	}

	  /* (non-Javadoc)
	 * @see com.dexels.navajo.document.types.impl.TipeManager#getNavajoType(java.lang.Class)
	 */
	@Override
	public String getNavajoType(Class<?> c) {
		  if ( c == null ) {
			  return "empty";
		  } else
		  if ( toNavajoType.containsKey(c) ) {
			  return toNavajoType.get(c);
		  } else {
			  return c.getName();
		  }
		  
	  }
	  
	  /* (non-Javadoc)
	 * @see com.dexels.navajo.document.types.impl.TipeManager#getJavaType(java.lang.String)
	 */
	@Override
	public Class<?> getJavaType(String p) {
		  return toJavaType.get(p);
	  }
	  
	  /* (non-Javadoc)
	 * @see com.dexels.navajo.document.types.impl.TipeManager#getNavajoTypes()
	 */
	@Override
	public Set<String> getNavajoTypes() {
		  return toJavaType.keySet();
	  }
	  
}

