/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Dexels BV</p>
 * @author 
 * @version $Id$.
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DEXELS BV OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
package navajobirt;

import navajoextension.AbstractCoreExtension;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.birt.BirtAdapterLibrary;

/**
 * VERSION HISTORY
 * 
 * 2.0.3. -Added more verbose timing information
 * 
 */
public class Version extends AbstractCoreExtension {

	public static final int MAJOR = 1;
	public static final int MINOR = 0;
	public static final int PATCHLEVEL = 0;
	public static final String VENDOR = "Dexels";
	public static final String PRODUCTNAME = "Navajo BIRT Adapter";
	public static final String RELEASEDATE = "2006-04-09";

	private final static Logger logger = LoggerFactory.getLogger(Version.class);

	public Version() {

	}

	@Override
	public void start(BundleContext bc) throws Exception {

		super.start(bc);
		try {
			BirtAdapterLibrary library = new BirtAdapterLibrary();
			registerAll(library);
//			FunctionFactoryInterface fi = FunctionFactoryFactory.getInstance();
//			fi.init();
//
//			fi.clearFunctionNames();
//
//			fi.injectExtension(library);
//			for (String adapterName : fi.getAdapterNames(library)) {
//				// FunctionDefinition fd =
//				// fi.getAdapterDefinition(adapterName,extensionDef);
////				FunctionDefinition fd = fi.getAdapterConfig(library).get(
////						adapterName);
//				// FunctionDefinition fd = fi.getDef(extensionDef, adapterName);
//
//				String adapterClass = fi.getAdapterClass(adapterName, library);
//				Class<?> c = null;
//
//				try {
//					if (adapterClass != null) {
//						c = Class.forName(adapterClass);
//						Dictionary<String, Object> props = new Hashtable<String, Object>();
//						props.put("adapterName", adapterName);
//						props.put("adapterClass", c.getName());
//						context.registerService(Class.class.getName(), c, props);
//					}
//				} catch (Exception e) {
//					logger.error("Error loading class for adapterClass: "
//							+ adapterClass, e);
//				}
//
//			}
		} catch (Throwable e) {
			logger.error("Trouble starting NavajoAdapters bundle", e);
		}

	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
	}

}