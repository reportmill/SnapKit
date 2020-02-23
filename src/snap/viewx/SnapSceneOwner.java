package snap.viewx;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * The controller class for a SnapScene.
 */
public class SnapSceneOwner extends ViewOwner {
    
    // The Scene
    SnapScene       _scene;
    
    // The ToolBar controller
    ToolBar         _toolBar = new ToolBar();

    // Whether to show controls
    boolean         _showControls = true;

/**
 * Returns whether to show controls (Act, Run, Reset).
 */
public boolean isShowControls()  { return _showControls; }

/**
 * Override to add controls.
 */
protected View createUI()
{
    // Create Scene
    _scene = (SnapScene)super.createUI(); if(!isShowControls()) return _scene;
    _scene.setAutoStart(false);
    
    // Configure ScrollView to hold scene
    ScrollView sview = new ScrollView(_scene); sview.setBorder(null);
    
    // Create border view and add Scene ScrollView, toolBar
    BorderView bview = new BorderView(); bview.setFont(Font.Arial12); bview.setFill(ViewUtils.getBackFill());
    bview.setCenter(sview); bview.setBottom(_toolBar.getUI()); bview.setBorder(Color.GRAY, 1);
    return bview;
}

/**
 * Returns the Scene.
 */
public SnapScene getScene()  { if(_scene==null) getUI(); return _scene; }

/**
 * Reset Scene.
 */
protected void resetScene()
{
    getScene().stop();
    View pauseBtn = getView("PauseButton");
    if(pauseBtn!=null) {
        pauseBtn.setText("Run"); pauseBtn.setName("RunButton");
        getView("ActButton").setDisabled(false);
    }
    
    SnapScene scene = (SnapScene)super.createUI();
    _scene.removeChildren();
    for(int i=0,iMax=scene.getChildCount();i<iMax;i++)
        _scene.addChild(scene.getChild(0));
    
    //World world = null;
    //try { world = _firstWorld.getClass().newInstance(); }
    //catch(Exception e) { new RuntimeException(e); }
    //setWorld(world);
}

/**
 * A class to provide SceneOwner controls.
 */
protected class ToolBar extends ViewOwner {
    
    /** Create UI. */
    protected View createUI()
    {
        // Create tool bar items
        Button actBtn = new Button("Act"); actBtn.setName("ActButton"); actBtn.setPrefSize(70,20);
        Button runBtn = new Button("Run"); runBtn.setName("RunButton"); runBtn.setPrefSize(70,20);
        Button resetBtn = new Button("Reset"); resetBtn.setName("ResetButton"); resetBtn.setPrefSize(70,20);
        Separator sep = new Separator(); sep.setPrefWidth(40); sep.setVisible(false);
        Label speedLbl = new Label("Frame Rate:"); speedLbl.setLeanX(HPos.CENTER); speedLbl.setFont(Font.Arial14);
        Slider speedSldr = new Slider(); speedSldr.setName("SpeedSlider"); speedSldr.setPrefWidth(180);
        TextField speedText = new TextField(); speedText.setName("SpeedText"); speedText.setPrefWidth(40);
        speedText.setAlign(HPos.CENTER);
    
        // Create toolbar
        RowView toolBar = new RowView(); toolBar.setAlign(Pos.CENTER); toolBar.setPadding(18,25,18,25); toolBar.setSpacing(15);
        toolBar.setChildren(actBtn, runBtn, resetBtn, sep, speedLbl, speedSldr, speedText);
        return toolBar;
    }
    
    /** Reset UI. */
    protected void resetUI()
    {
        setViewValue("SpeedSlider", getScene().getFrameRate()/100);
        setViewValue("SpeedText", Math.round(getScene().getFrameRate()));
    }
    
    /** Respond to UI changes. */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ActButton
        if(anEvent.equals("ActButton"))
            getScene().doAct();
        
        // Handle RunButton
        if(anEvent.equals("RunButton")) {
            View runBtn = anEvent.getView();
            runBtn.setText("Pause"); runBtn.setName("PauseButton");
            getView("ActButton").setDisabled(true);
            getScene().start();
        }
        
        // Handle PauseButton
        if(anEvent.equals("PauseButton")) {
            getScene().stop();
            View pauseBtn = anEvent.getView();
            pauseBtn.setText("Run"); pauseBtn.setName("RunButton");
            getView("ActButton").setDisabled(false);
        }
        
        // Handle ResetButton
        if(anEvent.equals("ResetButton"))
            resetScene();
        
        // Handle SpeedSlider
        if(anEvent.equals("SpeedSlider"))
            getScene().setFrameRate(anEvent.getFloatValue()*100);
    
        // Shouldn't need this
        getScene().requestFocus();
    }
}

}