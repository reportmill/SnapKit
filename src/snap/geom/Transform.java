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
    private Transform  _inv;
    
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
    public Transform(float[] anArray)
    {
        setMatrix(anArray);
    }

    /**
     * Creates a new Transform with given double array.
     */
    public Transform(double[] anArray)
    {
        setMatrix(anArray);
    }

    /**
     * Creates a new Transform with given components.
     */
    public Transform(float a, float b, float c, float d, float tx, float ty)
    {
        _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty;
    }

    /**
     * Creates a new Transform with given components.
     */
    public Transform(double a, double b, double c, double d, double tx, double ty)
    {
        _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty;
    }

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
    public final boolean isIdentity()
    {
        return equals(IDENTITY);
    }

    /**
     * Returns whether this transform is translation only.
     */
    public final boolean isSimple()
    {
        return _a == 1 && _b == 0 && _c == 0 && _d == 1;
    }

    /**
     * Returns whether this transform has a rotation component.
     */
    public final boolean isRotated()
    {
        return _b != 0 || _c != 0;
    }

    /**
     * Translates this transform by given x & y.
     */
    public void translate(double dx, double dy)
    {
        _tx += dx * _a + dy * _c;
        _ty += dx * _b + dy * _d;
        _inv = null;
    }

    /**
     * Translates this transform by given x & y in global space (pre-multiply).
     */
    public void preTranslate(double dx, double dy)
    {
        _tx += dx;
        _ty += dy;
        _inv = null;
    }

    /**
     * Rotates this transform by given angle in degrees.
     */
    public void rotate(double anAngle)
    {
        double angle = Math.toRadians(anAngle);
        double c = Math.cos(angle);
        double s = Math.sin(angle);
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
    public void scale(double sx, double sy)
    {
        concat(sx, 0, 0, sy, 0, 0);
    }

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
    public void concat(Transform aTr)
    {
        concat(aTr._a, aTr._b, aTr._c, aTr._d, aTr._tx, aTr._ty);
    }

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
        double a2 = a * _a + b * _c,    b2 = a * _b + b * _d;
        double c2 = c * _a + d * _c,    d2 = c * _b + d * _d;
        double tx2 = tx * _a + ty * _c + _tx;
        double ty2 = tx * _b + ty * _d + _ty;

        // Set new values
        _a = a2; _b = b2;
        _c = c2; _d = d2;
        _tx = tx2;
        _ty = ty2;
        _inv = null;
    }

    /**
     * Multiplies this transform by the given transform.
     */
    public void multiply(Transform aTr)
    {
        multiply(aTr._a, aTr._b, aTr._c, aTr._d, aTr._tx, aTr._ty);
    }

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
        double a2 = _a * a + _b * c, b2 = _a * b + _b * d;
        double c2 = _c * a + _d * c, d2 = _c * b + _d * d;
        double tx2 = _tx * a + _ty * c + tx;
        double ty2 = _tx * b + _ty * d + ty;

        // Set new values
        _a = a2; _b = b2;
        _c = c2; _d = d2;
        _tx = tx2;
        _ty = ty2;
        _inv = null;
    }

    /**
     * Inverts this transform.
     */
    public void invert()
    {
        double det = (_a * _d - _b * _c);
        if(det == 0) {
            _a = 1; _b = 0; _c = 0; _d = 1;
            _tx = _ty = 0;
        }

        else {
            double a = _d / det, b = -_b / det;
            double c = -_c / det, d = _a / det;
            double tx = (_c * _ty - _d * _tx) / det;
            double ty = (_b * _tx - _a * _ty) / det;
            _a = a; _b = b;
            _c = c; _d = d;
            _tx = tx;
            _ty = ty;
        }
        _inv = null;
    }

    /**
     * Returns the inverse.
     */
    public Transform getInverse()
    {
        if(_inv != null) return _inv;
        Transform t = clone();
        t.invert();
        return _inv = t;
    }

    /**
     * Clears the transform to identity.
     */
    public void clear()
    {
        _a = 1; _b = 0;
        _c = 0; _d = 1;
        _tx = _ty = 0;
        _inv = null;
    }

    /**
     * Returns the matrix.
     */
    public final double[] getMatrix()
    {
        double[] m = new double[6];
        getMatrix(m);
        return m;
    }

    /**
     * Loads the given matrix.
     */
    public final void getMatrix(double[] m)
    {
        m[0] = _a; m[1] = _b;
        m[2] = _c; m[3] = _d;
        m[4] = _tx;
        m[5] = _ty;
    }

    /**
     * Sets transform values to given matrix values.
     */
    public final void setMatrix(float[] m)
    {
        setMatrix(m[0], m[1], m[2], m[3], m[4], m[5]);
    }

    /**
     * Sets transform values to given matrix values.
     */
    public final void setMatrix(double[] m)
    {
        setMatrix(m[0], m[1], m[2], m[3], m[4], m[5]);
    }

    /**
     * Sets transform values to given transform values.
     */
    public final void setMatrix(Transform aTrans)
    {
        setMatrix(aTrans._a, aTrans._b, aTrans._c, aTrans._d, aTrans._tx, aTrans._ty);
    }

    /**
     * Sets transform values to given matrix values.
     */
    public final void setMatrix(double a, double b, double c, double d, double tx, double ty)
    {
        _a = a; _b = b;
        _c = c; _d = d;
        _tx = tx;
        _ty = ty;
        _inv = null;
    }

    /**
     * Transforms the given values.
     */
    public final void transformXYArray(double[] anAry)
    {
        transformXYArray(anAry, anAry.length / 2);
    }

    /**
     * Transforms the given values.
     */
    public final void transformXYArray(double[] anAry, int aPntCnt)
    {
        // Optimized
        if(isSimple()) {
            for(int i = 0, iMax = aPntCnt * 2; i < iMax; i += 2) {
                anAry[i] += _tx;
                anAry[i + 1] += _ty;
            }
            return;
        }

        // Normal
        for(int i = 0; i < aPntCnt * 2; i += 2) {
            double x = anAry[i];
            double y = anAry[i+1];
            anAry[i] = x * _a + y * _c + _tx;
            anAry[i+1] = x * _b + y * _d + _ty;
        }
    }

    /**
     * Transforms the given XY values and return as point.
     */
    public final Point transformXY(double aX, double aY)
    {
        Point point = new Point(aX, aY);
        transformPoint(point);
        return point;
    }

    /**
     * Transforms the given point.
     *
     *                     [  _a  _b  0 ]
     * P' = [ tx ty  1 ] x [  _c  _d  0 ] = [ tx * _a + ty * _c + _tx  tx * _b + ty * _d + ty   1  ]
     *                     [ _tx _ty  1 ]
     *
     */
    public final void transformPoint(Point aPoint)
    {
        double x2 = aPoint.x * _a + aPoint.y * _c + _tx;
        double y2 = aPoint.x * _b + aPoint.y * _d + _ty;
        aPoint.setXY(x2, y2);
    }

    /**
     * Transforms the given X value.
     */
    public final double transformX(double aX, double aY)
    {
        return aX * _a + aY * _c + _tx;
    }

    /**
     * Transforms the given Y value.
     */
    public final double transformY(double aX, double aY)
    {
        return aX * _b + aY * _d + _ty;
    }

    /**
     * Transforms the given size.
     */
    public final void transformSize(Size aSize)
    {
        double w = aSize.width;
        double h = aSize.height;
        double w2 = Math.abs(w * _a) + Math.abs(h * _c);
        double h2 = Math.abs(w * _b) + Math.abs(h * _d);
        aSize.setSize(w2, h2);
    }

    /**
     * Transforms the given rect.
     */
    public final void transformRect(Rect aRect)
    {
        double x1 = aRect.x;
        double y1 = aRect.y;
        double x2 = aRect.getMaxX();
        double y2 = aRect.getMaxY();
        double pts[] = new double[] { x1, y1, x2, y1, x2, y2, x1, y2 };
        transformXYArray(pts, 4);
        x1 = x2 = pts[0]; for(int i = 1; i < 4; i++) { double x = pts[i*2]; x1 = Math.min(x1,x); x2 = Math.max(x2,x); }
        y1 = y2 = pts[1]; for(int i = 1; i < 4; i++) { double y = pts[i*2+1]; y1 = Math.min(y1,y); y2 = Math.max(y2,y); }
        aRect.setRect(x1, y1,x2 - x1,y2 - y1);
    }

    /**
     * Transforms the given size as a vector (preserves negative values).
     */
    public void transformVector(Size aSize)
    {
        double w = aSize.width;
        double h = aSize.height;
        double w2 = w * _a + h * _c;
        double h2 = w * _b + h * _d;
        aSize.setSize(w2, h2);
    }

    /**
     * Standard clone implementation.
     */
    public Transform clone()
    {
        try { return (Transform) super.clone(); }
        catch(CloneNotSupportedException e) { return null; }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if(anObj == this) return true;
        Transform other = anObj instanceof Transform ? (Transform) anObj : null;
        if(other == null) return false;

        if(!MathUtils.equals(other._a, _a) || !MathUtils.equals(other._b, _b)) return false;
        if(!MathUtils.equals(other._c, _c) || !MathUtils.equals(other._d, _d)) return false;
        if(!MathUtils.equals(other._tx, _tx) || !MathUtils.equals(other._ty, _ty)) return false;
        return true;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return String.format("[ %f %f %f %f %f %f ]", _a, _b, _c, _d, _tx, _ty);
    }

    /**
     * Returns a rotation transform for given angle in degrees.
     */
    public static Transform getRotate(double theta)
    {
        Transform t = new Transform();
        t.rotate(theta);
        return t;
    }

    /**
     * Returns a rotation transform for given angle in degrees around given x/y point.
     */
    public static Transform getRotateAround(double anAngle, double aX, double aY)
    {
        Transform t = new Transform();
        t.rotateAround(anAngle, aX, aY);
        return t;
    }

    /**
     * Returns a scale transform.
     */
    public static Transform getScale(double sx, double sy)
    {
        return new Transform(sx, 0, 0, sy, 0, 0);
    }

    /**
     * Returns a transform from one rect to another.
     */
    public static Transform getTransformBetweenRects(Rect fromRect, Rect toRect)
    {
        // Sanity check for empty rect
        if(fromRect.isEmpty()) {
            System.err.println("Transform.getTrans: Empty rect");
            return IDENTITY;
        }

        // Get scale and translation and return new transform
        double sx = toRect.width / fromRect.width;
        double sy = toRect.height / fromRect.height;
        double tx = toRect.x - fromRect.x * sx;
        double ty = toRect.y - fromRect.y * sy;
        return new Transform(sx,0,0, sy, tx, ty);
    }
}