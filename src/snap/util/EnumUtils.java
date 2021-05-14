package snap.util;

/**
 * Utility methods for Enums.
 */
public class EnumUtils {

    /**
     * Returns an enum for enum class and string, ignoring case.
     */
    public static <T extends Enum<T>> T valueOfIC(Class<T> enumType, String aName)
    {
        // This is a non-null method
        if (aName == null)
            throw new NullPointerException("Name is null");

        // Iterate over enums and return any that match
        for (T value : enumType.getEnumConstants())
            if (value.toString().equalsIgnoreCase(aName))
                return value;

        // Throw exception
        throw new IllegalArgumentException("No enum const " + enumType +"." + aName);
    }

    /**
     * Returns an array of enum name strings adding spaces between camel case chars.
     */
    public static <T extends Enum<T>> String[] getNamesFromCamelCaseEnumClass(Class<T> enumClass)
    {
        // Get array of enum instances, create names array
        T[] enums = enumClass.getEnumConstants();
        String[] names = new String[enums.length];

        // Iterate over enums, get/set names, return
        for (int i=0; i<enums.length; i++)
            names[i] = StringUtils.fromCamelCase(enums[i].toString());
        return names;
    }
}
