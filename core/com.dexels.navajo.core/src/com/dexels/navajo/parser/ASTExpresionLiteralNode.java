/* Generated By:JJTree&JavaCC: Do not edit this line. ASTExpresionLiteralNode.java */

package com.dexels.navajo.parser;


public final class ASTExpresionLiteralNode extends SimpleNode {

    String val;

    public ASTExpresionLiteralNode(int id) {
        super(id);
    }

    public final Object interpret() {

        // Strip quotes.
        return val.substring(1, val.length() - 1);
    }
}
