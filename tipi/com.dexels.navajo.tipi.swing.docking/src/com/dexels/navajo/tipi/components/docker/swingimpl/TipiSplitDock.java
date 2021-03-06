package com.dexels.navajo.tipi.components.docker.swingimpl;

import com.dexels.navajo.tipi.components.core.TipiDataComponentImpl;
import com.dexels.navajo.tipi.components.docker.swingimpl.swing.TipiSwingSplitDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.Position;

public class TipiSplitDock extends TipiDataComponentImpl {
	private static final long serialVersionUID = -5637640238588319570L;
	TipiSwingSplitDock mySplitDock;

	@Override
	public Object createContainer() {
		runSyncInEventThread(new Runnable() {
			@Override
			public void run() {
				mySplitDock = new TipiSwingSplitDock(TipiSplitDock.this);
			}
		});
		return mySplitDock;
	}

	@Override
	public void addToContainer(final Object c, Object constraints) {
		int pos = Position.CENTER;
		
		if("top".equalsIgnoreCase(constraints.toString())){
			pos = Position.TOP;
		} else if("left".equalsIgnoreCase(constraints.toString())){
			pos = Position.LEFT;
		} else if("bottom".equalsIgnoreCase(constraints.toString())){
			pos = Position.BOTTOM;
		} else if("right".equalsIgnoreCase(constraints.toString())){
			pos = Position.RIGHT;
		} else if("center".equalsIgnoreCase(constraints.toString())){
			pos = Position.CENTER;
		}  
		final int position = pos;
		
		runSyncInEventThread(new Runnable() {
			@Override
			public void run() {
				mySplitDock.addChildDock((Dock)c, new Position(position));
			}
		});
	}
}
