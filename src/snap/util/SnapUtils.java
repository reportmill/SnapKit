/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import snap.gfx.GFXEnv;
import snap.web.*;

/**
 * This class provides general utility methods.
 */
public class SnapUtils {
    
    // The current platform
    private static Platform platform = getPlatform();

    // Whether app is currently running on Windows
    public static boolean isWindows = platform==Platform.WINDOWS;
    
    // Whether app is currently running on Mac
    public static boolean isMac = platform==Platform.MAC;
    
    // Whether app is currently running on TeaVM
    public static boolean isTeaVM = platform==Platform.TEAVM;
    
    // The build info string from "BuildInfo.txt" (eg, "Aug-31-04")
    private static String _buildInfo;
    
    // A map to track "print once" messages
    private static Map <String, Integer> _doOnceMap = new HashMap();
    
    // Constants for platform
    public enum Platform { WINDOWS, MAC, CHEERP, TEAVM, UNKNOWN };

    /**
     * Returns the current platform.
     */
    public static Platform getPlatform()
    {
        String name = System.getProperty("os.name"); if (name==null) name = "TeaVM";
        String vend = System.getProperty("java.vendor"); if (vend==null) vend = "TeaVM";
        if (name.indexOf("Windows") >= 0) return SnapUtils.Platform.WINDOWS;
        if (name.indexOf("Mac OS X") >= 0) return SnapUtils.Platform.MAC;
        if (vend.indexOf("Leaning") >= 0) return SnapUtils.Platform.CHEERP;
        if (name.indexOf("TeaVM") >= 0) return SnapUtils.Platform.TEAVM;
        return Platform.UNKNOWN;
    }

    /**
     * Returns a boolean value for the given object.
     */
    public static boolean boolValue(Object anObj)
    {
        // If Boolean, return bool value
        if (anObj instanceof Boolean) return ((Boolean)anObj).booleanValue();

        // If number, return true if number is non-zero
        else if (anObj instanceof Number) return !MathUtils.equalsZero(((Number)anObj).floatValue());

        // If string and "false", return false
        else if (anObj instanceof String && StringUtils.equalsIC((String)anObj, "false")) return false;

        // Other return true if object is non-null
        return anObj!=null;
    }

    /**
     * Returns the int value for a given object (assumed to be a string or number).
     */
    public static int intValue(Object anObj)  { return (int)longValue(anObj); }

    /**
     * Returns the int value for a given object (assumed to be a string or number).
     */
    public static long longValue(Object anObj)
    {
        if (anObj instanceof Number) return ((Number)anObj).longValue(); // If Number, return double value
        if (anObj instanceof String) return StringUtils.longValue((String)anObj); // If String, parse as double value
        return 0; // If anything else, return zero
    }

    /**
     * Returns the float value for a given object (assumed to be a string or number).
     */
    public static float floatValue(Object anObj)  { return (float)doubleValue(anObj); }

    /**
     * Returns the double value for a given object (assumed to be a string or number).
     */
    public static double doubleValue(Object anObj)
    {
        if (anObj instanceof Number) return ((Number)anObj).doubleValue(); // If Number, return double value
        if (anObj instanceof String) return StringUtils.doubleValue((String)anObj); // If String, parse as double
        return 0; // If anything else, return zero
    }

    /**
     * Returns a String for a given arbitrary object.
     */
    public static String stringValue(Object anObj)
    {
        // If object is null, return null
        if (anObj==null) return null;

        // If object is string, just return it
        if (anObj instanceof String)
            return (String) anObj;

        // If object is number, string format it
        if (anObj instanceof Number)
            return FormatUtils.formatNum( (Number) anObj);

        // If object is Date, date format it
        if (anObj instanceof Date)
            return FormatUtils.formatDate( (Date) anObj);

        // If byte array, format as base64
        if (anObj instanceof byte[]) {
            String s = ASCIICodec.encodeBase64((byte[])anObj);
            s = s.replace((char)0, ' ');
            return s;
        }

        // If class get standard name
        if (anObj instanceof Class)
            return ((Class)anObj).getName().replace('$', '.');

        // Return object's toString
        return anObj.toString();
    }

    /**
     * Returns the Boolean for a given object (assumed to be Number or String).
     */
    public static Boolean booleanValue(Object anObj)
    {
        if (anObj instanceof Boolean || anObj==null) return (Boolean)anObj; // If already boolean or null, just return it
        return Boolean.valueOf(boolValue(anObj)); // Otherwise return Boolean of boolValue
    }

    /**
     * Returns the Number for a given object (assumed to be Number or String).
     */
    public static Number numberValue(Object anObj)
    {
        // If already a number or null, just return it
        if (anObj instanceof Number || anObj==null) return (Number)anObj;

        // Try returning as BigDecimal  - can fail if is Nan or pos/neg infinity (returns as double)
        try { return getBigDecimal(anObj); }
        catch(Exception e) { return doubleValue(anObj); }
    }

    /**
     * Returns the Integer for a given object.
     */
    public static Integer getInteger(Object anObj)
    {
        if (anObj instanceof Integer || anObj==null) return (Integer)anObj; // If already Integer or null, just return it
        return intValue(anObj); // Otherwise, return new integer
    }

    /**
     * Returns a Float for a given object.
     */
    public static Float getFloat(Object anObj)
    {
        if (anObj instanceof Float || anObj==null) return (Float)anObj; // If already Float or null, just return it
        return floatValue(anObj); // Otherwise, return float
    }

    /**
     * Returns a Double for a given object.
     */
    public static Double getDouble(Object anObj)
    {
        if (anObj instanceof Double || anObj==null) return (Double)anObj; // If already Double or null, just return it
        return doubleValue(anObj); // Otherwise, return float
    }

    /**
     * Returns the BigDecimal for a given object (assumed to be a string or number).
     */
    public static BigDecimal getBigDecimal(Object anObj)
    {
        if (anObj instanceof BigDecimal || anObj==null) return (BigDecimal)anObj; // If already BigDecimal, just return it
        return new BigDecimal(doubleValue(anObj)); // If object is anything else, return big decimal of its double value
    }

    /**
     * Returns an enum for enum class and string, ignoring case.
     */
    public static <T extends Enum<T>> T valueOfIC(Class<T> enumType, String aName)
    {
        return EnumUtils.valueOfIC(enumType, aName);
    }

    /**
     * Returns a date for given object of arbitrary type.
     */
    public static Date getDate(Object anObj)
    {
        // If object is date or null, just return it
        if (anObj instanceof Date || anObj==null)
            return (Date)anObj;

        // If object is long, return date
        if (anObj instanceof Long)
            return new Date((Long)anObj);

        // Otherwise, try to parse string as simple date
        // Was this: return new java.text.SimpleDateFormat("MM/dd/yy").parse(anObj.toString());
        //try { return RMDateUtils.getDate(anObj.toString()); } catch(Exception e) { return null; }
        return DateParser.parseDate(anObj.toString());
    }

    /**
     * Returns a random integer between zero and given number.
     */
    public static int getRandomInt(int aValue)  { return MathUtils.randomInt()%aValue; }

    /**
     * Returns a random integer between zero and given number.
     */
    public static double getRandomDouble(double aValue)  { return MathUtils.randomFloat((float)aValue); }

    /**
     * Returns a clone of the given object (supports List, Map, Cloneable and null).
     * If object not cloneable, returns object.
     */
    public static <T> T clone(T anObj)
    {
        // Handle list
        if (anObj instanceof List)
            return (T)ListUtils.clone((List)anObj);

        // Handle map
        if (anObj instanceof Map)
            return (T)MapUtils.clone((Map)anObj);

        // Handle Cloneable: Invoke clone method with reflection
        if (anObj instanceof Cloneable)
            return (T)ClassUtils.cloneCloneable((Cloneable)anObj);

        // If all else fails, just return given object
        return anObj;
    }

    /**
     * Clones the given object, recursively, if the object is a collection.
     */
    public static <T> T cloneDeep(T anObj)
    {
        // Do normal clone
        T clone = clone(anObj);

        // If object is Map, duplicate entries and clone values
        if (clone instanceof Map) { Map map = (Map)clone;
            for (Map.Entry entry : (Set<Map.Entry>)map.entrySet())
                map.put(entry.getKey(), cloneDeep(entry.getValue()));
        }

        // If object is List, duplicate it's elements
        else if (clone instanceof List) { List list = (List)clone;
            for (int i=0, iMax=list.size(); i<iMax; i++) { Object item = list.get(i);
                list.set(i, cloneDeep(item)); }
        }

        // Return object
        return clone;
    }

    /**
     * Returns whether two objects are equal (supports either being null).
     */
    public static boolean equals(Object obj1, Object obj2)
    {
        return obj1==obj2 || (obj1!=null && obj2!=null && obj1.equals(obj2));
    }

    /**
     * Returns result of comparing two values.
     */
    public static int compare(Object anObj1, Object anObj2)
    {
        // If objects are same, return 0
        if (anObj1==anObj2) return 0;

        // If first is null return less than (-1), if second is null, return greater than (1)
        if (anObj1==null) return -1;
        if (anObj2==null) return 1;

        // If object is comparable and is same or super class, let it do the comparison (try both)
        if (anObj1 instanceof Comparable && anObj1.getClass().isInstance(anObj2))
            return ((Comparable)anObj1).compareTo(anObj2);
        if (anObj2 instanceof Comparable && anObj2.getClass().isInstance(anObj1))
            return -((Comparable)anObj2).compareTo(anObj1);

        // Compare big decimal values
        return getBigDecimal(anObj1).compareTo(getBigDecimal(anObj2));
    }

    /**
     * Returns the temp directory for this machine.
     */
    public static String getTempDir()
    {
        // Hack for TeaVM
        if (isTeaVM) return "/";

        // Get System property and make sure it ends with dir char
        String tdir = System.getProperty("java.io.tmpdir");
        if (!tdir.endsWith(java.io.File.separator))
            tdir += java.io.File.separator;
        return tdir;
    }

    /**
     * Returns a byte array from a File, String path, InputStream, URL, byte[], etc.
     */
    public static byte[] getBytes(Object aSource)
    {
        try { return getBytesOrThrow(aSource); }
        catch(Exception e) { return null; }
    }

    /**
     * Returns a byte array from a File, String path, InputStream, URL, byte[], etc.
     */
    public static byte[] getBytesOrThrow(Object aSource) throws IOException
    {
        // Handle byte array and InputStream
        if (aSource instanceof byte[]) return (byte[])aSource;
        if (aSource instanceof InputStream) return getBytesOrThrow((InputStream)aSource);

        // Handle File
        if (aSource instanceof File)
            return FileUtils.getBytesOrThrow((File)aSource);

        // Handle URL
        if (aSource instanceof URL)
            return URLUtils.getBytes((URL)aSource);

        // Handle WebFile
        if (aSource instanceof WebFile)
            return ((WebFile)aSource).getBytes();

        // Handle WebURL (URL, File, String path)
        WebURL url = WebURL.getURL(aSource);
        if (url!=null)
            return url.getBytesOrThrow();

        // Return null since bytes not found
        return null;
    }

    /**
     * Returns bytes for an input stream.
     */
    public static byte[] getBytes(InputStream aStream)
    {
        try { return getBytesOrThrow(aStream); }
        catch(IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns bytes for an input stream.
     */
    public static byte[] getBytesOrThrow(InputStream aStream) throws IOException
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte chunk[] = new byte[8192];
        for (int len=aStream.read(chunk, 0, chunk.length); len>0; len=aStream.read(chunk, 0, chunk.length))
            bs.write(chunk, 0, len);
        return bs.toByteArray();
    }

    /**
     * Returns text for a source.
     */
    public static String getText(Object aSource)
    {
        byte bytes[] = getBytes(aSource); if (bytes==null) return null;
        return StringUtils.getString(bytes);
    }

    /**
     * Returns bytes for a class and name/path.
     */
    public static byte[] getBytes(Class aClass, String aName)
    {
        WebURL url = WebURL.getURL(aClass, aName);
        return getBytes(url);
    }

    /**
     * Returns text string for a class and name/path.
     */
    public static String getText(Class aClass, String aName)
    {
        WebURL url = WebURL.getURL(aClass, aName);
        return getText(url);
    }

    /**
     * Returns an input stream from a File, String path, URL, byte array, InputStream, etc.
     */
    public static InputStream getInputStream(Object aSource)
    {
        // Handle byte array and InputStream
        if (aSource instanceof byte[]) return new ByteArrayInputStream((byte[])aSource);
        if (aSource instanceof InputStream) return (InputStream)aSource;

        // Handle WebFile
        if (aSource instanceof WebFile) return ((WebFile)aSource).getInputStream();

        // Handle WebURL (URL, File, String path)
        WebURL url = WebURL.getURL(aSource);
        if (url!=null) return url.getInputStream();

        // Complain and return null
        System.err.println("SnapUtils.getInputStream: Couldn't get stream for " + aSource);
        return null;
    }

    /**
     * Writes the given bytes to the given output object (string path or file).
     */
    public static void writeBytes(byte bytes[], Object aDest)
    {
        File file = FileUtils.getFile(aDest);
        try { FileUtils.writeBytes(file, bytes); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the hostname for this machine.
     */
    public static String getHostname()
    {
        return GFXEnv.getEnv().getHostname();
    }

    /**
     * Returns a build date string (eg, "Jan-26-03") as generated into BuildInfo.txt at build time.
     */
    public static String getBuildInfo()
    {
        // If already set, just return
        if (_buildInfo!=null) return _buildInfo;

        // If build info file hasn't been loaded, load it
        try { _buildInfo = SnapUtils.getText(SnapUtils.class, "/com/reportmill/BuildInfo.txt"); }
        catch(Exception e) { System.err.println("SnapUtils.getBuildInfo: " + e); _buildInfo = "BuildInfo not found"; }
        return _buildInfo;
    }

    /**
     * Returns the number of processors on this machine.
     */
    public static int getProcessorCount()
    {
        //try { return isMac ? 1 : Math.min(Runtime.getRuntime().availableProcessors(), 4); }
        //catch(Throwable t) { }
        return 1;
    }

    /**
     * Returns a unique id for a string each time it's called, starting with 0.
     */
    public static int getId(String anId)  { return intValue(_doOnceMap.put(anId, intValue(_doOnceMap.get(anId))+1)); }

    /**
     * Returns whether to do something once based on given unique id string.
     */
    public static boolean doOnce(String anId)  { return _doOnceMap.get(anId)==null && _doOnceMap.put(anId, 1)==null; }

    /**
     * Does a println of a given message to given print writer once.
     */
    public static void printlnOnce(PrintStream aStream, String aString)  { if (doOnce(aString)) aStream.println(aString); }

    /**
     * Returns a "not implemented" exception for string (method name).
     */
    public static RuntimeException notImpl(Object anObj, String aStr)
    {
        return new RuntimeException(anObj.getClass().getName() + ": Not implemented: " + aStr);
    }
}