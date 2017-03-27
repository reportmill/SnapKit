package snap.viewx;
import snap.view.ViewOwner;

/**
 * The controller class for a SnapScene.
 */
public class SnapSceneOwner extends ViewOwner {

/**
 * Returns the Scene.
 */
public SnapScene getScene()  { return (SnapScene)getUI(); }

}