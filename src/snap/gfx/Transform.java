/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * A class to represent a mathematical linear transform.
 */
public class Transform implements Cloneable {
    
    // Matrix components
    protected double _a = 1, _b = 0, _c = 0, _d = 1, _tx = 0, _ty = 0;
    
    // The inverse
    Transform        _inverse;
    
    // Identity transform
    public static final Transform IDENTITY = new Transform();

/**
 * Creates a new Transform.
 */
public Transform()  { }

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
public void translate(double dx, double dy)  { _tx += dx; _ty += dy; _inverse = null; }

/**
 * Translates this transform by given x & y.
 */
public void translate2(double dx, double dy)  { _tx += dx*_a + dy*_b; _ty += dx*_c + dy*_d; _inverse = null; }

/**
 * Rotates this transform by given angle in degrees.
 */
public void rotate(double anAngle)
{
    double angle = Math.toRadians(anAngle), c = Math.cos(angle), s = Math.sin(angle);
    multiply(c, s, -s, c, 0, 0);
}

/**
 * Rotates this transform by given angle in degrees about the given point.
 */
public void rotateAbout(double anAngle, double aX, double aY)
{
    translate(-aX, -aY);
    double angle = Math.toRadians(anAngle), c = Math.cos(angle), s = Math.sin(angle);
    multiply(c, s, -s, c, 0, 0);
    translate(aX, aY);
}

/**
 * Scales this transform by given scale x and scale y.
 */
public void scale(double sx, double sy)  { multiply(sx, 0, 0, sy, 0, 0); }

/**
 * Skews this transform by given skew x and skew y angles in degrees.
 */
public void skew(double aSkewX, double aSkewY)
{
    double skewX = Math.toRadians(aSkewX);
    double skewY = Math.toRadians(aSkewY);
    double tanSkewX = Math.tan(skewX);
    double tanSkewY = Math.tan(skewY);
    multiply(1, tanSkewX, tanSkewY, 1, 0, 0);
}

/**
 * Multiplies this transform by the given transform.
 */
public void multiply(Transform aTrans)
{
    double mat[] = aTrans.getMatrix();
    multiply(mat[0], mat[1], mat[2], mat[3], mat[4], mat[5]);
}

/**
 * Multiplies this transform by the given transform components.
 */
public void multiply(double a, double b, double c, double d, double tx, double ty)
{
    double a2 = _a*a + _b*c, b2 = _a*b + _b*d;
    double c2 = _c*a + _d*c, d2 = _c*b + _d*d;
    double tx2 = _tx*a + _ty*c + tx, ty2 = _tx*b + _ty*d + ty;
    _a = a2; _b = b2; _c = c2; _d = d2; _tx = tx2; _ty = ty2;
    _inverse = null;
}

/**
 * Inverts this transform (and returns this for convenience).
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
    _inverse = null;
}

/**
 * Returns the inverse.
 */
public Transform getInverse()  { Transform t = clone(); t.invert(); return t; }

/**
 * Clears the transform to identity.
 */
public void clear()  { _a = 1; _b = 0; _c = 0; _d = 1; _tx = 0; _ty = 0; }

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
public void setMatrix(float m[])  { _a = m[0]; _b = m[1]; _c = m[2]; _d = m[3]; _tx = m[4]; _ty = m[5]; }

/**
 * Sets transform values to given matrix values.
 */
public void setMatrix(double m[])  { _a = m[0]; _b = m[1]; _c = m[2]; _d = m[3]; _tx = m[4]; _ty = m[5]; }

/**
 * Sets transform values to given transform values.
 */
public void setMatrix(Transform aTrans)
{
    _a = aTrans._a; _b = aTrans._b; _c = aTrans._c; _d = aTrans._d; _tx = aTrans._tx; _ty = aTrans._ty;
}

/**
 * Sets transform values to given matrix values.
 */
public void setMatrix(double a, double b, double c, double d, double tx, double ty)
{
    _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty;
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
 */
public Point transform(Point aPnt, Point aDst)
{
    double x = aPnt.getX(), y = aPnt.getY();
    double x2 = x*_a + y*_c + _tx, y2 = x*_b + y*_d + _ty;
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

/** Transforms the given size as a vector (preserves negative values). */
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
 * Returns a translation transform.
 */
public static Transform getTrans(double tx, double ty)
{
    Transform t = new Transform(); t.translate(tx, ty); return t;
}

/**
 * Returns a rotation transform.
 */
public static Transform getRotate(double theta)
{
    Transform t = new Transform(); t.rotate(theta); return t;
}

/**
 * Returns a scale transform.
 */
public static Transform getScale(double aSX, double aSY)
{
    Transform t = new Transform(); t.scale(aSX, aSY); return t;
}

/**
 * Returns a transform for given matrix.
 */
public static Transform get(double m[])  { return get(m[0],m[1],m[2],m[3],m[4],m[5]); }

/**
 * Returns a transform for given matrix elements.
 */
public static Transform get(double a, double b, double c, double d, double tx, double ty)
{
    Transform t = new Transform(); t.multiply(a, b, c, d, tx, ty); return t;
}

/**
 * Returns a transform from one given rect to second given rect.
 */
public static Transform get(Rect aRect1, Rect aRect2)
{
    double sx = aRect2.width/aRect1.width;
    double sy = aRect2.height/aRect1.height;
    Transform xform = Transform.getTrans(-aRect1.x,-aRect1.y);
    xform.scale(sx,sy);
    xform.translate(aRect2.x, aRect2.y);
    return xform;
}

}