package com.dexels.navajo.functions;

import com.dexels.navajo.parser.FunctionInterface;
import com.dexels.navajo.parser.TMLExpressionException;

public class IsNumeric extends FunctionInterface {
    public IsNumeric() {}
    
    @Override
	public String usage() {
        return "IsNumeric(Object)";
    }

    @Override
	public String remarks() {
        return "Checks if the given object contains only numeric values. NULL is considered to be ok.";
    }

    @Override
	@SuppressWarnings("unused")
    public Object evaluate() throws TMLExpressionException {
        Object o = this.getOperands().get(0);
        if (o == null) {
            return Boolean.TRUE;
        } else {
            try {
                if (o instanceof String) {
                    double nr = Double.parseDouble((String)o);
                    return Boolean.TRUE;
                } else if (o instanceof Long || o instanceof Integer || o instanceof Double || o instanceof Float) {
                    return Boolean.TRUE;
                } else {
                    // Implementation please
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        }
    }

    public static void main(String [] args) throws TMLExpressionException {
        IsNumeric in = new IsNumeric();
        in.reset();
        in.insertOperand("1234112534534534 A");
        System.err.println("Value is numeric = " + in.evaluate());
    }
}
