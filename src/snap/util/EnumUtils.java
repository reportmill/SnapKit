package snap.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
        throw new IllegalArgumentException("No enum const " + enumType + "." + aName);
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
        for (int i = 0; i < enums.length; i++)
            names[i] = StringUtils.fromCamelCase(enums[i].toString());
        return names;
    }

    /**
     * Returns a string with names separated by comma for array of enums.
     */
    public static String getNamesStringFromEnumArray(Enum[] anEnumArray)
    {
        String str = null;
        for (Enum e : anEnumArray) {
            if (str == null)
                str = e.toString();
            else str += "," + e.toString();
        }
        return str != null ? str : "";
    }

    /**
     * Returns an enum array for a string of enum names separated by commas.
     */
    public static <T extends Enum<T>> T[] getEnumArrayFromNamesString(Class<T> enumType, String aName)
    {
        // Decode enum names into list
        String[] strings = aName.split(",");
        List<Enum> enumsList = new ArrayList<>();
        for (String str : strings) {
            str = str.trim();
            Enum e = valueOfIC(enumType, str);
            if (e != null)
                enumsList.add(e);
        }

        // Get enum array for enum list and return
        T[] enumArray = (T[]) Array.newInstance(enumType, 0);
        enumArray = enumsList.toArray(enumArray);
        return enumArray;
    }
}