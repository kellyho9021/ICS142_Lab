package types;

public class FloatType extends Type {
    
    public FloatType()
    {
    }
    
    @Override
    public String toString()
    {
        return "float";
    }
       
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof FloatType))
            return false;
        return true;
    }
    
    @Override
    public Type add(Type that)
    {
    	if(that instanceof FloatType)
    		return new FloatType();
    	return super.add(that);
    }
    
    @Override
    public Type sub(Type that)
    {
    	if(that instanceof FloatType)
    		return new FloatType();
    	else
        	return super.sub(that);
    }
    
    @Override
    public Type mul(Type that)
    {
    	if(that instanceof FloatType)
    		return new FloatType();
    	else
    		return super.mul(that);
    }
    
    @Override
    public Type div(Type that)
    {
    	if(that instanceof FloatType)
    		return new FloatType();
    	return super.div(that);
    }
    
    @Override
    public Type compare(Type that)
    {
    	if(that instanceof FloatType)
    		return new BoolType();
    	return super.compare(that);
    }
    
    @Override
    public Type assign(Type source)
    {
    	if(equivalent(source))
    		return new VoidType();
    	return super.assign(source);
    }
}
