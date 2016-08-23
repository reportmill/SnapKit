package snap.javafx;
import snap.gfx.*;

/**
 * A custom class.
 */
public class JFXGfxEnv extends GFXEnv {

    // The shared JFXGfxEnv
    static JFXGfxEnv     _shared = new JFXGfxEnv();

/**
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new JFXImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new JFXImage(aWidth,aHeight,hasAlpha); }

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)  { return new SoundData(aSource); }

/**
 * Creates a new sound clip.
 */
public SoundClip createSound()  { return new SoundData(); }

/**
 * Returns a shared instance.
 */
public static JFXGfxEnv get()  { return _shared; }

}