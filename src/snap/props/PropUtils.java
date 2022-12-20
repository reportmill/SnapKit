/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class holds utility methods for Props.
 */
public class PropUtils {

    /**
     * Returns whether given PropNode needs class declaration when referenced via given prop.
     */
    public static boolean isClassDeclarationNeededForObjectAndProp(PropObject propObject, Prop prop)
    {
        // If no prop, return true
        if (prop == null)
            return true;

        // If Prop.PropClassConstant, return false (archiver can determine class name from prop Name or PropClass)
        if (prop.isPropClassConstant())
            return false;

        // Get Prop.DefaultPropClass (if Prop.isArray, use component class)
        Class<?> defaultPropClass = prop.getDefaultPropClass();
        if (defaultPropClass != null && defaultPropClass.isArray() && prop.isArray())
            defaultPropClass = defaultPropClass.getComponentType();

        // Get PropObject.Class
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
}
