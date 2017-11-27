package snap.swing;
import java.io.*;
import javax.sound.sampled.*;
import snap.gfx.SoundClip;
import snap.util.SnapUtils;

/**
 * Manages sound data from sound file bytes.
 */
public class SwingSoundClip extends SoundClip {

    // Where the sound data came from
    Object          _source;
    
    // The raw sound file bytes
    byte            _bytes[];

    // Bits per sample
    int             _bitsPerSample;    // 8 bit, 16 bit
    
    // Samples per second
    int             _samplesPerSecond; // Usually 5.5 khz, 11 khz, 22 khz or 44 khz
    
    // Channel count
    int             _channelCount;     // 1 mono, 2 stereo
    
    // Sample count
    int             _sampleCount;      // Number of samples in sound
    
    // Uncompressed samples
    byte            _sampleBytes[];

    // The sound clip
    Clip            _clip;
    
    // The SoundRecorder
    SoundRecorder   _sndRec = new SoundRecorder();

    // Constants for bit rate
    public static final int  BitRate5k = 0;
    public static final int  BitRate11k = 1;
    public static final int  BitRate22k = 2;
    public static final int  BitRate44k = 3;
    public static final int  BitRateUndefined = 999;

/**
 * Creates a new sound data for given source.
 */
public SwingSoundClip(Object aSource)
{
    _source = aSource;
    _bytes = SnapUtils.getBytes(aSource);
}

/**
 * Returns the sound bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Sets the sound bytes.
 */
public void setBytes(byte theBytes[])  { _bytes = theBytes; _clip = null; }

/**
 * Returns the bits per sample.
 */
public int getBitsPerSample()  { readData(); return _bitsPerSample; }

/**
 * Returns the samples per second.
 */
public int getSamplesPerSecond()  { readData(); return _samplesPerSecond; }

/**
 * Returns the channel count.
 */
public int getChannelCount()  { readData(); return _channelCount; }

/**
 * Returns the sample count.
 */
public int getSampleCount()  { readData(); return _sampleCount; }

/**
 * Returns the sample bytes.
 */
public byte[] getSampleBytes()  { readData(); return _sampleBytes; }

/**
 * Returns the bit rate.
 */
public int bitRate()
{
    switch(getSamplesPerSecond()/5000) {
        case 1: return BitRate5k; case 2: return BitRate11k;
        case 4: return BitRate22k; case 8: return BitRate44k;
        default: return BitRateUndefined;
    }
}

/**
 * Reads sound format info from sounds data bytes.
 */
private void readData()
{
    if(_sampleBytes!=null) return;
    
    // Create audio input stream
    AudioInputStream audioStream;
    try { audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(getBytes())); }
    catch(Exception e) { throw new Error(e); }
    
    // Get audio format
    AudioFormat audioFormat = audioStream.getFormat();
    
    // Create line info
    DataLine.Info lineInfo = new DataLine.Info(Clip.class, audioFormat);
    
    // If not supported, try to get deoded format
    if(!AudioSystem.isLineSupported(lineInfo)) {
        
        // Get decoded format
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
            audioFormat.getChannels(), audioFormat.getChannels()*2, audioFormat.getSampleRate(), false);
        
        // Get decoded stream
        audioStream = AudioSystem.getAudioInputStream(audioFormat, audioStream);
    }
    
    // Get info
    _bitsPerSample = audioFormat.getSampleSizeInBits();
    _samplesPerSecond = (int)audioFormat.getSampleRate();
    _channelCount = audioFormat.getChannels();
    _sampleBytes = SnapUtils.getBytes(audioStream);
    _sampleCount = _sampleBytes.length/audioFormat.getFrameSize();
}

/**
 * Returns whether sound is playing.
 */
public boolean isPlaying()  { return getClip().isRunning(); }

/**
 * Plays the sound.
 */
public void play()  { play(0); }

/**
 * Plays the sound repeatedly for given count.
 */
public void play(int aCount)
{
    getClip().setFramePosition(0);
    getClip().loop(aCount);
}

/**
 * Tells sound to stop playing.
 */
public void stop()  { getClip().stop(); }

/**
 * Pauses a sound.
 */
public void pause()  { System.err.println("SwingSoundClip.pause: Not implemented"); }

/**
 * Returns the sound length in milliseconds.
 */
public int getLength()  { return getClip().getFrameLength(); }

/**
 * Returns the sound time in milliseconds.
 */
public int getTime()  { return getClip().getFramePosition(); }

/**
 * Sets the sound time in milliseconds.
 */
public void setTime(int aTime)  { getClip().setFramePosition(aTime); }

/**
 * Returns the clip, creating it if requested.
 */
public Clip getClip()
{
    // If clip is loaded, just return
    if(_clip!=null) return _clip;
    
    // Create audio input stream
    AudioInputStream audioStream;
    try { audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(getBytes())); }
    catch(Exception e) { e.printStackTrace(); return null; }
    
    // Get audio format
    AudioFormat audioFormat = audioStream.getFormat();
    
    // Create line info
    DataLine.Info lineInfo = new DataLine.Info(Clip.class, audioFormat);
    
    // If not supported, try to get deoded format
    if(!AudioSystem.isLineSupported(lineInfo)) {
        
        // Complain
        System.out.println("Converting from " + audioFormat.getEncoding() + " to " + AudioFormat.Encoding.PCM_SIGNED);
        
        // Get decoded format
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
            audioFormat.getChannels(), audioFormat.getChannels()*2, audioFormat.getSampleRate(), false);
        
        // Get decoded stream
        audioStream = AudioSystem.getAudioInputStream(audioFormat, audioStream);
        
        // Get decoded line info
        lineInfo = new DataLine.Info(Clip.class, audioFormat);
    }
    
    // Create clip
    try { _clip = (Clip)AudioSystem.getLine(lineInfo); }
    catch(Exception e) { e.printStackTrace(); return null; }
    
    // Add a line listener to send events to somebody when the sound finishes
    /*_clip.addLineListener(new LineListener() {
        public void update(LineEvent event)
        {
            // this could probably be ==
            if (event.getType().equals(LineEvent.Type.STOP)) {
                // notify rm listeners on the main thread
                if (getListenerCount(RMSoundListener.class)>0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for(int i=0, iMax=getListenerCount(RMSoundListener.class); i<iMax; i++) {
                                RMSoundListener listener = (RMSoundListener)getListener(RMSoundListener.class, i);
                                listener.soundStopped(RMSoundShape.this);
                            }
                        }
                    });
                }
                // flush the clip's resources
                _clip.flush();
            }
        }
    });*/
    
    // Have clip open stream
    try { _clip.open(audioStream); }
    catch(Exception e) { e.printStackTrace(); return null; }
        
    // Return clip
    return _clip;
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
    _sndRec.stopRecording();
    byte bytes[] = _sndRec.getBytes();
    if(bytes!=null)
        setBytes(bytes);
}

/**
 * Override to clear modified.
 */
public void save() throws IOException
{
    System.out.println("SwingSoundClip.save: Not implemented");
    //WebFile file = getSourceFile(); if(file==null) throw new RuntimeException("SnapData: No file available");
    //file.setBytes(getBytes());
    //file.save();
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    if(!(anObj instanceof SwingSoundClip)) return false;
    SwingSoundClip other = (SwingSoundClip)anObj;
    if(!SnapUtils.equals(other._bytes, _bytes)) return false;
    return true;
}

/**
 * Returns whether sound data can read given extension.
 */
public static boolean canRead(String anExt)
{
    AudioFileFormat.Type types[] = AudioSystem.getAudioFileTypes();
    for(int i=0; i<types.length; i++)
        if(types[i].getExtension().equalsIgnoreCase(anExt))
            return true;
    return false;
}

}