package com.dexels.navajo.camel.dataformat;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;

public class NavajoDataFormat implements DataFormat {

	public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
		String navajoString = graph.toString();
		stream.write(navajoString.getBytes(Charset.forName("UTF-8")));
	}

	public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {

		Navajo n =  NavajoFactory.getInstance().createNavajo(stream);
		// Copy any existing headers
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		
		// Get navajo header and add its attributes as headers
		com.dexels.navajo.document.Header header = n.getHeader();
		if (header != null) {
			for (String key : header.getHeaderAttributes().keySet()) {
				exchange.getOut().setHeader(key, header.getHeaderAttributes().get(key));
			}
		}
		return n;
	}

	

}
