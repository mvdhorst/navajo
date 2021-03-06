package tests.css;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.testimpl.AbstractTipiTest;

import tipi.TipiCoreExtension;
import tipi.TipiExtension;
import tipicss.TipiCssExtension;


public class CoreTipi extends AbstractTipiTest {

	
	private final static Logger logger = LoggerFactory
			.getLogger(CoreTipi.class);
	

	@Before
	public void setUp() throws Exception {
		List<TipiExtension> elist = new ArrayList<TipiExtension>();
		TipiExtension ed = new TipiCoreExtension();
		ed.loadDescriptor();
		elist.add(ed);

		TipiCssExtension tipiCss = new TipiCssExtension();
		tipiCss.loadDescriptor();
		elist.add(tipiCss);

		setContext("init", new File("testsrc/tests/css"),elist);
//		getContext().addOptionalInclude(tipiCss);
//		getContext().processRequiredIncludes(tipiCss);
		logger.info("Setup complete");
	}

	@Test
	public void testTipi() {
		 try {
		 Thread.sleep(1000);
		 } catch (InterruptedException e) {
		 }
		getContext().shutdown();
		String xx = getContext().getInfoBuffer();
		Assert.assertEquals("event1\nevent2\n0.99\n", xx);
		logger.info("Test ok: "+xx);
	}

}
