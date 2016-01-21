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
        throw new RuntimeException("implement this");
    }
    
    // designator := IDENTIFIER { "[" expression0 "]" } .
    public void designator()
    {
        enterRule(NonTerminal.DESIGNATOR);

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
        
    }
    
    //op0
    public void op0()
    {
        
    }
    
    //op1
    public void op1()
    {
        
    }
    
    //op2
    public void op2()
    {
        
    }
    
    //expression3
    public void expression3()
    {
        
    }
    
    //expression2
    public void expression2()
    {
        
    }
    
    //expression1
    public void expression1()
    {
        
    }
    
    //expression0
    public void expression0()
    {
        
    }	
    
    //call-expression
    public void call_expression()
    {
        
    }	
    
    //expression-list
    public void expression_list()
    {
        
    }
    
    //parameter
    public void parameter()
    {
        
    }
    
    //parameter-list
    public void parameter_list()
    {
        
    }
    
    //variable-declaration
    public void variable_declaration()
    {
        
    }
    
    //array-declaration
    public void array_declaration()
    {
        
    }
    
    //function-definition
    public void function_definition()
    {
        
    }
    
    //declaration
    public void declaration()
    {
        
    }
    
    //declaration-list
    public void declaration_list()
    {
        
    }
    
    //assignment-statement
    public void assignment_statement()
    {
        
    }
    
    //call-statement
    public void call_statement()
    {
        
    }
    
    //if-statement
    public void if_statement()
    {
        
    }
    
    //while-statement
    public void while_statement()
    {
        
    }
    
    //return-statement
    public void return_statement()
    {
        
    }
    
    //statement
    public void statement()
    {
        
    }
    
    //statement-list
    public void statement_list()
    {
        
    }
    
    //statement-block
    public void statement_block()
    {
        
    }
    
    // program := declaration-list EOF .
    public void program()
    {
        
    }
    
    
}
