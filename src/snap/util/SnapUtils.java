/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import snap.gfx.GFXEnv;
import snap.web.WebFile;
import snap.web.WebURL;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This class provides general utility methods.
 */
public class SnapUtils {

    // Legacy
    public static boolean isWebVM = SnapEnv.isWebVM;

    // The build info string from "BuildInfo.txt" (eg, "Aug-31-04")
    private static String _buildInfo;

    // A map to track "print once" messages
    private static Map<String, Integer> _doOnceMap = new HashMap<>();

    /**
     * Returns a clone of the given object (supports List, Map, Cloneable and null).
     * If object not cloneable, returns object.
     */
    public static <T> T clone(T anObj)
    {
        // Handle list
        if (anObj instanceof List)
            return (T) ListUtils.clone((List<?>) anObj);

        // Handle map
        if (anObj instanceof Map)
            return (T) MapUtils.clone((Map<?,?>) anObj);

        // Handle Cloneable: Invoke clone method with reflection
        if (anObj instanceof Cloneable)
            return (T) ClassUtils.cloneCloneable((Cloneable) anObj);

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
        if (clone instanceof Map) {
            Map<Object,Object> map = (Map<Object,Object>) clone;
            Set<Map.Entry<Object,Object>> entrySet = map.entrySet();
            for (Map.Entry<Object,Object> entry : entrySet)
                map.put(entry.getKey(), cloneDeep(entry.getValue()));
        }

        // If object is List, duplicate it's elements
        else if (clone instanceof List) {
            List<Object> list = (List<Object>) clone;
            for (int i = 0, iMax = list.size(); i < iMax; i++) {
                Object item = list.get(i);
                Object cloneItem = cloneDeep(item);
                list.set(i, cloneItem);
            }
        }

        // Return object
        return clone;
    }

    /**
     * Returns result of comparing two values.
     */
    public static int compare(Object anObj1, Object anObj2)
    {
        // If objects are same, return 0
        if (anObj1 == anObj2) return 0;

        // If first is null return less than (-1), if second is null, return greater than (1)
        if (anObj1 == null) return -1;
        if (anObj2 == null) return 1;

        // If object is comparable and is same or super class, let it do the comparison (try both)
        if (anObj1 instanceof Comparable && anObj1.getClass().isInstance(anObj2))
            return ((Comparable<Object>) anObj1).compareTo(anObj2);
        if (anObj2 instanceof Comparable && anObj2.getClass().isInstance(anObj1))
            return -((Comparable<Object>) anObj2).compareTo(anObj1);

        // Compare big decimal values
        return Convert.getBigDecimal(anObj1).compareTo(Convert.getBigDecimal(anObj2));
    }

    /**
     * Returns the temp directory for this machine.
     */
    public static String getTempDir()
    {
        // Hack for TeaVM, WebVM
        if (SnapEnv.isTeaVM) return "/";
        if (SnapEnv.isWebVM) return "/files/temp/";

        // Get System property and make sure it ends with dir char
        String tempDir = System.getProperty("java.io.tmpdir");
        if (!tempDir.endsWith(java.io.File.separator))
            tempDir += java.io.File.separator;

        // Return
        return tempDir;
    }

    /**
     * Returns a byte array from a File, String path, InputStream, URL, byte[], etc.
     */
    public static byte[] getBytes(Object aSource)
    {
        try { return getBytesOrThrow(aSource); }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a byte array from a File, String path, InputStream, URL, byte[], etc.
     */
    public static byte[] getBytesOrThrow(Object aSource) throws IOException
    {
        // Handle byte array and InputStream
        if (aSource instanceof byte[])
            return (byte[]) aSource;
        if (aSource instanceof InputStream)
            return getInputStreamBytes((InputStream) aSource);

        // Handle File
        if (aSource instanceof File)
            return FileUtils.getBytesOrThrow((File) aSource);

        // Handle URL
        if (aSource instanceof URL)
            return URLUtils.getBytes((URL) aSource);

        // Handle WebFile
        if (aSource instanceof WebFile)
            return ((WebFile) aSource).getBytes();

        // Handle WebURL (URL, File, String path)
        WebURL url = WebURL.getUrl(aSource);
        if (url != null)
            return url.getBytesOrThrow();

        // Return null since bytes not found
        return null;
    }

    /**
     * Returns bytes for an input stream.
     */
    public static byte[] getInputStreamBytes(InputStream aStream) throws IOException
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        for (int len = aStream.read(chunk, 0, chunk.length); len > 0; len = aStream.read(chunk, 0, chunk.length))
            bs.write(chunk, 0, len);
        return bs.toByteArray();
    }

    /**
     * Returns text for a source.
     */
    public static String getText(Object aSource)
    {
        // Handle WebFile
        if (aSource instanceof WebFile)
            return ((WebFile) aSource).getText();

        // Handle anything else: Get bytes
        byte[] bytes = getBytes(aSource);
        if (bytes == null)
            return null;

        // Convert to string and return
        String str = StringUtils.getString(bytes);
        return str;
    }

    /**
     * Returns bytes for a class and name/path.
     */
    public static byte[] getBytes(Class<?> aClass, String aName)
    {
        WebURL url = WebURL.getResourceUrl(aClass, aName);
        return getBytes(url);
    }

    /**
     * Returns text string for a class and name/path.
     */
    public static String getText(Class<?> aClass, String aName)
    {
        WebURL url = WebURL.getResourceUrl(aClass, aName);
        return getText(url);
    }

    /**
     * Returns an input stream from a File, String path, URL, byte array, InputStream, etc.
     */
    public static InputStream getInputStream(Object aSource)
    {
        // Handle byte array and InputStream
        if (aSource instanceof byte[])
            return new ByteArrayInputStream((byte[]) aSource);
        if (aSource instanceof InputStream)
            return (InputStream) aSource;

        // Handle WebFile
        if (aSource instanceof WebFile)
            return ((WebFile) aSource).getInputStream();

        // Handle WebURL (URL, File, String path)
        WebURL url = WebURL.getUrl(aSource);
        if (url != null)
            return url.getInputStream();

        // Complain and return null
        System.err.println("SnapUtils.getInputStream: Couldn't get stream for " + aSource);
        return null;
    }

    /**
     * Writes the given bytes to the given output object (string path or file).
     */
    public static void writeBytes(byte[] bytes, Object aDest)
    {
        // Get file for dest
        File file = FileUtils.getFile(aDest);

        // Write bytes
        try {
            FileUtils.writeBytes(file, bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if (_buildInfo != null) return _buildInfo;

        // If build info file hasn't been loaded, load it
        try {
            _buildInfo = SnapUtils.getText(SnapUtils.class, "/com/reportmill/BuildInfo.txt");
        } catch (Exception e) {
            System.err.println("SnapUtils.getBuildInfo: " + e);
            _buildInfo = "BuildInfo not found";
        }
        return _buildInfo;
    }

    /**
     * Returns the current java version as integer.
     */
    public static int getJavaVersionInt()
    {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1."))
            javaVersion = javaVersion.substring(2);
        return Convert.intValue(javaVersion);
    }

    /**
     * Returns the system info.
     */
    public static String getSystemInfo()
    {
        return "Java VM: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")\n" +
                "OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")\n";
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
     * Returns whether to do something once based on given unique id string.
     */
    public static boolean doOnce(String anId)
    {
        return _doOnceMap.get(anId) == null && _doOnceMap.put(anId, 1) == null;
    }

    /**
     * Does a println of a given message to given print writer once.
     */
    public static void printlnOnce(PrintStream aStream, String aString)
    {
        if (doOnce(aString))
            aStream.println(aString);
    }

    /**
     * Returns a "not implemented" exception for string (method name).
     */
    public static RuntimeException notImpl(Object anObj, String aStr)
    {
        return new RuntimeException(anObj.getClass().getName() + ": Not implemented: " + aStr);
    }
}