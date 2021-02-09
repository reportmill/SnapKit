/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.io.IOException;
import snap.web.*;

/**
 * A class to manage the loading and playing of a sound.
 */
public abstract class SoundClip {

    /**
     * Returns whether sound is playing.
     */
    public abstract boolean isPlaying();

    /**
     * Plays the sound.
     */
    public abstract void play();

    /**
     * Plays the sound repeatedly for given count.
     */
    public abstract void play(int aCount);

    /**
     * Tells sound to stop playing.
     */
    public abstract void stop();

    /**
     * Pauses a sound.
     */
    public abstract void pause();

    /**
     * Returns the sound length in milliseconds.
     */
    public abstract int getLength();

    /**
     * Returns the sound time in milliseconds.
     */
    public abstract int getTime();

    /**
     * Sets the sound time in milliseconds.
     */
    public abstract void setTime(int aTime);

    /**
     * Returns whether sound is recording.
     */
    public abstract boolean isRecording();

    /**
     * Starts a recording.
     */
    public abstract void recordStart();

    /**
     * Stops a recording.
     */
    public abstract void recordStop();

    /**
     * Saves this sound.
     */
    public abstract void save() throws IOException;

    /**
     * Returns a sound for a given source.
     */
    public static SoundClip get(Object aSource)
    {
        return GFXEnv.getEnv().getSound(aSource);
    }

    /**
     * Creates a new SnapSound for file name.
     */
    public static SoundClip get(Class aClass, String aName)
    {
        WebURL url = getSoundURL(aClass, aName); if (url==null) return null;
        WebFile file = url.getFile(); if (file==null) return null;
        return get(url);
    }

    /**
     * Creates a new empty clip.
     */
    public static SoundClip create()  { return GFXEnv.getEnv().createSound(); }

    /**
     * Creates a new SnapSound for file name.
     */
    protected static WebURL getSoundURL(Class aClass, String aName)
    {
        // Look for named sound file in same directory as class
        String name = aName; if (name.indexOf('.')<0) name += ".wav";
        WebURL url = WebURL.getURL(aClass, name);
        if (url==null)
            url = WebURL.getURL(aClass, "sounds/" + name);
        if (url==null)
            url = WebURL.getURL(aClass, "/sounds/" + name);
        return url;
    }
}