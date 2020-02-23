/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * A class to represent a mathematical linear transforms.
 * Transforms are represented by matrices in row-major form (vs column-major - google it).
 * This works fine with Java2D transforms, which are column-major, since when all six values are given/returned
 * from this class are transposed from Java2D transform (as is everything else).
 */
public class Transform implements Cloneable {
    
    // Matrix components
    protected double _a = 1, _b = 0, _c = 0, _d = 1, _tx = 0, _ty = 0;
    
    // The inverse
    Transform        _inv;
    
    // Identity transform
    public static final Transform IDENTITY = new Transform();

/**
 * Creates a new Transform.
 */
public Transform()  { }

/**
 * Creates a new Transform with given translation.
 */
public Transform(double tx, double ty)  { _tx = tx; _ty = ty; }

/**
 * Creates a new Transform with given float array.
 */
public Transform(float anArray[])  { setMatrix(anArray); }

/**
 * Creates a new Transform with given double array.
 */
public Transform(double anArray[])  { setMatrix(anArray); }

/**
 * Creates a new Transform with given components.
 */
public Transform(float a, float b, float c, float d, float tx, float ty)  { _a=a;_b=b;_c=c;_d=d;_tx=tx;_ty=ty; }

/**
 * Creates a new Transform with given components.
 */
public Transform(double a, double b, double c, double d, double tx, double ty)  { _a=a;_b=b;_c=c;_d=d;_tx=tx;_ty=ty; }

/**
 * Returns the translation x component.
 */
public double getX()  { return _tx; }

/**
 * Returns the translation y component.
 */
public double getY()  { return _ty; }

/**
 * Returns whether this transform is identity.
 */
public final boolean isIdentity() { return equals(IDENTITY); }

/**
 * Returns whether this transform is translation only.
 */
public final boolean isSimple()  { return _a==1 && _b==0 && _c==0 && _d==1; }

/**
 * Returns whether this transform has a rotation component.
 */
public final boolean isRotated()  { return _b!=0 || _c!=0; }

/**
 * Translates this transform by given x & y.
 */
public void translate(double dx, double dy)  { _tx += dx*_a + dy*_c; _ty += dx*_b + dy*_d; _inv = null; }

/**
 * Translates this transform by given x & y in global space (pre-multiply).
 */
public void preTranslate(double dx, double dy)  { _tx += dx; _ty += dy; _inv = null; }

/**
 * Rotates this transform by given angle in degrees.
 */
public void rotate(double anAngle)
{
    double angle = Math.toRadians(anAngle), c = Math.cos(angle), s = Math.sin(angle);
    concat(c, s, -s, c, 0, 0);
}

/**
 * Returns a rotation transform.
 */
public void rotateAround(double anAngle, double aX, double aY)
{
    translate(aX, aY);
    rotate(anAngle);
    translate(-aX, -aY);
}

/**
 * Scales this transform by given scale x and scale y.
 */
public void scale(double sx, double sy)  { concat(sx, 0, 0, sy, 0, 0); }

/**
 * Skews this transform by given skew x and skew y angles in degrees.
 */
public void skew(double aSkewX, double aSkewY)
{
    double skewX = Math.toRadians(aSkewX);
    double skewY = Math.toRadians(aSkewY);
    double tanSkewX = Math.tan(skewX);
    double tanSkewY = Math.tan(skewY);
    concat(1, tanSkewX, tanSkewY, 1, 0, 0);
}

/**
 * Concatentates (pre-multiplies) this transform by given transform: T' = Tn x T.
 */
public void concat(Transform aTr)  { concat(aTr._a, aTr._b, aTr._c, aTr._d, aTr._tx, aTr._ty); }

/**
 * Concatentates (pre-multiplies) this transform by given transform: T' = Tn x T.
 *
 *      [  a  b  0 ]   [  _a  _b  0 ]   [    a*_a+b*_c        a*_b+b*_d     0  ] 
 * T' = [  c  d  0 ] x [  _c  _d  0 ] = [    c*_a+d*_c        c*_b+d*_d     0  ]
 *      [ tx ty  1 ]   [ _tx _ty  1 ]   [ tx*_a+ty*_c+_tx  tx*_b+ty*_d+ty   1  ]
 * 
 */
public void concat(double a, double b, double c, double d, double tx, double ty)
{
    // Calc new values
    double a2 = a*_a + b*_c,            b2 = a*_b + b*_d;
    double c2 = c*_a + d*_c,            d2 = c*_b + d*_d;
    double tx2 = tx*_a + ty*_c + _tx,  ty2 = tx*_b + ty*_d + _ty;
    
    // Set new values
    _a = a2; _b = b2; _c = c2; _d = d2; _tx = tx2; _ty = ty2; _inv = null;
}

/**
 * Multiplies this transform by the given transform.
 */
public void multiply(Transform aTr)  { multiply(aTr._a, aTr._b, aTr._c, aTr._d, aTr._tx, aTr._ty); }

/**
 * Multiplies this transform by the given transform components.
 * 
 *      [  _a  _b  0 ]   [  a   b  0 ]   [    _a*a+_b*c       _a*b+_b*d     0  ] 
 * T' = [  _c  _d  0 ] x [  c   d  0 ] = [    _c*a+_d*c       _c*b+_d*d     0  ]
 *      [ _tx _ty  1 ]   [ tx  ty  1 ]   [ _tx*a+_ty*c+tx  _tx*b+_ty*d+ty   1  ]
 * 
 */
public void multiply(double a, double b, double c, double d, double tx, double ty)
{
    // Calc new values
    double a2 = _a*a + _b*c, b2 = _a*b + _b*d;
    double c2 = _c*a + _d*c, d2 = _c*b + _d*d;
    double tx2 = _tx*a + _ty*c + tx, ty2 = _tx*b + _ty*d + ty;
    
    // Set new values
    _a = a2; _b = b2; _c = c2; _d = d2; _tx = tx2; _ty = ty2; _inv = null;
}

/**
 * Inverts this transform.
 */
public void invert()
{
    double det = (_a*_d - _b*_c);
    if(det == 0) { _a = 1; _b = 0; _c = 0; _d = 1; _tx = _ty = 0; }
    else {
        double a = _d/det, b = -_b/det, c = -_c/det, d = _a/det;
        double tx = (_c*_ty - _d*_tx)/det, ty = (_b*_tx - _a*_ty)/det;
        _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty;
    }
    _inv = null;
}

/**
 * Returns the inverse.
 */
public Transform getInverse()
{
    if(_inv!=null) return _inv;
    Transform t = clone(); t.invert(); return _inv = t;
}

/**
 * Clears the transform to identity.
 */
public void clear()  { _a = 1; _b = 0; _c = 0; _d = 1; _tx = 0; _ty = 0; _inv = null; }

/**
 * Returns the matrix.
 */
public double[] getMatrix()  { double m[] = new double[6]; getMatrix(m); return m; }

/**
 * Loads the given matrix.
 */
public void getMatrix(double m[])  { m[0] = _a; m[1] = _b; m[2] = _c; m[3] = _d; m[4] = _tx; m[5] = _ty; }

/**
 * Sets transform values to given matrix values.
 */
public void setMatrix(float m[])  { setMatrix(m[0], m[1], m[2], m[3], m[4], m[5]); }

/**
 * Sets transform values to given matrix values.
 */
public void setMatrix(double m[])  { setMatrix(m[0], m[1], m[2], m[3], m[4], m[5]); }

/**
 * Sets transform values to given transform values.
 */
public void setMatrix(Transform aTrans)
{
    setMatrix(aTrans._a, aTrans._b, aTrans._c, aTrans._d, aTrans._tx, aTrans._ty);
}

/**
 * Sets transform values to given matrix values.
 */
public void setMatrix(double a, double b, double c, double d, double tx, double ty)
{
    _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty; _inv = null;
}

/**
 * Transforms the given values.
 */
public void transform(double anAry[])  { transform(anAry, anAry.length/2); }

/**
 * Transforms the given values.
 */
public void transform(double anAry[], int aPntCnt)
{
    // Optimized
    if(isSimple()) { for(int i=0,iMax=aPntCnt*2;i<iMax;i+=2) { anAry[i] += _tx; anAry[i+1] += _ty; } return; }
    
    // Normal
    for(int i=0;i<aPntCnt*2; i+=2) {
        double x = anAry[i], y = anAry[i+1];
        anAry[i] = x*_a + y*_c + _tx; anAry[i+1] = x*_b + y*_d + _ty;
    }
}

/**
 * Transforms the given point.
 */
public Point transform(double aX, double aY)  { return transform(new Point(aX,aY), null); }

/**
 * Transforms the given point.
 */
public void transform(Point aPoint)  { transform(aPoint, aPoint); }

/**
 * Transforms the given values.
 *
 *                     [  _a  _b  0 ]  
 * P' = [ tx ty  1 ] x [  _c  _d  0 ] = [ tx*_a+ty*_c+_tx  tx*_b+ty*_d+ty   1  ]
 *                     [ _tx _ty  1 ]  
 * 
 */
public Point transform(Point aPnt, Point aDst)
{
    // Calc new values
    double x = aPnt.getX(), y = aPnt.getY();
    double x2 = x*_a + y*_c + _tx, y2 = x*_b + y*_d + _ty;
    
    // Set new values and return
    if(aDst==null) aDst = new Point(x2, y2); else aDst.setXY(x2, y2);
    return aDst;
}

/**
 * Transforms the given size.
 */
public void transform(Size aSize)
{
    double w = aSize.getWidth(), h = aSize.getHeight();
    aSize.setSize(Math.abs(w*_a) + Math.abs(h*_c), Math.abs(w*_b) + Math.abs(h*_d));
}

/**
 * Transforms the given rect.
 */
public void transform(Rect aRect)
{
    double x1 = aRect.getX(), y1 = aRect.getY(), x2 = aRect.getMaxX(), y2 = aRect.getMaxY();
    double pts[] = new double[] { x1, y1, x2, y1, x2, y2, x1, y2 }; transform(pts, 4);
    x1 = x2 = pts[0]; for(int i=1;i<4;i++) { double x = pts[i*2]; x1 = Math.min(x1,x); x2 = Math.max(x2,x); }
    y1 = y2 = pts[1]; for(int i=1;i<4;i++) { double y = pts[i*2+1]; y1 = Math.min(y1,y); y2 = Math.max(y2,y); }
    aRect.setRect(x1,y1,x2-x1,y2-y1);
}

/**
 * Transforms the given size as a vector (preserves negative values).
 */
public void transformVector(Size aSize)
{
    double w = aSize.getWidth(), h = aSize.getHeight();
    aSize.setSize(w*_a + h*_c, w*_b + h*_d);
}

/**
 * Standard clone implementation.
 */
public Transform clone()
{
    try { return (Transform)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    Transform t = anObj instanceof Transform? (Transform)anObj : null; if(t==null) return false;
    if(!MathUtils.equals(t._a, _a) || !MathUtils.equals(t._b, _b)) return false;
    if(!MathUtils.equals(t._c, _c) || !MathUtils.equals(t._d, _d)) return false;
    if(!MathUtils.equals(t._tx, _tx) || !MathUtils.equals(t._ty, _ty)) return false;
    return true;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return StringUtils.format("[ %f %f %f %f %f %f ]", _a, _b, _c, _d, _tx, _ty);
}

/**
 * Returns a rotation transform for given angle in degrees.
 */
public static Transform getRotate(double theta)  { Transform t = new Transform(); t.rotate(theta); return t; }

/**
 * Returns a rotation transform for given angle in degrees around given x/y point.
 */
public static Transform getRotateAround(double anAngle, double aX, double aY)
{
    Transform t = new Transform(); t.rotateAround(anAngle, aX, aY); return t;
}

/**
 * Returns a scale transform.
 */
public static Transform getScale(double sx, double sy)  { return new Transform(sx, 0, 0, sy, 0, 0); }

/**
 * Returns a transform from one rect to another.
 */
public static Transform getTrans(Rect fromRect, Rect toRect)
{
    if(fromRect.isEmpty()) { System.err.println("Transform.getTrans: Empty rect"); return IDENTITY; }
    double sx = toRect.width/fromRect.width, sy = toRect.height/fromRect.height;
    double tx = toRect.x - fromRect.x*sx, ty = toRect.y - fromRect.y*sy;
    return new Transform(sx,0,0,sy,tx,ty);
}

}