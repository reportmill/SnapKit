/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.Image;
import snap.util.ListUtils;
import snap.view.ImageView;
import snap.view.ParentView;
import java.util.List;

/**
 * This class represents a game character in a GameView.
 */
public class Actor extends ParentView {

    // The ImageView
    private ImageView _imageView;

    // Constants for properties
    public static final String Image_Prop = ImageView.Image_Prop;

    /**
     * Constructor.
     */
    public Actor()
    {
        super();

        // Initialize name to class
        if (getClass() != Actor.class)
            setName(getClass().getSimpleName());

        // Get default image and set
        Image defaultImage = getDefaultImage();
        if (defaultImage != null)
            setImage(defaultImage);
    }

    /**
     * Returns the GameView.
     */
    public GameView getGameView()  { return getParent(GameView.class); }

    /**
     * Returns the GameView as given class.
     */
    public <T extends GameView> T getGameView(Class<? extends GameView> aClass)  { return (T) getParent(aClass); }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _imageView != null ? _imageView.getImage() : null; }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        if (anImage == getImage()) return;

        // Set image
        batchPropChange(Image_Prop, getImage(), anImage);
        getImageView().setImage(anImage);

        // Update size
        if (anImage != null) {
            if (!anImage.isLoaded())
                anImage.waitForImageLoad();
            if (getSize().isEmpty())
                setSize(anImage.getWidth(), anImage.getHeight());
        }

        // Fire prop change
        fireBatchPropChanges();
    }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)
    {
        Image image = Game.getImageForClassResource(getClass(), imageName);
        setImage(image);
    }

    /**
     * Returns the image view.
     */
    private ImageView getImageView()
    {
        if (_imageView != null) return _imageView;
        _imageView = new ImageView();
        addChild(_imageView);
        return _imageView;
    }

    /**
     * Move actor forward.
     */
    public void moveBy(double aCount)
    {
        double newX = getX() + aCount * Math.cos(Math.toRadians(getRotate()));
        double newY = getY() + aCount * Math.sin(Math.toRadians(getRotate()));
        setXY(newX, newY);
    }

    /**
     * Move actor to a point using setRotation and moveBy (so pen drawing works).
     */
    public void moveToXY(double anX, double aY)
    {
        setRotate(getAngleToXY(anX, aY));
        moveBy(getDistanceToXY(anX, aY));
    }

    /**
     * Turn actor by given degrees.
     */
    public void turnBy(double theDegrees)
    {
        setRotate(getRotate() + theDegrees);
    }

    /**
     * Turn actor to given point X/Y.
     */
    public void turnToXY(double aX, double aY)
    {
        double angle = getAngleToXY(aX, aY);
        turnBy(angle - getRotate());
    }

    /**
     * Turn actor to given point X/Y.
     */
    public void turnToActor(Actor anActor)
    {
        GameView gameView = getGameView();
        Point otherCenterInParent = anActor.localToParent(anActor.getMidX(), anActor.getMidY(), gameView);
        Point otherCenterInLocal = parentToLocal(otherCenterInParent.x, otherCenterInParent.y, gameView);
        turnToXY(otherCenterInLocal.x, otherCenterInLocal.y);
    }

    /**
     * Scale actor by given amount.
     */
    public void scaleBy(double aScale)
    {
        setScaleX(getScaleX() + aScale);
        setScaleY(getScaleY() + aScale);
    }

    /**
     * Returns the actors in range.
     */
    public <T extends Actor> List<T> getActorsInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actorsReversed = getGameView().getActorsReversed();
        return (List<T>) ListUtils.filter(actorsReversed, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns whether given actor is in range and of matching class.
     */
    private boolean isActorInRange(Actor anActor, double aRadius, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return getDistanceToActor(anActor) <= aRadius;
    }

    /**
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToXY(double aX, double aY)  { return Point.getAngle(getMidX(), getMidY(), aX, aY); }

    /**
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToActor(Actor anActor)  { return getAngleToXY(anActor.getMidX(), anActor.getMidY()); }

    /**
     * Returns the angle of the line from this actor center point to current mouse point.
     */
    public double getAngleToMouse()
    {
        GameView gameView = getGameView();
        return getAngleToXY(gameView.getMouseX(), gameView.getMouseY());
    }

    /**
     * Returns the distance from this actor center point to given point.
     */
    public double getDistanceToXY(double aX, double aY)  { return Point.getDistance(getMidX(), getMidY(), aX, aY); }

    /**
     * Returns the distance from this actor center point to given actor center point.
     */
    public double getDistanceToActor(Actor anActor)  { return getDistanceToXY(anActor.getMidX(), anActor.getMidY()); }

    /**
     * Returns the default image for this actor.
     */
    public Image getDefaultImage()
    {
        Image defaultImage = Image.getImageForClassResource(getClass(), getName() + ".png");
        if (defaultImage == null)
            defaultImage = Image.getImageForClassResource(getClass(), "images/" + getName() + ".png");
        if (defaultImage != null && !defaultImage.isLoaded())
            defaultImage.waitForImageLoad();
        return defaultImage;
    }

    /**
     * Returns whether actor intersects given actor.
     */
    public boolean intersectsActor(Actor anActor)
    {
        if (!getBounds().intersectsShape(anActor.getBounds()))
            return false;
        Shape thisBoundsInParent = localToParent(getBoundsShape());
        Shape otherBoundsInParent = anActor.localToParent(anActor.getBoundsShape());
        return thisBoundsInParent.intersectsShape(otherBoundsInParent);
    }

    /**
     * The act method.
     */
    protected void act()  { }

    /**
     * Layout.
     */
    @Override
    protected void layoutImpl()
    {
        if (_imageView != null)
            _imageView.setSize(getWidth(), getHeight());
    }

    /**
     * Pref width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)  { return _imageView != null ? _imageView.getPrefWidth(aH) : 0; }

    /**
     * Pref height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)  { return _imageView != null ? _imageView.getPrefHeight(aW) : 0; }
}