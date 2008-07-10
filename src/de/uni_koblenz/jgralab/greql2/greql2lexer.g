//
// JGraLab - The Java graph laboratory
// (c) 2006-2007 Institute for Software Technology
//               University of Koblenz-Landau, Germany
//
//               ist@uni-koblenz.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//
 

header
{
package de.uni_koblenz.jgralab.greql2.parser;
 
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
}

class Greql2Lexer extends Lexer;

options {
	testLiterals=false;    // don't automatically test for literals
	k=3;                   // four characters of lookahead
	defaultErrorHandler=false;	
	charVocabulary='\u0003'..'\uFFFF';
	codeGenBitsetTestThreshold=20;
}


tokens{
    NUM_REAL;
	DOTDOT;
	DOT;
	FUNCTIONID;
	THISVERTEX;
	THISEDGE;
	
	AND 		= "and";
	ANDTHEN 	= "andThen";
	FALSE 		= "false";
	NOT 		= "not";
	NULL_VALUE 	= "null";
	OR  		= "or";
	ORELSE 		= "orElse";
	TRUE 		= "true";
	XOR 		= "xor";
	AS 		= "as";
	BAG 		= "bag";
	E 		= "E";
	ESUBGRAPH	= "eSubgraph";
	EXISTS_ONE	= "exists!";
	EXISTS		= "exists";
	END 		= "end";
	FORALL		= "forall";
	FROM  		= "from";
	IN 			= "in";
	LET 		= "let";
	LIST 		= "list";
	PATH 		= "path";
	PATHSYSTEM 	= "pathsystem";
	REC 		= "rec";
	REPORT 		= "report";
	REPORTSET	= "reportSet";
	REPORTBAG	= "reportBag";
	REPORTTABLE	= "reportTable";
	STORE		= "store";
	SET 		= "set";
	T 			= "T";
	TUP 		= "tup";
	USING		= "using";
	V 			= "V";
	VSUBGRAPH	= "vSubgraph";
	WHERE 		= "where";
	WITH 		= "with";
}

{

	private boolean isFunctionName(String ident)
	{
		return Greql2FunctionLibrary.instance().isGreqlFunction(ident);		
	}	
	
}

QUESTION
options {  paraphrase = "'?'";}	 
	:	
		
		'?'	 
	;

EXCL 
options {  paraphrase = "'!'";} 		
	:	
		'!'	
	;

COLON 
options {  paraphrase = "':'";}		
	:	
		
		':'	
	;
		
COMMA 
options {  paraphrase = "','";}		
	:	
		','	
	;

AT	
options {  paraphrase = "'@'";}	
	: 			
		'@'
	;

LPAREN	
options {  paraphrase = "'('";}	
	: 		
		'('	
	;

RPAREN	
options {  paraphrase = "')'";}	
	: 
		
		')'	
	;

LBRACK	
options {  paraphrase = "'['";}	
	: 
		
		'['	
	;

RBRACK 
options {  paraphrase = "']'";}		
	:			
		']'	
	;


LCURLY 
options {  paraphrase = "'{'";}		
	:	
		
		'{'
	;

RCURLY 
options {  paraphrase = "'}'";}		
	:	
		'}'	
	;



ASSIGN 
options {  paraphrase = "':='";}		
	:	
		":="	
	;
	
GASSIGN 
options {  paraphrase = "'::='";}	
	:	
		"::="	
	;


EQUAL 
options {  paraphrase = "'='";}		
	:
		
		'='	
	;

MATCH 
options {  paraphrase = "'=~'";}		
	:	
		
		"=~"	
	;


NOT_EQUAL 
options {  paraphrase = "'<>'";}	
	:	
		"<>"	
	;

LE	
options {  paraphrase = "'<='";}	
	:	
		
		"<="	
	;

GE 
options {  paraphrase = "'>='";}		
	:	
		">="	
	;

L_T 
options {  paraphrase = "'<'";}		
	:	
		
		'<'	
	; 

G_T 
options {  paraphrase = "'>'";}		
	:	
		
		'>'	
	;


DIV 
options {  paraphrase = "'/'";}		
	:	
		
		'/'	
	;
	
PLUS 
options {  paraphrase = "'+'";}		
	:	
		
		'+'	
	;
	
MINUS 	
options {  paraphrase = "'-'";}		
	:	
		
		'-'	
	;
	
STAR 
options {  paraphrase = "'*'";}		
	:	
		
		'*'	
	;

MOD	
options {  paraphrase = "'%'";}	
	:	
		
		'%'	
	;

SEMI 
options {  paraphrase = "';'";}		
	:	
		
		';'	
	;

CARET 
options {  paraphrase = "'^'";}		
	:	
		
		'^'	
	;

BOR 
options {  paraphrase = "'|'";}		
	:	
		
		'|'	
	;

AMP 
options {  paraphrase = "'&'";}		
	:	
		
		'&'	
	;


SMILEY 
options {  paraphrase = "':-)'";}		
	: 	
		
		":-)"	
	;


EDGESTART	
options {  paraphrase = "'<-'";}
	:	
		
		"<-"	
	;

EDGEEND		
options {  paraphrase = "'->'";}
	:	
		
		"->"	
	;

EDGE		
options {  paraphrase = "'--'";}
	:	
		
		"--"	
	;	

RARROW  	
options {  paraphrase = "'-->'";}
	:	
		
		"-->"	
	;

LARROW  	
options {  paraphrase = "'<--'";}
	:	
		
		"<--"	
	;

ARROW   	
options {  paraphrase = "'<->'";}
	: 	
		
		"<->"	
	;

HASH 
options {  paraphrase = "'#'";}
	: 
		"#"
	;	

// Whitespace -- ignored
WS	:	(	' '
			|	'\t'
			|	'\f'
				// handle newlines
			|	(	options { generateAmbigWarnings=false; }
				:	"\r\n"     
				                 // DOS
					|	'\r'    // Macintosh vor Mac OS 9
					    
					|	( '\n'    // Unix 
			     )
				)
				{ 					
					// newline(); 					
				}
		)+
		{ $setType(Token.SKIP); }
	;

// Single-line comments
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)?
		{			
			$setType(Token.SKIP);			
		}
	;  
	    

// multiple-line comments
ML_COMMENT	
{ int start = getColumn()-1;}
	:	"/*"	     
		
		( 		
			options { generateAmbigWarnings=false; 	}
		:		
			{ LA(1) != EOF_CHAR && LA(2)!='/' }? '*'			
			| { LA(1) != EOF_CHAR }? ('/' '*') => ML_COMMENT				    
			| { LA(1) != EOF_CHAR }? ~('*')
			| { LA(1) == EOF_CHAR }? { throw new TokenStreamException("Unterminated /*-comment starting at offset " + start); }
		
		)*
		
		"*/"
		{$setType(Token.SKIP);}
	;

		
// string literals
STRING_LITERAL options {  paraphrase = "a string literal";}
{ int start = getColumn()-1; }	
	:	
		'"' 
		( 	options { generateAmbigWarnings=false; 	}
			:	
			  ESC	          
	      |  {LA(1) != EOF_CHAR}? ~( '"' | '\\' | '\n' | '\r' )	   
	      |  { LA(1) == EOF_CHAR }? { throw new TokenStreamException("Unterminated string-literal starting at offset "+start); }     
	    )* 
	    '"'
	;


// escape sequence -- note that this is protected; it can only be called
//   from another lexer rule -- it will not ever directly return a token to
//   the parser
// There are various ambiguities hushed in this rule.  The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched.  ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
		)
	;

// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	options 
	{  
		testLiterals=true;
		paraphrase = "an identifier";
	}	
	:	
		(('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
		{			
			if ($getText.equals("thisEdge")) 
				{_ttype = THISEDGE;}
			else if ($getText.equals("thisVertex"))
				{_ttype = THISVERTEX;}
			else if (isFunctionName($getText))
				{_ttype = FUNCTIONID;} 		
				
		})
	;
	    
// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	
		('0'..'9'|'A'..'F'|'a'..'f')
	;

	    	
protected 
DIGIT
	:	
		
		'0' | NONZERO_DIGIT
	;
	
		
protected
NONZERO_DIGIT
	:	
		
		('1'..'9')
	;



protected
OCT_DIGIT
	:	
		
		('0'..'7')
	;
	
// a numeric literal
NUM_INT
{boolean range = false;
 boolean isDecimal = false;}
	: 
	  
		'.' 
	  (	( ~('0'..'9') )	=> { _ttype = DOT;}
	  	|'.' { _ttype = DOTDOT; }
		| (DIGIT)* (EXPONENT)? {_ttype = NUM_REAL;}
	  )
	| ( '0' {isDecimal = true;}
	      ( ('x'|'X') (options{warnWhenFollowAmbig=false;}:HEX_DIGIT)+
	      | ((DIGIT)* ( '.' '.' )) => (DIGIT)* 
	      	{range = true; }
	      | ((DIGIT)+ ( '.' (~('.') | EXPONENT) )) => (DIGIT)+	      
	      | (OCT_DIGIT)+
	      )?
	    | (NONZERO_DIGIT) {isDecimal = true;}
	      (((DIGIT)* ('.' '.')) => (DIGIT)* {range = true;}
	       |
	      (DIGIT)* )	    
	  )
	  (
		{isDecimal & !range}?
		( '.' (DIGIT)* (EXPONENT)?
		| EXPONENT
		){_ttype = NUM_REAL;}
		
	  )?
	;


// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	
		('e'|'E') ('+'|'-')? ('0'..'9')+
	;
	
    
 