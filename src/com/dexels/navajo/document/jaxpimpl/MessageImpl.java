

/**
 * Title:        Navajo<p>
 * Description:  <p>
 * Copyright:    Copyright (c) Arjen Schoneveld<p>
 * Company:      Dexels<p>
 * @author Arjen Schoneveld
 * @version $Id$
 *
 * Support for lazy array messages:
 *
 * <header>
 *   <transaction rpc_usr="" rpc_pwd="" rpc_name="">
 *      <lazymessage name="MemberData" startindex="10" endindex="100"/>
 *   </transaction>
 * </header>
 */
package com.dexels.navajo.document.jaxpimpl;

import com.dexels.navajo.document.*;
import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.*;
import gnu.regexp.*;
import com.dexels.navajo.document.jaxpimpl.xml.XMLDocumentUtils;
import javax.xml.transform.stream.StreamResult;


/**
 * The message object is used to store properties (see @Property.class).
 */
public class MessageImpl implements Message {

    private int totalElements;
    private Navajo myRootDoc = null;

    public Element ref;

    public String toString() {
        return ref.getAttribute(Message.MSG_NAME);
    }

    public String getName() {
        return ref.getAttribute(Message.MSG_NAME);
    }

    /**
     * Return the parent message if there is one.
     * @return
     */

    public Message getParentMessage() {
        Node n = ref.getParentNode();
        if (n instanceof Element) {
          Element e = (Element) n;
          if (e.getTagName().equals("message"))
            return new MessageImpl(e);
          else
            return null;
        } else
          return null;
    }

    /**
     * Return the fully qualified Navajo message name.
     */
    public String getFullMessageName() {

        String result = this.getName();
        Node n = ref.getParentNode();

        while (n != null) {
            if (n.getNodeName().equals(Message.MSG_DEFINITION)) {
                Element parent = (Element) n;
                String name = parent.getAttribute(Message.MSG_NAME);

                result = name + Navajo.MESSAGE_SEPARATOR + result;
            }
            n = n.getParentNode();
        }
        return result;
    }

    /**
     * Set the type of a message.
     * Default "simple".
     *
     * @param s
     */
    public void setType(String s) {
      ref.setAttribute(Message.MSG_TYPE, s);
    }

    public String getType() {
      return ref.getAttribute(Message.MSG_TYPE);
    }

    public boolean isArrayMessage() {
      return (this.getType().equals(Message.MSG_TYPE_ARRAY));
    }

    public int getArraySize() throws NavajoException {
        if (!this.isArrayMessage())
          throw new NavajoExceptionImpl("getArraySize() is only supported by array type messages");
        return ref.getChildNodes().getLength();
    }

    public int getIndex() {
      if ((ref.getAttribute(Message.MSG_INDEX) != null) && !ref.getAttribute(Message.MSG_INDEX).equals(""))
        return Integer.parseInt(ref.getAttribute(Message.MSG_INDEX));
      else
        return -1;
    }

    /**
     * Set the index of the message.
     * @param name
     */
    public void setIndex(int i) {
      ref.setAttribute(Message.MSG_INDEX, i+"");
    }


    /**
     * Set the name of the message.
     */
    public void setName(String name) {
        ref.setAttribute(Message.MSG_NAME, name);
    }

    public String getMode() {
      return ref.getAttribute(Message.MSG_MODE);
    }

    /**
     * Set the mode of the message.
     * Current modes are: default, lazy.
     *
     * @param mode
     */
    public void setMode(String mode) {
      ref.setAttribute(Message.MSG_MODE, mode);
    }

    /**
     * Set the total number of lazy array element sub messages.
     * @param c
     */
    public void setLazyTotal(int c) {
      ref.setAttribute(Message.MSG_LAZY_COUNT, c+"");
    }

    /**
     * Set the total number of remaining lazy array element sub messages.
     *
     * @param c
     */
    public void setLazyRemaining(int c) {
      ref.setAttribute(Message.MSG_LAZY_REMAINING, c+"");
    }

    /**
     * Set the total number of array element sub messages.
     *
     * @param c
     */
    public void setArraySize(int c) {
      ref.setAttribute(Message.MSG_ARRAY_SIZE, c+"");
    }

    /**
     * Create a message.
     */

    public static Message create(Navajo tb, String name) {
      return MessageImpl.create(tb, name, "");
    }

    public static Message create(Navajo tb, String name, String type) {

        Message p = null;

        Document d = (Document) tb.getMessageBuffer();
        Element n = (Element) d.createElement(Message.MSG_DEFINITION);

        p = new MessageImpl(n);
        p.setName(name);
        p.setRootDoc(tb);
        if (!type.equals(""))
          p.setType(type);

        return p;
    }

    /**
     * Add a property to a message. If a property with the specified name already exists,
     * replace it with the new one.
     */
    public void addProperty(Property p) {
        // First check if property is already present. If it is overwrite with new version.
        if (p == null)
            return;

        Property dummy = this.getProperty(p.getName());

        if (dummy != null) {
            ref.removeChild((Node) dummy.getRef());
        }
        ref.appendChild((Node) p.getRef());
    }

    /**
     * Use this method to add an element to an array type message.
     *
     * @param m
     * @return
     */
    public Message addElement(Message m) {
      return addMessage(m, false);
    }

    public Message addMessage(Message m) {

        // Do not add messages with mode "ignore".
        if (m.getMode().endsWith(Message.MSG_MODE_IGNORE)) {
            System.out.println("IGNORING ADDMESSAGE(), MODE = IGNORE!!!!!!!");
            return null;
        }

        if (m == null)
          return null;

        if (this.getType().equals(Message.MSG_TYPE_ARRAY))
          return addMessage(m, false);
        else
          return addMessage(m, true);
    }

    /**
     * Add a message to a message. If a message with the specified name already exists
     * withing the parent message, replace it with the new one.
     */
    public Message addMessage(Message m, boolean overwrite) {

        if (m == null)
            return null;

        Message dummy = this.getMessage(m.getName());
        //if ((dummy != null) && overwrite)
        //    return dummy;

        if (dummy != null && overwrite) {
            ref.removeChild((Node) dummy.getRef());
        }
        ref.appendChild((Node) m.getRef());

        /**
         * If message is array type, insert new message as "element".
         */

        if (this.getType().equals(Message.MSG_TYPE_ARRAY)) {
          // Increase element counter.
          m.setIndex(totalElements++);
          // Element message MUST have same name as parent array message.
          m.setName(getName());
        }

        return m;
    }

    /**
     * Remove a property from a message. If a null value is given as input do nothing.
     */
    public void removeProperty(Property p) {
        if (p != null) {
            ref.removeChild((Node) p.getRef());
        }
    }

    /**
     * Remove a message from a message. If a null value is given as input do nothing.
     */
    public void removeMessage(Message m) {
        if (m != null)
            ref.removeChild((Node) m.getRef());
    }

    /**
     * Return all properties that match a given regular expression. Regular expression may include sub-messages and even
     * absolute message references starting at the root level.
     */
    public ArrayList getProperties(String regularExpression) throws NavajoException {

        if (regularExpression.startsWith(Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR)) {
          regularExpression = regularExpression.substring((Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR).length());
          return getParentMessage().getProperties(regularExpression);
        } else
        if (regularExpression.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We have an absolute offset

            Navajo d = new NavajoImpl(this.ref.getOwnerDocument());
            return d.getProperties(regularExpression);
        } else {
            ArrayList props = new ArrayList();
            Property prop = null;
            ArrayList messages = null;
            ArrayList sub = null;
            ArrayList sub2 = null;
            String property = null;
            Message message = null;

            StringTokenizer tok = new StringTokenizer(regularExpression, Navajo.MESSAGE_SEPARATOR);
            String messageList = "";

            int count = tok.countTokens();

            for (int i = 0; i < count - 1; i++) {
                property = tok.nextToken();
                messageList += property;
                if ((i + 1) < count - 1)
                    messageList += Navajo.MESSAGE_SEPARATOR;
            }
            String realProperty = tok.nextToken();


            if (!messageList.equals("")) {
              messages = this.getMessages(messageList);
            } else {
              messages = new ArrayList();
              messages.add(this);
            }

            for (int i = 0; i < messages.size(); i++) {
                message = (Message) messages.get(i);
                ArrayList allProps = message.getAllProperties();
                try {
                  RE re = new RE(realProperty);
                  for (int j = 0; j < allProps.size(); j++) {
                    String name = ((Property) allProps.get(j)).getName();
                    if (re.isMatch(name))
                        props.add(allProps.get(j));
                  }
                } catch (REException re) {
                  throw new NavajoExceptionImpl(re.getMessage());
                }
            }
            return props;
        }
    }

    /**
     * Return all messages that match a given regular expression. Regular expression may include sub-messages and even
     * absolute message references starting at the root level.
     */
    public ArrayList getMessages(String regularExpression) throws NavajoException {

        ArrayList messages = new ArrayList();
        ArrayList sub = null;
        ArrayList sub2 = null;

        if (regularExpression.startsWith(Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR)) {
          regularExpression = regularExpression.substring((Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR).length());
          return getParentMessage().getMessages(regularExpression);
        } else
        if (regularExpression.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We have an absolute offset

            Navajo d = new NavajoImpl(this.ref.getOwnerDocument());

            return d.getMessages(regularExpression);
        } else // Contains submessages.
          if (regularExpression.indexOf(Navajo.MESSAGE_SEPARATOR) != -1) // contains a path, descent it first
        {
            StringTokenizer tok = new StringTokenizer(regularExpression, Navajo.MESSAGE_SEPARATOR);
            Message m = null;

            while (tok.hasMoreElements()) {
                String msgName = tok.nextToken();

                if (sub == null) { // First message in path.
                    sub = getMessages(msgName);
                } else {// Subsequent submessages in path.
                    messages = new ArrayList();
                    for (int i = 0; i < sub.size(); i++) {
                        m = (Message) sub.get(i);
                        sub2 = m.getMessages(msgName);
                        messages.addAll(sub2);
                    }
                    sub = messages;
                }
            }
            return sub;
        }  else {
            ArrayList msgList = getAllMessages();
            ArrayList result = new ArrayList();
            try {
                RE re = new RE(regularExpression);
                for (int i = 0; i < msgList.size(); i++) {
                    Message m = (Message) msgList.get(i);
                    String name = m.getName();
                    if (m.getType().equals(Message.MSG_TYPE_ARRAY) && re.isMatch(name)) { // If message is array type add all children.
                      result.addAll(m.getAllMessages());
                    } else {
                      if (re.isMatch(name))
                          result.add(msgList.get(i));
                    }
                }
            } catch (REException re) {
                throw new NavajoExceptionImpl(re.getMessage());
            }
            return result;
        }
    }

    public Message getMessage(int index) {
      Message m = null;
      NodeList list = ref.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals(Message.MSG_DEFINITION)) {
                Element e = (Element) list.item(i);
                m = new MessageImpl(e);
                if (m.getIndex() == index)
                  return m;
            }
        }

        return null;
    }

    /**
     * Return a message with a specific name if it exists. If it does not exist return null.
     */
    public Message getMessage(String name) {

        if (name.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We have an absolute offset
            Navajo d = new NavajoImpl(this.ref.getOwnerDocument());
            return d.getMessage(name);
        } if (name.startsWith(Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR)) {
           name = name.substring((Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR).length());
           return getParentMessage().getMessage(name);
        } else {
            NodeList list = ref.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeName().equals(Message.MSG_DEFINITION)) {
                    Element e = (Element) list.item(i);
                    String type = e.getAttribute("type");
                    String msgName = e.getAttribute(Message.MSG_NAME);
                    StringTokenizer arEl = new StringTokenizer(name, "()");
                    String realName = arEl.nextToken();
                    if ((type != null) && (type.equals(Message.MSG_TYPE_ARRAY)) && msgName.equals(realName)) {
                      if (arEl.hasMoreTokens()) {
                        String index = arEl.nextToken();
                        Message mp = new MessageImpl(e);
                        ArrayList elements = mp.getAllMessages();
                        for (int j = 0; j < elements.size(); j++) {
                          Message m = (Message) elements.get(j);
                          if ((m.getIndex()+"").equals(index))
                            return m;
                        }
                      } else {
                        return new MessageImpl(e);
                      }
                    } else {
                      if (msgName.equals(realName))
                        return new MessageImpl(e);
                    }
                }
            }
        }
        return null;
    }

    public Property getPathProperty(String property) {

        if (property.startsWith(Navajo.MESSAGE_SEPARATOR)) { // We have an absolute offset

            Navajo d = new NavajoImpl(this.ref.getOwnerDocument());

            return d.getProperty(property);
        } else {
            Property prop = null;
            StringTokenizer tok = new StringTokenizer(property, Navajo.MESSAGE_SEPARATOR);
            Message message = null;

            int count = tok.countTokens();
            int index = 0;

            while (tok.hasMoreElements()) {
                property = tok.nextToken();
                // Check if last message/property reached.
                if (index == (count - 1)) { // Reached property field.
                    if (message != null) {
                        // Check if name contains ":", which denotes a selection.
                        if (property.indexOf(":") != -1) {
                            StringTokenizer tok2 = new StringTokenizer(property, ":");
                            String propName = tok2.nextToken();

                            prop = message.getProperty(propName);
                        } else {
                            prop = message.getProperty(property);
                        }
                    }
                } else { // Descent message tree.
                    if (index == 0) // First message.
                        message = this.getMessage(property);
                    else // Subsequent messages.
                        message = message.getMessage(property);
                    if (message == null)
                        return null;
                }
                index++;
            }

            return prop;
        }
    }

    /**
     * Return a property with a specific name if it exists. Property name may include references to sub-messages.
     * Example: getProperty("mymessage/sub1/subsub/propy").
     */
    public Property getProperty(String name) {

        if (name.indexOf(Navajo.MESSAGE_SEPARATOR) != -1)
            return getPathProperty(name);

        if (name.startsWith(Navajo.PARENT_MESSAGE+Navajo.MESSAGE_SEPARATOR)) {

        }

        NodeList list = ref.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals(PropertyImpl.PROPERTY_DEFINITION)) {
                Element e = (Element) list.item(i);

                if (e.getAttribute(PropertyImpl.PROPERTY_NAME).equals(name))
                    return new PropertyImpl(e);
            }
        }
        return null;
    }

    /**
     * Return all properties in this message. Properties in submessages are not included(!).
     */
    public ArrayList getAllProperties() {

        ArrayList h = new ArrayList();
        Property p = null;
        NodeList list = ref.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals(PropertyImpl.PROPERTY_DEFINITION)) {
                Element e = (Element) list.item(i);

                p = new PropertyImpl(e);
                h.add(p);
            }
        }

        return h;
    }

    /**
     * Return all messages in this message. Only first level sub-messages are returned(!).
     */
    public ArrayList getAllMessages() {

        ArrayList h = new ArrayList();
        Message m = null;
        NodeList list = ref.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals(Message.MSG_DEFINITION)) {
                Element e = (Element) list.item(i);

                m = new MessageImpl(e);
                h.add(m);
            }
        }

        return h;
    }

    /**
     * Check if this message contains a property with a specific name. Property name may include references
     * to sub-messages.
     */
    public boolean contains(String name) {
        if (getProperty(name) != null)
            return true;
        else
            return false;
    }

    public MessageImpl(Element e) {
        this.ref = e;
        String type = e.getAttribute("type");
        if ((type != null) && (type.equals("array")))
          this.totalElements = ref.getChildNodes().getLength();
    }

    public Object getRef() {
      return this.ref;
    }

    public Navajo getRootDoc() {
      if (myRootDoc == null) {
         Document d = ref.getOwnerDocument();
         myRootDoc = new NavajoImpl(d);
      }
      return myRootDoc;
    }

    public void setRootDoc(Navajo n) {
      myRootDoc = n;
    }

    public void write(java.io.Writer writer) {
      try {
        XMLDocumentUtils.toXML(this.ref, "", "", "", new StreamResult(writer));
      }
      catch (NavajoException ex) {
        ex.printStackTrace(System.err);
      }
    }

    public void write(java.io.OutputStream stream) {
      try {
        XMLDocumentUtils.toXML(this.ref, "", "", "", new StreamResult(stream));
      }
      catch (NavajoException ex) {
        ex.printStackTrace(System.err);
      }
    }

    public static void main (String [] args) throws Exception {
        Navajo n = NavajoFactory.getInstance().createNavajo();
        Message ar = NavajoFactory.getInstance().createMessage(n, "array");
        n.addMessage(ar);
        ar.setType(Message.MSG_TYPE_ARRAY);
        Message el1 = NavajoFactory.getInstance().createMessage(n, "el");
        Message el2 = NavajoFactory.getInstance().createMessage(n, "el");
        ar.addMessage(el1);
        ar.addMessage(el2);
        System.out.println("size = " + ar.getArraySize());
    }
}
