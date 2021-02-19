package snap.util;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for formatting numbers and dates.
 */
public class FormatUtils {

    // A map of known Decimal formats for pattern
    private static Map<String,DecimalFormat> _formats = new HashMap<>();

    /**
     * Formats a number with given Decimal format pattern.
     */
    public static String formatNum(String aPattern, Object aNum)
    {
        // Get format and string
        DecimalFormat fmt = getDecimalFormat(aPattern);
        String str = fmt.format(aNum);

        // TeaVM seem to have issues with: #.## and .977757 ?
        if (SnapUtils.isTeaVM) {
            if (str.indexOf(':')>=0) {
                System.out.println("StringUtils.formatNum: TeaVM formatted: " + aNum + " to " + str + " for " + aPattern);
                return String.valueOf(aNum);
            }
        }

        // Return string
        return str;
    }

    /**
     * Returns a decimal format for given pattern.
     */
    public static DecimalFormat getDecimalFormat(String aPattern)
    {
        DecimalFormat fmt = _formats.get(aPattern);
        if (fmt==null)
            _formats.put(aPattern, fmt = new DecimalFormat(aPattern));
        return fmt;
    }
}
