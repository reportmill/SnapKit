/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.text.DecimalFormat;

/**
 * This class holds utility methods for Props.
 */
public class PropUtils {

    // A formatter to format double without exponent
    private static DecimalFormat _doubleFmt = new DecimalFormat("0.#########");

    /**
     * Returns whether given PropNode needs class declaration when referenced via given prop.
     */
    public static boolean isNodeNeedsClassDeclarationForProp(PropNode propNode, Prop prop)
    {
        // If no prop, return false (assume class defined by element name or reference key)
        if (prop == null)
            return false;

        // If Prop.PropClassConstant, return false (archiver can determine class name from prop Name or PropClass)
        if (prop.isPropClassConstant())
            return false;

        // Get Prop.DefaultPropClass (if Prop.isArray, use component class)
        Class<?> defaultPropClass = prop.getDefaultPropClass();
        if (prop.isArray() && defaultPropClass.isArray())
            defaultPropClass = defaultPropClass.getComponentType();

        // Get PropObject.Class
        PropObject propObject = propNode.getPropObject();
        Class<?> propObjectClass = propObject.getClass();
        if (propObject instanceof PropObjectProxy)
            propObjectClass = ((PropObjectProxy<?>) propObject).getReal().getClass();

        // If PropObject.Class matches Prop.DefaultPropClass, return false
        if (propObjectClass == defaultPropClass)
            return false;

        // If class name matches prop name, return false (assume class will come from prop key reference)
        String propObjectClassName = propObjectClass.getSimpleName();
        if (propObjectClassName.equals(prop.getName()))
            return false;

        // Return true
        return true;
    }

    /**
     * Return string for double array.
     */
    public static String getStringForDoubleArray(double[] theValues)
    {
        // If empty, return empty array string
        if (theValues.length == 0) return "[ ]";

        // Create string with open bracket and first val
        StringBuilder sb = new StringBuilder("[ ");
        sb.append(_doubleFmt.format(theValues[0]));

        // Iterate over remaining vals and add separator plus val for each
        for (int i = 1; i < theValues.length; i++)
            sb.append(", ").append(_doubleFmt.format(theValues[i]));

        // Return string with close bracket
        return sb.append(" ]").toString();
    }
}
