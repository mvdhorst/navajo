/*
 * Created on Feb 16, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.dexels.navajo.tipi.components.swingimpl;

import java.util.*;

import com.dexels.navajo.document.*;
import com.dexels.navajo.tipi.*;
import com.dexels.navajo.tipi.actions.*;
import com.dexels.navajo.tipi.internal.*;

public abstract class TipiBaseQuestionList extends TipiPanel {
    
    protected String messagePath = null;
    protected String questionDefinitionName = null;
    protected String questionGroupDefinitionName = null;
        
    protected abstract Object getGroupConstraints(Message groupMessage);
    
    public void setComponentValue(String name, Object object) {
        if (name.equals("messagePath")) {
          messagePath = (String) object;
        }
//        if (name.equals("groupMode")) {
//            groupMode = (String) object;
//          }
//
        if (name.equals("questionDefinitionName")) {
          questionDefinitionName = (String) object;
        }
        if (name.equals("questionGroupDefinitionName")) {
          questionGroupDefinitionName = (String) object;
        }
        super.setComponentValue(name, object);
      }

      public Object getComponentValue(String name) {
        if (name.equals("valid")) {
          return new Boolean(isValid());
        }
        return super.getComponentValue(name);
      }


    public boolean isValid() {
      for (int i = 0; i < getChildCount(); i++) {
        TipiDataComponent tc = (TipiDataComponent) getTipiComponent(i);
        Boolean b = (Boolean) tc.getValue("valid");
        System.err.println("checked group: " + tc.getId() + " returned: " + b.toString());
        if (!b.booleanValue()) {
          return false;
        }
      }
      System.err.println("No invalid groups found. List is valid.");
      return true;
    }

    public void setValid(boolean b) {
//      Map m = new HashMap();
//      m.put("valid",new Boolean(b));
//      try {
//        performTipiEvent("onValidationChanged", m, false);
//      }
//      catch (TipiException ex) {
//        ex.printStackTrace();
//      }
    }

    protected void performComponentMethod(final String name, final TipiComponentMethod compMeth, TipiEvent event) throws TipiBreakException {
      if ("flatten".equals(name)) {
        String serviceName = (String) compMeth.getEvaluatedParameter("serviceName", event).value;
        Operand serviceOperand = compMeth.getEvaluatedParameter("serviceUrl", event);
        Operand usernameOperand = compMeth.getEvaluatedParameter("username", event);
        Operand passwordOperand = compMeth.getEvaluatedParameter("password", event);
        Operand pincodeOperand = compMeth.getEvaluatedParameter("pincode", event);
        Operand keystoreOperand = compMeth.getEvaluatedParameter("keystore", event);
        Operand keypassOperand = compMeth.getEvaluatedParameter("keypass", event);
        String serviceUrl = null;
        String username = null;
        String password = null;
        String pincode = null;
        String keystore = null;
        String keypass = null;
        if (serviceOperand != null) {
          serviceUrl = (String) serviceOperand.value;
        }
        if (usernameOperand != null) {
          username = (String) usernameOperand.value;
        }
        if (passwordOperand != null) {
          password = (String) passwordOperand.value;
        }
        if (pincodeOperand != null) {
          pincode = (String) pincodeOperand.value;
        }
        if (keystoreOperand != null) {
          keystore = (String) keystoreOperand.value;
        }
        if (keypassOperand != null) {
          keypass = (String) keypassOperand.value;
        }
        try {
          flatten(serviceName, serviceUrl, username, password, pincode, keystore, keypass);
        }
        catch (NavajoException ex) {
          ex.printStackTrace();
        }
      }
      super.performComponentMethod(name,compMeth,event);
    }

    public void flatten(String serviceName, String server,String username, String password,String pincode,String keystore,String keypass) throws NavajoException,TipiBreakException {
      Navajo input = getNearestNavajo();
      System.err.println("^^^^^^^^^^^^^^^^^^^^^^^^^^>>> "+input);
      input.write(System.err);
      Message questionList = input.getMessage("QuestionList@0");
      Navajo n = NavajoFactory.getInstance().createNavajo();
       Message aap = input.getMessage("ObjectForm");
       Message m = null;
       if (aap!=null) {
           m = aap.copy(n);
           n.addMessage(m);
       }


      Message sendForm = input.getMessage("SendForm");
      if (sendForm!=null) {
          Message m2 = sendForm.copy(n);
          n.addMessage(m2);
    }

      Message clubMsg = input.getMessage("Club");
      if (clubMsg!=null) {
        n.addMessage(clubMsg.copy(n));
      }
      if (m!=null) {
          Property pin = NavajoFactory.getInstance().createProperty(n,"Pincode",Property.STRING_PROPERTY,pincode,16,"",Property.DIR_IN);
          m.addProperty(pin);

          Property date = NavajoFactory.getInstance().createProperty(n, "TimeStamp", Property.DATE_PROPERTY, "", 0, "",
              Property.DIR_IN);
          date.setValue(new Date());
          m.addProperty(date);
       }
      
      
      Message answers = NavajoFactory.getInstance().createMessage(n, "Answers", Message.MSG_TYPE_ARRAY);
      n.addMessage(answers);
      ArrayList questionGroups = questionList.getMessage("Group").getAllMessages();
      for (int i = 0; i < questionGroups.size(); i++) {
        Message group = (Message) questionGroups.get(i);
        flattenGroup(group, answers);
      }
       try {
        myContext.performTipiMethod(this, n, "*", serviceName, true, null, -1, server, username, password,keystore,keypass);
      }
      catch (TipiException ex1) {
        ex1.printStackTrace();
      }
//      myContext.doSimpleSend(n, serviceName, this,-1,server,username,password);
    }

    private final void flattenGroup(Message groupMessage, Message answerMessage) throws NavajoException {
      Property id = groupMessage.getProperty("Id");
      Message questions = groupMessage.getMessage("Question");
      if (questions == null) {
        return;
      }
      for (int i = 0; i < questions.getArraySize(); i++) {
        Message current = questions.getMessage(i);
        flattenQuestion(current, answerMessage, "/" + id.getValue());
      }
    }

    private final void flattenQuestion(Message questionMessage, Message answerMessage, String prefix) throws NavajoException {
      Property value = questionMessage.getProperty("Value");
      Property id = questionMessage.getProperty("Id");
      Message answer = createAnswerMessage(answerMessage.getRootDoc(), prefix + "/" + id.getValue(), value);
      answerMessage.addMessage(answer);
      Message subQuestions = questionMessage.getMessage("Question");
      if (subQuestions == null) {
        return;
      }
      for (int i = 0; i < subQuestions.getArraySize(); i++) {
        Message current = subQuestions.getMessage(i);
        flattenQuestion(current, answerMessage, prefix + "/" + id.getValue());
      }
    }

    private Message createAnswerMessage(Navajo answerDoc, String id, Property value) throws NavajoException {
      Property newValue = NavajoFactory.getInstance().createProperty(answerDoc, "Value", value.getType(), value.getValue(),
          value.getLength(), value.getDescription(), value.getDirection());
      newValue.setCardinality(value.getCardinality());
      Message answerMessage = NavajoFactory.getInstance().createMessage(answerDoc, "Answer");
      Property idProp = NavajoFactory.getInstance().createProperty(answerDoc, "Id", Property.STRING_PROPERTY, id, 0, "",
          Property.DIR_IN);
      ArrayList al = value.getAllSelections();
      for (int i = 0; i < al.size(); i++) {
        Selection current = (Selection) al.get(i);
        Selection s = NavajoFactory.getInstance().createSelection(answerDoc, current.getName(), current.getValue(),
            current.isSelected());
        newValue.addSelection(s);
      }
      answerMessage.addProperty(idProp);
      answerMessage.addProperty(newValue);
      return answerMessage;
    }
    public void loadData(Navajo n, TipiContext context,String method) throws TipiException {
        removeInstantiatedChildren();
        myNavajo = n;
        Message m = n.getMessage(messagePath);
        if (m == null) {
          return;
        }
        if (!m.getType().equals(Message.MSG_TYPE_ARRAY)) {
          return;
        }
        for (int i = 0; i < m.getArraySize(); i++) {
          Message current = m.getMessage(i); 
          String id = current.getProperty("Id").getValue();
          System.err.println("Instantiating with id: "+id+" definition name: "+questionGroupDefinitionName);
          TipiDataComponent tc = (TipiDataComponent) TipiInstantiateTipi.instantiateByDefinition(this, false, id,questionGroupDefinitionName,getGroupConstraints(current));
          System.err.println("Created component: "+tc.getClass()+" container: "+tc.getContainer());
          tc.setValue("messagePath", current.getFullMessageName() );
          tc.setPrefix(current.getFullMessageName());
          tc.setValue("questionDefinitionName",  questionDefinitionName);
          tc.setValue("questionGroupDefinitionName", questionGroupDefinitionName );
          tc.loadData(n, myContext,method);
          if (tc instanceof TipiBaseQuestionGroup) {
              TipiBaseQuestionGroup tqg = (TipiBaseQuestionGroup) tc;
            tqg.setQuestionList(this);
          }
        }
      }

}
