/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * A collection of routines for pulling various objects out of PDF dictionaries 
 */
public class PDFDictUtils {

/** An array of floats: << /Nums [1.414 2.717 3.1415 ] >> */
public static float[] getFloatArray(Map dictionary, PDFFile srcFile, String key)
{
    Object val = dictionary.get(key);
    List vlist;
    float farray[];
    int i,n;
    
    // If the object can be a reference, resolve it
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if (val == null) return null;
    if (!(val instanceof List))
        throw new PDFException("Parameter \""+key+"\" is not an array");
    vlist = (List)val;
    n=vlist.size();
    if (n==0)
        return null;
    farray = new float[n];
    for(i=0; i<n; ++i) {
        val = vlist.get(i);
        if (!(val instanceof Number)) 
            throw new PDFException("Non-numeric value in \""+key+"\" array");
        farray[i]=((Number)val).floatValue();
    }
    return farray;
}

/** An array of ints : << /Ints [1 3 5 7] >> */
public static int[] getIntArray(Map dictionary, PDFFile srcFile, String key)
{
    Object val = dictionary.get(key);
    List vlist;
    int iarray[];
    int i,n;
    
    // If the object can be a reference, resolve it
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if (val == null) return null;
    if (!(val instanceof List))
        throw new PDFException("Parameter \""+key+"\" is not an array");
    vlist = (List)val;
    n=vlist.size();
    if (n==0)
        return null;
    iarray = new int[n];
    for(i=0; i<n; ++i) {
        val = vlist.get(i);
        if (!(val instanceof Number)) 
            throw new PDFException("Non-numeric value in \""+key+"\" array");
        iarray[i]=((Number)val).intValue();
    }
    return iarray;
}

/** An array of bools : << /Vals [true false] >> */
public static boolean[] getBoolArray(Map dictionary, PDFFile srcFile, String key)
{
    Object val = dictionary.get(key);
    List vlist;
    boolean barray[];
    int i,n;
    
    // If the object can be a reference, resolve it
    if (srcFile != null)
        val = srcFile.getXRefObj(val);
    
    if (val == null) return null;
    if (!(val instanceof List))
        throw new PDFException("Parameter \""+key+"\" is not an array");
    vlist = (List)val;
    n=vlist.size();
    if (n==0)
        return null;
    barray = new boolean[n];
    for(i=0; i<n; ++i) {
        val = vlist.get(i);
        if (!(val instanceof Boolean)) 
            throw new PDFException("Non-numeric value in \""+key+"\" array");
        barray[i]=((Boolean)val).booleanValue();
    }
    return barray;
}

/** A transformation matrix: << /Matrix [1 0 0 1 0 0] >> */
public static AffineTransform getTransform(Map dictionary, PDFFile srcFile, String key) 
{
    float marray[] = getFloatArray(dictionary, srcFile, key);
    
    // It probably would be safe to return the identity matrix here
    if (marray == null) 
        return null;
    
    if (marray.length != 6)
        throw new PDFException("Wrong number of elements in matrix definition");
    
    return new AffineTransform(marray);
}

/** A rectangle : << /BBox [0 0 612 792] >> */
public static Rectangle2D getRectangle(Map dictionary, PDFFile srcFile, String key)
{
    float rarray[] = getFloatArray(dictionary, srcFile, key);
    
    if (rarray == null)
        return null;
    
    if (rarray.length != 4)
        throw new PDFException("Wrong number of elements in rectangle definition");
    
    return new Rectangle2D.Float(rarray[0],rarray[1],rarray[2]-rarray[0],rarray[3]-rarray[1]);
}

// For required entries in the dictionary
public static int getInt(Map dictionary, PDFFile srcFile, String  key)
{
    Object obj = srcFile.getXRefObj(dictionary.get(key));
    if (!(obj instanceof Number)) 
        throw new PDFException("Error reading required entry \""+key+"\"");
    return ((Number)obj).intValue();
}

public static float getFloat(Map dictionary, PDFFile srcFile, String  key)
{
    Object obj = srcFile.getXRefObj(dictionary.get(key));
    if (!(obj instanceof Number)) 
        throw new PDFException("Error reading required entry \""+key+"\"");
    return ((Number)obj).floatValue();
}
       
}
