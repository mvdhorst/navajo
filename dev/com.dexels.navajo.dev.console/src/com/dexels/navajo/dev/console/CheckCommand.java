package com.dexels.navajo.dev.console;


import java.io.FileNotFoundException;
import java.util.Date;

import org.apache.felix.service.command.Descriptor;

import com.dexels.navajo.compiler.BundleCreator;

public class CheckCommand extends ConsoleCommand {
	
	private BundleCreator bundleCreator = null;
//	private final static Logger logger = LoggerFactory
//			.getLogger(CheckCommand.class);

	public void setBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = bundleCreator;
	}

	/**
	 * 
	 * @param bundleCreator the bundlecreator to clear
	 */
	public void clearBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = null;
	}


	@Descriptor(value = "Check the modification dates of a certain script.") 

	public void check(@Descriptor(value = "The path of the script") String script,@Descriptor(value = "The current tenant")  String tenant) throws FileNotFoundException {
		final String extension = ".xml";
		Date installed = bundleCreator.getBundleInstallationDate(script,tenant,extension);
		Date modified = bundleCreator.getScriptModificationDate(script,tenant,extension);
		Date compiled = bundleCreator.getCompiledModificationDate(script,extension);
		System.out.println("Modified at: "+modified);
		System.out.println("Compiled at: "+compiled);
		System.out.println("Installed at: "+installed);
	}

	@Override
	public String showUsage() {
		// TODO Auto-generated method stub
		return null;
	}
}
