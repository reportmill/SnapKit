# SnapKit
SnapKit is a Java UI kit. Why do we need another UI kit? Because JavaFX is missing many great things about Swing, and vice versa.

What's to love about Swing?

	- The full set of geometric shape primitives: Line, Rect, Ellipse, Path, Polygon, etc.
	- Easily set border, background, font on any component with simple API
	- The whole convenient painting model - just override paint() to customize
	- It handles property changes in conventional Java property change manner
	- It binds easily with POJOs

What's to love about JavaFX?

	- Easily mix graphics and app controls
	- Easily add gradients, textures, effects
	- Set arbitrary transforms (rotate, scale, skew) on any node
	- It has built-in binding support to easily wire values across objects
	- It has a full set of nodes for easy layout: Box, BorderView, StackPane, etc.
	- It has support for easily defining UI in a separate text file (FXML)

What's to love about SnapKit?

	- It provides all these features and more
	- It runs on top of either JavaFX or Swing
	- It is portable to any future UI kit
	- The base class is called View. Now that puts the V in MVC!
	- The ViewOwner class facilitates control functionally (whoops, there goes the C)
	- The ViewEvent class unifies all input events for more consistent handling

One of the great aspects of Swing is the separation between the core graphics layer (Java2D) and the UI layer.
SnapKit provides this same separation with the completely independent snap.gfx package that contains:

	- Full set of geometric primitives: Rect, Point, Size, Insets, Pos (for alignment)
	- Transform for arbitrary transforms and coordinate conversions: rotate, scale, skew
	- Full set of Shape primitives: Rect, RoundRect, Arc, Ellipse, Line, Path, Polygon
	- Paint define fill styles with common subclasses: Color, GradientPaint, ImagePaint
	- Stroke for defining outline style, and Border for a stroke in a specific Paint
	- Effects for rich rendering: Shadow, Reflect, Emboss, Blur
	- Font and FontFile objects (wrap around platform fonts)
	- Painter capable of rendering shapes, images and text with transforms, fills, strokes, effects
	- Image object (wraps around platform image)
	- RichText object for managing large text content with attributes
	- TextStyle object to manage a set of attributes: font, color, underline, hyper links, format, etc.
	- TextBox object for managing RichText in a geometric region (with spelling and hyphenation)
	- SoundClip for playing sounds

And the essentail part of a good UI kit are the classes that model standard UI controls.

	- View for managing hierarchy of coordinate systems, drawing and input events
	- ViewArchiver for reading/writing views from simple XML files
	- ViewEvent for encapsulating all input events in unified object
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
	- DialogBox, FormBuilder: For quickly generating UI for common user input
