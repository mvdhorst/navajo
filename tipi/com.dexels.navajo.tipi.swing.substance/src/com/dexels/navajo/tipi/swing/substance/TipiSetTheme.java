package com.dexels.navajo.tipi.swing.substance;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Operand;
import com.dexels.navajo.tipi.TipiBreakException;
import com.dexels.navajo.tipi.TipiException;
import com.dexels.navajo.tipi.internal.TipiAction;
import com.dexels.navajo.tipi.internal.TipiEvent;
@Deprecated

public class TipiSetTheme extends TipiAction {

	private static final long serialVersionUID = 7162024086267040701L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiSetTheme.class);
	
	@Override
	protected void execute(TipiEvent event) throws TipiBreakException, TipiException {
		final Operand valueOp = getEvaluatedParameter("value", event);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.getLookAndFeelDefaults().put("ClassLoader", SubstanceBusinessBlackSteelLookAndFeel.class.getClassLoader());

					String value = null;
					if (valueOp != null) {
						value = (String) valueOp.value;
					}
					if (value == null) {
						setDefaultLnF();
					} else {
			//			setSubstanceTheme(value);
					}
				} catch (Throwable e) {
					logger.error("Error: ",e);
				}
			}
		});

	}

	private void setDefaultLnF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.error("Error: ",e);
		}
	}



}
