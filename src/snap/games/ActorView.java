/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Shape;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents a game character in a StageView.
 */
public class ActorView extends ParentView {

    // The actor
    private Actor _actor;

    // The actor class name
    private String _actorClassName;

    // Image name set in archival
    private String _imageName;

    // The ImageView
    private ImageView _imageView;

    // Constants for properties
    public static final String Image_Prop = ImageView.Image_Prop;

    /**
     * Constructor.
     */
    public ActorView()
    {
        super();
        _actor = new Actor();
        _actor._actorView = this;
    }

    /**
     * Constructor.
     */
    public ActorView(Actor anActor)
    {
        super();
        _actor = anActor;

        // Initialize name to simple class name
        setName(getClass().getSimpleName());
    }

    /**
     * Returns the actor.
     */
    public Actor getActor()  { return _actor; }

    /**
     * Returns the StageView.
     */
    public StageView getStageView()  { return getParent(StageView.class); }

    /**
     * Returns the StageView as given class.
     */
    public <T extends StageView> T getStageView(Class<? extends StageView> aClass)  { return (T) getParent(aClass); }

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
            if (getSize().isEmpty()) {
                setSize(anImage.getWidth(), anImage.getHeight());
                setXY(getX() - anImage.getWidth() / 2, getY() - anImage.getHeight() / 2);
            }

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
     * Returns the image name.
     */
    public String getImageName()  { return _imageName; }

    /**
     * Returns the image for given name.
     */
    private Image getImageForName(String imageName)
    {
        if (imageName.contains("."))
            return Game.getImageForClassResource(getClass(), imageName);
        return Game.getLibraryImageForName(imageName);
    }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)
    {
        _imageName = imageName;
        Image image = getImageForName(imageName);
        if (image != null)
            setImage(image);
    }

    /**
     * Returns the image view.
     */
    private ImageView getImageView()
    {
        if (_imageView != null) return _imageView;
        _imageView = new ImageView();
        _imageView.setFillWidth(true);
        _imageView.setFillHeight(true);
        _imageView.setGrowWidth(true);
        _imageView.setGrowHeight(true);
        _imageView.setPickable(false);
        addChild(_imageView);
        return _imageView;
    }

    /**
     * Returns whether this actor intersects given actor.
     */
    public boolean intersectsActor(ActorView actorView)
    {
        if (!getBounds().intersectsShape(actorView.getBounds()))
            return false;
        Shape thisBoundsInParent = localToParent(getBoundsShape());
        Shape otherBoundsInParent = actorView.localToParent(actorView.getBoundsShape());
        return thisBoundsInParent.intersectsShape(otherBoundsInParent);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public boolean isIntersectingActor(Class<? extends ActorView> aClass)  { return getIntersectingActor(aClass) != null; }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T extends ActorView> T getIntersectingActor(Class<T> aClass)
    {
        List<? extends View> actorViews = getStageView().getChildren();
        Stream<T> actorStream = (Stream<T>) actorViews.stream();
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).findFirst().orElse(null);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T extends ActorView> List<T> getIntersectingActors(Class<T> aClass)
    {
        List<? extends View> actorViews = getStageView().getChildren();
        Stream<T> actorStream = (Stream<T>) actorViews.stream();
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).toList();
    }

    /**
     * Moves this actor to given XY from current location, stopping if it hits actor of given class.
     */
    public void moveByXyNoCollision(double moveX, double moveY, Class<? extends ActorView> hitClass)
    {
        if (moveX != 0 || moveY != 0)
            moveToXyNoCollision(getX() + moveX, getY() + moveY, hitClass);
    }

    /**
     * Moves this actor to given XY from current location, stopping if it hits actor of given class.
     */
    public void moveToXyNoCollision(double newX, double newY, Class<? extends ActorView> hitClass)
    {
        // Store previous XY, try new XY and return if no collision
        double prevX = getX();
        double prevY = getY();
        setXY(newX, newY);
        if (!isIntersectingActor(hitClass))
            return;

        // Get values
        double dx = newX - prevX;
        double dy = newY - prevY;
        double distX = Math.abs(dx);
        double distY = Math.abs(dy);
        int signX = MathUtils.sign(dx);
        int signY = MathUtils.sign(dy);

        // Restore XY
        setXY(prevX, prevY);

        // Move to new X incrementally as long as no hit
        for (double incr = 0; incr < distX; incr++) {
            setX(getX() + signX);
            if (isIntersectingActor(hitClass)) {
                setX(getX() - signX);
                break;
            }
        }

        // Move to new Y incrementally as long as no hit
        for (int incr = 0; incr < distY; incr++) {
            setY(getY() + signY);
            if (isIntersectingActor(hitClass)) {
                setY(getY() - signY);
                break;
            }
        }
    }

    /**
     * Override to return stack layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new StackViewLayout(this); }

    /**
     * Override to look for default image if empty when shown.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        // If showing empty, look for default image for class and set
        if (aValue && getWidth() == 0 && getHeight() == 0 && getImage() == null) {
            Class<?> actorClass = getClass(); if (actorClass == ActorView.class) actorClass = _actor.getClass();
            Image defaultClassImage = Game.getImageForClass(actorClass);
            if (defaultClassImage != null)
                setImage(defaultClassImage);
        }

        super.setShowing(aValue);
    }

    /**
     * Override to archive X/Y and ImageName.
     */
    @Override
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXMLView(anArchiver);

        // Archive X,Y
        int attrIndex = xml.hasAttribute(Name_Prop) ? 1 : 0;
        if (getY() != 0) xml.addAttribute(new XMLAttribute(Y_Prop, getY()), attrIndex);
        if (getX() != 0) xml.addAttribute(new XMLAttribute(X_Prop, getX()), attrIndex);

        // Archive ImageName
        if (_imageName != null && !_imageName.isEmpty())
            xml.add("ImageName", _imageName);

        // Archive Actor class name
        if (_actorClassName != null && !_actorClassName.equals(Actor.class.getName()))
            xml.add("ActorClass", _actor.getClass().getName());

        // Return
        return xml;
    }

    /**
     * Override to support image name.
     */
    @Override
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Create Actor class
        _actorClassName = anElement.getAttributeValue("ActorClass");
        if (_actorClassName != null) {
            _actor = getActorForClassName(anArchiver.getOwnerClass(), _actorClassName);
            _actor._actorView = this;
        }

        super.fromXMLView(anArchiver, anElement);
        if (anElement.hasAttribute("ImageName")) {
            String imageName = anElement.getAttributeValue("ImageName");
            setImageForName(imageName);
        }
    }

    /**
     * Returns an actor instance for given class name.
     */
    private static Actor getActorForClassName(Class<?> ownerClass, String className)
    {
        ClassLoader classLoader = ownerClass != null ? ownerClass.getClassLoader() : ViewArchiver.class.getClassLoader();
        try { return (Actor) Class.forName(className, false, classLoader).getConstructor().newInstance(); }
        catch (Exception ignored) { }
        try { return (Actor) Class.forName("snap.games." + className, false, classLoader).getConstructor().newInstance(); }
        catch (Exception e) { System.err.println("ActorView: Can't find actor class: " + className); return new Actor(); }
    }
}