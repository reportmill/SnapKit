package snap.swing;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A GFXEnv implementation using AWT.
 */
public class AWTEnv extends GFXEnv {
    
    // The shared AWTEnv
    static AWTEnv     _shared = new AWTEnv();

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public String[] getFontNames()  { return AWTFontUtils.getFontNames(); }

/**
 * Returns a list of all system family names.
 */
public String[] getFamilyNames()  { return AWTFontUtils.getFamilyNames(); }

/**
 * Returns a list of all font names for a given family name.
 */
public String[] getFontNames(String aFamilyName)  { return AWTFontUtils.getFontNames(aFamilyName); }

/**
 * Returns a font file for given name.
 */
public FontFile getFontFile(String aName)  { return new AWTFontFile(aName); }

/**
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new J2DImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new J2DImage(aWidth,aHeight,hasAlpha); }

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)
{
    try {
        Class cls = Class.forName("snap.javafx.SoundData");
        return (SoundClip)cls.getDeclaredConstructor(Object.class).newInstance(aSource);
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Creates a sound for given source.
 */
public SoundClip createSound()
{
    try {
        Class cls = Class.forName("snap.javafx.SoundData");
        return (SoundClip)cls.newInstance();
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Tries to open the given file source with the platform reader.
 */
public void openFile(Object aSource)
{
    if(aSource instanceof WebFile) aSource = ((WebFile)aSource).getStandardFile();
    if(aSource instanceof WebURL) aSource = ((WebURL)aSource).getSourceURL();
    File file = FileUtils.getFile(aSource);
    try { Desktop.getDesktop().open(file); return; }
    catch(Throwable e) { System.err.println(e.getMessage()); }
}

/**
 * Tries to open the given URL with the platform reader.
 */
public void openURL(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    String urls = url!=null? url.getString() : null;
    try { Desktop.getDesktop().browse(new URI(urls)); return; } // RM13 has a pre-JVM 6 implementation
    catch(Throwable e) { System.err.println(e.getMessage()); }
}

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()
{
    try { return Toolkit.getDefaultToolkit().getScreenResolution(); }
    catch(java.awt.HeadlessException he) { return 72; }
}

/**
 * Plays a beep.
 */
public void beep()  { Toolkit.getDefaultToolkit().beep(); }

/**
 * Override to return AWTPrefs for name.
 */
public Prefs getPrefs(String aName)  { return AWTPrefs.getPrefs(aName); }

/**
 * Sets this JVM to be headless.
 */
public void setHeadless()
{
    try { System.setProperty("java.awt.headless", "true"); }
    catch(Throwable e) { }
}

/**
 * Returns the current platform.
 */
public SnapUtils.Platform getPlatform()
{
    if(System.getProperty("os.name").indexOf("Windows") >= 0) return SnapUtils.Platform.WINDOWS;
    if(System.getProperty("os.name").indexOf("Mac OS X") >= 0) return SnapUtils.Platform.MAC;
    return SnapUtils.Platform.UNKNOWN;
}

/**
 * Returns a key value.
 */
public Object getKeyValue(Object anObj, String aKey)  { return Key.getValue(anObj, aKey); }

/**
 * Sets a key value.
 */
public void setKeyValue(Object anObj, String aKey, Object aValue)  { Key.setValueSafe(anObj, aKey, aValue); }

/**
 * Returns a key chain value.
 */
public Object getKeyChainValue(Object anObj, String aKeyChain)  { return KeyChain.getValue(anObj, aKeyChain); }

/**
 * Sets a key chain value.
 */
public void setKeyChainValue(Object anObj, String aKC, Object aValue)  { KeyChain.setValueSafe(anObj, aKC, aValue); }

/**
 * Returns a key list value.
 */
public Object getKeyListValue(Object anObj, String aKey, int anIndex) { return KeyList.getValue(anObj, aKey, anIndex); }

/**
 * Adds a key list value.
 */
public void setKeyListValue(Object anObj, String aKey, Object aValue, int anIndex)
{
    KeyList.setValue(anObj, aKey, aValue, anIndex);
}

/**
 * Returns a shared instance.
 */
public static AWTEnv get()  { return _shared; }

/**
 * Sets AWTEnv to be the default env.
 */
public static void set()  { GFXEnv.setEnv(get()); }

}