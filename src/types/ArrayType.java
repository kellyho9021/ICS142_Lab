package types;

import crux.Symbol;

public class ArrayType extends Type {
    
    private Type base;
    private int extent;
    
    public ArrayType(int extent, Type base)
    {
        this.extent = extent;
        this.base = base;
    }
    
    public int extent()
    {
        return extent;
    }
    
    public Type base()
    {
        return base;
    }
    
    @Override
    public String toString()
    {
        return "array[" + extent + "," + base + "]";
    }
    
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        
        ArrayType aType = (ArrayType)that;
        return this.extent == aType.extent && base.equivalent(aType.base);
    }
    
    @Override
    public Type index(Type that)
    {
    	Type intType = new IntType();
    	Type indx = super.index(that);
    	if(that.equivalent(intType))
    		indx = base;
        return indx;
    }
    
    @Override
    public Type assign(Type source)
    {
        return base.assign(source);
    }
    
    public Type baseType(Symbol sym)
    {
    	Type result = base.baseType(sym);
    	return result;
    }
}
