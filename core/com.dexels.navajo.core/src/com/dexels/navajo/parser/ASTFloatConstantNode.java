/* Generated By:JJTree&JavaCC: Do not edit this line. ASTFloatConstantNode.java */
package com.dexels.navajo.parser;


public final class ASTFloatConstantNode extends SimpleNode {

    double val;

    public ASTFloatConstantNode(int id) {
        super(id);
    }

    @Override
	public final Object interpret() {

        return new Double(val);
    }

}
