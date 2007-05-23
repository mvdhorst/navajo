package person;

import com.dexels.navajo.server.*;
import com.dexels.navajo.mapping.*;
import com.dexels.navajo.document.*;
import com.dexels.navajo.parser.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


/**
 * Generated Java code by TSL compiler.
 * $Id$
 *
 * Java version: Java HotSpot(TM) Client VM (1.5.0_10-b03)
 * OS: Linux 2.6.20-1.2948.fc6
 * Hostname: localhost.localdomain
 *
 * WARNING NOTICE: DO NOT EDIT THIS FILE UNLESS YOU ARE COMPLETELY AWARE OF WHAT YOU ARE DOING
 *
 */

public final class InitSearchPersons extends CompiledScript {


public final void setValidations() {
}

public final void finalBlock(Parameters parms, Navajo inMessage, Access access, NavajoConfig config) throws Exception {
}
public final void execute(Parameters parms, Navajo inMessage, Access access, NavajoConfig config) throws Exception { 

inDoc = inMessage;
if (true) {
  com.dexels.navajo.document.Method m = NavajoFactory.getInstance().createMethod(access.getOutputDoc(), "person/ProcessSearchPersons", "");
  m.setDescription("");
  access.getOutputDoc().addMethod(m);
}
if (!kill) { execute_sub0(parms, inMessage, access, config); }
}// EOM
private final void execute_sub0(Parameters parms, Navajo inMessage, Access access, NavajoConfig config) throws Exception {

  if (!kill) {
  count = 1;
  Message [] messageList0 = null;
  messageList0 = MappingUtils.addMessage(access.getOutputDoc(), currentOutMsg, "SearchCriteria", "", count, "", "", "");
  for (int messageCount2 = 0; messageCount2 < messageList0.length; messageCount2++) {
 if (!kill) {
    outMsgStack.push(currentOutMsg);
    currentOutMsg = messageList0[messageCount2];
    access.setCurrentOutMessage(currentOutMsg);
    matchingConditions = false;
    sValue = new String("");
    type = "string";
    subtype = "";
    p = MappingUtils.setProperty(false, currentOutMsg, "LastName", sValue, type, subtype, "in", "Achternaam", -1, access.getOutputDoc(), inMessage, !matchingConditions);
p.setCardinality("1");
    matchingConditions = false;
    sValue = new String("");
    type = "selection";
    subtype = "";
    p = MappingUtils.setProperty(false, currentOutMsg, "Sex", sValue, type, subtype, "in", "Geslacht", -1, access.getOutputDoc(), inMessage, !matchingConditions);
p.addSelection(NavajoFactory.getInstance().createSelection(access.getOutputDoc(), "-", "-1", false));
p.addSelection(NavajoFactory.getInstance().createSelection(access.getOutputDoc(), "Onbekend", "0", false));
p.addSelection(NavajoFactory.getInstance().createSelection(access.getOutputDoc(), "Man", "1", false));
p.addSelection(NavajoFactory.getInstance().createSelection(access.getOutputDoc(), "Vrouw", "2", false));
p.setCardinality("1");
    matchingConditions = false;
    sValue = new String("");
    type = "string";
    subtype = "";
    p = MappingUtils.setProperty(false, currentOutMsg, "ZipCode", sValue, type, subtype, "in", "Postcode", -1, access.getOutputDoc(), inMessage, !matchingConditions);
p.setCardinality("1");
  currentOutMsg = (Message) outMsgStack.pop();
  access.setCurrentOutMessage(currentOutMsg);
  }
 } // EOF messageList for 
  }
}


}//EOF