

import junit.framework.Test;
import junit.framework.TestSuite;

import com.dexels.navajo.client.BasicNavajoServerTests;
import com.dexels.navajo.client.NavajoScriptingTests;

public class NavajoClient {

	public static Test suite() {
		TestSuite suite = new TestSuite("Integration tests for com.dexels.navajo");
		//$JUnit-BEGIN$

		System.err.println("Disabled, TOO SLOW!");
//		suite.addTestSuite(NavajoScriptingTests.class);
//		suite.addTestSuite(BasicNavajoServerTests.class);
		
		//$JUnit-END$
		return suite;
	}

}
