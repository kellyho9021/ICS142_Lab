package crux;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
   	
	public static String studentName = "Kelly Ho";
    public static String studentID = "81482302";
    public static String uciNetID = "doankhah";
    private SymbolTable parent;
	private int depth;
	private Map<String, Symbol> table;
	
    public SymbolTable()
    {
        this.table = new LinkedHashMap<String, Symbol>();
        this.parent = null;
        this.depth = 0;
    }
    
    public SymbolTable(SymbolTable symbolTable) {
		// TODO Auto-generated constructor stub
    	this.table = new LinkedHashMap<String, Symbol>();
        this.parent = symbolTable;
        this.depth = parent.depth + 1;
	}

	public Symbol lookup(String name) throws SymbolNotFoundError
    {
        if(parent != null)
        {	
        	if(table.get(name) == null)
        		return parent.lookup(name);
        }
        else 
        {
        	if(table.get(name) == null)
        		throw new SymbolNotFoundError(name);
        }
        return table.get(name);
    }
       
    public Symbol insert(String name) throws RedeclarationError
    {
        if(table.get(name) != null)
        	throw new RedeclarationError(table.get(name));
        Symbol sym = new Symbol(name);
        table.put(name, sym);
        return sym;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (parent != null)
            sb.append(parent.toString());
        
        String indent = new String();
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        
        for (Symbol s : table.values())
        {
            sb.append(indent + s.toString() + "\n");
        }
        return sb.toString();
    }

	public SymbolTable parentTable() {
		// TODO Auto-generated method stub
		return parent;
	}
}

class SymbolNotFoundError extends Error
{
    private static final long serialVersionUID = 1L;
    private String name;
    
    SymbolNotFoundError(String name)
    {
        this.name = name;
    }
    
    public String name()
    {
        return name;
    }
}

class RedeclarationError extends Error
{
    private static final long serialVersionUID = 1L;

    public RedeclarationError(Symbol sym)
    {
        super("Symbol " + sym + " being redeclared.");
    }
}
