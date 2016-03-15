package types;

import java.util.HashMap;
import java.util.List;

import ast.*;
import crux.Symbol;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;
    private Symbol currentFunction = Symbol.newError("There is no return type");
    private boolean needsReturn;

    private int nbrFoundReturns;
	private Symbol curFuncSym;
	private Type curFuncRetType;
	
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
        //throw new RuntimeException("Implement this");
    	TypeList typL = new TypeList();
    	for(Expression i : node)
    	{
    		i.accept(this);
    		typL.append(getType((Command)i));
    	}
    	put(node,typL);
    }
    

	@Override
    public void visit(DeclarationList node) {
		TypeList typL = new TypeList();
		for(Declaration i : node)
		{
			i.accept(this);
    		typL.append(typeMap.get(i));
    	}
    	put(node,typL);
    }

    @Override
    public void visit(StatementList node) {
//    	TypeList typL = new TypeList();
//    	for(Statement i : node)
//    	{
//			i.accept(this);
//    		typL.append(typeMap.get(i));
//    	}
//    	put(node,typL);
        needsReturn = true;
        boolean foundReturn = false;
        for (Statement stmt : node) {
        	stmt.accept(this);
        	if (!needsReturn) {
        		foundReturn = true;
        	}
        }
        needsReturn = !foundReturn;
    }

    @Override
    public void visit(AddressOf node) {
//    	Type temp = new AddressType(node.symbol().type());
//    	put(node, temp);
        Type type = node.symbol().type();
		put(node, new AddressType(type));    	
    }
  
    @Override
    public void visit(LiteralBool node) {
    	Type temp = new BoolType();
    	put(node, temp);
    }

    @Override
    public void visit(LiteralFloat node) {
    	Type temp = new FloatType();
    	put(node, temp);
    }

    @Override
    public void visit(LiteralInt node) {
    	Type temp = new IntType();
    	put(node, temp);
    }
    
    public boolean returnSelect(Type t)
    {
    	return (t.equivalent(new BoolType()) || t.equivalent(new IntType()) || t.equivalent(new FloatType()));
    }
    
    @Override
    public void visit(VariableDeclaration node) {
//    	Symbol temp = node.symbol();
//    	Type varType = temp.type();
//    	boolean re = returnSelect(varType);
//    	if(!re)
//    		reportError(node.lineNumber(), node.charPosition(), "Variable " + temp.name() + " has invalid type " + varType + ".");
//    	else
//    		put(node, new VoidType());
    	Symbol symbol = node.symbol();
    	Type varType = symbol.type();
        put(node, varType.declare(symbol));    		
    }

    @Override
    public void visit(ArrayDeclaration node) {
//    	Symbol temp = node.symbol();
//    	Type type = temp.type();
//    	while(type instanceof ArrayType)
//    	{
//    		ArrayType at = (ArrayType) type;
//    		type = at.base();
//    	}
//    	boolean re = returnSelect(type);
//    	if(!re)
//    		reportError(node.lineNumber(), node.charPosition(), "Array " + temp.name() + " has invalid base type " + type + ".");
//    	else
//    		put(node, new VoidType());
		Symbol symbol = node.symbol();
		Type type = symbol.type();
		put(node, type.baseType(symbol));
    }
    
//    public Type returnType()
//    {
//    	FuncType funcType = (FuncType)currentFunction.type();
//    	Type reType = funcType.returnType();
//    	return reType;
//    }
    
    private boolean allReturn(Command node)
    {
    	boolean result = false;
    	if(node instanceof Return)
    		result = true;
    	else if(node instanceof StatementList)
    	{
    		StatementList sList = (StatementList) node;
    		for(Statement stm : sList)
    		{
    			result = result || allReturn((Command) stm);
    		}
    	}
    	else if(node instanceof IfElseBranch)
    	{
    		IfElseBranch ifElse = (IfElseBranch) node;
    		
    		result = allReturn(ifElse.thenBlock()) && allReturn(ifElse.elseBlock());
    	}
    	return result;
    }
    
    @Override
    public void visit(FunctionDefinition node) {
        Symbol func = node.function();
        List<Symbol> args = node.arguments();
        Type returnType = ((FuncType) func.type()).returnType();

        if (func.name().equals("main")) {
        	if (args.size() != 	0 || !(returnType instanceof VoidType)) {
				put(node, new ErrorType("Function main has invalid signature."));
				return;
			}
        } else {
        	int pos = 0;
        	for (Symbol arg : args) {
				Type argType = arg.type();
				if (argType instanceof ErrorType) {
					put(node, new ErrorType("Function " + func.name() + " has an error in argument in position " + pos + ": " + ((ErrorType) argType).getMessage()));
					return;
				} else if (argType instanceof VoidType) {
 	 	 	 		put(node, new ErrorType("Function " + func.name() + " has a void argument in position " + pos + "."));
					return;
				}
				++pos;
        	}
        }

		curFuncSym = func;
		curFuncRetType = returnType;
		nbrFoundReturns = 0;
        visit(node.body());
		if (!(returnType instanceof VoidType) && needsReturn) { 
        	put(node, new ErrorType("Not all paths in function " + func.name() + " have a return."));
		} else {
			put(node, returnType);
		}
    }


////////////////////////////
    @Override
    public void visit(Comparison node) {
    	
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left);
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = typeMap.get(right);
    	put(node, leftType.compare(rightType));
    }
    
    @Override
    public void visit(Addition node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left); 
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);
    	
    	put(node, leftType.add(rightType));
    
    }
    
    @Override
    public void visit(Subtraction node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left); 
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);
    	put(node, leftType.sub(rightType));
    }
    
    @Override
    public void visit(Multiplication node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left);
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);
    	put(node, leftType.mul(rightType));
    }
    
    @Override
    public void visit(Division node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left);
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);
    	put(node, leftType.div(rightType));
    }
    
    @Override
    public void visit(LogicalAnd node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left);
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);
    	put(node, leftType.and(rightType));
    }

    @Override
    public void visit(LogicalOr node) {
    	Expression left = node.leftSide(); 
    	left.accept(this);
    	Type leftType = getType((Command)left);
    	
    	Expression right = node.rightSide();
    	right.accept(this);
    	Type rightType = getType((Command)right);

    	put(node, leftType.or(rightType));
    }

    @Override
    public void visit(LogicalNot node) {
    	Expression exp = node.expression();
    	exp.accept(this);
    	Type expType = getType((Command)exp);
    	put(node, expType.not());
    }
    
    @Override
    public void visit(Dereference node) {
    	Expression exp = node.expression();
    	exp.accept(this);
    	Type expType = getType((Command)exp);
    	put(node, expType.deref());
    }

    @Override
    public void visit(Index node) {
    	Expression base = node.base();
    	base.accept(this);
    	Type baseType = getType((Command)base);
    	Expression amount = node.amount();
    	amount.accept(this);
    	Type amountType = getType((Command)amount);
    	put(node,baseType.index(amountType));
    }

    @Override
    public void visit(Assignment node) {
    	Expression source = node.source();
    	source.accept(this);
    	Type sourceType = getType((Command)source);
    	Expression destination = node.destination();
    	destination.accept(this);
    	Type desType = getType((Command)destination);
    	put(node, desType.assign(sourceType));
    }

    @Override
    public void visit(Call node) {
    	Symbol func = node.function();
    	Type funcType = func.type();
    	ExpressionList args = node.arguments();
    	args.accept(this);
    	Type argsType = getType((Command)args);
    	put(node, funcType.call(argsType));
    	
    }

    @Override
    public void visit(IfElseBranch node) {
    	Expression cond = node.condition();
    	cond.accept(this);
    	Type condType = getType((Command)cond);
    	if(condType instanceof BoolType)
    	{
    		put(node, new VoidType());
    	}
    	else
    	{
    		put(node, new ErrorType("IfElseBranch requires bool condition not " + condType + "."));
    	}
    	StatementList then = node.thenBlock();
    	then.accept(this);
    	StatementList el = node.elseBlock();
    	el.accept(this);
    	
    }

    @Override
    public void visit(WhileLoop node) {
    	Expression cond = node.condition();
    	cond.accept(this);
    	Type condType = getType((Command)cond);

    	if(condType instanceof BoolType)
    		put(node, new VoidType());
    	else
    		put(node,new ErrorType("WhileLoop requires bool condition not " + condType + "."));
		StatementList body = node.body();
		body.accept(this);
    	
    }

    @Override
    public void visit(Return node) {
    	Type retType = visitRetriveType(node.argument());
		++nbrFoundReturns;
		if (!retType.equivalent(curFuncRetType)) {
			put(node, new ErrorType("Function " + curFuncSym.name() + " returns " + curFuncRetType + " not " + retType + "."));
		} else {
			put(node, retType);
		}
        needsReturn = false;
    }
    
    private Type visitRetriveType(Visitable node) {
		node.accept(this);
		return getType((Command) node);
    }

	@Override
    public void visit(ast.Error node) {
    	Type error = new ErrorType(node.message());
        put(node, error);
    }
}
