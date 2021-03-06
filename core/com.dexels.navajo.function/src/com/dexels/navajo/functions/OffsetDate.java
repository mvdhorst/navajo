package com.dexels.navajo.functions;

import java.util.Calendar;

import com.dexels.navajo.parser.FunctionInterface;

/**
 * Title: Navajo Description: Copyright: Copyright (c) 2001 Company: Dexels
 * 
 * @author Arjen Schoneveld en Martin Bergman
 * @version $Id$
 */

public final class OffsetDate extends FunctionInterface {

	public OffsetDate() {
	}

	@Override
	public String remarks() {
		return "Returns a date with a certain offset.";
	}

	@Override
	public String usage() {
		return "OffsetDate(<Date>,<int +/- year>,<int +/- month>,<int +/- day>,<int +/- hour(24h)>,<int +/- min>,<int +/- sec>)";
	}

	@Override
	public final Object evaluate() throws com.dexels.navajo.parser.TMLExpressionException {
		java.util.Date date = ( java.util.Date ) this.getOperands().get( 0 );
		int year = ( ( Integer ) getOperands().get( 1 ) ).intValue();
		int month = ( ( Integer ) getOperands().get( 2 ) ).intValue();
		int day = ( ( Integer ) getOperands().get( 3 ) ).intValue();

		int hour = ( ( Integer ) getOperands().get( 4 ) ).intValue();
		int min = ( ( Integer ) getOperands().get( 5 ) ).intValue();
		int sec = ( ( Integer ) getOperands().get( 6 ) ).intValue();

		Calendar c = Calendar.getInstance();
		c.setTime( date );

		if ( year != 0 ) {
			c.add( Calendar.YEAR, year );
		}
		if ( month != 0 ) {
			c.add( Calendar.MONTH, month );
		}
		if ( day != 0 ) {
			c.add( Calendar.DAY_OF_MONTH, day );
		}
		if ( hour != 0 ) {
			c.add( Calendar.HOUR_OF_DAY, hour );
		}
		if ( min != 0 ) {
			c.add( Calendar.MINUTE, min );
		}
		if ( sec != 0 ) {
			c.add( Calendar.SECOND, sec );
		}

		return c.getTime();
	}

	public static void main( String args[] ) throws Exception {
		OffsetDate st = new OffsetDate();

		st.reset();

		java.util.Date input = new java.util.Date();
		java.util.Date output = null;

		// offset date by two days, 3 hours, 15 minutes and 8 seconds
		st.insertOperand( input );
		st.insertOperand( 0 );
		st.insertOperand( 0 );
		st.insertOperand( 2 );
		st.insertOperand( 3 );
		st.insertOperand( 15 );
		st.insertOperand( 8 );

		output = ( java.util.Date ) st.evaluate();

		System.out.println( "Old date : " + input + " - New date : " + output );
	}

}
