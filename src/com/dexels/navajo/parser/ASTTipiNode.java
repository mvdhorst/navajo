/* Generated By:JJTree: Do not edit this line. ASTTipiNode.java */

package com.dexels.navajo.parser;

import com.dexels.navajo.tipi.*;

public class ASTTipiNode extends SimpleNode {

  String val = "";

  public ASTTipiNode(int id) {
    super(id);
  }

  public Object interpret() throws TMLExpressionException {
    System.out.println("in ASTTipiNode(), val = " + val);
    // TipiLink link = blblala
    // return link.evaluateExpression(val);
    return  "AAP";
  }

  public static void main(String [] args) throws Exception {
      java.io.StringReader reader  = new java.io.StringReader("property:/.:.:/Club/Aap+ 5");
      TMLParser parser = new TMLParser(reader);
      parser.Expression();
      Object result = parser.jjtree.rootNode().interpret();
      System.out.println("result = " + result);
  }
}
