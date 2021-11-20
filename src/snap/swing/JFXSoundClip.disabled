package snap.swing;
import java.io.*;
import java.util.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import snap.gfx.SoundClip;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * A SoundClip implementation using JavaFX.
 */
public class JFXSoundClip extends SoundClip {

    // The source of the data
    private Object  _src;
    
    // The URL for the source of this data
    private WebURL  _url;
    
    // The JavaFX media object
    private Media  _media;
    
    // A list of clips
    private List <ClipPlayer>  _clips = new ArrayList();
    
    // The SoundRecorder
    private SoundRecorder  _sndRec = new SoundRecorder();

    // Whether sound has been initialized
    private static boolean  _init;

    /**
     * Creates a new JFXSoundClip.
     */
    public JFXSoundClip()  { checkInit(); }

    /**
     * Creates a new SoundData from given source.
     */
    public JFXSoundClip(Object aSource)  { _src = aSource; checkInit(); }

    // Does JFX initialization.
    private void checkInit()
    {
        if (_init) return;
        new JFXPanel();
        _init = true;
    }

    /**
     * Returns the source.
     */
    public Object getSource()  { return _src; }

    /**
     * Sets the source.
     */
    protected void setSource(Object aSource)  { _src = aSource; }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()
    {
        return _url!=null ? _url : (_url=WebURL.getURL(_src));
    }

    /**
     * Returns the source file.
     */
    public WebFile getSourceFile()
    {
        WebURL url = getSourceURL();
        return url!=null ? url.getFile() : null;
    }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return getSourceFile().getBytes(); }

    /**
     * Sets the bytes.
     */
    public void setBytes(byte theBytes[])
    {
        getSourceFile().setBytes(theBytes);
        _media = null; _clips.clear();
    }

    /**
     * Returns whether sound is playing.
     */
    public boolean isPlaying()
    {
        for (int i=0, iMax=_clips.size(); i<iMax; i++)
            if (_clips.get(i).isRunning())
                return true;
        return false;
    }

    /**
     * Play.
     */
    public void play()  { play(0); }

    /**
     * Play sound with given loop count.
     */
    public void play(int count)
    {
        // Look for existing clip that isn't in use
        for (int i=0, iMax=_clips.size(); i<iMax; i++) { ClipPlayer clip = _clips.get(i);
            if (!clip.isRunning()) {
                clip.setMillisecondPosition(0); clip.loop(count);
                clip.start();
                return;
            }
        }

        // Otherwise, create/add/start new clip
        try { _clips.add(createClip()); _clips.get(_clips.size()-1).start(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    // Creates new clip.
    private ClipPlayer createClip()
    {
        return getBytes()!=null ? new ClipPlayer() : null;
    }

    /**
     * Stops the clip(s).
     */
    public void stop()
    {
        for (int i=0, iMax=_clips.size(); i<iMax; i++)
            _clips.get(i).stop();
    }

    /**
     * Pauses the clip(s).
     */
    public void pause()
    {
        for (int i=0, iMax=_clips.size(); i<iMax; i++)
            _clips.get(i).pause();
    }

    /**
     * Returns the sound length in milliseconds.
     */
    public int getLength()
    {
        return _clips.size()>0 ? _clips.get(0).getMillisecondLength() : 0;
    }

    /**
     * Returns the sound time in milliseconds.
     */
    public int getTime()
    {
        return _clips.size()>0 ? _clips.get(0).getMillisecondPosition() : 0;
    }

    /**
     * Sets the sound time in milliseconds.
     */
    public void setTime(int aTime)
    {
        if (_clips.size()>0)
            _clips.get(0).setMillisecondPosition(aTime);
    }

    /**
     * Whether file is recording.
     */
    public boolean isRecording()  { return _sndRec.isRecording(); }

    /**
     * Record start.
     */
    public void recordStart()  { _sndRec.startRecording(); }

    /**
     * Record stop.
     */
    public void recordStop()
    {
        // Stop recording
        _sndRec.stopRecording();

        // If sound bytes collected, set new bytes
        byte bytes[] = _sndRec.getBytes();
        if (bytes!=null)
            setBytes(bytes);
    }

    /**
     * Override to clear modified.
     */
    public void save() throws IOException
    {
        WebFile file = getSourceFile();
        if (file==null)
            throw new RuntimeException("JFXSoundClip.save: No file available");
        file.save();
    }

    /**
     * Returns the media.
     */
    private Media getMedia()
    {
        // If already set, just return
        if (_media!=null) return _media;

        // Get sound file
        File file = getSourceFile().getJavaFile();
        if (_sndRec.getSoundFile()!=null)
            file = _sndRec.getSoundFile();

        // Create media from sound file
        String spath = file.toURI().toString();
        return new Media(spath);
    }

    /**
     * A class to represent a clip.
     */
    public class ClipPlayer {

        // The media player
        MediaPlayer  _player;

        // Whether sound is playing, is paused
        boolean      _playing, _paused;

        /** Creates a new sound clip. */
        public ClipPlayer()
        {
            _player = new MediaPlayer(getMedia());
            _player.setOnEndOfMedia(() -> stop());
        }

        /** Starts the clip. */
        public void start()
        {
            if (!isRunning())
                _player.play();
            _playing = true;
            _paused = false;
        }

        /** Stops the clip. */
        public void stop()
        {
            _player.stop(); _playing = false; _paused = false;
        }

        /** Pauses the clip. */
        public void pause()
        {
            _player.pause(); _playing = false; _paused = true;
        }

        /** Loops the clip. */
        public void loop(int aCount)
        {
            _player.setCycleCount(aCount);
        }

        /** Whether clip is running - has been asked to play or capture sound. */
        public boolean isRunning()  { return _playing; } //{ return _player.getStatus()==MediaPlayer.Status.PLAYING; }

        /** Returns the millisecond length. */
        public int getMillisecondLength()
        {
            return (int)_player.getCycleDuration().toMillis();
        }

        /** Returns the millisecond position. */
        public int getMillisecondPosition()
        {
            return (int)_player.getCurrentTime().toMillis();
        }

        /** Sets the millisecond position. */
        public void setMillisecondPosition(long aPosition)
        {
            _player.seek(Duration.millis(aPosition));
        }
    }
}