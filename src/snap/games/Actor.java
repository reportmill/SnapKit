/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.*;
import snap.gfx.Image;
import snap.gfx.Paint;

/**
 * This class represents a game character in a StageView.
 */
public class Actor {

    // The ActorView
    protected ActorView _actorView;

    /**
     * Constructor.
     */
    public Actor()
    {
        super();
        _actorView = new ProxyActorView(this);
    }

    /**
     * Returns the actor view.
     */
    public ActorView getActorView()  { return _actorView; }

    /**
     * Returns the Stage.
     */
    public Stage getStage()  { return _actorView.getStageView() instanceof ProxyStageView stageView ? stageView.getStage() : null; }

    /**
     * Returns the StageView as given class.
     */
    public <T extends Stage> T getStage(Class<? extends Stage> aClass)
    {
        Stage stage = getStage();
        return aClass.isInstance(stage) ? (T) stage : null;
    }

    /**
     * Returns the actor name.
     */
    public String getName()  { return _actorView.getName(); }

    /**
     * Sets the actor name.
     */
    public void setName(String aName)  { _actorView.setName(aName); }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _actorView.getImage(); }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)  { _actorView.setImage(anImage); }

    /**
     * Returns the image name.
     */
    public String getImageName()  { return _actorView.getImageName(); }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)  { _actorView.setImageForName(imageName); }

    /**
     * Returns the X location of the view.
     */
    public double getX()  { return _actorView.getX(); }

    /**
     * Sets the X location of the view.
     */
    public void setX(double aValue)  { _actorView.setX(aValue); }

    /**
     * Returns the Y location of the view.
     */
    public double getY()  { return _actorView.getY(); }

    /**
     * Sets the Y location of the view.
     */
    public void setY(double aValue)  { _actorView.setY(aValue); }

    /**
     * Returns the width of the view.
     */
    public double getWidth()  { return _actorView.getWidth(); }

    /**
     * Sets the width of the view.
     */
    public void setWidth(double aValue)  { _actorView.setWidth(aValue); }

    /**
     * Returns the height of the view.
     */
    public double getHeight()  { return _actorView.getHeight(); }

    /**
     * Sets the height of the view.
     */
    public void setHeight(double aValue)  { _actorView.setHeight(aValue); }

    /**
     * Returns the mid x.
     */
    public double getMidX()  { return _actorView.getMidX(); }

    /**
     * Returns the mid y.
     */
    public double getMidY()  { return _actorView.getMidY(); }

    /**
     * Returns the view x/y.
     */
    public Point getXY()  { return _actorView.getXY(); }

    /**
     * Sets the view x/y.
     */
    public void setXY(double aX, double aY)  { _actorView.setXY(aX, aY); }

    /**
     * Returns the view size.
     */
    public Size getSize()  { return _actorView.getSize(); }

    /**
     * Sets the size.
     */
    public void setSize(Size aSize)  { _actorView.setSize(aSize); }

    /**
     * Sets the size.
     */
    public void setSize(double aW, double aH)  { _actorView.setSize(aW, aH); }

    /**
     * Returns the rotation of the view in degrees.
     */
    public double getRotate()  { return _actorView.getRotate(); }

    /**
     * Turn to given angle.
     */
    public void setRotate(double theDegrees)  { _actorView.setRotate(theDegrees); }

    /**
     * Returns the scale of this view.
     */
    public double getScale()  { return _actorView.getScale(); }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScale(double aValue)  {  _actorView.setScale(aValue); }

    /**
     * Sets the fill.
     */
    public void setFill(Paint aPaint)  { _actorView.setFill(aPaint); }

    /**
     * Move actor forward.
     */
    public void moveBy(double aDistance)
    {
        double distX = aDistance * Math.cos(Math.toRadians(getRotate()));
        double distY = aDistance * Math.sin(Math.toRadians(getRotate()));
        moveByXY(distX, distY);
    }

    /**
     * Move actor by given distance X and Y.
     */
    public void moveByXY(double distX, double distY)
    {
        double newX = getX() + distX;
        double newY = getY() + distY;
        setXY(newX, newY);
    }

    /**
     * Move actor to a point using setRotation and moveBy (so pen drawing works).
     */
    public void moveToXY(double aX, double aY)
    {
        double angleToXY = Point.getAngle(getMidX(), getMidY(), aX, aY);
        double distanceToXY = Point.getDistance(getMidX(), getMidY(), aX, aY);
        setRotate(angleToXY);
        moveBy(distanceToXY);
    }

    /**
     * Turn actor by given degrees.
     */
    public void turnBy(double theDegrees)
    {
        setRotate(getRotate() + theDegrees);
    }

    /**
     * Returns the angle of the line from this actor center point to current mouse point.
     */
    public double getAngleToMouse()
    {
        Stage stage = getStage();
        return Point.getAngle(getMidX(), getMidY(), stage.getMouseX(), stage.getMouseY());
    }

    /**
     * The act method.
     */
    protected void act()  { }
}