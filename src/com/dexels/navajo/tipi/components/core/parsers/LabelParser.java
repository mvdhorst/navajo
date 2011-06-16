package com.dexels.navajo.tipi.components.core.parsers;

import com.dexels.navajo.tipi.TipiComponent;
import com.dexels.navajo.tipi.TipiTypeParser;
import com.dexels.navajo.tipi.internal.DescriptionProvider;
import com.dexels.navajo.tipi.internal.TipiEvent;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class LabelParser extends TipiTypeParser {
	public LabelParser() {
	}

	public Object parse(TipiComponent source, String expression, TipiEvent event) {
		DescriptionProvider dp = myContext.getDescriptionProvider();

		if (dp == null) {

			return "[" + expression + "]";
		} else {
			return myContext.XMLUnescape(dp.getDescription(expression));
		}
	}

	/**
	 * Replace all occurrences of the escaped characters &amp;, &quot;, &apos;,
	 * &lt; and &gt; by the unescaped characters &, ', ", < and >.
	 */

}
