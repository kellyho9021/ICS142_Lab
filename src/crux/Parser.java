package crux;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {
    public static String studentName = "Kelly Ho";
    public static String studentID = "81482302";
    public static String uciNetID = "doankhah";
    
// Grammar Rule Reporting ==========================================
    private int parseTreeRecursionDepth = 0;
    private StringBuffer parseTreeBuffer = new StringBuffer();

    public void enterRule(NonTerminal nonTerminal) {
        String lineData = new String();
        for(int i = 0; i < parseTreeRecursionDepth; i++)
        {
            lineData += "  ";
        }
        lineData += nonTerminal.name();
        //System.out.println("descending " + lineData);
        parseTreeBuffer.append(lineData + "\n");
        parseTreeRecursionDepth++;
    }
    
    private void exitRule(NonTerminal nonTerminal)
    {
        parseTreeRecursionDepth--;
    }
    
    public String parseTreeReport()
    {
        return parseTreeBuffer.toString();
    }

// Error Reporting ==========================================
    private StringBuffer errorBuffer = new StringBuffer();
    
    private String reportSyntaxError(NonTerminal nt)
    {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
     
    private String reportSyntaxError(Token.Kind kind)
    {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    private class QuitParseException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    private int lineNumber()
    {
        return currentToken.lineNumber();
    }
    
    private int charPosition()
    {
        return currentToken.charPosition();
    }
          
// Parser ==========================================
    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner)
    {
        this.scanner = scanner;
        this.currentToken = scanner.next();
    }
    
    public void parse()
    {
        try {
            program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        }
    }
    
// Helper Methods ==========================================
    private boolean have(Token.Kind kind)
    {
        return currentToken.is(kind);
    }
    
    private boolean have(NonTerminal nt)
    {
        return nt.firstSet().contains(currentToken.kind());
    }

    private boolean accept(Token.Kind kind)
    {
        if (have(kind)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }    
    
    private boolean accept(NonTerminal nt)
    {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }
   
    private boolean expect(Token.Kind kind)
    {
        if (accept(kind))
            return true;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return false;
    }
        
    private boolean expect(NonTerminal nt)
    {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return false;
    }
   
// Grammar Rules =====================================================
    
    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public void literal()
    {
    	enterRule(NonTerminal.LITERAL);
    	//System.out.println("LITERAL");
    	if(have(Token.Kind.INTEGER))
    		expect(Token.Kind.INTEGER);
    	else if(have(Token.Kind.FLOAT))
    		expect(Token.Kind.FLOAT);
    	else if(have(Token.Kind.TRUE))
    		expect(Token.Kind.TRUE);
    	else if(have(Token.Kind.FALSE))
    		expect(Token.Kind.FALSE);
    	exitRule(NonTerminal.LITERAL);
    }
    
    // designator := IDENTIFIER { "[" expression0 "]" } .
    public void designator()
    {
        enterRule(NonTerminal.DESIGNATOR);
        //System.out.println("DESIGNATOR");
        expect(Token.Kind.IDENTIFIER);
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expression0();
            expect(Token.Kind.CLOSE_BRACKET);
        }
        
        exitRule(NonTerminal.DESIGNATOR);
    }
    
    //type
    public void type()
    {
    	enterRule(NonTerminal.TYPE);
    	//System.out.println("TYPE");
    	expect(Token.Kind.IDENTIFIER);
    	exitRule(NonTerminal.TYPE);
    }
    
    //op0
    public void op0()
    {
    	enterRule(NonTerminal.OP0);
    	//System.out.println("OP0");
    	if(have(Token.Kind.GREATER_EQUAL))
    		expect(Token.Kind.GREATER_EQUAL);
    	else if(have(Token.Kind.LESSER_EQUAL))
    		expect(Token.Kind.LESSER_EQUAL);
    	else if(have(Token.Kind.NOT_EQUAL))
    		expect(Token.Kind.NOT_EQUAL);
    	else if(have(Token.Kind.EQUAL))
    		expect(Token.Kind.EQUAL);
    	else if(have(Token.Kind.LESS_THAN))
    		expect(Token.Kind.LESS_THAN);
    	else if(have(Token.Kind.GREATER_THAN))
    		expect(Token.Kind.GREATER_THAN);
    	exitRule(NonTerminal.OP0);
    }
    
    //op1
    public void op1()
    {
    	enterRule(NonTerminal.OP1);
    	//System.out.println("OP1");
    	if(have(Token.Kind.ADD))
    		expect(Token.Kind.ADD);
    	else if(have(Token.Kind.SUB))
    		expect(Token.Kind.SUB);
    	else if(have(Token.Kind.OR))
    		expect(Token.Kind.OR);
    	exitRule(NonTerminal.OP1);
    }
    
    //op2
    public void op2()
    {
    	enterRule(NonTerminal.OP2);
    	//System.out.println("OP2");
    	if(have(Token.Kind.MUL))
    		expect(Token.Kind.MUL);
    	else if(have(Token.Kind.DIV))
    		expect(Token.Kind.DIV);
    	else if(have(Token.Kind.AND))
    		expect(Token.Kind.AND);
    	exitRule(NonTerminal.OP2);
    }
    
    //expression3
    public void expression3()
    {
    	enterRule(NonTerminal.EXPRESSION3);
    	//System.out.println("EXPRESSION3");
        if(accept(Token.Kind.NOT))
        	expression3();
        else if(accept(Token.Kind.OPEN_PAREN))
        {
        	expression0();
        	expect(Token.Kind.CLOSE_PAREN);
        }
        else if(have(NonTerminal.DESIGNATOR))
        	designator();
        else if(have(NonTerminal.CALL_EXPRESSION))
        	call_expression();
        else if(have(NonTerminal.LITERAL))
        	literal();
        exitRule(NonTerminal.EXPRESSION3);
    }
    
    //expression2
    public void expression2()
    {
    	enterRule(NonTerminal.EXPRESSION2);
    	//System.out.println("EXPRESSION2");
        if(have(NonTerminal.EXPRESSION3))
        	expression3();
        while(accept(NonTerminal.OP2))
        {
        	op2();
        	expression3();
        }
        exitRule(NonTerminal.EXPRESSION2);
    }
    
    //expression1
    public void expression1()
    {
    	enterRule(NonTerminal.EXPRESSION1);
    	//System.out.println("EXPRESSION1");
        if(have(NonTerminal.EXPRESSION2))
        	expression2();
        while(accept(NonTerminal.OP1))
        {
        	op1();
        	expression2();
        }
        exitRule(NonTerminal.EXPRESSION1);
    }
    
    //expression0
    public void expression0()
    {
    	enterRule(NonTerminal.EXPRESSION0);
    	//System.out.println("EXPRESSION0");
        if(have(NonTerminal.EXPRESSION1))
        	expression1();
        while(have(NonTerminal.OP0))
        {
        	op0();
        	expression1();
        }
        exitRule(NonTerminal.EXPRESSION0);
    }	
    
    //call-expression
    public void call_expression()
    {
    	enterRule(NonTerminal.CALL_EXPRESSION);
    	//System.out.println("CALL_EXPRESSION");
    	expect(Token.Kind.CALL);
    	expect(Token.Kind.IDENTIFIER);
    	
    	while(accept(Token.Kind.OPEN_PAREN))
    	{
    		expression_list();
    		expect(Token.Kind.CLOSE_PAREN);
    	}
    	exitRule(NonTerminal.CALL_EXPRESSION);
    }	
    
    //expression-list
    public void expression_list()
    {
    	enterRule(NonTerminal.EXPRESSION_LIST);
    	//System.out.println("EXPRESSION_LIST");
    	if(have(NonTerminal.EXPRESSION0))
    	{
    		expression0();
    		while(accept(Token.Kind.COMMA))
    			expression0();
    	}
    	exitRule(NonTerminal.EXPRESSION_LIST);
    }
    
    //parameter
    public void parameter()
    {
    	enterRule(NonTerminal.PARAMETER);    
    	//System.out.println("PARAMETER");
        expect(Token.Kind.IDENTIFIER);
        expect(Token.Kind.COLON);
        type();
        exitRule(NonTerminal.PARAMETER);
    }
    
    //parameter-list
    public void parameter_list()
    {
    	enterRule(NonTerminal.PARAMETER_LIST);
    	//System.out.println("PARAMETER_LIST");
    	if(have(NonTerminal.PARAMETER))
    	{
    		parameter();
    		while(accept(Token.Kind.COMMA))
    			parameter();
    	}
    	exitRule(NonTerminal.PARAMETER_LIST);
    }
    
    //variable-declaration
    public void variable_declaration()
    {
    	enterRule(NonTerminal.VARIABLE_DECLARATION);
    	//System.out.println("VARIABLE_DECLARATION");
    	expect(Token.Kind.VAR);
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.COLON);
		type();
		expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.VARIABLE_DECLARATION);
    }
    
    //array-declaration
    public void array_declaration()
    {
    	enterRule(NonTerminal.ARRAY_DECLARATION);
    	//System.out.println("ARRAY_DECLARATION");
    	expect(Token.Kind.ARRAY);
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.COLON);
		type();
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			expect(Token.Kind.INTEGER);
			expect(Token.Kind.CLOSE_BRACKET);
		}
		expect(Token.Kind.SEMICOLON);
    	
    	exitRule(NonTerminal.ARRAY_DECLARATION);
    }
    
    //function-definition
    public void function_definition()
    {
    	enterRule(NonTerminal.FUNCTION_DEFINITION);
    	//System.out.println("FUNCTION_DEFINITION");
    	if(accept(Token.Kind.FUNC))
    	{
    		expect(Token.Kind.IDENTIFIER);
    		while(accept(Token.Kind.OPEN_PAREN))
    		{
    			parameter_list();
    			expect(Token.Kind.CLOSE_PAREN);
    		}
    		expect(Token.Kind.COLON);
    		type();
    		statement_block();
    	}
    	exitRule(NonTerminal.FUNCTION_DEFINITION);
    }
    
    //declaration
    public void declaration()
    {
    	enterRule(NonTerminal.DECLARATION);
    	//System.out.println("DECLARATION");
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    			variable_declaration();
    	else if(have(NonTerminal.ARRAY_DECLARATION))
    		array_declaration();
    	else if(have(NonTerminal.FUNCTION_DEFINITION))
    		function_definition();
    	exitRule(NonTerminal.DECLARATION);
    }
    
    //declaration-list
    public void declaration_list()
    {
    	enterRule(NonTerminal.DECLARATION_LIST);
    	//System.out.println("DECLARATION_LIST");
    	while(have(NonTerminal.DECLARATION))
    			declaration();
    	exitRule(NonTerminal.DECLARATION_LIST);
    }
    
    //assignment-statement
    public void assignment_statement()
    {
    	enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
    	//System.out.println("ASSIGNMENT_STATEMENT");
    	expect(Token.Kind.LET);
		designator();
		expect(Token.Kind.ASSIGN);
		expression0();
		expect(Token.Kind.SEMICOLON);
    	
    	exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
    }
    
    //call-statement
    public void call_statement()
    {
    	enterRule(NonTerminal.CALL_STATEMENT);
    	//System.out.println("CALL_STATEMENT");
    	call_expression();
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.CALL_STATEMENT);
    }
    
    //if-statement
    public void if_statement()
    {
    	enterRule(NonTerminal.IF_STATEMENT);
    	//System.out.println("IF_STATEMENT");
    	expect(Token.Kind.IF);
    	expression0();
    	statement_block();
    	if(accept(Token.Kind.ELSE))
    		statement_block();
    	exitRule(NonTerminal.IF_STATEMENT);
    }
    
    //while-statement
    public void while_statement()
    {
    	enterRule(NonTerminal.WHILE_STATEMENT);
    	//System.out.println("WHILE_STATEMENT");
    	if(accept(Token.Kind.WHILE))
    	{
    		expression0();
    		statement_block();
    	}
    	exitRule(NonTerminal.WHILE_STATEMENT);
    }
    
    //return-statement
    public void return_statement()
    {
    	enterRule(NonTerminal.RETURN_STATEMENT);
    	//System.out.println("RETURN_STATEMENT");
    	if(accept(Token.Kind.RETURN))
    	{
    		expression0();
    		expect(Token.Kind.SEMICOLON);
    	}
    	exitRule(NonTerminal.RETURN_STATEMENT);
    }
    
    //statement
    public void statement()
    {
    	enterRule(NonTerminal.STATEMENT);
    	//System.out.println("STATEMENT");
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    		variable_declaration();
    	else if(have(NonTerminal.CALL_STATEMENT))
			call_statement();
    	else if(have(NonTerminal.ASSIGNMENT_STATEMENT))
			assignment_statement();
    	else if(have(NonTerminal.IF_STATEMENT))
			if_statement();
    	else if(have(NonTerminal.WHILE_STATEMENT))
			while_statement();
    	else if(have(NonTerminal.RETURN_STATEMENT))
			return_statement();
    	exitRule(NonTerminal.STATEMENT);
    }
    
    //statement-list
    public void statement_list()
    {
    	enterRule(NonTerminal.STATEMENT_LIST);
    	//System.out.println("STATEMENT_LIST");
    	while(have(NonTerminal.STATEMENT))
    			statement();
    	exitRule(NonTerminal.STATEMENT_LIST);
    }
    
    //statement-block
    public void statement_block()
    {
    	enterRule(NonTerminal.STATEMENT_BLOCK);
    	//System.out.println("STATEMENT_BLOCK");
        while(accept(Token.Kind.OPEN_BRACE))
        {
        	statement_list();
        	expect(Token.Kind.CLOSE_BRACE);
        }
        exitRule(NonTerminal.STATEMENT_BLOCK);
    }
    
    // program := declaration-list EOF .
    public void program()
    {
        enterRule(NonTerminal.PROGRAM);
       // System.out.println("PROGRAM");
        declaration_list();
        expect(Token.Kind.EOF);
        exitRule(NonTerminal.PROGRAM);
    }
    
    
}
