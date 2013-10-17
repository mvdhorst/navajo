package tipi;

import java.net.MalformedURLException;
import java.net.URI;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.TipiContext;
import com.dexels.navajo.tipi.websocket.TipiWebsocketConnector;

public class TipiWebsocketExtension extends TipiAbstractXMLExtension implements TipiExtension {

	private static final long serialVersionUID = 5014050975833573426L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiWebsocketExtension.class);
	
	
	public TipiWebsocketExtension() {
	}
	@Override
	public void initialize(TipiContext tc) {
		// Do nothing
		String appstoreUrl = tc.getSystemProperty("tipi.appstore.websocketurl");
		String appstoreSession = tc.getSystemProperty("tipi.appstore.session");
		String appstoreApplication =  tc.getSystemProperty("tipi.appstore.application");
		String appstoreTenant =  tc.getSystemProperty("tipi.appstore.tenant");
		try {
			URI uri = new URI(appstoreUrl);
			TipiWebsocketConnector twt = new TipiWebsocketConnector(tc);
			twt.startup(uri);
			twt.sendMessage("tipi.appstore.session="+appstoreSession+"\n");
			twt.sendMessage("tipi.appstore.application="+appstoreApplication+"\n");
			twt.sendMessage("tipi.appstore.tenant="+appstoreTenant+"\n");
		} catch (MalformedURLException e) {
			logger.error("Error: ", e);
		} catch (Exception e) {
			logger.error("Error: ", e);
		}
		
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
