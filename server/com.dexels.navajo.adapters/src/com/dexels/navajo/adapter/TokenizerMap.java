package com.dexels.navajo.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.Mappable;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.UserException;

public class TokenizerMap implements Mappable{

	public String delimiter;
	public String value;
  public Token[] tokens;
  
	public Token[] getTokens(){
		StringTokenizer tok = new StringTokenizer(value, delimiter);
		List<Token> toks = new ArrayList<Token>();
		while(tok.hasMoreTokens()){
			Token t = new Token();
			t.value = tok.nextToken();
			toks.add(t);
		}
		tokens = new Token[toks.size()];
		tokens = toks.toArray(tokens);
		return tokens;		
	}
	
	@Override
	public void kill() {
		
	}

	@Override
	public void load(Access access) throws MappableException, UserException {
		
	}

	@Override
	public void store() throws MappableException, UserException {
		
	}
	
	public void setDelimiter(String d){
		delimiter = d;
	}
	
	public void setValue(String v){
		value = v;
	}

}
