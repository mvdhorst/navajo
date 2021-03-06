package com.dexels.navajo.tipi.internal;

import java.util.List;

import com.dexels.navajo.tipi.TipiBreakException;
import com.dexels.navajo.tipi.TipiComponent;
import com.dexels.navajo.tipi.TipiException;
import com.dexels.navajo.tipi.TipiExecutable;
import com.dexels.navajo.tipi.TipiSuspendException;
import com.dexels.navajo.tipi.tipixml.XMLElement;

public class TipiAnonymousAction implements TipiExecutable {

	private final Runnable myRunnable;

	public TipiAnonymousAction(Runnable r) {
		myRunnable = r;
	}

	@Override
	public String getBlockParam(String key) {
		return null;
	}

	@Override
	public TipiComponent getComponent() {
		return null;
	}

	@Override
	public TipiEvent getEvent() {
		return null;
	}

	@Override
	public void performAction(TipiEvent te, TipiExecutable parent, int index)
			throws TipiBreakException, TipiException {
		myRunnable.run();

	}

	@Override
	public void setEvent(TipiEvent e) {

	}

	public XMLElement store() {
		return null;
	}

	@Override
	public TipiStackElement getStackElement() {
		return null;
	}

	@Override
	public void setStackElement(TipiStackElement myStackElement) {

	}

	@Override
	public void dumpStack(String message) {

	}

	@Override
	public void setComponent(TipiComponent c) {

	}

	@Override
	public void setExecutionIndex(int i) {
		
	}

	@Override
	public int getExecutionIndex() {
		return 0;
	}

	

	@Override
	public void setParent(TipiExecutable tipiAbstractExecutable) {
		
	}

	@Override
	public TipiExecutable getParent() {
		return null;
	}

	@Override
	public List<TipiExecutable> getExecutables() {
		return null;
	}

	@Override
	public int getExeIndex(TipiExecutable child) {
		return 0;
	}

	@Override
	public void continueAction(TipiEvent original)
			throws TipiBreakException, TipiException, TipiSuspendException {
		
	}

}
