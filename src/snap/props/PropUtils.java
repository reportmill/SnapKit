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
