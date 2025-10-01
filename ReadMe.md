# SnapKit - a Java UI toolkit

SnapKit is a modern Java UI library + tools for creating rich Java Client applications that achieve the original
promise of Java by running pixel-perfect and native on the desktop and in the browser ([WORA](https://en.wikipedia.org/wiki/Write_once,_run_anywhere)).

Why do we need another UI kit? Because Swing is out of date, JavaFX missed the boat, and neither run natively
in the browser. But Java desktop development is hard to beat: the iterative dev cycle is fast,
debugging is powerful, refactoring is easy, and advanced dev tools solve many problems at compile time.
The drawback is deployment: browser deployment is impossible to beat. This situation alone has led to the decline of
Java Client adoption and prevented it from being a serious contender for most new development.
SnapKit is designed to resolve this with no compromises.

* [Overview](#Overview)
* [SnapKit Inspirations](#snapKit-inspirations)
* [SnapKit Advantages](#snapKit-advantages)
* [The ViewOwner](#the-viewowner)
* [The Graphics Package](#the-graphics-package)
* [The View Package](#the-view-package)
* [The Text Package](#the-text-package)
* [The Parser Package](#the-parser-package)
* [The Properties Package](#the-properties-package)
* [The Games Package](#the-games-package)
* [The 3D Graphics Package](#the-3d-graphics-package)
* [The Web Package](#the-web-package)
* [The SnapBuilder UI Builder](#the-snapbuilder-ui-builder)
* [Integrated Runtime Developer Tools](#integrated-runtime-developer-tools)
* [Including SnapKit with Gradle and Maven](#including-snapkit-with-gradle-and-maven)

Check out demos of [SnapKit running in the browser](http://www.reportmill.com/snaptea/):

[ ![SnapKit](https://reportmill.com/snaptea/Samples.png)](http://www.reportmill.com/snaptea/)


## Overview

SnapKit runs optimally everywhere via a high level design where low-level functionality (such as painting, user
input, system clipboard, drag-and-drop and windowing) is provided via interfaces to the native platform implementation.
This makes SnapKit itself comparatively small and simple, light-weight and performant. SnapKit apps run very well in
the browser with zero modification with the [CheerpJ browser JVM](https://leaningtech.com/what-is-cheerpj/). When
compiled to the browser with a transpiler (via [TeaVM](http://teavm.org)), many apps are only 1 MB in size.

## SnapKit Inspirations

SnapKit is strongly inspired by both Swing and JavaFX. Swing is still a favorite with Java desktop developers, despite
its age and lack of recent updates. There are still many things developers love:

- Solid view hierarchy and set of controls/components
- Relatively easy to create and update UI and respond to user input and UI changes
- Full set of geometric shape primitives: Line, Rect, Ellipse, Path, Polygon, etc.
- Easily set component borders, fills, and fonts with simple API
- The whole convenient painting model - just override paint() to customize
- It handles property changes in conventional Java property change manner
- It binds easily with POJOs

When JavaFX was introduced it rewrote the rulebook for Java UI with dramatic changes, often for the better:

- Easily mix graphics and app controls
- Easily add gradients, textures, effects
- Set arbitrary transforms (rotate, scale, skew) on any node
- It has built-in binding support to easily wire values across objects
- It has a full set of nodes for easy layout: Box, BorderView, StackPane, etc.
- It has support for easily defining UI in a separate text file (FXML)

## SnapKit Advantages

SnapKit is the right blend of modern and conventional. SnapKit tries to be more of a "Swing 2.0". More precisely, it
keeps the basic simplicity and standard conventions of Swing while adding the visual richness of JavaFX and bringing the
whole thing to the browser:

- It provides all the essential features of Swing and JavaFX (above)
- It runs on top of Swing, JavaFX and HTML DOM
- It is easily portable to any future UI kit and platform
- The base class is called View (that puts the V in MVC!)
- The ViewOwner class provides control functionally
- The ViewEvent class unifies all input events for more consistent handling

    
## The ViewOwner

The one thing that may have hurt Swing and JavaFX the most is that there is no standard class to manage the basics of UI
management: Create, Init, Reset, Respond (otherwise known as the "Controller" in MVC).
    
This resulted in confusing controller code, with UI controls often having code for all four functions
in the same place. Initially this can be deceptively simple and attractive, but falls apart when dozens of inter-dependent
controls are present and order-dependent updates are necessary.

Here is a simple Swing example that quickly gets out of control when extended to many
properties and controls:

```
// Create UI
_textField = new JTextField();

// Init UI
_textField.setText("Initial Value");

// Respond UI
_textField.addActionListener(event -> {
    _myModel.updatePropertyForTextField(_textField.getText());
    SwingUtilities.invokeLater(this::updateUI);
});

// Update UI
public void updateUI()
{
    _textField.setText(_myModel.getPropertyForTextField());
});
```

Here is the same thing with a ViewOwner:

```java
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

Some things to note:

**CreateUI()** is usually handled automatically by loading a '.snp' UI file created in SnapBuilder.
    
**InitUI()** is also usually not needed, because UI is configured in createUI() and updated in ResetUI().
    
**ResetUI()** updates are deferred, coalesced and "protected" (will not cause respondUI() side effects) and
is called automatically when the user interacts with any UI (or explicitly via resetLater()).
    
**RespondUI()** is called automatically by controls (they are preconfigured to do this by default)
    
**ResetUI()** and **RespondUI()** make tracking UI interactions convenient and easy by providing a consistent
place to look for all get/set code between controls and the model.
    
    
## <a name="UniversalAccessors">ViewOwner "Universal Accessors"</a>

As a convenience, ViewOwner will let you get/set values using standard methods and support all controls, which
avoids having to lookup or remember specific get/set methods for controls. It also provides common type conversions
to avoid tedious conversions to/from String/number/boolean formats.

```
public void resetUI()
{
    // Update MyTextField, MySlider, ...
    setViewValue("MyTextField", _myModel.getPropertyForTextField());
    setViewValue("MySlider", _myModel.getPropertyForSlider());
    ...
}
```

The same applies to ViewEvent (the sole parameter to respondUI()):

```
public void respondUI(ViewEvent anEvent)
{
    // Handle MyTextField, MySlider, ...
    if (anEvent.equals("MyTextField"))
        _myModel.updatePropertyForTextField(anEvent.getStringValue());
    if (anEvent.equals("MySlider"))
        _myModel.updatePropertyForSlider(anEvent.getFloatValue());
    ...
}
```

In addition to get/setViewValue(), there are methods for get/set other View properties: Enabled, Visible, Text,
SelectedItem, SelectedIndex.

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
- SoundClip for playing sounds


## The View Package

The essentail part of a good UI kit is the set of classes that model the scene graph and standard UI controls.

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

## The Text Package

The **[snap.text](https://github.com/reportmill/SnapKit/tree/master/src/snap/text)** package provides all
the classes necessary to efficiently display and edit large text files and rich text files. There is even
fundamental support for text with 'tokens' (like source code text files), to efficiently support working
with large source files and providing syntax coloring and symbol highlighting.

- TextRun: Manages a group of characters with a display style
- TextLine: Manages a paragraph of text (with multiple runs)
- TextModel: Manages lines of styled text
- TextStyle: Manages a group of text display attributes
- TextLineStyle: Manages a group of text paragraph attributes (indent, spacing)
- TextToken: Manages a group of characters in a line that represent a word or code part

## The Parser Package

The **[snap.parse](https://github.com/reportmill/SnapKit/tree/master/src/snap/parse)** package
dynamically generates parsers based on conventional grammar files combined with a rule handler class.
Separating the grammar from the handler code makes the parser much easier to read, write and maintain.

- Parser: Processes the rules of a grammar to parse a file
- ParseRule: Represents an individual parse rule with ops for: And, Or, ZeroOrOne, ZeroOrMore, OneOrMore, Pattern, Lookahead
- ParseHandler: Processes individual parsed nodes for an individual rule to generate an AST
- Tokenizer: Reads from input to generate individual parse tokens
- ParseToken: Represents a parsed token
- Regex: Represents a regex pattern

Several parsers are included in SnapKit to parse JSON, XML and Java expressions. Other parsers based on this
package to parse PDF and Java are available in separate SnapKit dependent projects.

See [SnapCode](https://github.com/reportmill/SnapCode) and [SnapPDF](https://github.com/reportmill/SnapPDF).

## The Properties Package

The **[snap.props](https://github.com/reportmill/SnapKit/tree/master/src/snap/props)** package provides
an easy way to serialize Java objects and provides automatic support for read/write (JSON/XML), copy/paste,
undo/redo and more. Specifically the props support provides the following:

- Read/write an object graph to XML and JSON
- "Sparse Serialization" (only write attributes that have changed from default)
- Clipboard copy/paste an object or object graph
- Undo/Redo support
- Automatic support for clone(), equals(), hashCode() and toString()

This serialization is done by simply defining each serializable property of an object in this fashion:

- Define **string constant** for property name  
```public static String Name_Prop = "Name";```

- Configure prop in **initProps()** method of class  
```addPropNamed(Name_Prop, <PropClass>, <DefaultValue>)```

- Provide getter in **getPropValue(aPropName)**  
```if (aPropValue == Name_Prop) return getName();```

- Provide setter in **setPropValue(aPropName,aValue)**  
```if (aPropValue == Name_Prop) setName((String) aValue);```

Here is an example class that can automatically read/write sparse JSON/XML, handle clipboard copy/pase and handle undo:

```java
/**
 * This class supports automatic read/write (JSON/XML), clipboard copy/pase, user
 * undo/redo and automatic clone()/equals()/hashCode()/toString() implementations.
 *
 *  To XML: String xmlString = new PropArchiverXML().writeToXML(new MyClass()).getString();
 *
 *  To JSON: String jsonString = new PropArchiverJS().writeToJSON(new MyClass()).getString();
 *
 *  Clone: MyClass myClone = new PropArchiver().copy(new MyClass());
 */
public class MyClass extends PropObject {

    // Serialzable Name property
    private String  _name;

    // Constants for properties
    public static final String Name_Prop = "Name";

    // Constants for property defaults
    public static final String DEFAULT_NAME = "John Doe";

    /**
     * Constructor.
     */
    public MyClass()
    {
        _name = DEFAULT_NAME;
    }

    /**
     * Override to configure properties for this class.
     */
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        aPropSet.addPropForName(Name_Prop, String.class, DEFAULT_NAME);
    }

    /**
     * Override to return propery values for this class.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {
            case Name_Prop: return getName();
            default: return super.getPropValue(aPropName);
        }
    }
    
    /**
     * Override to set property values for this class.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {
            case Name_Prop: setName((String) aValue); break;
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}
```

## The Games Package

The package provides fundamental classes for creating many games like Asteroids, Tetris, FlappyBird and more. These
classes are great for educational and recreational purposes.

- **Actor**
  - Models individual game entities like a spaceship, asteroid, bullet, etc.
  - Manages location, size, rotation and movement
  - Manages painting, user input, frame behavior and collision detection
- **GameView**
  - Manages a list of actors
  - Manages the game timer (play, pause, stop, framerate)
  - Manages background painting, user input and collision detection
- **GameController**
  - Manages top level UI and the current game view
- **DevConsole**
  - Shows the game controller with ability to play, pause, step, set frame speed, etc.
- **PenActor**
  - Proves functionality for pen graphics
- **Game**
  - Manages global state and game resources (images, sounds, etc.)

Check out the **[source code](https://github.com/reportmill/SnapKit/tree/master/src/snap/games)** and
**[Javadoc](https://reportmill.com/snap1/javadoc/snap/games/package-summary.html)**.

[ ![SnapKit](https://reportmill.com/SnapCode/images/Games.png)](http://www.reportmill.com/snaptea/)

## The 3D Graphics Package

The **[snap.gfx3d](https://github.com/reportmill/SnapKit/tree/master/src/snap/gfx3d)** package provides a
elegant 3D api based on OpenGL that uses JOGL on the desktop and WebGL in the browser. This allows
for write-once-run-anywhere 3D. There is also a simple built-in renderer that renders 3D using standard 2D
graphics (this avoids unnecessary external JOGL dependencies when 3D isn't really needed and can actually look better
in PDF, SVG or print).

The 3D package has:

- Basic geometry classes for matrices, vectors and points
- Fundamental scene elements for Camera, Lights and Scene
- Fundamental VertexArray class to model and render and mesh of triangles, lines and points

[ ![Sample 3D](http://reportmill.com/SnapCharts/Sample3D.png)](https://reportmill.com/SnapCharts/)

## The Web Package

The **[snap.web](https://github.com/reportmill/SnapKit/tree/master/src/snap/web)** package provides a
set of classes to abstract and unify interactions with URLs, files and sites (file systems).

- WebURL: Represents a URL for a file from a site
- WebFile: Represents a file and can return contents (file) or child files (dir)
- WebSite: Represents a file system or web site and can create or find files by path
- FileSite, HTTPSite, ZipSite: Implementations of WebSite to support local, remote and compressed files

These classes make it easy to ambiguously work with files regardless of whether they are from the local file system
or from a remote HTTP site or even from a Zip archive file, Github repository or DropBox account. The package provides
unique shared instances of WebFile and WebSite to make it easy to track changes across different modules of code that
work with the same files and sites.

## The SnapBuilder UI Builder

Because the best line of code is the one you don't have to write, UI is almost always created using the UI builder
and stored in simple XML files ('.snp' files). Simply create/save a .snp file with the same name as your custom ViewOwner class, and the default ViewOwner.createUI() method will load it.

As a bonus, you can run SnapBuilder in the browser and even open any UI file from a running application using the
"Developer Tools Console", also available in any running app (see below).

[ ![SnapBuilder](https://reportmill.com/snaptea/SnapBuilder/SnapBuilder.gif)](https://reportmill.com/snaptea/SnapBuilder/)

## Integrated Runtime Developer Tools

If you double-tap the control key in any SnapKit app, a developer console will appear. There are many features
here to make it easier to debug visual layouts and explore new or large code bases:

- Mouse-Over to select and inspect any individual nested ViewOwner controller and UI
- Mouse-Over to select and inspect any View
- Open any UI in the UI Builder, or controller in GitHub code, or View JavaDoc
- Select different UI themes (standard, light, dark, light-blue, etc.)
- Enable debug flashing of repaint regions to ensure efficient repaints
- Enable Frames-Per-Second paint speed measurement tool

## Including SnapKit with Gradle and Maven

SnapKit can easily be included with build tools like Gradle by referencing the maven package:

```
repositories {

    // Maven package repo at reportmill.com
    maven { url 'https://reportmill.com/maven' }
}

dependencies {

    // Latest release: https://github.com/reportmill/SnapKit/releases
    implementation 'com.reportmill:snapkit:2025.10'
}
```