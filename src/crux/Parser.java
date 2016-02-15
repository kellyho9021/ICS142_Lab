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
    
// SymbolTable Management ==========================
    private SymbolTable symbolTable;
    
    private void initSymbolTable()
    {
        symbolTable = new SymbolTable();
        Symbol s = symbolTable.insert("readInt");
        
        s = symbolTable.insert("readFloat");
        
        s = symbolTable.insert("printBool");
        
        s = symbolTable.insert("printInt");
        
        s = symbolTable.insert("printFloat");
        
        s = symbolTable.insert("println");
    }
    
    private void enterScope()
    {
        symbolTable = new SymbolTable(symbolTable);
    }
    
    private void exitScope()
    {
        symbolTable = symbolTable.parentTable();
    }

    private Symbol tryResolveSymbol(Token ident)
    {
    	assert(ident.is(Token.Kind.IDENTIFIER));
    	String name = ident.lexeme();
        try {
            return symbolTable.lookup(name);
        } catch (SymbolNotFoundError e) {
            String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos)
    {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

    private Symbol tryDeclareSymbol(Token ident)
    {
    	assert(ident.is(Token.Kind.IDENTIFIER));
    	String name = ident.lexeme();
        try {
            return symbolTable.insert(name);
        } catch (RedeclarationError re) {
            String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos)
    {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }    
           
// Parser ==========================================
    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner)
    {
        this.scanner = scanner;
        this.currentToken = scanner.next();
    }
    
    public ast.Command parse()
    {
        initSymbolTable();
        try {
            return program();
        } catch (QuitParseException q) {
            return new ast.Error(lineNumber(), charPosition(), "Could not complete parsing.");
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

    /*
    private boolean assume(Token.Kind kind)
    {
        if (!have(kind))
            reportSyntaxError("Expected token " + kind.name() + " but got " + currentToken);
        return have(kind);
    }
    
    private Token assume(Set<Token.Kind> kinds)
    {
        Token tok = peek(kinds);
        if (tok.isNull())
            reportSyntaxError("Expected one of " + kinds + " but got " + currentToken);
        return tok;
    }
    */
    
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
    
    private Token expectRetrieve(Token.Kind kind)
    {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
        
    private Token expectRetrieve(NonTerminal nt)
    {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
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
    
    private Integer expectInteger()
    {
        String num = currentToken.lexeme();
        if (expect(Token.Kind.INTEGER))
            return Integer.valueOf(num);
        return null;
    }
    
// Grammar Rules =====================================================
    
    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public ast.Expression literal()
    {
        ast.Expression expr;
        enterRule(NonTerminal.LITERAL);
        
        Token tok = expectRetrieve(NonTerminal.LITERAL);
        expr = ast.Command.newLiteral(tok);
        
        exitRule(NonTerminal.LITERAL);
        return expr;
    }
    
    // designator := IDENTIFIER { "[" expression0 "]" } .
    public ast.Expression designator()
    {
    	ast.Expression expr;
        enterRule(NonTerminal.DESIGNATOR);
        int lineNum = lineNumber();
        int charPos = charPosition();
        Symbol sym = tryResolveSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        expr = new ast.AddressOf(lineNum, charPos, sym);
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expr = new ast.Index(lineNumber(), charPosition(), expr, expression0());
            expect(Token.Kind.CLOSE_BRACKET);
        }
        
        exitRule(NonTerminal.DESIGNATOR);
        return expr;
    }
    
    // type := IDENTIFIER .
    public void type()
    {
        enterRule(NonTerminal.TYPE);
        expectRetrieve(Token.Kind.IDENTIFIER);
        exitRule(NonTerminal.TYPE);
    }

    // op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
    public Token op0()
    {
        enterRule(NonTerminal.OP0);
        Token tok = expectRetrieve(NonTerminal.OP0);
        exitRule(NonTerminal.OP0);
        return tok;
    }
        
    // expression0 := expression1 [ op0 expression1 ] .
    public ast.Expression expression0()
    {
        enterRule(NonTerminal.EXPRESSION0);
        ast.Expression expr = expression1();
        if (have(NonTerminal.OP0)) { 
            Token op = op0();
            ast.Expression rightExpr = expression1();
            expr = ast.Command.newExpression(expr, op, rightExpr);
        }
        exitRule(NonTerminal.EXPRESSION0);
        return expr;
    }

    // op1 := "+" | "-" | "or" .
    public Token op1()
    {
        enterRule(NonTerminal.OP1);
        Token tok = expectRetrieve(NonTerminal.OP1);
        exitRule(NonTerminal.OP1);
        return tok;
    }
    
    // expression1 := expression2 { op1 expression2 } .
    public ast.Expression expression1()
    {
        enterRule(NonTerminal.EXPRESSION1);
        ast.Expression expr = expression2();
        while (have(NonTerminal.OP1)) {
            Token op = op1();
            ast.Expression rightExpr = expression2();
            expr = ast.Command.newExpression(expr, op, rightExpr);
        }
        exitRule(NonTerminal.EXPRESSION1);
        return expr;
    }
        
    // op2 := "*" | "/" | "and" .
    public Token op2()
    {
        enterRule(NonTerminal.OP2);
        Token tok = expectRetrieve(NonTerminal.OP2);
        exitRule(NonTerminal.OP2);
        return tok;
    }
    
    // expression2 := expression3 { op2 expression3 } .
    public ast.Expression expression2()
    {
        enterRule(NonTerminal.EXPRESSION2);
        ast.Expression expr = expression3();
        while (have(NonTerminal.OP2)) {
            Token op = op2();
            ast.Expression rightExpr = expression3();
            expr = ast.Command.newExpression(expr, op, rightExpr);
        }
        exitRule(NonTerminal.EXPRESSION2);
        return expr;
    }
    
    /* expression3 := "not" expression3
               | "(" expression0 ")"
               | designator
               | call-expression
               | literal .
    */
    public ast.Expression expression3()
    {
        enterRule(NonTerminal.EXPRESSION3);
        ast.Expression expr = null;
        int lineNum = lineNumber();
        int charPos = charPosition();
        if (have(Token.Kind.NOT)) {
            Token tok = expectRetrieve(Token.Kind.NOT);
            expr = new ast.LogicalNot(lineNum, charPos, expression3());
        } else if (accept(Token.Kind.OPEN_PAREN)) {
            expr = expression0();
            expect(Token.Kind.CLOSE_PAREN);
        } else if (have(NonTerminal.DESIGNATOR)) {
            expr = designator();
            expr = new ast.Dereference(lineNum, charPos, expr);
        } else if (have(NonTerminal.CALL_EXPRESSION)) {
            expr = call_expression();
        } else if (have(NonTerminal.LITERAL)) {
            expr = literal();
        } else {
            String message = reportSyntaxError(NonTerminal.EXPRESSION3);
            expr = new ast.Error(lineNum, charPos, message);
        }
        exitRule(NonTerminal.EXPRESSION3);
        return expr;
    }
    
    // call-expression := "::" IDENTIFIER "(" expression-list ")" .
    public ast.Call call_expression()
    {
        enterRule(NonTerminal.CALL_EXPRESSION);
        ast.Call call;
        int lineNum = lineNumber();
        int charPos = charPosition();
        
        expect(Token.Kind.CALL);
        Symbol func = tryResolveSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        expect(Token.Kind.OPEN_PAREN);
        ast.ExpressionList exprList = expression_list();
        expect(Token.Kind.CLOSE_PAREN);
        call = new ast.Call(lineNum, charPos, func, exprList);
        exitRule(NonTerminal.CALL_EXPRESSION);
        return call;
    }
    
    // expression-list := [ expression0 { "," expression0 } ] .
    public ast.ExpressionList expression_list()
    {
        enterRule(NonTerminal.EXPRESSION_LIST);
        ast.ExpressionList exprList = new ast.ExpressionList(lineNumber(), charPosition());
        if (have(NonTerminal.EXPRESSION0)) {
            exprList.add(expression0());
            while (accept(Token.Kind.COMMA)) {
            	exprList.add(expression0());
            }
        }
        exitRule(NonTerminal.EXPRESSION_LIST);
        return exprList;
    }
    
    // parameter := IDENTIFIER ":" type .
    public Symbol parameter()
    {
        enterRule(NonTerminal.PARAMETER);
        Symbol sym = tryDeclareSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        expect(Token.Kind.COLON);
        type();
        exitRule(NonTerminal.PARAMETER);
        return sym;
    }
    
    //parameter-list := [ parameter { "," parameter } ] .
    public List<Symbol> parameter_list()
    {
        List<Symbol> params = new ArrayList<Symbol>();
        enterRule(NonTerminal.PARAMETER_LIST);
        if (have(NonTerminal.PARAMETER)) {
            params.add(parameter());
            while (accept(Token.Kind.COMMA)) {
                params.add(parameter());
            }
        }
        exitRule(NonTerminal.PARAMETER_LIST);
        return params;
    }

    // variable-declaration := "var" IDENTIFIER ":" type ";"
    public ast.VariableDeclaration variable_declaration()
    {
        enterRule(NonTerminal.VARIABLE_DECLARATION);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.VariableDeclaration var;
        expect(Token.Kind.VAR);
        Symbol sym = tryDeclareSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        var = new ast.VariableDeclaration(lineNum, charPos, sym);
        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.SEMICOLON);
        exitRule(NonTerminal.VARIABLE_DECLARATION);
        return var;
    }
    
    // array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
    public ast.ArrayDeclaration array_declaration()
    {
        enterRule(NonTerminal.ARRAY_DECLARATION);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.ArrayDeclaration array;
        expect(Token.Kind.ARRAY);
        Symbol sym = tryDeclareSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        array = new ast.ArrayDeclaration(lineNum, charPos, sym);
        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.OPEN_BRACKET);
        expect(Token.Kind.INTEGER);
        expect(Token.Kind.CLOSE_BRACKET);
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expect(Token.Kind.INTEGER);
            expect(Token.Kind.CLOSE_BRACKET);
        }
        expect(Token.Kind.SEMICOLON);
        
        exitRule(NonTerminal.ARRAY_DECLARATION);
        return array;
    }
    
    // function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
    public ast.FunctionDefinition function_definition()
    {
        enterRule(NonTerminal.FUNCTION_DECLARATION);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.FunctionDefinition func;
        expect(Token.Kind.FUNC);
        Symbol sym = tryDeclareSymbol(expectRetrieve(Token.Kind.IDENTIFIER));
        expect(Token.Kind.OPEN_PAREN);
        enterScope();
        List<Symbol> paraList = parameter_list();
        expect(Token.Kind.CLOSE_PAREN);
        expect(Token.Kind.COLON);
        type();
        
        ast.StatementList statList = statement_block();
        func = new ast.FunctionDefinition(lineNum, charPos, sym, paraList, statList);
        exitScope();
        
        exitRule(NonTerminal.FUNCTION_DECLARATION);
        return func;
    }

    // declaration := variable-declaration | array-declaration | function-definition .
    public ast.Declaration declaration()
    {
        enterRule(NonTerminal.DECLARATION);
        ast.Declaration dec;
        if (have(NonTerminal.VARIABLE_DECLARATION)) {
            dec = variable_declaration();
        } else if (have(NonTerminal.ARRAY_DECLARATION)) {
            dec = array_declaration();
        } else if (have(NonTerminal.FUNCTION_DECLARATION)){
            dec = function_definition();
        } else {
            String message = reportSyntaxError(NonTerminal.DECLARATION);
            dec = new ast.Error(lineNumber(), charPosition(), message);
        }
        
        exitRule(NonTerminal.DECLARATION);
        return dec;
    }

    // declaration-list := { declaration } .
    public ast.DeclarationList declaration_list() {
        enterRule(NonTerminal.DECLARATION_LIST);
        ast.DeclarationList decList = new ast.DeclarationList(lineNumber(), charPosition());
        while (have(NonTerminal.DECLARATION)) {
            decList.add(declaration());
        }
        
        exitRule(NonTerminal.DECLARATION_LIST);
        return decList;
    }

    // assignment-statement := "let" designator "=" expression0 ";"
    public ast.Assignment assignment_statement()
    {
        enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
        ast.Assignment assign;
        Token eql = expectRetrieve(Token.Kind.LET);
        ast.Expression des = designator();
        expect(Token.Kind.ASSIGN);
        ast.Expression expr = expression0();
        expect(Token.Kind.SEMICOLON);
        exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
        assign = new ast.Assignment(eql.lineNumber(), eql.charPosition(), des, expr);
        return assign;
    }
        
    // call-statement := call-expression ";"
    public ast.Call call_statement()
    {
        enterRule(NonTerminal.CALL_STATEMENT);
        ast.Call call = call_expression();
        expect(Token.Kind.SEMICOLON);
        exitRule(NonTerminal.CALL_STATEMENT);
        return call;
    }
    
    // if-statement := "if" expression0 statement-block [ "else" statement-block ] .
    public ast.IfElseBranch if_statement()
    {
        enterRule(NonTerminal.IF_STATEMENT);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.IfElseBranch ifElse;
        expect(Token.Kind.IF);
        ast.Expression expr = expression0();
        enterScope();
        ast.StatementList statList = statement_block();
        exitScope();
        ast.StatementList elseStatList = new ast.StatementList(lineNumber(), charPosition());
        if (accept(Token.Kind.ELSE)) {
            enterScope();
            elseStatList = statement_block();
            exitScope();
        }
        ifElse = new ast.IfElseBranch(lineNum, charPos, expr, statList, elseStatList);
        exitRule(NonTerminal.IF_STATEMENT);
        return ifElse;
    }
    
    // while-statement := "while" expression0 statement-block .
    public ast.WhileLoop while_statement()
    {
        enterRule(NonTerminal.WHILE_STATEMENT);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.WhileLoop whileLoop;
        expect(Token.Kind.WHILE);
        ast.Expression expr = expression0();
        enterScope();
        ast.StatementList statList = statement_block();
        exitScope();
        whileLoop = new ast.WhileLoop(lineNum, charPos, expr, statList);
        exitRule(NonTerminal.WHILE_STATEMENT);
        return whileLoop;
    }
    
    // return-statement := "return" expression0 ";" .
    public ast.Return return_statement()
    {
        enterRule(NonTerminal.RETURN_STATEMENT);
        int lineNum = lineNumber();
        int charPos = charPosition();
        ast.Return ret;
        expect(Token.Kind.RETURN);
        ast.Expression expr = expression0();
        expect(Token.Kind.SEMICOLON);
        ret = new ast.Return(lineNum, charPos, expr);
        exitRule(NonTerminal.RETURN_STATEMENT);
        return ret;
    }
    
    // statement := variable-declaration
    //            | call-statement
    //            | assignment-statement
    //            | if-statement
    //            | while-statement
    //            | return-statement .
    public ast.Statement statement()
    {
        enterRule(NonTerminal.STATEMENT);
        ast.Statement stat;
        if (have(NonTerminal.VARIABLE_DECLARATION)) {
            stat = variable_declaration();
        } else if (have(NonTerminal.ARRAY_DECLARATION)) {
            stat = array_declaration();
        } else if (have(NonTerminal.CALL_STATEMENT)) {
            stat = call_statement();
        } else if (have(NonTerminal.ASSIGNMENT_STATEMENT)) {
            stat = assignment_statement();
        } else if (have(NonTerminal.IF_STATEMENT)) {
            stat = if_statement();
        } else if (have(NonTerminal.WHILE_STATEMENT)) {
            stat = while_statement();
        } else if (have(NonTerminal.RETURN_STATEMENT)) {
            stat = return_statement();
        } else {
            String message = reportSyntaxError(NonTerminal.STATEMENT);
            stat = new ast.Error(lineNumber(), charPosition(), message);
        }
            
        exitRule(NonTerminal.STATEMENT);
        return stat;
    }
    
    // statement-list := { statement } .
    public ast.StatementList statement_list()
    {
        enterRule(NonTerminal.STATEMENT_LIST);
        ast.StatementList statList = new ast.StatementList(lineNumber(), charPosition());
        while (have(NonTerminal.STATEMENT)) {
            statList.add(statement());
        }
        
        exitRule(NonTerminal.STATEMENT_LIST);
        return statList;
    }
    
    // statement-block := "{" statement-list "}" .
    public ast.StatementList statement_block()
    {
        enterRule(NonTerminal.STATEMENT_BLOCK);
        ast.StatementList statList = new ast.StatementList(lineNumber(), charPosition());
        expect(Token.Kind.OPEN_BRACE);
        statList = statement_list();
        expect(Token.Kind.CLOSE_BRACE);
        
        exitRule(NonTerminal.STATEMENT_BLOCK);
        return statList;
    }

    // program := declaration-list EOF .
    // program := declaration-list EOF .
    public ast.DeclarationList program()
    {
        enterRule(NonTerminal.PROGRAM);
        ast.DeclarationList program = declaration_list();
        expect(Token.Kind.EOF);
        exitRule(NonTerminal.PROGRAM);
        return program;
    }
}
