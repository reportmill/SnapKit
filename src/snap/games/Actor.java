/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Image;
import snap.util.ListUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.view.ImageView;
import snap.view.ParentView;
import snap.view.StackView;
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

        // Initialize name to simple class name
        setName(getClass().getSimpleName());

        // Get default image for class and set
        Image defaultClassImage = Game.getImageForClass(getClass());
        if (defaultClassImage != null)
            setImage(defaultClassImage);
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

            // If not loaded, wait for load
            if (!anImage.isLoaded())
                anImage.waitForImageLoad();

            // If no actor size, set to image size
            if (getSize().isEmpty())
                setSize(anImage.getWidth(), anImage.getHeight());

            // If pref width not set, set width to image width maintaining center x
            if (!isPrefWidthSet() && getWidth() != anImage.getWidth()) {
                double dx = (anImage.getWidth() - getWidth()) / 2;
                setWidth(anImage.getWidth());
                setX(getX() + dx);
            }

            // If pref height not set, set height to image height maintaining center y
            if (!isPrefHeightSet() && getHeight() != anImage.getHeight()) {
                double dy = (anImage.getHeight() - getHeight()) / 2;
                setHeight(anImage.getHeight());
                setY(getY() + dy);
            }
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
        _imageView.setPickable(false);
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
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public boolean isIntersectingActor(Class<?> aClass)  { return getIntersectingActor(aClass) != null; }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T> T getIntersectingActor(Class<T> aClass)
    {
        List<Actor> actors = getGameView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> isIntersectingActor(actor, aClass));
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T> List<T> getIntersectingActors(Class<T> aClass)
    {
        List<Actor> actors = getGameView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> isIntersectingActor(actor, aClass));
    }

    /**
     * Returns whether given actor is intersecting and of matching class (class can be null).
     */
    protected boolean isIntersectingActor(Actor anActor, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return intersectsActor(anActor);
    }

    /**
     * Returns the first actor in given range radius that match given class (class can be null).
     */
    public <T> T getActorInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actors = getGameView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns the actors in given range radius that match given class (class can be null).
     */
    public <T> List<T> getActorsInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actors = getGameView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns whether given actor is in range and of matching class (class can be null).
     */
    protected boolean isActorInRange(Actor anActor, double aRadius, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return getDistanceToActor(anActor) <= aRadius;
    }

    /**
     * Returns the first actor hit by given point that match given class (class can be null).
     */
    public <T> T getActorAtXY(double aX, double aY, Class<T> aClass)
    {
        GameView gameView = getGameView();
        Point gameXY = localToParent(aX, aY, gameView);
        List<Actor> actors = getGameView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> actor != this && gameView.isActorAtXY(actor, gameXY.x, gameXY.y, aClass));
    }

    /**
     * Returns the actors hit by given point that match given class (class can be null).
     */
    public <T> List<T> getActorsAtXY(double aX, double aY, Class<T> aClass)
    {
        GameView gameView = getGameView();
        Point gameXY = localToParent(aX, aY, gameView);
        List<Actor> actors = getGameView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> actor != this && gameView.isActorAtXY(actor, gameXY.x, gameXY.y, aClass));
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
     * Returns whether at edge.
     */
    public boolean isAtGameViewEdge()
    {
        Rect actorBounds = localToParent(getBoundsShape(), getGameView()).getBounds();
        return actorBounds.x <= 0 || actorBounds.y <= 0 ||
            actorBounds.getMaxX() >= getWidth() || actorBounds.getMaxY() >= getHeight();
    }

    /**
     * The act method.
     */
    protected void act()  { }

    /**
     * Layout.
     */
    @Override
    protected void layoutImpl()  { StackView.layout(this); }

    /**
     * Pref width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)  { return StackView.getPrefWidth(this, aH); }

    /**
     * Pref height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)  { return StackView.getPrefHeight(this, aW); }

    /**
     * Override to support image name.
     */
    @Override
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXMLView(anArchiver, anElement);
        if (anElement.hasAttribute("ImageName")) {
            String imageName = anElement.getAttributeValue("ImageName");
            setImage(Game.getLibraryImageForName(imageName));
        }
    }
}