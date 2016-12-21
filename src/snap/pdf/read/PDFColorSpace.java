package snap.pdf.read;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.InputStream;
import java.util.*;
import snap.pdf.*;
import snap.pdf.read.PDFColorSpaces.*;

/**
 * A custom class.
 */
public class PDFColorSpace {

    static ColorSpace cmykspace = null;
    
    static Map _colorCache = new Hashtable();
 
    /** Colorspace constants */
    public static final int DeviceGrayColorspace = 1;
    public static final int CalibratedGrayColorspace = 2;
    public static final int DeviceRGBColorspace = 3;
    public static final int CalibratedRGBColorspace = 4;
    public static final int DeviceCMYKColorspace = 5;
    public static final int LabColorspace = 6;
    public static final int IndexedColorspace = 7;
    public static final int ICCBasedColorspace = 8;
    public static final int SeparationColorspace = 9;
    public static final int DeviceNColorspace = 10;
    public static final int PatternColorspace = 11;
    public static final int UnknownColorspace = -1;

/** Create a colorspace object from one of the above space IDs.
 *
 * The value of "params" can be as follows:
 *   Device spaces - ignored
 *   CIE spaces -  a Map
 *   ICC spaces -  a PDF Stream
 *   Indexed spaces - a Map with keys 'base", "hival", and "lookup"
 *   Pattern - null
 *   Separation - a Map with "Colorant", "Base", & "TintTransform"
 *   DeviceN - - a Map with "Colorants", "Base", "TintTransform", & "Attributes"
 */
/**
 * A colorspace can be specified in several ways. If a colorspace needs no arguments, in can just be the name.
 * Otherwise, it is an entry in the page's resource dictionary. The format of the object in the resource dictionary
 * is dependent upon the type of the colorspace. Once we figure out what kind of colorspace it is and the parameters,
 * we can call the ColorFactory to make an awt ColorSpace object.
 *
 *  Note that colorspaces in pdf are usually specified as strings (for built-ins) or arrays (eg. [/CalRGB << ... >>])
 */
public static java.awt.color.ColorSpace getColorspace(Object csobj, PDFFile _pfile, PDFPage page)
{
    int type=-1;
    Object params = null;
    List cslist = null;
    
    // Strings are either a name of a built in color space or the name of a
    // colorspace resource
    if (csobj instanceof String) { String pdfName = (String)csobj;
        
        // TODO:This shouldn't really be necessary
        if (pdfName.charAt(0)=='/')
            pdfName = pdfName.substring(1);
        
        // All Device spaces are subject to possible remapping through a Default space
        if (pdfName.startsWith("Device")) {
            String defName = "Default"+pdfName.substring(6);
            Object resource = page.findResource("ColorSpace", defName);
            if (resource != null)
                return getColorspace(resource, _pfile, page);
        }
        
        // Device... & Pattern are all fully specified by the name
        if (pdfName.equals("DeviceGray"))
            type = PDFColorSpace.DeviceGrayColorspace;
        else if (pdfName.equals("DeviceRGB"))
            type = PDFColorSpace.DeviceRGBColorspace;
        else if (pdfName.equals("DeviceCMYK"))
            type = PDFColorSpace.DeviceCMYKColorspace;
        else if (pdfName.equals("Pattern"))
            type = PDFColorSpace.PatternColorspace;
        else {
            // Look up the name in the resource dictionary and try again
            Object resource = page.findResource("ColorSpace", pdfName);
            if (resource != null)
                return getColorspace(resource, _pfile, page);
        }
    }
    
    else if (csobj instanceof List) { cslist = (List)csobj;
        // The usual format is [/SpaceName obj1 obj2...]
        // We do color space cacheing by adding the awt colorspace as the last object in the list.
        // The normal map cacheing strategy is to add an element with key _rb_cached_..., but Adobe, in their inifinite
        // wisdom, decided that color spaces should be arrays instead of dictionaries, like everything else.
        // TODO:  Make sure not to export this extra element when saving pdf
        Object cachedObj = cslist.get(cslist.size()-1);
        if (cachedObj instanceof ColorSpace)
            return (ColorSpace)cachedObj;
        
        String pdfName = ((String)cslist.get(0)).substring(1);
        params = cslist.size()>1 ? _pfile.getXRefObj(cslist.get(1)) : null;
        
        if (pdfName.equals("CalGray")) 
            type = PDFColorSpace.CalibratedGrayColorspace;
        else if (pdfName.equals("CalRGB"))
            type = PDFColorSpace.CalibratedRGBColorspace;
        else if (pdfName.equals("Lab"))
            type = PDFColorSpace.LabColorspace;
        else if (pdfName.equals("ICCBased"))
            type = PDFColorSpace.ICCBasedColorspace;
        else if (pdfName.equals("Pattern")) {
            type = PDFColorSpace.PatternColorspace;
            if (params != null)
                params = getColorspace(params, _pfile, page);
        }
        else if (pdfName.equals("Separation")) {
            type = PDFColorSpace.SeparationColorspace;
            Map paramDict = new Hashtable(2);
            paramDict.put("Colorant", cslist.get(1));
            paramDict.put("Base",getColorspace(_pfile.getXRefObj(cslist.get(2)), _pfile, page));
            paramDict.put("TintTransform", PDFFunction.getInstance(_pfile.getXRefObj(cslist.get(3)), _pfile));
            params = paramDict;
         }
        else if (pdfName.equals("DeviceN")) {
            type = PDFColorSpace.DeviceNColorspace;
            Map paramDict = new Hashtable(2);
            paramDict.put("Colorants", _pfile.getXRefObj(cslist.get(1)));
            paramDict.put("Base", getColorspace(_pfile.getXRefObj(cslist.get(2)), _pfile, page));
            paramDict.put("TintTransform", PDFFunction.getInstance(_pfile.getXRefObj(cslist.get(3)), _pfile));
            if (cslist.size()>4)
                paramDict.put("Attributes", _pfile.getXRefObj(cslist.get(4)));
            
            params=paramDict;
        }
        
        else if (pdfName.equals("Indexed")) {
            type = PDFColorSpace.IndexedColorspace;
            //  [/Indexed basecolorspace hival <hex clut>]
            // params set above is the base colorspace. Turn it into a real colorspace
            // NB: this is recursive and there's no check for following illegal sequence, which would cause
            // infinite recursion:
            //   8 0 obj [/Indexed  8 0 R  1 <FF>] endobj
            // Also note that in the time it took to write this comment, you could have put in a check for this case.
            if (cslist.size() != 4)
                throw new PDFException("Wrong number of elements in colorspace definition");
 
            if ((params instanceof String) && ( ((String)params).charAt(0)=='/') )
                params = ((String)params).substring(1);
            
            ColorSpace base = getColorspace(params, _pfile, page);
            Object hival = cslist.get(2);
            byte lookup_table[];
            
            if (!(hival instanceof Number))
                throw new PDFException("Illegal Colorspace definition "+cslist);

            // The lookuptable is next
            Object val = _pfile.getXRefObj(cslist.get(3));
            if (val instanceof PDFStream) 
                lookup_table = ((PDFStream)val).decodeStream();
                
            else if (val instanceof String) {
                // get the pageparser to decode the hex
                // NB:  For historical reasons, PDFReader doesn't interpret the string as hex, but just leaves it alone.
                // It probably makes sense to move much of this parsing back into PDFReader and let javacc handle it.
                lookup_table = PageToken.getPDFHexString((String)val);
            }
            
            // In the case of inline images, the pageparser has already done the conversion.
            else if (val instanceof byte[]) {
                lookup_table = (byte [])val; }
            
            else throw new PDFException("Can't read color lookup table");
            
            // Build a dictionary to pass to the colorFactory
            Map paramDict = new Hashtable(3);
            paramDict.put("Base", base);
            paramDict.put("HiVal", hival);
            paramDict.put("Lookup", lookup_table);
            params = paramDict;
        }
    }

    if (type != -1) {
        ColorSpace outerSpace = createColorSpace(type, params);
        // cache it
        if (cslist != null)
            cslist.add(outerSpace);
        return outerSpace;
    }

    throw new PDFException("Unknown colorspace : "+csobj);
}

// Get a cached instance of the generic deviceCMYK space
private static ColorSpace getDeviceCMYK()
{
    // should probably cache all colorspaces, but since this is such a monster, we'll start here
    if(cmykspace==null) cmykspace = findICCSpace("CMYK.icc"); //cmykspace = new PDFDeviceCMYK();
    return cmykspace;
}

// utility routine to load .pf file resources and initialize color spaces from them
private static ColorSpace findICCSpace(String name)
{
    try {
        InputStream s = PDFColorSpace.class.getResourceAsStream(name);
        ICC_Profile prof = ICC_Profile.getInstance(s);
        return new ICC_ColorSpace(prof);
    }
    catch (Exception e) {
        System.err.println("Error loading color space resource");
        return null;
    }
}
    
public static ColorSpace createColorSpace(int type, Object params)
{
    //TODO: recheck this mapping
    switch (type) {
        // The device spaces 
        case DeviceGrayColorspace: return ColorSpace.getInstance(ColorSpace.CS_GRAY);
        case DeviceRGBColorspace: return DeviceRGB.get();
        case DeviceCMYKColorspace: return getDeviceCMYK();
        
        // The CIE spaces. TODO: Get appropriate .pf files for these to match the spec
        case CalibratedGrayColorspace: return createColorSpace(DeviceGrayColorspace,null);
        case CalibratedRGBColorspace: return createColorSpace(DeviceRGBColorspace, null);
       
        // ICC Based space - a CIE space that is specified in the pdf stream
        case ICCBasedColorspace: 
            PDFStream s = (PDFStream)params;
            Map iccinfo = s.getDict();
            ICC_Profile profile = null;
            try {
                byte iccdata[] = s.decodeStream();
                profile = ICC_Profile.getInstance(iccdata);
            } catch (Exception e) {System.err.println("Error reading colorspace");}
            if (profile != null) {
                Object ncomps = iccinfo.get("N");
                if (ncomps != null) {
                  if (profile.getNumComponents() != ((Number)ncomps).intValue()) 
                      System.err.println("Error reading embedded colorspace.  Wrong number of components.");
                  }
                return new ICC_ColorSpace(profile);
             }
            else {
                Object alternate = iccinfo.get("Alternate");  //TODO:real implementation
                System.err.println("Couldn't load ICC color space .  Need to use alternate "+ alternate);
            }
            break;
        case IndexedColorspace:
            Map indexdict = (Map)params;
            return new IndexedColorSpace((ColorSpace)indexdict.get("Base"),
                ((Number)indexdict.get("HiVal")).intValue(), (byte[])indexdict.get("Lookup"));
        case SeparationColorspace:
            Map sepdict = (Map)params;
            return new SeparationColorSpace((String)sepdict.get("Colorant"), (ColorSpace)sepdict.get("Base"),
               (PDFFunction)sepdict.get("TintTransform"));
        case DeviceNColorspace:
            Map devndict = (Map)params;
            return new DeviceNColorSpace((List)devndict.get("Colorants"), (ColorSpace)devndict.get("Base"),
                    (PDFFunction)devndict.get("TintTransform"), (Map)devndict.get("Attributes"));            
        case PatternColorspace:
            if (params instanceof ColorSpace)
                return new PatternSpace((ColorSpace)params);
            return new PatternSpace();
        default: System.err.println("This is getting boring.  Need to implement colorspace id="+type);
    }
    
    // Return a default.  The parser's going to barf if the number of parameters passed to a 
    // sc operation doesn't match the number of components in this space.   Don't say you weren't warned.
    return ColorSpace.getInstance(ColorSpace.CS_sRGB);
}

// According to JProfiler, 50% of the time parsing a page is spent in java.awt.Color.<init>
// God only know what they could possibly be doing in there, but it certainly warrants a color cache.

public static Color createColor(ColorSpace space, float values[])
{
    // make a hashtable key from the params
    ArrayList key = new ArrayList(4); key.add(space);
    for(int i=0, n=values.length; i<n; ++i)
        key.add(values[i]);
    
    // see if it's in the cache
    Color c = (Color)_colorCache.get(key);
    if(c==null) // alpha = 1
        _colorCache.put(key, c = new Color(space, values, 1f));

    return c;
}

}