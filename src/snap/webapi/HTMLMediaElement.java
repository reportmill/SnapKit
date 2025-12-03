package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLMediaElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement).
 */
public class HTMLMediaElement extends HTMLElement {

    /**
     * Constructor.
     */
    public HTMLMediaElement(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Attempts to begin playback of the media.
     */
    public void play()  { call("play"); }

    /**
     * Pause playback of the media
     */
    public void pause()  { call("pause"); }

    /**
     * Resets the media element to its initial state and begins the process of selecting a media source and loading
     * the media in preparation for playback to begin at the beginning.
     */
    public void load()  { call("load"); }

    /**
     * Property indicates whether the media element has ended playback.
     */
    public boolean isEnded()  { return getMemberBoolean("ended"); }
}
