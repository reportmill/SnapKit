/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;

import snap.geom.Point;
import snap.gfx.*;
import snap.view.*;

/**
 * A JavaFX implementation of actor.
 */
public class SnapActor extends ImageView {

    // The actor pen
    SnapPen          _pen = new SnapPen();
    
/**
 * Creates a new actor.
 */
public SnapActor()
{
    if(getClass()!=SnapActor.class)
        setName(getClass().getSimpleName());
        
    Image img = Image.get(getClass(), getName() + ".png");
    if(img==null) img = Image.get(getClass(), "images/" + getName() + ".png");
    if(img!=null) {
        setImage(img);
        setSize(img.getWidth(),img.getHeight());
    }
}

/**
 * Returns the center X.
 */
public double getCenterX()  { return getX() + getWidth()/2; }

/**
 * Returns the center Y.
 */
public double getCenterY()  { return getY() + getHeight()/2; }

/**
 * Returns the cell X.
 */
public int getCellX()
{
    double cw = getScene()!=null? getScene().getCellWidth() : 1; return (int)Math.floor(getX()/cw);
}

/**
 * Returns the cell Y.
 */
public int getCellY()
{
    double ch = getScene()!=null? getScene().getCellHeight() : 1; return (int)Math.floor(getY()/ch);
}

/**
 * Move sprite forward.
 */
public void moveBy(double aCount)
{
    // Do move
    double x = getX() + aCount*Math.cos(Math.toRadians(getRotate()));
    double y = getY() + aCount*Math.sin(Math.toRadians(getRotate()));
    setXY(x, y);
    _pen.lineTo(getPenPoint());
}

/**
 * Move sprite to a point using setRotation and moveBy (so pen drawing works).
 */
public void moveTo(double anX, double aY)
{
    setRotate(getAngle(anX, aY));
    moveBy(getDistance(anX, aY));
}

/**
 * Turn sprite by given degrees.
 */
public void turnBy(double theDegrees)  { setRotate(getRotate() + theDegrees); }

/**
 * Scale actor by given amount.
 */
public void scaleBy(double aScale)  { setScaleX(getScaleX() + aScale); setScaleY(getScaleY() + aScale); }

/**
 * Returns whether actor intersects given rect in local coords.
 */
public boolean intersects(double aX, double aY, double aW, double aH)
{
    double maxx = aX + aW, maxy = aY + aH;
    return 0<maxx && aX<getWidth() && 0<maxy && aY<getHeight();
}

/**
 * Returns the intersecting actors of given class.
 */
/*public <T extends SnapActor> T getIntersectingActor(Class <T> aClass)
{
    return _parent.getIntersectingActor(getBoundsInParent(), aClass);
}*/

/**
 * Returns the intersecting actors of given class.
 */
/*public <T extends SnapActor> List <T> getIntersectingActors(Class <T> aClass)
{
    return _parent.getIntersectingActors(getBoundsInParent(), aClass);
}*/

/**
 * Returns the actors in range.
 */
public <T extends SnapActor> List <T> getActorsInRange(double aRadius, Class <T> aClass)
{
    List actors = new ArrayList();
    for(int i=getScene().getChildCount()-1; i>=0; i--) { View child = getScene().getChild(i);
        if(aClass==null || aClass.isInstance(child)) {
            if(getDistance((SnapActor)child)<=aRadius)
                actors.add(child); } }
    return actors;
}

/**
 * Returns the angle to the mouse point.
 */
public double getAngle(String aName)  { return getAngle(getScene().getX(aName), getScene().getY(aName)); }

/**
 * Returns the angle to the mouse point.
 */
public double getAngle(double mx, double my)
{
    double x = getCenterX(), y = getCenterY();
    double dx = mx - x, dy = my - y;
    double angle = Math.toDegrees(Math.atan(dy/Math.abs(dx)));
    if(dx<0) angle = 180 - angle;
    else if(dx==0) angle = dy>0? -90 : dy<0? 90 : getRotate();
    return angle;
}

/**
 * Returns the distance to the mouse point.
 */
public double getDistance(String aName)  { return getDistance(getScene().getX(aName), getScene().getY(aName)); }

/**
 * Returns the distance to the given actor.
 */
public double getDistance(SnapActor anActor)  { return getDistance(anActor.getCenterX(), anActor.getCenterY()); }

/**
 * Returns the distance to the given point.
 */
public double getDistance(double x2, double y2)
{
    double x1 = getCenterX(), y1 = getCenterY();
    double dx = x2 - x1, dy = y2 - y1;
    return Math.sqrt(dx*dx + dy*dy);
}

/**
 * Returns whether mouse button is down.
 */
public boolean isMouseDown()  { return getScene().isMouseDown(); }

/**
 * Returns whether mouse was clicked on this frame.
 */
public boolean isMouseClicked()  { return getScene().isMouseClicked(); }

/**
 * Returns whether a key is pressed down.
 */
public boolean isKeyDown(String aKey)  { return getScene().isKeyDown(aKey); }

/**
 * Returns whether a key was pressed in current frame.
 */
public boolean isKeyClicked(String aKey)  { return getScene().isKeyClicked(aKey); }

/**
 * Returns a named sound.
 */
public SoundClip getSound(String aName)  { return SoundClip.get(getClass(), aName); }

/**
 * Plays a named sound.
 */
public void playSound(String aName)  { SoundClip sound = getSound(aName); if(sound!=null) sound.play(); }

/**
 * Returns the pen.
 */
public SnapPen getPen()  { return _pen; }

/**
 * Returns the pen point.
 */
public Point getPenPoint()  { return localToParent(getWidth()/2,getHeight()/2); }

/**
 * Sets the pen color.
 */
public void setPenColor(String aString)  { _pen.setColor(aString); }

/**
 * Set pen down.
 */
public void penDown()
{
    if(isShowing()) _pen.penDown(getPenPoint());
    else _pen.setPenDown(true);
}

/**
 * Override to update pen location.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return; super.setShowing(aValue);
    if(_pen.isPenDown()) penDown();
}

/**
 * Returns the scene.
 */
public SnapScene getScene()  { return getParent(SnapScene.class); }

/**
 * The main execution loop.
 */
protected void main()  { }

/**
 * Calls the act method and actors act methods.
 */
void doAct()  { act(); }

/**
 * The act method.
 */
protected void act()  { }

}