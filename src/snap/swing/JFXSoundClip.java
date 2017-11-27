package snap.swing;
import java.io.*;
import java.util.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.sound.sampled.*;
import snap.gfx.SoundClip;
import snap.util.*;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * A SoundClip implementation using JavaFX.
 */
public class JFXSoundClip extends SoundClip {

    // The source of the data
    Object                _source;
    
    // The URL for the source of this data
    WebURL                _url;
    
    // The JavaFX media object
    Media                 _media;

    // The line in when recording
    TargetDataLine        _lineIn;
    
    // The record thread
    Thread                _recordThead;
    
    // Whether file has been modified
    boolean               _modified; File _modFile;
    
    // A list of clips
    List <ClipPlayer>      _clips = new ArrayList();
    
    
    // Whether sound has been initialized
    static boolean     _init;

/**
 * Creates a new JFXSoundClip.
 */
public JFXSoundClip()  { checkInit(); }

/**
 * Creates a new SoundData from given source.
 */
public JFXSoundClip(Object aSource)  { _source = aSource; checkInit(); }

/**
 * Does JFX initialization.
 */
private void checkInit()  { if(_init) return; new JFXPanel(); _init = true; }

/**
 * Returns the source.
 */
public Object getSource()  { return _source; }

/**
 * Sets the source.
 */
protected void setSource(Object aSource)  { _source = aSource; }

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _url!=null? _url : (_url=createSourceURL()); }

/**
 * Creates the source URL from source if possible.
 */
protected WebURL createSourceURL()  { try { return WebURL.getURL(_source); } catch(Exception e) { return null; } }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { WebURL url = getSourceURL(); return url!=null? url.getFile() : null; }

/**
 * Returns the bytes.
 */
public byte[] getBytes()  { return getSourceFile().getBytes(); }

/**
 * Sets the bytes.
 */
public void setBytes(byte theBytes[])  { getSourceFile().setBytes(theBytes); }

/**
 * Creates a new clip.
 */
public ClipPlayer createClip()  { return getBytes()!=null? new ClipPlayer() : null; }

/**
 * Play.
 */
public void play()  { play(0); }

/**
 * Play sound with given loop count.
 */
public void play(int count)
{
    for(int i=0, iMax=_clips.size(); i<iMax; i++) { ClipPlayer clip = _clips.get(i);
        if(!clip.isRunning()) {
            clip.setMillisecondPosition(0); clip.loop(count); clip.start(); return; }
    }
    try { _clips.add(createClip()); _clips.get(_clips.size()-1).start(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Stops the clip(s).
 */
public void stop() { for(int i=0, iMax=_clips.size(); i<iMax; i++) _clips.get(i).stop(); }

/**
 * Pauses the clip(s).
 */
public void pause() { for(int i=0, iMax=_clips.size(); i<iMax; i++) _clips.get(i).pause(); }

/**
 * Returns whether sound is playing.
 */
public boolean isPlaying()
{
    for(int i=0, iMax=_clips.size(); i<iMax; i++) if(_clips.get(i).isRunning())
        return true;
    return false;
}

/**
 * Returns the sound length in milliseconds.
 */
public int getLength()  { return _clips.size()>0? _clips.get(0).getMillisecondLength() : 0; }

/**
 * Returns the sound time in milliseconds.
 */
public int getTime()  { return _clips.size()>0? _clips.get(0).getMillisecondPosition() : 0; }

/**
 * Sets the sound time in milliseconds.
 */
public void setTime(int aTime)  { if(_clips.size()>0) _clips.get(0).setMillisecondPosition(aTime); }

/**
 * Record start.
 */
public void recordStart()
{
    if(_recordThead!=null) return;
    _recordThead = new Thread() { public void run() {
       try { recordImpl(); }
       catch(Exception e) { System.err.println("SoundData.recordStart:" + e); }
    }};
    _recordThead.start();
}

/**
 * Record stop.
 */
public void recordStop()
{
    _lineIn.stop();
    try { _recordThead.join(); }
    catch(Exception e) { System.err.println("SoundData.recordStop:" + e); }
    finally { _recordThead = null; }
    _lineIn.close(); _lineIn = null;
}

/**
 * Whether file is recording.
 */
public boolean isRecording()  { return _recordThead!=null; }

/**
 * Record start.
 */
private void recordImpl() throws LineUnavailableException
{
    // Get audio format
    float sampleRate = 22050; // 8000, 11025, 16000, 22050, 44100
    int sampleSizeInBits = 16, channels = 1; // SampleSize: 8, 16, Channels: 1,2
    boolean signed = true, bigEndian = false;
    AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    
    // Get data line
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    _lineIn = (TargetDataLine)AudioSystem.getLine(info);

    // Setup and initialize the line
    _lineIn.open(format);
    _lineIn.start();

    int bufSize = (int)format.getSampleRate()*format.getFrameSize(); byte buffer[] = new byte[bufSize];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for(int len=_lineIn.read(buffer, 0, bufSize); len>0; len=_lineIn.read(buffer, 0, bufSize))
        out.write(buffer, 0, len);
    
    // Close stream and set bytes
    try { out.close(); } catch(Exception e) { System.err.println("SoundData.record" + e); }
    
    byte audio[] = out.toByteArray();
    InputStream input = new ByteArrayInputStream(audio);
    AudioInputStream ais = new AudioInputStream(input, format, audio.length/format.getFrameSize());
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try { AudioSystem.write(ais, AudioFileFormat.Type.WAVE, output); }
    catch(Exception e) { System.err.println("SoundData.recordStartImpl: AudioSystem.write failed: " + e); }
    
    // Set bytes
    setBytes(output.toByteArray());
    _media = null; _modified = true;
    if(_modFile==null) { _modFile = FileUtils.getTempFile("SoundData_Recorded.wav"); _modFile.deleteOnExit(); }
    try { FileUtils.writeBytes(_modFile, getBytes()); }
    catch(Exception e) { throw new RuntimeException(e); }
    _clips.clear();
}

/**
 * Whether file is modified.
 */
public boolean isModified()  { return _modified; }

/**
 * Override to clear modified.
 */
public void save() throws IOException
{
    WebFile file = getSourceFile(); if(file==null) throw new RuntimeException("SnapData: No file available");
    file.setBytes(getBytes());
    file.save();
    _modified = false;
}

/**
 * Returns the media.
 */
private Media getMedia()
{
    if(_media!=null) return _media;

    File file = getSourceFile().getStandardFile();
    if(_modified) file = _modFile;
    return new Media(file.toURI().toString());
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
    public void start()  { if(!isRunning()) _player.play(); _playing = true; _paused = false; }
    
    /** Stops the clip. */
    public void stop()  { _player.stop(); _playing = false; _paused = false; }
    
    /** Pauses the clip. */
    public void pause()  { _player.pause(); _playing = false; _paused = true; }
    
    /** Loops the clip. */
    public void loop(int aCount)  { _player.setCycleCount(aCount); }
    
    /** Whether clip is running - has been asked to play or capture sound. */
    public boolean isRunning()  { return _playing; } //{ return _player.getStatus()==MediaPlayer.Status.PLAYING; }
    
    /** Returns the millisecond length. */
    public int getMillisecondLength()  { return (int)_player.getCycleDuration().toMillis(); }
    
    /** Returns the millisecond position. */
    public int getMillisecondPosition()  { return (int)_player.getCurrentTime().toMillis(); }
    
    /** Sets the millisecond position. */
    public void setMillisecondPosition(long aPosition)  { _player.seek(Duration.millis(aPosition)); }
}

}