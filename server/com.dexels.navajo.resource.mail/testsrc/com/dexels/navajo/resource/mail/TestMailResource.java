package com.dexels.navajo.resource.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dexels.navajo.adapter.mailmap.AttachementMap;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.resource.mail.impl.ResourceComponent;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.UserException;

public class TestMailResource {

	@Before
	public void setUp() throws Exception {
		MailResourceFactory factory = new MailResourceFactory();
		ResourceComponent component = new ResourceComponent();
		Map<String,Object> settings = new HashMap<String, Object>();
		settings.put("name", "junit.mail");
		settings.put("host", "smtp.google.com");
		settings.put("username", "dexels@gmail.com");
		settings.put("password", "seriously");
		settings.put("encrypt", true);
		component.activate(settings);
		factory.addMailResource(component, settings);
		factory.activate();

		
	}

	@After
	public void tearDown() throws Exception {
		MailResourceFactory.getInstance().deactivate();	
	}

	@Test
	public void testSimple() throws MappableException, UserException {
		ResourceMailAdapter rma = new ResourceMailAdapter();
		rma.load(new Access());
		rma.setRecipients("dexels@gmail.com");
		rma.setSender("dexels@gmail.com");
		rma.setSubject("Mail at "+new Date());	
		rma.store();
	}

	@Test
	public void testAttach() throws MappableException, UserException {
		ResourceMailAdapter rma = new ResourceMailAdapter();
		rma.load(new Access());
		rma.setRecipients("dexels@gmail.com");
		rma.setSender("dexels@gmail.com");
		rma.setSubject("Attachment at "+new Date());	
		AttachementMap am = new AttachementMap();
		am.setAttachContentType("image/jpeg");
		am.setAttachContentDisposition("inline");
		am.setAttachFileContent(new Binary(getClass().getResourceAsStream("monkey.jpeg")));
		rma.setAttachment(am);
		rma.store();
	}
}