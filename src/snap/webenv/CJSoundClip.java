package snap.webenv;
import snap.gfx.SoundClip;
import snap.web.WebURL;
import snap.webapi.*;

/**
 * A SnapKit SoundClip implementation for CheerpJ.
 */
public class CJSoundClip extends SoundClip {

    // The Audio Element
    private HTMLAudioElement _snd, _snd2;

    // The mime type
    private String _mimeType = "audio/wav";

    /**
     * Constructor.
     */
    public CJSoundClip(Object aSource)
    {
        String sourceUrlString = getSourceURL(aSource);
        _snd = (HTMLAudioElement) HTMLDocument.getDocument().createElement("audio");
        _snd.setAttribute("src", sourceUrlString);
        _snd.setAttribute("preload", "auto");
        _snd.load();
    }

    /**
     * Returns a Source URL from source object.
     */
    private String getSourceURL(Object aSource)
    {
        // Handle byte[] and InputStream
        if (aSource instanceof byte[]) {
            byte[] bytes = (byte[]) aSource;
            Blob blob = new Blob(bytes, _mimeType);
            String urls = blob.createURL();
            return urls;
        }

        // Get URL
        WebURL url = WebURL.getUrl(aSource);
        if (url == null)
            return null;

        // Set mime type
        String type = url.getFileType();
        if (type.equals("mp3"))
            _mimeType = "audio/mpeg";
        else if (type.equals("oga"))
            _mimeType = "audio/ogg";
        else if (type.equals("aac"))
            _mimeType = "audio/aac";
        else if (type.equals("mid") || type.equals("midi"))
            _mimeType = "audio/midi";


        // If URL can't be fetched by browser, load from bytes
        if (!isBrowsable(url)) {
            byte[] urlBytes = url.getBytes();
            return getSourceURL(urlBytes);
        }

        // Return URL string
        String urls = url.getString().replace("!", "");
        return urls;
    }

    /**
     * Returns whether sound is playing.
     */
    public boolean isPlaying()
    {
        return _snd2 != null && !_snd2.isEnded();
    }

    /**
     * Plays the sound.
     */
    public void play()
    {
        _snd2 = (HTMLAudioElement) _snd.cloneNode(false);
        _snd2.play();
    }

    /**
     * Plays the sound repeatedly for given count.
     */
    public void play(int aCount)  { }

    /**
     * Tells sound to stop playing.
     */
    public void stop()  { }

    /**
     * Pauses a sound.
     */
    public void pause()  { }

    /**
     * Starts a recording.
     */
    public void recordStart()  { }

    /**
     * Stops a recording.
     */
    public void recordStop()  { }

    /**
     * Returns whether sound is recording.
     */
    public boolean isRecording()  { return false; }

    /**
     * Returns the sound length in milliseconds.
     */
    public int getLength()  { return 0; }

    /**
     * Returns the sound time in milliseconds.
     */
    public int getTime()  { return 0; }

    /**
     * Sets the sound time in milliseconds.
     */
    public void setTime(int aTime)  { }

    /**
     * Saves this sound.
     */
    public void save()  { }

    /**
     * Returns whether URL can be fetched by browser.
     */
    private static boolean isBrowsable(WebURL aURL)
    {
        String scheme = aURL.getScheme();
        return scheme.equals("http") || scheme.equals("https") || scheme.equals("data") || scheme.equals("blob");
    }
}