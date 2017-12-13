/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.List;
import java.util.Map;
import snap.gfx.Rect;
import snap.gfx.Transform;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * A collection of routines for pulling various objects out of PDF dictionaries 
 */
public class PDFDictUtils {

/**
 * Returns an int for given dictionary and key.
 */
public static int getInt(Map dictionary, PDFFile srcFile, String  key)
{
    Object obj = srcFile.getXRefObj(dictionary.get(key));
    if (!(obj instanceof Number)) 
        throw new PDFException("Error reading required entry \""+key+"\"");
    return ((Number)obj).intValue();
}

/**
 * Returns a float for given dictionary and key.
 */
public static float getFloat(Map dictionary, PDFFile srcFile, String  key)
{
    Object obj = srcFile.getXRefObj(dictionary.get(key));
    if (!(obj instanceof Number)) 
        throw new PDFException("Error reading required entry \""+key+"\"");
    return ((Number)obj).floatValue();
}
       
/**
 * Returns a rectangle : << /BBox [0 0 612 792] >>
 */
public static Rect getRect(Map dictionary, PDFFile srcFile, String key)
{
    float rarray[] = getFloatArray(dictionary, srcFile, key);
    if(rarray == null) return null;
    if(rarray.length != 4) throw new PDFException("Wrong number of elements in rectangle definition");
    return new Rect(rarray[0],rarray[1],rarray[2]-rarray[0],rarray[3]-rarray[1]);
}

/**
 * Returns a transformation matrix: << /Matrix [1 0 0 1 0 0] >>
 */
public static Transform getTransform(Map dictionary, PDFFile srcFile, String key) 
{
    float marray[] = getFloatArray(dictionary, srcFile, key);
    if (marray == null) return null; // It probably would be safe to return the identity matrix here
    if(marray.length != 6) throw new PDFException("Wrong number of elements in matrix definition");
    return new Transform(marray);
}

/**
 * Returns an array of bools : << /Vals [true false] >>
 */
public static boolean[] getBoolArray(Map dictionary, PDFFile srcFile, String key)
{
    // If the object can be a reference, resolve it
    Object val = dictionary.get(key);
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if(val == null) return null;
    if(!(val instanceof List)) throw new PDFException("Parameter \""+key+"\" is not an array");
    List vlist = (List)val;
    int n = vlist.size(); if(n==0) return null;
    boolean barray[] = new boolean[n];
    for(int i=0; i<n; ++i) { val = vlist.get(i);
        if(!(val instanceof Boolean)) throw new PDFException("Non-numeric value in \"" + key + "\" array");
        barray[i]=((Boolean)val).booleanValue();
    }
    return barray;
}

/**
 * Returns an array of ints : << /Ints [1 3 5 7] >>
 */
public static int[] getIntArray(Map dictionary, PDFFile srcFile, String key)
{
    // If the object can be a reference, resolve it
    Object val = dictionary.get(key);
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if (val == null) return null;
    if (!(val instanceof List)) throw new PDFException("Parameter \"" + key + "\" is not an array");
    List vlist = (List)val; int n = vlist.size(); if (n==0) return null;
    int iarray[] = new int[n];
    for(int i=0; i<n; ++i) { val = vlist.get(i);
        if (!(val instanceof Number)) throw new PDFException("Non-numeric value in \"" + key + "\" array");
        iarray[i]=((Number)val).intValue();
    }
    return iarray;
}

/**
 * Returns an array of floats: << /Nums [1.414 2.717 3.1415 ] >>
 */
public static float[] getFloatArray(Map dictionary, PDFFile srcFile, String key)
{
    // If the object can be a reference, resolve it
    Object val = dictionary.get(key);
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if(val == null) return null;
    if(!(val instanceof List)) throw new PDFException("Parameter \"" + key + "\" is not an array");
    List vlist = (List)val; int n = vlist.size(); if (n==0) return null;
    float farray[] = new float[n];
    for(int i=0; i<n; ++i) { val = vlist.get(i);
        if(!(val instanceof Number)) throw new PDFException("Non-numeric value in \"" + key + "\" array");
        farray[i]=((Number)val).floatValue();
    }
    return farray;
}

}