package snap.util;
import java.io.*;
import java.math.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import snap.web.WebURL;

/**
 * This class provides a bunch of utility methods for common problems.
 */
public class SnapUtils {

    // Whether app is currently running on Windows
    public static boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
    
    // Whether app is currently running on Mac
    public static boolean isMac = (System.getProperty("os.name").indexOf("Mac OS X") >= 0);
    
    // Whether app is currently running as desktop App
    public static boolean isApp = false;
    
    // The build info string from "BuildInfo.txt" (eg, "Aug-31-04")
    private static String _buildInfo;
    
    // A map to track "print once" messages
    private static Map <String, Integer> _doOnceMap = new HashMap();

/**
 * Returns a boolean value for the given object.
 */
public static boolean boolValue(Object anObj)
{
    // If Boolean, return bool value
    if(anObj instanceof Boolean) return ((Boolean)anObj).booleanValue();
    
    // If number, return true if number is non-zero
    else if(anObj instanceof Number) return !MathUtils.equalsZero(((Number)anObj).floatValue());
    
    // If string and "false", return false
    else if(anObj instanceof String && StringUtils.equalsIC((String)anObj, "false")) return false;
    
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
    if(anObj instanceof Number) return ((Number)anObj).longValue(); // If Number, return double value
    if(anObj instanceof String) return StringUtils.longValue((String)anObj); // If String, parse as double value
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
    if(anObj instanceof Number) return ((Number)anObj).doubleValue(); // If Number, return double value
    if(anObj instanceof String) return StringUtils.doubleValue((String)anObj); // If String, parse as double
    return 0; // If anything else, return zero
}

/**
 * Returns a String for a given arbitrary object.
 */
public static String stringValue(Object anObj)
{
    // If object is null, return null
    if(anObj==null) return null;
    
    // If object is string, just return it
    if(anObj instanceof String) return (String)anObj;
    
    // If object is number, string format it
    if(anObj instanceof Number) {
        if(anObj instanceof Float || anObj instanceof Double)
            return StringUtils.toString(((Number)anObj).doubleValue());
        return anObj.toString();
    }
    
    // If object is File, get absolute path
    if(anObj instanceof File) return ((File)anObj).getAbsolutePath();
    
    // If object is Date, date format it
    if(anObj instanceof Date) return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)anObj);
    
    // If byte array, format as base64
    if(anObj instanceof byte[]) {
        String s = ASCIICodec.encodeBase64((byte[])anObj);
        s = s.replace((char)0, ' ');
        return s;
    }
    
    // If class get standard name
    if(anObj instanceof Class) return ((Class)anObj).getName().replace('$', '.');
    
    // Return object's toString
    return anObj.toString();
}

/**
 * Returns the Boolean for a given object (assumed to be Number or String).
 */
public static Boolean booleanValue(Object anObj)
{
    if(anObj instanceof Boolean || anObj==null) return (Boolean)anObj; // If already boolean or null, just return it
    return Boolean.valueOf(boolValue(anObj)); // Otherwise return Boolean of boolValue
}

/**
 * Returns the Number for a given object (assumed to be Number or String).
 */
public static Number numberValue(Object anObj)
{
    // If already a number or null, just return it
    if(anObj instanceof Number || anObj==null) return (Number)anObj;
    
    // Try returning as BigDecimal  - can fail if is Nan or pos/neg infinity (returns as double)
    try { return getBigDecimal(anObj); }
    catch(Exception e) { return doubleValue(anObj); }
}

/**
 * Returns the Integer for a given object.
 */
public static Integer getInteger(Object anObj)
{
    if(anObj instanceof Integer || anObj==null) return (Integer)anObj; // If already Integer or null, just return it
    return intValue(anObj); // Otherwise, return new integer
}

/**
 * Returns a Float for a given object.
 */
public static Float getFloat(Object anObj)
{
    if(anObj instanceof Float || anObj==null) return (Float)anObj; // If already Float or null, just return it
    return floatValue(anObj); // Otherwise, return float
}

/**
 * Returns a Double for a given object.
 */
public static Double getDouble(Object anObj)
{
    if(anObj instanceof Double || anObj==null) return (Double)anObj; // If already Double or null, just return it
    return doubleValue(anObj); // Otherwise, return float
}

/**
 * Returns the BigDecimal for a given object (assumed to be a string or number).
 */
public static BigDecimal getBigDecimal(Object anObj)
{
    if(anObj instanceof BigDecimal || anObj==null) return (BigDecimal)anObj; // If already BigDecimal, just return it
    return new BigDecimal(doubleValue(anObj)); // If object is anything else, return big decimal of its double value
}

/**
 * Returns an enum for enum class and string, ignoring case.
 */
public static <T extends Enum<T>> T valueOfIC(Class<T> enumType, String aName)
{
    for(T value : enumType.getEnumConstants())
        if(value.toString().equalsIgnoreCase(aName))
            return value;
    if(aName==null)
        throw new NullPointerException("Name is null");
    throw new IllegalArgumentException("No enum const " + enumType +"." + aName);
}

/**
 * Returns a date for given object of arbitrary type.
 */
public static Date getDate(Object anObj)
{
    // If object is date or null, just return it
    if(anObj instanceof Date || anObj==null)
        return (Date)anObj;
    
    // If object is long, return date
    if(anObj instanceof Long)
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
 * Returns a clone of the given object (supports List, Map, RMObject, null and others by reflection).
 */
public static <T> T clone(T anObj)
{
    // Handle list
    if(anObj instanceof List)
        return (T)ListUtils.clone((List)anObj);
    
    // Handle map
    if(anObj instanceof Map)
        return (T)MapUtils.clone((Map)anObj);
    
    // Handle SnapObject
    if(anObj instanceof SnapObject)
        return (T)((SnapObject)anObj).clone();
    
    // Try to invoke clone method manually
    if(anObj!=null)
        try { return (T)anObj.getClass().getMethod("clone").invoke(anObj); }
        catch(Throwable t) { }
        
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
    if(clone instanceof Map) { Map map = (Map)clone;
        for(Map.Entry entry : (Set<Map.Entry>)map.entrySet())
            map.put(entry.getKey(), cloneDeep(entry.getValue())); }
    
    // If object is List, duplicate it's elements
    else if(clone instanceof List) { List list = (List)clone;
        for(int i=0, iMax=list.size(); i<iMax; i++) { Object item = list.get(i);
            list.set(i, cloneDeep(item)); } }

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
    if(anObj1==anObj2) return 0;
    
    // If first is null return less than (-1), if second is null, return greater than (1)
    if(anObj1==null) return -1;
    if(anObj2==null) return 1;
    
    // If object is comparable and is same or super class, let it do the comparison (try both)
    if(anObj1 instanceof Comparable && anObj1.getClass().isInstance(anObj2))
        return ((Comparable)anObj1).compareTo(anObj2);
    if(anObj2 instanceof Comparable && anObj2.getClass().isInstance(anObj1))
        return -((Comparable)anObj2).compareTo(anObj1);
    
    // Compare big decimal values
    return getBigDecimal(anObj1).compareTo(getBigDecimal(anObj2));
}

/**
 * Returns the temp directory for this machine.
 */
public static String getTempDir()
{
    String tempDir = System.getProperty("java.io.tmpdir");
    if(!tempDir.endsWith(java.io.File.separator))
        tempDir += java.io.File.separator;
    return tempDir;
}

/**
 * Returns text for a source.
 */
public static String getText(Object aSource)
{
    byte bytes[] = getBytes(aSource); if(bytes==null) return null;
    return StringUtils.getString(bytes);
}

/**
 * Returns a byte array from a File, String path, InputStream, URL, byte[], etc.
 */
public static byte[] getBytes(Object aSource)
{
    if(aSource instanceof byte[]) return (byte[])aSource;
    if(aSource instanceof InputStream) return getBytes((InputStream)aSource);
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    if(url!=null && url.getFile()!=null) return url.getFile().getBytes();
    if(aSource instanceof URL) try { return URLUtils.getBytes((URL)aSource); }
    catch(IOException e) { e.printStackTrace(); }
    return null;
}

/**
 * Returns bytes for an input stream.
 */
public static byte[] getBytes(InputStream aStream)
{
    try { return getBytes2(aStream); }
    catch(IOException e) { throw new RuntimeException(e); }
}

/**
 * Returns bytes for an input stream.
 */
public static byte[] getBytes2(InputStream aStream) throws IOException
{
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    byte chunk[] = new byte[8192];
    for(int len=aStream.read(chunk, 0, chunk.length); len>0; len=aStream.read(chunk, 0, chunk.length))
        bs.write(chunk, 0, len);
    return bs.toByteArray();
}

/**
 * Returns an input stream from a File, String path, URL, byte array, InputStream, etc.
 */
public static InputStream getInputStream(Object aSource)
{
    if(aSource instanceof InputStream) return (InputStream)aSource;
    if(aSource instanceof byte[]) return new ByteArrayInputStream((byte[])aSource);
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    if(url!=null && url.getFile()!=null) return url.getFile().getInputStream();
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
 * Checks a string to see if it's valid.
 */
public static boolean checkString(String aString, boolean isApplication)
{
    // If null string provided, just return
    if(aString==null) return false;

    // License is uppercase version of string - return if not long enough or has wrong prefix
    String license = aString.toUpperCase();
    
    // Get index of dash in license string
    int dash = license.indexOf("-"); if(dash<0) return false;
    
    // Get license prefix (substring before dash) and suffix (substring after dash)
    String prefix = license.substring(0, dash);
    String suffix = license.substring(dash+1);
    
    // Decrypt suffix
    suffix = dString(suffix, "RMRules");
    
    // If prefix/suffix lengths not equal, return false
    if(prefix.length()!=suffix.length()) return false;
    
    // Strip off first two characters of suffix
    suffix = suffix.substring(2);
    
    // If prefix doesn't start with suffix, return false
    if(!prefix.startsWith(suffix)) return false;
    
    // Declare local variable for license processor count
    int procCount = 0;
        
    // Decode processor count hex digit at second to last prefix char
    try { procCount = Integer.parseInt(prefix.substring(prefix.length()-2, prefix.length()-1), Character.MAX_RADIX); }
    catch(Exception e) { return false; }
    
    // If less than actual processor count, return false
    if((isApplication && procCount!=0) || (!isApplication && procCount<getProcessorCount())) {
        if(!isApplication) {
            System.err.println("Warning: License key not valid for CPU Count " + getProcessorCount());
            System.err.println("Warning: License key CPU count is " + procCount);
        }
        return false;
    }
    
    // Declare local variable for license version
    int version = 0;
    
    // Decode version hex digit at last prefix char
    try { version = Integer.parseInt(prefix.substring(prefix.length()-1), Character.MAX_RADIX); }
    catch(Exception e) { System.err.println("Warning: License key invalid format (2)"); return false; }
    
    // If version is less than getVersion(), complain and return false
    if(version<9) { //getVersion()) { Change back soon
        System.err.print("Warning: License not valid for RM " + getVersion());
        System.err.println(" (license valid for RM " + version + ")");
        return false;
    }

    // Return true since all checks passed
    return true;
}

/**
 * Decrypts aString with aPassword (takes hex string of form "AC5FDE" and returns human readable string).
 */
private static String dString(String aString, String aPassword)
{
    if(aString==null || aPassword==null) return null;
    
    // Get bytes for string and password
    byte string[] = ASCIICodec.decodeHex(aString);
    byte password[] = StringUtils.getBytes(aPassword);
    
    // XOR string bytes with password bytes
    for(int i=0; i<string.length; i++)
        string[i] = (byte)(string[i]^password[i%password.length]);
    
    return StringUtils.getISOLatinString(string);
}

/**
 * Returns the hostname for this machine.
 */
public static String getHostname()
{
    try { InetAddress h = InetAddress.getLocalHost(); return h==null? "localhost" : h.getHostName(); }
    catch(Exception e) { return "localhost"; }
}

/**
 * Returns a build date string (eg, "Jan-26-03") as generated into BuildInfo.txt at build time.
 */
public static String getBuildInfo()
{
    // If build info file hasn't been loaded, load it
    if(_buildInfo==null) {
        try {
            InputStream s = SnapUtils.class.getResourceAsStream("/com/reportmill/BuildInfo.txt");
            InputStreamReader r = new InputStreamReader(s);
            BufferedReader br = new BufferedReader(r);
            _buildInfo = br.readLine(); 
        }
        catch(Exception e) { _buildInfo = ""; }
    }
    
    // Return build info string
    return _buildInfo;
}

/**
 * Returns the version number of the app.
 */
public static float getVersion()  { return 14; }

/**
 * Returns the number of processors on this machine.
 */
public static int getProcessorCount()
{
    try { return isMac? 1 : Math.min(Runtime.getRuntime().availableProcessors(), 4); }
    catch(Throwable t) { }
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
public static void printlnOnce(PrintStream aStream, String aString)  { if(doOnce(aString)) aStream.println(aString); }

/**
 * Returns a "not implemented" exception for string (method name).
 */
public static RuntimeException notImpl(Object anObj, String aStr)
{
    return new RuntimeException(anObj.getClass().getName() + ": Not implemented: " + aStr);
}

}