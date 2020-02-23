package snap.geom;

// Constants for segments
public enum Seg {
    
    // Constants
    MoveTo(1), LineTo(1), QuadTo(2), CubicTo(3), Close(0);
        
    // Methods
    Seg(int count)  { _count = count; } int _count;
    public int getCount() { return _count; }
}