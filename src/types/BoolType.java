package types;

public class BoolType extends Type {
    
    public BoolType()
    {
    }
    
    @Override
    public String toString()
    {
        return "bool";
    }

    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof BoolType))
            return false;
        
        return true;
    }
    
    @Override
    public Type and(Type that)
    {
    	if(that instanceof BoolType)
    		return new BoolType();
    	return super.and(that);
    }
    
    @Override
    public Type or(Type that)
    {
    	if(that instanceof BoolType)
    		return new BoolType();
    	return super.or(that);
    }
    
    @Override
    public Type not()
    {
        return new BoolType();
    }
    
    @Override
    public Type assign(Type source)
    {
    	if(equivalent(source))
    		return new VoidType();
    	return super.assign(source);
    }
}    