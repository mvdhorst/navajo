package tipirss;

import org.osgi.framework.BundleContext;

import tipi.TipiAbstractXMLExtension;
import tipi.TipiExtension;

import com.dexels.navajo.tipi.TipiContext;

public class TipiRssExtension extends TipiAbstractXMLExtension implements TipiExtension {


	private static final long serialVersionUID = -6275880208619775468L;

	public TipiRssExtension() {
	}
	
	@Override
	public void initialize(TipiContext tc) {
		// Do nothing
	}

	@Override
	public void start(BundleContext context) throws Exception {
		registerTipiExtension(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		deregisterTipiExtension(context);
	}
}
