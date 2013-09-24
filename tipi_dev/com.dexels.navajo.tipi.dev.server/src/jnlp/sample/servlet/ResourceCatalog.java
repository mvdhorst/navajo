/*
 * @(#)ResourceCatalog.java	1.6 05/11/17
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package jnlp.sample.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jnlp.sample.util.VersionID;
import jnlp.sample.util.VersionString;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
public class ResourceCatalog {
	public static final String VERSION_XML_FILENAME = "version.xml";

	private Logger _log = null;
	private ServletContext _servletContext = null;

	private HashMap _entries;

	/**
	 * Class to contain the information we know about a specific directory
	 */
	static private class PathEntries {
		/* Version-based entries at this particular path */
		private List _versionXmlList;
		private List _directoryList;
		private List _platformList;
		/* Last time this entry was updated */
		private long _lastModified; // Last modified time of entry;

		public PathEntries(List versionXmlList, List directoryList,
				List platformList, long lastModified) {
			_versionXmlList = versionXmlList;
			_directoryList = directoryList;
			_platformList = platformList;
			_lastModified = lastModified;
		}

		public void setDirectoryList(List dirList) {
			_directoryList = dirList;
		}

		public List getVersionXmlList() {
			return _versionXmlList;
		}

		public List getDirectoryList() {
			return _directoryList;
		}

		public List getPlatformList() {
			return _platformList;
		}

		public long getLastModified() {
			return _lastModified;
		}
	}

	public ResourceCatalog(ServletContext servletContext, Logger log) {
		_entries = new HashMap();
		_servletContext = servletContext;
		_log = log;
	}

	public JnlpResource lookupResource(DownloadRequest dreq)
			throws ErrorResponseException {
		// Split request up into path and name
		String path = dreq.getPath();
		_log.debug("Looking up: " + dreq.getPath());
		String name = null;
		String dir = null;
		int idx = path.lastIndexOf('/');
		if (idx == -1) {
			name = path;
		} else {
			name = path.substring(idx + 1); // Exclude '/'
			dir = path.substring(0, idx + 1); // Include '/'
		}

		// Lookup up already parsed entries, and san directory for entries if
		// neccesary
		PathEntries pentries = (PathEntries) _entries.get(dir);
		JnlpResource xmlVersionResPath = new JnlpResource(_servletContext, dir
				+ VERSION_XML_FILENAME);
		if (pentries == null
				|| (xmlVersionResPath.exists() && xmlVersionResPath
						.getLastModified() > pentries.getLastModified())) {
			_log.info("servlet.log.scandir", dir);
			List dirList = scanDirectory(dir, dreq);
			// Scan XML file
			List versionList = new ArrayList();
			List platformList = new ArrayList();
			parseVersionXML(versionList, platformList, dir, xmlVersionResPath);
			pentries = new PathEntries(versionList, dirList, platformList,
					xmlVersionResPath.getLastModified());
			_entries.put(dir, pentries);
		}
		// Search for a match
		JnlpResource[] result = new JnlpResource[1];

		if (dreq.isPlatformRequest()) {
			int sts = findMatch(pentries.getPlatformList(), name, dreq, result);
			if (sts != DownloadResponse.STS_00_OK) {
				throw new ErrorResponseException(
						DownloadResponse.getJnlpErrorResponse(sts));
			}
		} else {
			// First lookup in versions.xml file
			int sts1 = findMatch(pentries.getVersionXmlList(), name, dreq,
					result);
			if (sts1 != DownloadResponse.STS_00_OK) {
				// Then lookup in directory
				_log.debug("Looking up directoryList...");
				int sts2 = findMatch(pentries.getDirectoryList(), name, dreq,
						result);
				if (sts2 != DownloadResponse.STS_00_OK) {

					// fix for 4450104
					// try rescan and see if it helps
					pentries.setDirectoryList(scanDirectory(dir, dreq));
					sts2 = findMatch(pentries.getDirectoryList(), name, dreq,
							result);
					// try again after rescanning directory
					if (sts2 != DownloadResponse.STS_00_OK) {
						// Throw the most specific error code
						throw new ErrorResponseException(
								DownloadResponse.getJnlpErrorResponse(Math.max(
										sts1, sts2)));
					}
				}
			}
		}
		return result[0];
	}

	/**
	 * This method finds the best match, or return the best error code. The
	 * result parameter must be an array with room for one element.
	 * 
	 * If a match is found, the method returns DownloadResponse.STS_00_OK If one
	 * or more entries matches on: name, version-id, os, arch, and locale, then
	 * the one with the highest version-id is set in the result[0] field.
	 * 
	 * If a match is not found, it returns an error code, either:
	 * ERR_10_NO_RESOURCE, ERR_11_NO_VERSION, ERR_20_UNSUP_OS,
	 * ERR_21_UNSUP_ARCH, ERR_22_UNSUP_LOCALE, ERR_23_UNSUP_JRE.
	 * 
	 */
	public int findMatch(List list, String name, DownloadRequest dreq,
			JnlpResource[] result) {
		if (list == null)
			return DownloadResponse.ERR_10_NO_RESOURCE;
		// Setup return values
		VersionID bestVersionId = null;
		int error = DownloadResponse.ERR_10_NO_RESOURCE;
		VersionString vs = new VersionString(dreq.getVersion());
		// Iterate through entries
		for (int i = 0; i < list.size(); i++) {
			JnlpResource respath = (JnlpResource) list.get(i);
			VersionID vid = new VersionID(respath.getVersionId());
			int sts = matchEntry(name, vs, dreq, respath, vid);
			if (sts == DownloadResponse.STS_00_OK) {
				if (result[0] == null || vid.isGreaterThan(bestVersionId)) {
					result[0] = respath;
					bestVersionId = vid;
				}
			} else {
				error = Math.max(error, sts);
			}
		}
		return (result[0] != null) ? DownloadResponse.STS_00_OK : error;
	}

	public int matchEntry(String name, VersionString vs, DownloadRequest dreq,
			JnlpResource jnlpres, VersionID vid) {
		if (!name.equals(jnlpres.getName())) {
			return DownloadResponse.ERR_10_NO_RESOURCE;
		}
		if (!vs.contains(vid)) {
			return DownloadResponse.ERR_11_NO_VERSION;
		}
		if (!prefixMatchLists(jnlpres.getOSList(), dreq.getOS())) {
			return DownloadResponse.ERR_20_UNSUP_OS;
		}
		if (!prefixMatchLists(jnlpres.getArchList(), dreq.getArch())) {
			return DownloadResponse.ERR_21_UNSUP_ARCH;
		}
		if (!prefixMatchLists(jnlpres.getLocaleList(), dreq.getLocale())) {
			return DownloadResponse.ERR_22_UNSUP_LOCALE;
		}
		return DownloadResponse.STS_00_OK;
	}

	private static boolean prefixMatchStringList(String[] prefixList,
			String target) {
		// No prefixes matches everything
		if (prefixList == null)
			return true;
		// No target, but a prefix list does not match anything
		if (target == null)
			return false;
		for (int i = 0; i < prefixList.length; i++) {
			if (target.startsWith(prefixList[i]))
				return true;
		}
		return false;
	}

	/*
	 * Return true if at least one of the strings in 'prefixes' are a prefix to
	 * at least one of the 'keys'.
	 */
	public boolean prefixMatchLists(String[] prefixes, String[] keys) {
		// The prefixes are part of the server resources. If none is given,
		// everything matches
		if (prefixes == null)
			return true;
		// If no os keyes was given, and the server resource is keyed of this,
		// then return false.
		if (keys == null)
			return false;
		// Check for a match on a key
		for (int i = 0; i < keys.length; i++) {
			if (prefixMatchStringList(prefixes, keys[i]))
				return true;
		}
		return false;
	}

	/**
	 * This method scans the directory pointed to by the given path and creates
	 * a list of ResourcePath elements that contains information about all the
	 * entries
	 * 
	 * The version-based information is encoded in the file name given the
	 * following format:
	 * 
	 * entry ::= <name> __ ( <options> ). <ext> options ::= <option> ( __
	 * <options> )? option ::= V<version-id> | O<os> | A<arch> | L<locale>
	 * 
	 */

	private String jnlpGetPath(DownloadRequest dreq) {
		// fix for 4474021
		// try to manuually generate the filename
		// extract file name
		String path = dreq.getPath();
		String filename = path.substring(path.lastIndexOf("/") + 1);
		path = path.substring(0, path.lastIndexOf("/") + 1);
		String name = filename;
		String ext = null;

		if (filename.lastIndexOf(".") != -1) {
			ext = filename.substring(filename.lastIndexOf(".") + 1);

			filename = filename.substring(0, filename.lastIndexOf("."));

		}
		if (dreq.getVersion() != null) {
			filename += "__V" + dreq.getVersion();
		}

		String[] temp = dreq.getOS();

		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				filename += "__O" + temp[i];
			}
		}

		temp = dreq.getArch();

		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				filename += "__A" + temp[i];
			}
		}
		temp = dreq.getLocale();

		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				filename += "__L" + temp[i];
			}
		}

		if (ext != null) {
			filename += "." + ext;
		}

		path += filename;

		return path;
	}

	public List scanDirectory(String dirPath, DownloadRequest dreq) {
		ArrayList list = new ArrayList();

		// fix for 4474021
		File dir = ((ResourceResolver) _servletContext
				.getAttribute("resourceResolver")).getDir(dirPath);
		_log.debug("Dir resolved to: " + dir);
		if (!dir.exists()) {
			String path = jnlpGetPath(dreq);
			_log.debug("Dir PATH: " + path);

			String name = dreq.getPath().substring(path.lastIndexOf("/") + 1);
			_log.debug("Dir NAME: " + name);

			JnlpResource jnlpres = new JnlpResource(_servletContext, name,
					dreq.getVersion(), dreq.getOS(), dreq.getArch(),
					dreq.getLocale(), path, dreq.getVersion());

			// the file does not exist
			if (jnlpres.getResource() == null)
				return null;

			list.add(jnlpres);
			return list;
		}
		// File dir = new File(_servletContext.getRealPath(dirPath));
		_log.debug("File directory: " + dir);
		if (dir.exists() && dir.isDirectory()) {
			File[] entries = dir.listFiles();
			for (int i = 0; i < entries.length; i++) {
				JnlpResource jnlpres = parseFileEntry(dirPath,
						entries[i].getName());
				if (jnlpres != null) {
						_log.debug("Read file resource: " + jnlpres);
					list.add(jnlpres);
				}
			}
		} else {
			_log.debug("WTF? NOT File directory: " + dir);

		}
		return list;
	}

	private JnlpResource parseFileEntry(String dir, String filename) {
		_log.debug("Cheching dir: " + dir + " with file: " + filename);
		int idx = filename.indexOf("__");
		if (idx == -1)
			return null;

		// Cut out name
		String name = filename.substring(0, idx);
		String rest = filename.substring(idx);

		// Cut out extension
		idx = rest.lastIndexOf('.');
		String extension = "";
		if (idx != -1) {
			extension = rest.substring(idx);
			rest = rest.substring(0, idx);
		}
		_log.debug("name " + name + " == " + rest + " == " + extension);
		// Parse options
		String versionId = null;
		ArrayList osList = new ArrayList();
		ArrayList archList = new ArrayList();
		ArrayList localeList = new ArrayList();
		while (rest.length() > 0) {
			/* Must start with __ at this point */
			if (!rest.startsWith("__"))
				return null;
			rest = rest.substring(2);
			// Get option and argument
			char option = rest.charAt(0);
			idx = rest.indexOf("__");
			String arg = null;
			if (idx == -1) {
				arg = rest.substring(1);
				rest = "";
			} else {
				arg = rest.substring(1, idx);
				rest = rest.substring(idx);
			}
			switch (option) {
			case 'V':
				versionId = arg;
				break;
			case 'O':
				osList.add(arg);
				break;
			case 'A':
				archList.add(arg);
				break;
			case 'L':
				localeList.add(arg);
				break;
			default:
				return null; // error
			}
		}

		return new JnlpResource(_servletContext, name + extension, /*
																	 * Resource
																	 * name in
																	 * URL
																	 * request
																	 */
		versionId, listToStrings(osList), listToStrings(archList),
				listToStrings(localeList), dir + filename, /*
															 * Resource name in
															 * WAR file
															 */
				versionId);
	}

	private String[] listToStrings(List list) {
		if (list.size() == 0)
			return null;
		return (String[]) list.toArray(new String[list.size()]);
	}

	// Returns false if parsing failed
	private void parseVersionXML(final List versionList,
			final List platformList, final String dir,
			final JnlpResource versionRes) {
		if (!versionRes.exists())
			return;

		// Parse XML into a more understandable format
		XMLNode root = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new BufferedInputStream(versionRes
					.getResource().openStream()));
			doc.getDocumentElement().normalize();

			// Convert document into an XMLNode structure, since we already got
			// utility methods
			// to handle these. We should really use the data-binding stuff here
			// - but that will come
			// later
			//
			root = XMLParsing.convert(doc.getDocumentElement());
		} catch (SAXParseException err) {
			_log.warn("servlet.log.warning.xml.parsing",
					versionRes.getPath(),
					Integer.toString(err.getLineNumber()), err.getMessage());
			return;
		} catch (Throwable t) {
			_log.warn("servlet.log.warning.xml.reading",
					versionRes.getPath(), t);
			return;
		}

		// Check that root element is a <jnlp> tag
		if (!root.getName().equals("jnlp-versions")) {
			_log.warn("servlet.log.warning.xml.missing-jnlp",
					versionRes.getPath());
			return;
		}

		// Visit all <resource> elements
		XMLParsing.visitElements(root, "<resource>",
				new XMLParsing.ElementVisitor() {
					@Override
					public void visitElement(XMLNode node) {
						XMLNode pattern = XMLParsing.findElementPath(node,
								"<pattern>");
						if (pattern == null) {
							_log.warn(
									"servlet.log.warning.xml.missing-pattern",
									versionRes.getPath());
						} else {
							// Parse pattern
							String name = XMLParsing.getElementContent(pattern,
									"<name>", "");
							String versionId = XMLParsing.getElementContent(
									pattern, "<version-id>");
							String[] os = XMLParsing.getMultiElementContent(
									pattern, "<os>");
							String[] arch = XMLParsing.getMultiElementContent(
									pattern, "<arch>");
							String[] locale = XMLParsing
									.getMultiElementContent(pattern, "<locale>");
							// Get return request
							String file = XMLParsing.getElementContent(node,
									"<file>");
							if (versionId == null || file == null) {
								_log.warn(
										"servlet.log.warning.xml.missing-elems",
										versionRes.getPath());
							} else {
								JnlpResource res = new JnlpResource(
										_servletContext, name, versionId, os,
										arch, locale, dir + file, versionId);
								if (res.exists()) {
									versionList.add(res);
										_log.debug("Read resource: " + res);
								} else {
									_log.warn(
											"servlet.log.warning.missing-file",
											file, versionRes.getPath());
								}
							}
						}
					}
				});

		// Visit all <resource> elements
		XMLParsing.visitElements(root, "<platform>",
				new XMLParsing.ElementVisitor() {
					@Override
					public void visitElement(XMLNode node) {
						XMLNode pattern = XMLParsing.findElementPath(node,
								"<pattern>");
						if (pattern == null) {
							_log.warn(
									"servlet.log.warning.xml.missing-pattern",
									versionRes.getPath());
						} else {
							// Parse pattern
							String name = XMLParsing.getElementContent(pattern,
									"<name>", "");
							String versionId = XMLParsing.getElementContent(
									pattern, "<version-id>");
							String[] os = XMLParsing.getMultiElementContent(
									pattern, "<os>");
							String[] arch = XMLParsing.getMultiElementContent(
									pattern, "<arch>");
							String[] locale = XMLParsing
									.getMultiElementContent(pattern, "<locale>");
							// Get return request
							String file = XMLParsing.getElementContent(node,
									"<file>");
							String productId = XMLParsing.getElementContent(
									node, "<product-version-id>");

							if (versionId == null || file == null
									|| productId == null) {
								_log.warn(
										"servlet.log.warning.xml.missing-elems2",
										versionRes.getPath());
							} else {
								JnlpResource res = new JnlpResource(
										_servletContext, name, versionId, os,
										arch, locale, dir + file, productId);
								if (res.exists()) {
									platformList.add(res);
										_log.debug("Read platform resource: "
												+ res);
								} else {
									_log.warn(
											"servlet.log.warning.missing-file",
											file, versionRes.getPath());
								}
							}
						}
					}
				});
	}
}