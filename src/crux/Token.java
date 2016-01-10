package crux;

import java.util.Iterator;

public class Token {
	
	public static enum Kind {
		//Reserved keywords
		AND("and"),
		OR("or"),
		NOT("not"),
		LET("let"),
		VAR("var"),
		ARRAY("array"),
		FUNC("func"),
		IF("if"),
		ELSE("else"),
		WHILE("while"),
		TRUE("true"),
		FALSE("false"),
		RETURN("return"),
		
		//Character sequences
		OPEN_PAREN("("),
		CLOSE_PAREN(")"),
		OPEN_BRACE("{"),
		CLOSE_BRACE("}"),
		OPEN_BRACKET("["),
		CLOSE_BRACKET("]"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		GREATER_EQUAL(">="),
		LESSER_EQUAL("<="),
		NOT_EQUAL("!="),
		EQUAL("=="),
		GREATER_THAN(">"),
		LESS_THAN("<"),
		ASSIGN("="),
		COMMA(","),
		SEMICOLON(";"),
		COLON(":"),
		CALL("::"),
		
		//Reserved value literals
		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF();
		
		// TODO: complete the list of possible tokens
		
		private String default_lexeme;
		
		Kind()
		{
			default_lexeme = "";
		}
		
		Kind(String lexeme)
		{
			default_lexeme = lexeme;
		}
		
		public boolean hasStaticLexeme()
		{
			return default_lexeme != null;
		}
		
		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}
	
	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";
	
	
	// OPTIONAL: implement factory functions for some tokens, as you see fit
	public static Token Identifier(String lex, int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.IDENTIFIER;
		tok.lexeme = lex;
		return tok;
	}
	
	public static Token Integer(String lex, int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.INTEGER;
		tok.lexeme = lex;
		return tok;
	}
          
	public static Token Float(String lex, int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.FLOAT;
		tok.lexeme = lex;
		return tok;
	}
	
	public static Token Error(String lex, int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.ERROR;
		tok.lexeme = lex;
		return tok;
	}
	
	public static Token EOF(int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}

	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		
		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	public Token(String lexeme, int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		boolean match = false;
		// TODO: based on the given lexeme determine and set the actual kind
		for(Kind i : Kind.values())
		{
			if(i.equals(lexeme))
			{
				this.kind = i;
				match = true;
			}
		}
		// if we don't match anything, signal error
		if(!match)
		{
			this.kind = Kind.ERROR;
			this.lexeme = "Unrecognized lexeme: " + lexeme;
		}
	}
	
	public int lineNumber()
	{
		return lineNum;
	}
	
	public int charPosition()
	{
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme()
	{
		// TODO: implement
		if(!kind.hasStaticLexeme())
			return lexeme;
		else
			return kind.default_lexeme;
	}
	
	public String toString()
	{
		// TODO: implement this
		String name = kind.name();
		if(kind.hasStaticLexeme() == false)
			name += " (" + lexeme() + ") ";
		
		name += "(lineNume: " + lineNum + ", charPos: " + charPos + ")";
		return name;
	}
	
	// OPTIONAL: function to query a token about its kind
	//           boolean is(Token.Kind kind)
	public boolean is(Token.Kind key)
	{
		return this.kind.equals(key);
	}
	// OPTIONAL: add any additional helper or convenience methods
	//           that you find make for a clean design
	

}
