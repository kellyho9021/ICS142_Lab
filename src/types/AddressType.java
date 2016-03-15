package types;

public class AddressType extends Type {
    
    private Type base;
    
    public AddressType(Type base)
    {
        this.base = base;
    }
    
    public Type base()
    {
        return base;
    }

    @Override
    public String toString()
    {
        return "Address(" + base + ")";
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;
        
        AddressType aType = (AddressType)that;
        return this.base.equivalent(aType.base);
    }
    
    @Override
    public Type add(Type that)
    {
        return base.add(that);
    }
    
    @Override
    public Type sub(Type that)
    {
        return base.sub(that);
    }
    
    @Override
    public Type mul(Type that)
    {
        return base.mul(that);
    }
    
    @Override
    public Type div(Type that)
    {
        return base.div(that);
    }
    
    @Override
    public Type and(Type that)
    {
        return base.and(that);
    }
    
    @Override
    public Type or(Type that)
    {
        return base.or(that);
    }
    
    @Override
    public Type not()
    {
        return base.not();
    }
    
    @Override
    public Type compare(Type that)
    {
        return base.compare(that);
    }
    
    @Override
    public Type deref()
    {
        return base;
    }
    
    @Override
    public Type index(Type that)
    {
    	Type indx = super.index(that);;
    	if(base instanceof ArrayType)
    		indx = new AddressType(base.index(that));
        return indx;
    }
    
    @Override
    public Type call(Type args)
    {
        return base.call(args);
    }
    
    @Override
    public Type assign(Type source)
    {
		Type assignType = base.assign(source);
		if(!base.equivalent(source))
			assignType = super.assign(source);
        return assignType;
    }
}
