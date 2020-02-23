/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * A WebPage subclass for a sound file.
 */
public class SoundPage extends WebPage {

    // Contents of a sampled audio file
    SoundClip         _clip;
    
    // A Timer
    ViewTimer         _timer = new ViewTimer(100, t -> sendEvent("ProgressTimer"));
    
/**
 * Returns the clip to be edited.
 */
public SoundClip getClip()
{
    if(_clip==null)
        try { _clip = SoundClip.get(getFile()); }
        catch(Exception e) { System.err.println(e); }
    return _clip;
}

/**
 * Override to stop sound when file pane removed.
 */
public void notifyPageRemoved(WebBrowser aBrowser)  { stop(); }

/**
 * Start playing the sound at the current position.
 */
public void play()
{
    getClip().play();
    _timer.stop(); _timer.start();
    setViewText("PlayButton", "Stop");
}

/**
 * Stop playing the sound, but retain the current position.
 */
public void stop()
{
    _timer.stop();
    if(_clip!=null) { _clip.stop(); _clip.setTime(0); }
    setViewText("PlayButton", "Play");
}

/**
 * Skip to the specified position. Called when user drags the slider.
 */
public void skip(int position)
{
    if(position<0 || position>_clip.getLength()) return;
    _clip.setTime(position);
}

/**
 * Creates the UI panel.
 */
protected View createUI()
{
    // Create play button
    Button playButton = new Button(); playButton.setText("Play"); playButton.setName("PlayButton");
    playButton.setMinWidth(100);
    addViewBinding(playButton, "Enabled", "Clip");
    
    // Create progress slider
    Slider progSlider = new Slider(); progSlider.setName("ProgressSlider"); progSlider.setPrefWidth(200);
    addViewBinding(progSlider, "Enabled", "Clip");
    
    // Create time label
    Label timeLabel = new Label("0"); timeLabel.setName("TimeLabel"); timeLabel.setPrefWidth(36);

    // Put those controls in a panel
    RowView playPanel = new RowView(); playPanel.setSpacing(5); playPanel.setPadding(20,10,20,10);
    playPanel.setBorder(Color.BLACK, 1);
    playPanel.setChildren(playButton, progSlider, timeLabel);
    Label playPanelTitle = new Label("Playback");

    // Create record button
    Button recButton = new Button(); recButton.setText("Record"); recButton.setName("RecordButton");
    recButton.setMinWidth(100);
    
    // Create save button
    Button saveButton = new Button(); saveButton.setText("Save"); saveButton.setName("SaveButton");
    saveButton.setMinWidth(100);
    addViewBinding(saveButton, "Enabled", "Data.Modified");
    
    // Put those controls in a panel
    RowView recSavePane = new RowView(); recSavePane.setSpacing(5); recSavePane.setPadding(20,20,20,20);
    recSavePane.setBorder(Color.BLACK,1);
    recSavePane.setChildren(recButton, saveButton);
    Label recSavePaneTitle = new Label("Record/Save");
    
    // Return VBox, add pieces and return
    ColView vbox = new ColView(); vbox.setAlign(Pos.TOP_CENTER); vbox.setPadding(50,50,50,50);vbox.setSpacing(10);
    vbox.setChildren(playPanelTitle, playPanel, recSavePaneTitle, recSavePane);
    return vbox;
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    setViewValue("ProgressSlider", _clip!=null? _timer.getTime() : 0);
    getView("ProgressSlider", Slider.class).setMax(_clip!=null? _clip.getLength() : 0);
    setViewValue("TimeLabel", _clip!=null? _clip.getLength() : 0);
}

/**
 * Respond to UI changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    SoundClip sound = getClip();
    
    // Handle ProgressTimer
    if(anEvent.equals("ProgressTimer")) {
        if(_clip.isPlaying()) setViewValue("ProgressSlider", _clip.getTime());
        else stop();
    }
    
    // Handle PlayButton
    else if(anEvent.equals("PlayButton")) {
        if(getClip().isPlaying()) stop();
        else play();
    }
    
    // Handle ProgressSlider
    else if(anEvent.equals("ProgressSlider")) {
        int value = getViewIntValue("ProgressSlider");
        setViewText("TimeLabel", value / 1000 + "." + (value % 1000) / 100); // Update the time label
        if(value != _timer.getTime()) // If we're not already there, skip there.
            skip(value);
    }
    
    // Handle RecordButton
    else if(anEvent.equals("RecordButton")) {
        if(sound.isRecording()) {
            sound.recordStop();
            setViewText("RecordButton", "Record");
        }
        else {
            sound.recordStart();
            setViewText("RecordButton", "Stop");
        }
    }
    
    // Handle SaveButton
    else if(anEvent.equals("SaveButton")) {
        try { sound.save(); }
        catch(Exception e2) { throw new RuntimeException(e2); }
    }
}

}