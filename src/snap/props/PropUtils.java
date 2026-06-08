/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class holds utility methods for Props.
 */
public class PropUtils {

    /**
     * Returns the props string for given prop object.
     */
    public static String getPropsString(PropObject propObject)
    {
        return getPropsString(propObject, "; ", ": ");
    }

    /**
     * Returns the props string for given prop object.
     */
    public static String getPropsString(PropObject propObject, String propSep, String valSep)
    {
        StringBuilder sb = new StringBuilder();

        // Iterate over props and append string if changed
        for (Prop prop : propObject.getPropSet().getArchivalProps()) {
            if (prop.isRelation() || prop.isArray() || propObject.isPropDefault(prop.getName()))
                continue;
            Object propValue = propObject.getPropValue(prop.getName());
            String propValueStr = StringCodec.SHARED.codeString(propValue);
            if (!sb.isEmpty())
                sb.append(propSep);
            sb.append(prop.getName()).append(valSep).append(propValueStr);
        }

        // Return string
        return sb.toString();
    }

    /**
     * Sets prop values in given prop object for given JSON/CSS style string, e.g.: "Name: AgeText; Margin: 4; PrefWidth: 24;"
     */
    public static void setPropsString(PropObject propObject, String propsString)
    {
        // Get individual prop/value strings (separated by semi-colons)
        String[] propStrings = propsString.split("\\s*;\\s*");
        PropSet propSet = propObject.getPropSet();

        // Iterate over prop strings and add each
        for (String propString : propStrings) {

            // Get "name:value" string parts
            String[] nameValueStrings = propString.split("\\s*:\\s*");

            // If both prop/value parts found, get prop name and set value
            if (nameValueStrings.length == 2) {
                String propName = nameValueStrings[0].trim();
                Prop prop = propSet.getPropForName(propName);
                if (prop != null)
                    propObject.setPropValue(prop.getName(), nameValueStrings[1]);

                    // If prop not found for name, complain
                else System.err.println("PropUtils.setPropsString: Unknown prop name: " + propName);
            }

            // If "name:value" parts not found, complain
            else System.err.println("PropUtils.setPropsString: Invalid prop string: " + propString);
        }
    }

    /**
     * Returns whether given PropMap needs class declaration when referenced via given prop.
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
