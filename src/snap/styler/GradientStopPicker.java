/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import java.util.*;

import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.gfx.GradientPaint.Stop;
import snap.props.PropChange;
import snap.util.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * A multi-stop gradient stop inspector.
 */
public class GradientStopPicker extends ParentView {

    // The stops
    private Stop[]  _stops;
    
    // The knobs
    private List <Rect>  _knobs = new ArrayList<>();
    
    // The wells
    private List <ColorWell>  _wells = new ArrayList<>();
    
    // The gradient rect
    private Rect  _gradientRect;
    
    // The selected knob
    private int _selKnob;
    
    // The knob images
    private Image  _knob = Image.get(getClass(), "Knob.png");
    private Image  _knobLit = Image.get(getClass(), "KnobHilighted.png");
    
    // The texture paint
    private ImagePaint  _background;
    
    // The cursor
    //private static Image  addStopImg = Image.get(getClass(),"AddStopCursor.png");
    private static Cursor  _addStopCursor = Cursor.CROSSHAIR; //Tkit.createCustomCursor(_addStopImg,new Pnt(6,16),"add");
    
    // The drag point
    private Point  _dragPoint;
    
    // Constants
    public static final int WELL_SIZE = 24;
    public static final int KNOB_WIDTH = 19;
    public static final int KNOB_HEIGHT = 22;
    public static final int KNOB_BASELINE = 11;

    /**
     * Creates new GradientStopPicker.
     */
    public GradientStopPicker()
    {
        setActionable(true);
        enableEvents(MousePress, MouseDrag, MouseMove);
        enableEvents(DragEvents);
    }

    /**
     * Returns the number of color stops in the gradient
     */
    public int getStopCount()  { return _stops.length; }

    /**
     * Returns the individual color stop at given index.
     */
    public Stop getStop(int anIndex)  { return _stops[anIndex]; }

    /**
     * Returns the color of the stop at the given index.
     */
    public Color getStopColor(int index)  { return getStop(index).getColor(); }

    /**
     * Returns the position (in the range {0-1}) for the given stop index.
     */
    public double getStopOffset(int index)  { return getStop(index).getOffset(); }

    /**
     * Returns the list of color stops.
     */
    public Stop[] getStops()  { return _stops; }

    /**
     * Resets all the stops from the new list.
     */
    public void setStops(Stop[] theStops)
    {
        if(Arrays.equals(theStops, _stops) && _gradientRect!=null) return;
        _stops = Arrays.copyOf(theStops, theStops.length);  // Copy stops
        _selKnob = -1;                                 // Deselect knob
        relayout();                                  // call revalidate to add/remove components
    }

    /**
     * Adds a new color stop at the given position. Returns the index of the new stop.
     */
    public int addStop(double anOffset, Color aColor)
    {
        // Get loop counter and max
        int i, nstops = getStopCount();
        if(anOffset<0) anOffset = 0;
        else if(anOffset>1) anOffset = 1;

        // Find location within sorted list for new stop
        for(i=0; i<nstops; i++) {
            double listPos = getStop(i).getOffset();

            // inserting one exactly where one already exists just replaces the old one
            if(MathUtils.equals(listPos, anOffset)) {
                if(aColor!=null) setStop(i, anOffset, aColor);
                return i;
            }
            // break if location of new stop is found.
            if(listPos>anOffset) break;
        }

        // a null color defaults to whatever value the old gradient has at the new position.
        // The new gradient will be visually identical to the old one, therefore, but with an explicit stop at 'position'
        if(aColor==null) {
            if(i==0 || i==nstops) aColor = Color.BLACK;
            else {
                double distBefore = getStopOffset(i-1);
                double distAfter = getStopOffset(i);
                double stopDistance = (anOffset - distBefore) / (distAfter-distBefore);
                Color colorBefore = getStopColor(i-1);
                Color colorAfter = getStopColor(i);
                aColor = colorBefore.blend(colorAfter, stopDistance);
            }
        }

        // insert stop at index
        _stops = ArrayUtils.add(_stops, new Stop(anOffset, aColor), i);
        return i;
    }

    /**
     * Removes the stop at the given index.
     */
    public void removeStop(int index)
    {
        // Complain if only one stop would be left otherwise remove stop
        if(getStopCount()==2) throw new IndexOutOfBoundsException("GradientStopPicker: Cannot have fewer than 2 stops");
        _stops = ArrayUtils.remove(_stops, index);
    }

    /**
     * Sets the color & position of the stop at the given index.
     */
    public void setStop(int index, double anOffset, Color aColor)
    {
        int nstops = getStopCount();
        if(index<0 || index>=nstops) throw new IndexOutOfBoundsException("Invalid color index ("+index+")");

        double pmin = index==0? 0 : getStop(index-1).getOffset()+1e-4;
        double pmax = index==nstops-1? 1 : getStop(index+1).getOffset()-1e-4;

        if (anOffset<pmin) anOffset = pmin;
        else if (anOffset>pmax) anOffset = pmax;

        _stops = Arrays.copyOf(_stops, _stops.length);
        _stops[index] = new Stop(anOffset, aColor);
    }

    /**
     * Create an explicit stop at the corresponding gradient position.
     */
    public int addStop(Point pt, Color aColor)
    {
        double soff = getStopOffset(pt);
        int sindex = addStop(soff, aColor);
        relayout();
        return sindex;
    }

    /**
     * Called when a ColorStop is deleted.
     */
    public void deleteColorStop()
    {
        removeStop(_selKnob);
        _selKnob = -1;
        relayout();
        getEnv().runLater(() -> fireActionEvent(null));
    }

    /**
     * Reverse the order of the color stops
     */
    public void reverseStops()
    {
        int nstops = getStopCount();
        Stop[] stops = new Stop[nstops];
        for(int i = 0; i < nstops; i++)
            stops[nstops-i-1] = new Stop(1 - getStopOffset(i), getStopColor(i));
        _stops = stops;
    }

    public int getKnobIndex(Point pt)
    {
        for(int i = 0, iMax = _knobs.size(); i < iMax; i++) {
            Rect r = _knobs.get(i);
            if(r.contains(pt))
                return i;
        }
        return -1;
    }

    public void selectStop(int anIndex)
    {
        if(anIndex == _selKnob) return;
        _selKnob = anIndex; repaint();
    }

    /**
     * Given a mouse point within gradient rectangle, returns corresponding position in gradient
     * (0 at left of gradient, 1 at right).
     */
    public double getStopOffset(Point pt)
    {
        double position = (pt.x -_gradientRect.getX()) / _gradientRect.getWidth();
        return position < 0 ? 0 : (position>1? 1 : position);
    }

    /**
     * Move the selected stop to the new position.
     */
    public void adjustColorStop(int index, Point pt)
    {
        // turn mouse point into a number in the range {0,1}
        double newposition = getStopOffset(pt);

        if(getStopOffset(index) != newposition) {
            setStop(index, newposition, getStopColor(index));  // move the stop
            double x = getGradientCoordinate(index);    // reset control bounds so they're centered over new position
            ColorWell well = _wells.get(index);
            well.setBounds((int)(x-well.getWidth()/2), well.getY(), well.getWidth(), well.getHeight());
            Rect krect = _knobs.get(index); krect.x = (int)(x-krect.getWidth()/2);
            repaint();
        }
    }

    /**
     * Returns the corresponding x coordinate in the gradient rectangle for stop index.
     */
    private double getGradientCoordinate(int stopIndex)
    {
        double soff = getStopOffset(stopIndex);
        return _gradientRect.getX() + soff*_gradientRect.getWidth();
    }

    /**
     * Paint Component.
     */
    protected void paintFront(Painter aPntr)
    {
        // Draw background
        if(_stops==null) return;

        // Draw a background under gradients with alpha
        if(GradientPaint.getStopsHaveAlpha(getStops())) {
            aPntr.setPaint(getBackgroundTexture());
            aPntr.fill(_gradientRect);
        }

        // draw the gradient
        aPntr.setPaint(new GradientPaint(_gradientRect.getX(), 0, _gradientRect.getMaxX(), 0, getStops()));
        aPntr.fill(_gradientRect);

        // draw indicator for drag & drop
        if(_dragPoint!=null && _gradientRect.contains(_dragPoint)) {
            aPntr.setPaint(Color.BLUE);
            aPntr.drawLine(_dragPoint.x, _gradientRect.getY(), _dragPoint.x, _gradientRect.getMaxY());
            aPntr.draw(_gradientRect);
        }

        // draw the gradient rect
        aPntr.setColor(Color.BLACK);
        aPntr.draw(_gradientRect);

        // draw the knobs (back to front, so stacking order matches the wells)
        for(int i=_knobs.size()-1; i>=0; --i) { Rect k = _knobs.get(i);
            aPntr.drawImage(i== _selKnob ? _knobLit : _knob, k.getX(), k.getY()); }
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePressed
        if(anEvent.isMousePress()) {    if(_stops==null) return;

            // find the part clicked in
            Point pt = anEvent.getPoint();
            int sindex = getKnobIndex(pt);

            // Clicking inside gradient creates a new stop whose color is color just clicked on.
            // A drag after this moves the new stop.
            if ((sindex<0) && _gradientRect.contains(pt)) {
                sindex = addStop(pt, null);
                fireActionEvent(anEvent);
            }

            // Create and run the pop-up menu
            else if(anEvent.isPopupTrigger() && getStopCount()>2) {
                Menu pmenu = new Menu();
                MenuItem mitem = new MenuItem(); mitem.setText("Delete Color Stop");
                mitem.addEventHandler(e -> deleteColorStop(), Action);
                pmenu.addItem(mitem);
                pmenu.show(anEvent.getView(), pt.x, pt.y);
                anEvent.consume(); // Consume event.
            }

            selectStop(sindex);
        }

        // Handle MouseDragged
        else if(anEvent.isMouseDrag()) {
            if(_selKnob >=0) {
                adjustColorStop(_selKnob, anEvent.getPoint());
                fireActionEvent(anEvent);
            }
        }

        // Handle MouseMoved: cursor (if the gradient were its own component, it could do this automatically
        else if(anEvent.isMouseMove()) {
            boolean in = _gradientRect.contains(anEvent.getPoint()) && (getKnobIndex(anEvent.getPoint())==-1);
            setCursor(in? _addStopCursor : null);
        }

        // Handle DragEnger
        else if(anEvent.isDragEnter()) { Clipboard db = anEvent.getClipboard();
            if(db.hasColor()) anEvent.acceptDrag();
            //else dtde.rejectDrag();
        }

        // Handle DragOver
        else if(anEvent.isDragOver()) { Clipboard db = anEvent.getClipboard();
            if(db.hasColor()) { anEvent.acceptDrag(); _dragPoint = anEvent.getPoint(); repaint(); }
        }

        // Handle DragExit
        else if(anEvent.isDragExit()) { _dragPoint = null; repaint(); }

        // Handle DragDrop
        else if(anEvent.isDragDrop()) {
            if(_gradientRect.contains(anEvent.getPoint())) {
                anEvent.acceptDrag();
                Color color = anEvent.getClipboard().getColor();
                addStop(getStopOffset(_dragPoint), color);
                _dragPoint = null;
                relayout();
                fireActionEvent(anEvent);
                anEvent.dropComplete();
            } //else dtde.rejectDrop();
        }
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return 180; }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return 62; }

    /**
     * Override to reset knobs and such.
     */
    protected void layoutImpl()
    {
        // Reset wells, knobs and remove all children
        for(ColorWell well : _wells) removeChild(well);
        _wells.clear(); _knobs.clear();

        // Reset gradient rect
        double w = getWidth(), h = getHeight();
        _gradientRect = new Rect(WELL_SIZE/2, KNOB_BASELINE, w - WELL_SIZE, h-KNOB_BASELINE-WELL_SIZE-1);

        // Create bounds rects for knobs & color wells
        Rect wrect = new Rect(0, (int)_gradientRect.getMaxY()+1, WELL_SIZE, WELL_SIZE);

        // add wells & knobs for each stop
        for(int i=0, iMax=getStopCount(); i<iMax; i++) {

            // Calc x of this stop in gradient rect - controls are placed above/below gradient, centered about this position
            double position = getGradientCoordinate(i);

            // Create, configure and add color well for this stop
            ColorWell well = new ColorWell();
            wrect.x = (int)(position - WELL_SIZE/2);
            well.setColor(getStopColor(i));
            well.setBounds(wrect.x, wrect.y, wrect.width, wrect.height);
            _wells.add(well);
            addChild(well);

            // Add action listener to ColorWell to update gradient
            well.addPropChangeListener(e -> colorWellPropertyChange(e));

            // Set the knob image rectangle for this stop
            _knobs.add(new Rect((int)(position - KNOB_WIDTH/2), 0, KNOB_WIDTH, KNOB_HEIGHT));
        }

        repaint();
    }

    /**
     * Handle ColorWell property change.
     */
    private void colorWellPropertyChange(PropChange aPC)
    {
        if(aPC.getPropertyName()==ColorWell.Color_Prop) { ColorWell cwell = (ColorWell)aPC.getSource();
            int which = _wells.indexOf(cwell); Color color = cwell.getColor();
            setStop(which, getStopOffset(which), color);
            repaint();
            getEnv().runLater(() -> fireActionEvent(null));
        }
    }

    /** Creates & returns a texture to be used for the background of transparent gradients */
    private ImagePaint getBackgroundTexture()
    {
        if(_background!=null) return _background;
        int cs = 4, w = 2*cs;
        Image img = Image.get(w,w,true); Painter pntr = img.getPainter();
        pntr.setColor(Color.WHITE); pntr.fillRect(0, 0, w, w);
        pntr.setColor(new Color(168,193,255)); pntr.fillRect(0,0,cs,cs); pntr.fillRect(cs,cs,cs,cs);
        return _background = new ImagePaint(img);
    }
}