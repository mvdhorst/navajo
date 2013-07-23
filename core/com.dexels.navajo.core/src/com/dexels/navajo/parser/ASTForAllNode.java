/* Generated By:JJTree&JavaCC: Do not edit this line. ASTForAllNode.java */

package com.dexels.navajo.parser;


import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.script.api.SystemException;

public final class ASTForAllNode extends SimpleNode {

    String functionName;
    Navajo doc;
    Message parentMsg;
    MappableTreeNode mapObject;
    
	private final static Logger logger = LoggerFactory
			.getLogger(ASTForAllNode.class);
    
    public ASTForAllNode(int id) {
        super(id);
    }

   
    /**
     * FORALL(<EXPRESSION>, `[$x] <EXPRESSION>`)
     * E.G. FORALL([/ClubMembership/ClubMemberships/ClubIdentifier], `CheckRelatieCode([$x])`)
     * @return
     * @throws TMLExpressionException
     */
    public final Object interpret() throws TMLExpressionException {

        boolean matchAll = true;

        if (functionName.equals("FORALL"))
            matchAll = true;
        else
            matchAll = false;


        Object a = jjtGetChild(0).interpret();

        String msgList = (String) a;

        Object b = jjtGetChild(1).interpret();

        try {
                ArrayList<Message> list = null;

                if (parentMsg == null) {
                  list = doc.getMessages(msgList);
                }
                else {
                  list = parentMsg.getMessages(msgList);
                }

                for (int i = 0; i < list.size(); i++) {
                    Object o = list.get(i);

                    parentMsg = (Message) o;

                    // ignore definition messages in the evaluation
                    if (parentMsg.getType().equals(Message.MSG_TYPE_DEFINITION))
                      continue;

                    String expr = (String) b;

                    boolean result = Condition.evaluate(expr, doc, mapObject, parentMsg);

                    if ((result == false) && matchAll)
                        return Boolean.FALSE;
                    if ((result == true) && !matchAll)
                        return Boolean.TRUE;
                }

        } catch (SystemException se) {
        	logger.error("Error: ", se);
        	throw new TMLExpressionException("Invalid expression in FORALL construct: \n" + se.getMessage());
        } catch (NavajoException ne) {
        	logger.error("Error: ", ne);
        	throw new TMLExpressionException("Invalid expression in FORALL construct: \n" + ne.getMessage());
        }

        if (matchAll)
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }
}
