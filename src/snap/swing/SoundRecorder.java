package snap.swing;
import java.io.*;
import javax.sound.sampled.*;
import snap.util.*;

/**
 * A class to record sounds.
 */
public class SoundRecorder {
    
    // The recorded sounds bytes
    byte                  _bytes[];

    // The line in when recording
    TargetDataLine        _lineIn;
    
    // The record thread
    Thread                _recordThread;
    
    // The temp file that holds the last recorded sound
    File                  _sndFile;
    
/**
 * Returns the bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Sets the bytes.
 */
public void setBytes(byte theBytes[])  { _bytes = theBytes; }

/**
 * Returns the temporary file that holds the last recorded bytes.
 */
public File getSoundFile()  { return _sndFile; }

/**
 * Whether file is recording.
 */
public boolean isRecording()  { return _recordThread!=null; }

/**
 * Start recording.
 */
public void startRecording()
{
    // If already recording, just return
    if(_recordThread!=null) return;
    
    // Clear bytes/file
    _bytes = null; _sndFile = null;
    
    // Create new thread and start it
    _recordThread = new Thread() { public void run() {
       try { startRecordingImpl(); }
       catch(Exception e) { System.err.println("SoundRecorder.recordStart:" + e); }
    }};
    _recordThread.start();
}

/**
 * Start recording (real implementation).
 */
private void startRecordingImpl() throws LineUnavailableException
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
    byte bytes[] = output.toByteArray();
    setBytes(bytes);
    
    // Create SoundFile and write bytes
    _sndFile = FileUtils.getTempFile("SoundRec_Last.wav"); _sndFile.deleteOnExit();
    try { FileUtils.writeBytes(_sndFile, bytes); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Stop recording.
 */
public void stopRecording()
{
    _lineIn.stop();
    try { _recordThread.join(); }
    catch(Exception e) { System.err.println("SoundRecorder.recordStop:" + e); }
    finally { _recordThread = null; }
    _lineIn.close(); _lineIn = null;
}

}