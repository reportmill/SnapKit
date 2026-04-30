/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.gfx.Image;
import snap.util.XMLArchiver;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snap.view.*;

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

        // Get default image for class and set
        Image defaultClassImage = Game.getImageForClass(getClass());
        if (defaultClassImage != null)
            setImage(defaultClassImage);
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
     * Override to return stack layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new StackViewLayout(this); }

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