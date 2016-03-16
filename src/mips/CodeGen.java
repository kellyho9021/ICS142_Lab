package mips;

import java.util.regex.Pattern;

import ast.*;
import types.*;

public class CodeGen implements ast.CommandVisitor {
    
    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private String retFunc;
    private ActivationRecord currentFunction;

    public CodeGen(TypeChecker tc)
    {
        this.tc = tc;
        this.program = new Program();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    public boolean generate(Command ast)
    {
        try {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);
            return !hasError();
        } catch (CodeGenException e) {
            return false;
        }
    }
    
    public Program getProgram()
    {
        return program;
    }

    @Override
    public void visit(ExpressionList node) {
        for(Expression expr : node)
        	expr.accept(this);
    }

    @Override
    public void visit(DeclarationList node) {
        for(Declaration dec : node)
        	dec.accept(this);
    }

    @Override
    public void visit(StatementList node) {
        for(Statement stat : node)
        {
        	stat.accept(this);
        	if(stat instanceof Call)
        	{
        		Call cNode = (Call) stat;
        		if(!(tc.getType(cNode).equals(new VoidType())))
        			if(tc.getType(cNode).equivalent(new FloatType()))
        				program.popFloat("$t0");
        			else if(tc.getType(cNode).equivalent(new IntType()) || tc.getType(cNode).equivalent(new BoolType()))
        				program.popInt("$t0");
        	}
        }
    }

    @Override
    public void visit(AddressOf node) {
        currentFunction.getAddress(program, "$t0", node.symbol());
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralBool node) {
    	String val = node.value().toString();
        if(val == "TRUE")
        
        	program.appendInstruction("li $t0, " + 1);
        else
        	program.appendInstruction("li $t0, " + 0);        	
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralFloat node) {
        program.appendInstruction("li.s $f0, " + node.value());
        program.pushFloat("$f0");
    }

    @Override
    public void visit(LiteralInt node) {
    	 program.appendInstruction("li $t0, " + node.value());
         program.pushInt("$t0");
    }

    @Override
    public void visit(VariableDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(ArrayDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(FunctionDefinition node) {
        String fName = node.function().name();
        retFunc = program.newLabel();
        if(!fName.equals("main"))
        	fName = "cruxfunc." + node.function().name();
        currentFunction = new ActivationRecord(node, currentFunction);
        int start = program.appendInstruction(fName + ":");
        node.body().accept(this);
        program.insertPrologue(start + 1, currentFunction.stackSize());
        program.appendInstruction(retFunc + ":");
        if(!(tc.getType(node).equivalent(new VoidType())))
        	if(tc.getType(node).equivalent(new FloatType()))
        		program.popFloat("$v0");
        	else if(tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType()))
        		program.popInt("$v0");
        program.appendEpilogue(currentFunction.stackSize(), fName.equals("main"));
        currentFunction = currentFunction.parent();
    }

    @Override
    public void visit(Addition node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        if(tc.getType(node).equivalent(new FloatType()))
        {
        	program.popFloat("$f2");
        	program.popFloat("$f0");
        	program.appendInstruction("add.s $f4, $f0, $f2");
        	program.pushFloat("$f4");
        }
        else if(tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("add $t2, $t0, $t1");
        	program.pushInt("$t2");
        }
    }

    @Override
    public void visit(Subtraction node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        if(tc.getType(node).equivalent(new FloatType()))
        {
        	program.popFloat("$f2");
        	program.popFloat("$f0");
        	program.appendInstruction("sub.s $f4, $f0, $f2");
        	program.pushFloat("$f4");
        }
        else if(tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("sub $t3, $t0, $t1");
        	program.pushInt("$t3");
        }
    }

    @Override
    public void visit(Multiplication node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        if(tc.getType(node).equivalent(new FloatType()))
        {
        	program.popFloat("$f2");
        	program.popFloat("$f0");
        	program.appendInstruction("mul.s $f4, $f0, $f2");
        	program.pushFloat("$f4");
        }
        else if(tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("mul $t3, $t0, $t1");
        	program.pushInt("$t3");
        }
    }

    @Override
    public void visit(Division node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        if(tc.getType(node).equivalent(new FloatType()))
        {
        	program.popFloat("$f2");
        	program.popFloat("$f0");
        	program.appendInstruction("div.s $f4, $f0, $f2");
        	program.pushFloat("$f4");
        }
        else if(tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("div $t3, $t0, $t1");
        	program.pushInt("$t3");
        }
    }

    @Override
    public void visit(LogicalAnd node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        program.popInt("$t1");
    	program.popInt("$t0");
    	program.appendInstruction("and $t2, $t0, $t1");
    	program.pushInt("$t2");
    }

    @Override
    public void visit(LogicalOr node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        program.popInt("$t1");
    	program.popInt("$t0");
    	program.appendInstruction("or $t2, $t0, $t1");
    	program.pushInt("$t2");
    }
    
    @Override
    public void visit(LogicalNot node) {
    	String falseLabel = program.newLabel();
    	String pushLabel = program.newLabel();
    	node.expression().accept(this);
    	program.popInt("$t0");
    	program.appendInstruction("beqz $t0, " + falseLabel);
    	program.appendInstruction("li $t1, 0");
    	program.appendInstruction("b " + pushLabel);
    	program.appendInstruction(falseLabel + ":");
    	program.appendInstruction("li $t1, 1");
    	program.appendInstruction(pushLabel + ":");
    	program.pushInt("$t1");
    }

    @Override
    public void visit(Comparison node) {
		node.leftSide().accept(this);
		node.rightSide().accept(this);
        
        if (tc.getType((Command)node.leftSide()).equivalent(new FloatType())) {
			program.popFloat("$f2");
			program.popFloat("$f0");
			String eName = node.operation().toString();
			for(ast.Comparison.Operation num : ast.Comparison.Operation.values()) {
				if(num.toString() == eName)
					{
						program.appendInstruction("c." + eName.toLowerCase() + ".s $f0, $f2");
						pushCond();
					}
			}
        } else if (tc.getType((Command)node.leftSide()).equivalent(new IntType()) || tc.getType((Command)node.leftSide()).equivalent(new BoolType())) {
			program.popInt("$t1");
			program.popInt("$t0");
			String eName = node.operation().toString();
			for(ast.Comparison.Operation num : ast.Comparison.Operation.values()) {
				if(num.toString() == eName)
					program.appendInstruction("s" + eName.toLowerCase() + " $t2, $t0, $t1");
			}
			program.pushInt("$t2");
        }
    }

	private void pushCond() {
		String eLabel = program.newLabel();
		String jLabel = program.newLabel();

		program.appendInstruction("bc1f " + eLabel);
		program.appendInstruction("li $t0, 1");
		program.appendInstruction("b " + jLabel);
		program.appendInstruction(eLabel + ":");
		program.appendInstruction("li $t0, 0");
		program.appendInstruction(jLabel + ":");

		program.pushInt("$t0");
	}

    @Override
    public void visit(Dereference node) {
        node.expression().accept(this);
        program.popInt("$t0"); // Contains address to type ,/
        if (tc.getType(node).equivalent(new FloatType())) {
			program.appendInstruction("lwc1 $f0, 0($t0)");
			program.pushFloat("$f0");
        } else if (tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType())) {
			program.appendInstruction("lw $t1, 0($t0)");
			program.pushInt("$t1");
        } else {
        	throw new CodeGenException("Bad type in deref?.");
        }
    }

    @Override
    public void visit(Index node) {
        node.base().accept(this);
        node.amount().accept(this);
        program.popInt("$t0");
        program.popInt("$t1");
        ActivationRecord a = new ActivationRecord();
		program.appendInstruction("li $t2, " + a.numBytes(tc.getType(node)));
		program.appendInstruction("mul $t3, $t0, $t2");
		program.appendInstruction("add $t4, $t1, $t3");
		program.pushInt("$t4");
    }

	@Override
    public void visit(Assignment node) {
    	node.destination().accept(this);
    	node.source().accept(this);
    	
        if (tc.getType(node).equivalent(new FloatType())) {
        	program.popFloat("$f0");
    		program.popInt("$t0");
        	program.appendInstruction("swc1 $f0, 0($t0)");
        } else if (tc.getType(node).equivalent(new IntType()) || tc.getType(node).equivalent(new BoolType())){
    		program.popInt("$t0");
        	program.popInt("$t1"); 
        	program.appendInstruction("sw $t0, 0($t1)");
        } else {
        	throw new CodeGenException("Weird type \"" + tc.getType(node) + "\" in assignment.");
        }
    }

    @Override
    public void visit(Call node) {
		node.arguments().accept(this);
		ActivationRecord a = new ActivationRecord();
        String funcName =  node.function().name();
        if (funcName.matches("print(Bool|Float|Int|ln)|read(Float|Int)")) {
        	funcName = "func." + funcName;
        } else {
        	funcName = "cruxfunc." + funcName;
        }
        program.appendInstruction("jal " + funcName);

        int size = node.arguments().size();
        if (size > 0) {
        	int argSize = 0;
        	for (Expression expr : node.arguments()) 
        		argSize += a.numBytes(tc.getType((Command) expr));
			program.appendInstruction("addi $sp, $sp, " + argSize);
        }

		FuncType func = (FuncType) node.function().type();
 		if (!func.returnType().equivalent(new VoidType())) {
			program.appendInstruction("subu $sp, $sp, 4");
			program.appendInstruction("sw $v0, 0($sp)");
 		}

    }

    @Override
    public void visit(IfElseBranch node) {
		String elseLabel = program.newLabel();
		String joinLabel = program.newLabel();

        node.condition().accept(this);
    	program.popInt("$t7");
    	program.appendInstruction("beqz $t7, " + elseLabel);

		node.thenBlock().accept(this);
		program.appendInstruction("b " + joinLabel);
    	program.appendInstruction(elseLabel + ":");
		node.elseBlock().accept(this);

    	program.appendInstruction(joinLabel + ":");
    }

    @Override
    public void visit(WhileLoop node) {
		String condLabel = program.newLabel();
		String joinLabel = program.newLabel();

    	program.appendInstruction(condLabel + ":");
        node.condition().accept(this);
    	program.popInt("$t7");
    	program.appendInstruction("beqz $t7, " + joinLabel);

    	node.body().accept(this);
    	program.appendInstruction("b " + condLabel);
    	program.appendInstruction(joinLabel + ":");
    }

    @Override
    public void visit(Return node) {
    	node.argument().accept(this);
    	program.appendInstruction("b " + retFunc);
    }
    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }
}
