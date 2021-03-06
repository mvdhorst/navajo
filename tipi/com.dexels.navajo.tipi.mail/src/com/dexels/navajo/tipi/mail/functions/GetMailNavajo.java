package com.dexels.navajo.tipi.mail.functions;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.functions.mail.impl.BinaryDataSource;
import com.dexels.navajo.parser.FunctionInterface;
import com.dexels.navajo.parser.TMLExpressionException;

public class GetMailNavajo extends FunctionInterface {

	
	private final static Logger logger = LoggerFactory
			.getLogger(GetMailNavajo.class);
	
	public GetMailNavajo() {	
	}
	
	@Override
	public String remarks() {
		return "Returns a navajo, depicting the message along with its parts. It does not do any link replacements";
	}

	@Override
	public Object evaluate() throws TMLExpressionException {
		Binary b = (Binary) getOperand(0);
		DataSource ds = new BinaryDataSource(b);
		Navajo result = NavajoFactory.getInstance().createNavajo();
		Message mail = NavajoFactory.getInstance().createMessage(result, "Mail");
		result.addMessage(mail);
		Message parts = NavajoFactory.getInstance().createMessage(result, "Parts",Message.MSG_TYPE_ARRAY);
		mail.addMessage(parts);
		try {
			MimeMultipart mmp = new MimeMultipart(ds);
			
			for (int i=0; i<mmp.getCount();i++) {
				BodyPart bp = mmp.getBodyPart(i);
				String type = bp.getContentType();
				Binary partBinary = new Binary( bp.getInputStream(),false);

				if (i==0) {
//					Property content = NavajoFactory.getInstance().createProperty(result, "Content", Property.BINARY_PROPERTY, null, 0,"",Property.DIR_OUT);
					Property mimeType = NavajoFactory.getInstance().createProperty(result, "ContentType", Property.STRING_PROPERTY, type, 0,"",Property.DIR_OUT);
//					mail.addProperty(content);
					mail.addProperty(mimeType);
//					content.setAnyValue(partBinary);
					partBinary.setMimeType(type);
				}
				Message element = parts.addElement(NavajoFactory.getInstance().createMessage(result, "Parts",Message.MSG_TYPE_ARRAY_ELEMENT));
				Property content = NavajoFactory.getInstance().createProperty(result, "Content", Property.BINARY_PROPERTY, null, 0,"",Property.DIR_OUT);
				element.addProperty(content);
				Property mimeType = NavajoFactory.getInstance().createProperty(result, "ContentType", Property.STRING_PROPERTY, type, 0,"",Property.DIR_OUT);
				element.addProperty(mimeType);
				content.setAnyValue(partBinary);					
				partBinary.setMimeType(type);
				content.addSubType("browserMime="+type);
			}
			
			return result;
		} catch (MessagingException e) {
			logger.error("Error: ",e);
		} catch (IOException e) {
			logger.error("Error: ",e);
		}
		return null;

	}
	
	
	public static void main(String [] args) throws Exception {
		InputStream is = GetPart.class.getResourceAsStream("testdata.xml");
		Navajo nn = NavajoFactory.getInstance().createNavajo(is);
		is.close();
		nn.write(System.err);
		Binary b = (Binary) nn.getProperty("MailBox/Mail@0/Content").getTypedValue();
		logger.info("Length: "+b.getLength()+" type: "+b.guessContentType());
		GetMailNavajo gp = new GetMailNavajo();
		gp.reset();
		gp.insertOperand(b);
		gp.insertOperand(0);
		Navajo result = (Navajo) gp.evaluate();
		result.write(System.err);
//		logger.info("Length of part 0: "+result.getLength());
		
	}
	

}
