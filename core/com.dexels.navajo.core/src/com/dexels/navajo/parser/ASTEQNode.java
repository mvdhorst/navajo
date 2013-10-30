package com.dexels.navajo.parser;



public final class ASTEQNode extends SimpleNode {
    public ASTEQNode(int id) {
        super(id);
    }

    @Override
	public final Object interpret() throws TMLExpressionException {
        // System.out.println("in ASTEQNode()");
        Object a = jjtGetChild(0).interpret();
        // System.out.println("Got first argument");
        Object b = jjtGetChild(1).interpret();

        // System.out.println("Got second argument");
        
        return Boolean.valueOf(Utils.equals(a, b));
    }
}
