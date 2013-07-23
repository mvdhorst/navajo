/* Generated By:JJTree&JavaCC: Do not edit this line. ASTMappableNode.java */

package com.dexels.navajo.parser;


import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.mapping.MappingUtils;
import com.dexels.navajo.mapping.bean.DomainObjectMapper;
import com.dexels.navajo.script.api.MappableTreeNode;

@SuppressWarnings("unchecked")
public final class ASTMappableNode extends SimpleNode {

	
	private final static Logger logger = LoggerFactory
			.getLogger(ASTMappableNode.class);
    String val = "";
    MappableTreeNode mapObject;
    int args = 0;

    public ASTMappableNode(int id) {
        super(id);
    }

    public final Object interpret() throws TMLExpressionException {

        if (mapObject == null) {
            throw new TMLExpressionException("No known mapobject");
        }

        ArrayList objects = null;

        // Parameter array may contain parameters that are used when calling the get method.
        Object[] parameterArray = null;

        if (args > 0) {
            objects = new ArrayList();
        }
        for (int i = 0; i < args; i++) {
            Object a = jjtGetChild(i).interpret();
            if(objects!=null) {
                objects.add(a);
            }
        }

        if (objects != null) {
            parameterArray = new Object[objects.size()];
            parameterArray = objects.toArray(parameterArray);
        }

        try {
        	Object oValue = null;
        	try {
        		oValue = MappingUtils.getAttributeValue(mapObject, val, parameterArray);
        	} catch (Exception e2) {
        		logger.error("Error: ", e2);
        		// Maybe domainobjectmapper?
        		if ( mapObject.myObject instanceof DomainObjectMapper ) {
        			oValue = ((DomainObjectMapper) mapObject.myObject).getDomainObjectAttribute(val, parameterArray);
        		} else {
        			throw new TMLExpressionException("Can not resolve attribute value",e2);
        		}
        	}
            if (oValue == null)
                return null;
            else if (oValue instanceof Float) {
              return new Double(((Float) oValue).doubleValue());
            } else if (oValue instanceof Long) {
              return new Integer(((Long) oValue).intValue());
            } else
              return oValue;

        } catch (Exception me) {
            throw new TMLExpressionException(me.getMessage(),me);
        }
    }
}

