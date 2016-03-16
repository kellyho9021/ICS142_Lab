package types;

import java.util.HashMap;
import java.util.List;
import ast.*;
import crux.Symbol;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;

    private Symbol curFunc = Symbol.newError("There is no return type");
    Type v = new VoidType();

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */
    
   public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) {
        	reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) {
        TypeList tList = new TypeList();
        for(Expression expr : node)
        {
        	expr.accept(this);
        	Type temp = getType((Command) expr);
        	tList.append(temp);
        }
        put(node, tList);
    }

    @Override
    public void visit(DeclarationList node) {
    	TypeList tList = new TypeList();
        for(Declaration dec : node){
        	dec.accept(this);
        	tList.add(typeMap.get(dec));
        }
        	
    }

    @Override
    public void visit(StatementList node) {
    	TypeList tList = new TypeList();
        for(Statement stat : node)
        {
        	stat.accept(this);
        	tList.add(typeMap.get(stat));
        }
    }

    @Override
    public void visit(AddressOf node) {
        put(node, new AddressType(node.symbol().type()));
    }

    @Override
    public void visit(LiteralBool node) {
        put(node, new BoolType());
    }

    @Override
    public void visit(LiteralFloat node) {
        put(node, new FloatType());
    }

    @Override
    public void visit(LiteralInt node) {
        put(node, new IntType());
    }

    @Override
    public void visit(VariableDeclaration node) {
    	Type t = node.symbol().type();
    	Type v = new VoidType();
    	if(t.equivalent(new BoolType()) || t.equivalent(new IntType()) || t.equivalent(new FloatType()))
	   		put(node, v);
	   	else
	  		put(node,new ErrorType("Variable " + node.symbol().name() + " has invalid type " + t + "."));
}

    @Override
    public void visit(ArrayDeclaration node) {
    	Symbol sym = node.symbol();
    	Type t = sym.type();
    	while(t instanceof ArrayType)
    		t = ((ArrayType) t).base();
    	if(t.equivalent(new BoolType()) || t.equivalent(new IntType()) || t.equivalent(new FloatType()))
    		put(node, v);
    	else
    		put(node,new ErrorType("Array " + node.symbol().name() + " has invalid base type " + t + "."));
    	
    	put(node, v);
    }

    public boolean isReturn(Command stmts)
    {
    	boolean isReturn = false;
    	if(stmts instanceof Return)
        	isReturn = true;
        else if(stmts instanceof StatementList)
        {
        	for(Statement stmt : (StatementList) stmts)
        	{
        		isReturn = isReturn || isReturn((Command) stmt);
        	}
        }
        else if(stmts instanceof IfElseBranch)
        {
        	isReturn = isReturn(((IfElseBranch) stmts).thenBlock()) && isReturn(((IfElseBranch) stmts).elseBlock());
        }
    	return isReturn;
    }
    @Override
    public void visit(FunctionDefinition node) {
        put(node, v);
        Symbol func = node.function();
        Symbol prevSym = curFunc;
        curFunc = func;
        
        Type fType = func.type();
        List<Symbol> argList = node.arguments();
        Type retType = ((FuncType)fType).returnType();
        Type signature = new FuncType(new TypeList(), new VoidType());
        int p = 0;
        if(func.name().equals("main") && !signature.equivalent(fType))
        {
        	put(node,new ErrorType("Function main has invalid signature."));
        } 
        else
        {
        	for(Symbol s : argList)
	        {
	        	Type t = s.type();
	        	if(t instanceof VoidType)
	        		put(node,new ErrorType("Function " + curFunc.name() 
	        		+ " has a void argument in position " + p + "."));
	        	else if(t instanceof ErrorType)
	        	{
	        		put(node,new ErrorType("Function " + curFunc.name() 
	        		+ " has an error in argument in position " + p + ": " + ((ErrorType)t).getMessage()));
	        	}
	        	p++;
	        }
        }
        node.body().accept(this);
        if(!(retType instanceof VoidType) && !isReturn(node.body()))
        	put(node,new ErrorType("Not all paths in function " + curFunc.name() + " have a return."));
        else
        	put(node, retType);
        curFunc = prevSym;
    }

    @Override
    public void visit(Comparison node) {
        node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.compare(right));
    }
    
    @Override
    public void visit(Addition node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.add(right));
    }
    
    @Override
    public void visit(Subtraction node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.sub(right));
    }
    
    @Override
    public void visit(Multiplication node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.mul(right));
    }
    
    @Override
    public void visit(Division node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.div(right));
    }
    
    @Override
    public void visit(LogicalAnd node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.and(right));
    }

    @Override
    public void visit(LogicalOr node) {
    	node.leftSide().accept(this);
        Type left = typeMap.get(node.leftSide());
        node.rightSide().accept(this);
        Type right = typeMap.get(node.rightSide());
        put(node, left.or(right));
    }

    @Override
    public void visit(LogicalNot node) {
    	node.expression().accept(this);
        Type expr = typeMap.get(node.expression());
        put(node, expr.not());
    }
    
    @Override
    public void visit(Dereference node) {
    	node.expression().accept(this);
        Type expr = typeMap.get(node.expression());
        put(node, expr.deref());
    }

    @Override
    public void visit(Index node) {
    	node.base().accept(this);
        Type base = typeMap.get(node.base());
        node.amount().accept(this);
        Type amount = typeMap.get(node.amount());
        put(node, base.index(amount));
    }

    @Override
    public void visit(Assignment node) {
        node.source().accept(this);
        Type source = typeMap.get(node.source());
        node.destination().accept(this);
        Type des = typeMap.get(node.destination());
        put(node, des.assign(source));
    }

    @Override
    public void visit(Call node) {
        Type func = node.function().type();
        node.arguments().accept(this);
        Type args = typeMap.get(node.arguments());
        put(node, func.call(args));
    }

    @Override
    public void visit(IfElseBranch node) {
        node.condition().accept(this);
        Type con = typeMap.get(node.condition());
        if(con instanceof BoolType)
        	put(node, v);
        else
        	put(node,new ErrorType("IfElseBranch requires bool condition not " + con + "."));
        node.thenBlock().accept(this);
        node.elseBlock().accept(this);
    }

    @Override
    public void visit(WhileLoop node) {
    	node.condition().accept(this);
    	Type con = typeMap.get(node.condition());
    	if(!(con instanceof BoolType))
    		put(node, new ErrorType("WhileLoop requires bool condition not " + con + "."));
    	else
    		put(node, v);
    	node.body().accept(this);
    }

    @Override
    public void visit(Return node) {
    	node.argument().accept(this);
    	Type arg = typeMap.get(node.argument());
    	Type retType = ((FuncType)curFunc.type()).returnType();
    	if(arg.equivalent(retType))
    		put(node, v);
    	else
    		put(node, new ErrorType("Function " + curFunc.name() + " returns " + retType + " not " + arg + "."));
    		
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
