# SnapKit - a Java UI Kit for the Modern World

SnapKit is a new Java UI kit for creating rich Java Client applications that achieve the original promise of Java
by running pixel-perfect and native on the desktop and in the browser ([WORA](https://en.wikipedia.org/wiki/Write_once,_run_anywhere)).

Check out [demos of SnapKit running in the browser](http://www.reportmill.com/snaptea/).

## Everything in its place

SnapKit runs optimally everywhere utilizing a high level design where low-level functionality (such as painting, user
input, windowing, system clipboard and drag-and-drop) is provided via interfaces to native platform implementations.
This makes SnapKit itself comparatively small and simple, light-weight and performant. When compiled to the browser
(via [TeaVM](http://teavm.org), many applications are 1 Mb in size (compressed).

## So much to love about Swing

Why do we need another UI kit? Because Swing is out of date, and JavaFX missed the boat.
And neither run natively in the browser. A list of things to love about Swing:

    - Solid view hierarchy and set of controls/components
    
    - Relatively easy to create and update UI and respond to user input and UI changes
    
    - Full set of geometric shape primitives: Line, Rect, Ellipse, Path, Polygon, etc.
	
    - Easily set border, background, font on any component with simple API
	
    - The whole convenient painting model - just override paint() to customize
	
    - It handles property changes in conventional Java property change manner
	
    - It binds easily with POJOs
	

## And much to love about JavaFX

JavaFX rewrote the rulebook for Java UI by doing everything different. Still, there was much to love:

    - Easily mix graphics and app controls

    - Easily add gradients, textures, effects
	
    - Set arbitrary transforms (rotate, scale, skew) on any node
	
    - It has built-in binding support to easily wire values across objects
	
    - It has a full set of nodes for easy layout: Box, BorderView, StackPane, etc.
	
    - It has support for easily defining UI in a separate text file (FXML)
	

## What's to love about SnapKit?

    - It provides all these features
	
    - It runs on top of Swing, JavaFX and HTML DOM
	
    - It is easily portable to any future UI kit and platform
	
    - The base class is called View. Now that puts the V in MVC!
	
    - The ViewOwner class provides control functionally (whoops, there goes the C)
	
    - The ViewEvent class unifies all input events for more consistent handling
    
    
## The ViewOwner

Now here's the thing that really hurt Swing: There was no standard convention for the basics of UI: Create, Init, Reset, Respond.
    
This resulted in confusing controller code where UI controls often had code to do all four functions
in the same place. This initially seems simple and attractive, but falls apart when dozens of inter-dependent
controls are present.

Here is a simple Swing example that quickly gets out of control when extended to many
properties and controls:

```
_textField = new JTextField();
_textField.setText("Initial Value");
_textField.addActionListener(event -> {
    _myModel.updatePropertyForTextField(_textField.getText());
    _textField.setText(_myModel.getPropertyForTextField());
});
```

Here is the same thing with a ViewOwner:

```
/** Create UI. */
public View createUI()
{
    _textField = new TextField();
    return _textField;
}

/** Initialize UI. */
public void initUI()
{
    _textField.setText("Initial Value");
}

/** Reset UI. */
public void resetUI()
{
    // Update TextField from Model
    _textField.setText(_myModel.getPropertyForTextField());
}

/** Respond UI. */
public void respondUI(ViewEvent anEvent)
{
    // Update Model from TextField
    if (anEvent.equals(_textField))
        _myModel.updatePropertyForTextField(_textField.getText());
}
```

	
## The Graphics Package

One of the great aspects of Swing is the separation and synergy between the core graphics layer (Java2D) and
the UI layer. SnapKit provides this same separation with the snap.gfx package that contains:

    - Full set of geometric primitives: Rect, Point, Size, Insets, Pos (for alignment)
	
    - Transform for arbitrary transforms and coordinate conversions: rotate, scale, skew
	
    - Full set of Shape primitives: Rect, RoundRect, Arc, Ellipse, Line, Path, Polygon
	
    - Paint define fill styles with common subclasses: Color, GradientPaint, ImagePaint
	
    - Stroke for defining outline style, and Border for a stroke in a specific Paint
	
    - Effects for rich rendering: Shadow, Reflect, Emboss, Blur
	
    - Font and FontFile objects (wrap around platform fonts)
	
    - Painter capable of rendering shapes, images and text with transform, fill, stroke, effect
	
    - Image object (wraps around platform image)
	
    - RichText object for managing large text content with attributes
	
    - TextStyle object to manage a set of attributes: font, color, underline, hyper links, format, etc.
	
    - TextBox object for managing RichText in a geometric region (with spelling and hyphenation)
	
    - SoundClip for playing sounds
    

## The View Package

And the essentail part of a good UI kit is the set of classes that model the scene graph and
standard UI controls.

    - View for managing hierarchy of coordinate systems, drawing and input events
	
    - Full set of classes for graphics primitives: RectView, ShapeView, ImageView, StringView
	
    - Label: Convenient View+StringView+View layout to easily label UI
	
    - ButtonBase: Embeds Label for simple, flexible and customizable buttons
	
    - Button subclasses: Button, CheckBox, ToggleButton RadioButton, MenuButton, MenuItem
	
    - TextField for editing simple text values (with flexible background label for prompts, icons, etc.)
	
    - TextView: Comprehensive rich text editing with style setting, spellcheck, etc.
	
    - ComboBox, Slider, Spinner, ThumbWheel for modifying values with more advanced UI
	
    - ListView, TableView, TreeView, BrowserView for displaying large sets of objects
	
    - ParentView for Views that manage children (and ChildView for views that allow others to add them)
	
    - Box, VBox, HBox, BorderView, StackView, SpringView to facilitate layout
	
    - ScrollView, SplitView, TabView, TitleView
	
    - DocView, PageView: represent a real world document and page
	
    - ViewOwner: integrated controller class to manage UI creation, initialization, updates, bindings and events
	
    - RootPane: Manages view event dispatch and hierarchy updates, layout and painting
	
    - WindowView: Maps to a platform window
	
    - MenuItem, Menu, MenuBar
	
    - ProgressBar, Separator

    - ViewArchiver for reading/writing views from simple XML files
	
    - ViewEvent for encapsulating all input events in unified object

    - DialogBox, FormBuilder: For quickly generating UI for common user input
    
## Integrated Developer Tools

If you double-tap the control key in any SnapKit app, a developer console will appear. There are many features
here to make it easier to debug visual layouts and explore new or large code bases:

    - Mouse-Over to select and inspect any individual nested ViewOwner controller and UI
    
    - Mouse-Over to select and inspect any View
    
    - Open any UI in the UI Builder, or controller in GitHub code, or View JavaDoc
    
    - Select different UI themes (standard, light, dark, light-blue, etc.)
    
    - Enable debug flashing of repaint regions to ensure efficient repaints
    
    - Enable Frames-Per-Second paint speed measurement tool


## The 3D Graphics Package

SnapKit also has a basic 3D package based on OpenGL that uses JOGL on the desktop and WebGL in the browser.
There is also a simple built-in renderer that renders 3D using standard 2D graphics (this avoids unnecessary
external JOGL dependencies when 3D isn't really needed and can actually look better in PDF, SVG or print).

The 3D package has:

    - Basic geometry classes for matrices, vectors and points
    
    - Fundamental scene elements for Camera, Lights and Scene
    
    - Fundamental VertexArray class to model and render and mesh of triangles, lines and points


