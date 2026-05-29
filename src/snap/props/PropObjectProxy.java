/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class is meant to stand in for archival objects that aren't PropObject.
 */
public abstract class PropObjectProxy<T> extends PropObject {

    // The Real class
    protected T  _real;

    /**
     * Constructor.
     */
    public PropObjectProxy()
    {
        super();
    }

    /**
     * Returns the real object.
     */
    public T getReal()
    {
        if (_real != null) return _real;
        return _real = getRealImpl();
    }

    /**
     * Returns the real object.
     */
    protected T getRealImpl()  { return null; }

    /**
     * Sets the real object.
     */
    public void setReal(T aReal)
    {
        _real = aReal;
    }

    /**
     * Override to forward to Real if set.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // If Real is set and is PropObject, use its props (should probably add copies).
        if (_real instanceof PropObject realPropObj) {
            Prop[] dataSetProps = realPropObj.getPropSet().getProps();
            for (Prop prop : dataSetProps)
                aPropSet.addProp(prop);
        }

        // Do normal version
        else super.initProps(aPropSet);
    }

    /**
     * Override to forward to Real.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // If Real is set and is PropObject, forward on.
        if (_real instanceof PropObject realPropObj)
            return realPropObj.getPropValue(aPropName);

        // Do normal version
        return super.getPropValue(aPropName);
    }
}
