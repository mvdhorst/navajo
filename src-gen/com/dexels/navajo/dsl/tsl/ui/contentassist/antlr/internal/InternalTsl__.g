lexer grammar InternalTsl;
@header {
package com.dexels.navajo.dsl.tsl.ui.contentassist.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;
}

T11 : '/>' ;
T12 : '.' ;
T13 : '..' ;
T14 : '<navascript' ;
T15 : '>' ;
T16 : '</navascript>' ;
T17 : '=' ;
T18 : '"=' ;
T19 : ';"' ;
T20 : '<message' ;
T21 : '</message>' ;
T22 : '<map.' ;
T23 : '</map.' ;
T24 : '<property' ;
T25 : '</property>' ;
T26 : '<expression>' ;
T27 : '</expression>' ;
T28 : '[' ;
T29 : '/' ;
T30 : ']' ;
T31 : '?' ;
T32 : '+' ;
T33 : '-' ;
T34 : '(' ;
T35 : ')' ;
T36 : ',' ;
T37 : '}' ;
T38 : 'OR' ;
T39 : 'AND' ;
T40 : '==' ;
T41 : '!=' ;
T42 : '*' ;
T43 : '!' ;
T44 : 'FORALL' ;
T45 : '{' ;
T46 : 'NULL' ;
T47 : 'TODAY' ;
T48 : 'TRUE' ;
T49 : 'FALSE' ;

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5951
RULE_ATTRIBUTESTRING : ('"' ~('=') ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'"')))* ~(';') '"'|'""'|'"' ~('"') '"');

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5953
RULE_INT : ('0'..'9')+;

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5955
RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5957
RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5959
RULE_WS : (' '|'\t'|'\r'|'\n')+;

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5961
RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

// $ANTLR src "../com.dexels.navajo.dsl.tsl.ui/src-gen/com/dexels/navajo/dsl/tsl/ui/contentassist/antlr/internal/InternalTsl.g" 5963
RULE_LITERALSTRING : '\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'';


