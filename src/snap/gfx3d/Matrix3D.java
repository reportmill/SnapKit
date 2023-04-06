/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.Arrays;

/**
 * This class represents a 3D transform. 
 */
public class Matrix3D implements Cloneable {

    // Double array holding actual matrix values
    public double[] mtx = new double[] { 1, 0, 0, 0,  0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

    // Constant for Identity transform
    public static Matrix3D IDENTITY = new Matrix3D();

    /**
     * Constructor (creates identity).
     */
    public Matrix3D()  { }

    /**
     * Constructor (creates identity).
     */
    public Matrix3D(double[] theDoubles)
    {
        System.arraycopy(theDoubles, 0, mtx, 0, 16);
    }

    /**
     * Constructor with given translations.
     */
    public Matrix3D(double aX, double aY, double aZ)
    {
        translate(aX, aY, aZ);
    }

    /**
     * Multiplies receiver by given transform: [this] = [this] x [aTrans]
     */
    public Matrix3D multiply(Matrix3D aTransform)
    {
        // Get this float array, given float array and new float array
        double[] m2 = aTransform.mtx;
        double[] m3 = new double[16];

        // Perform multiplication
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    m3[i + j * 4] += mtx[i + k * 4] * m2[k + j * 4];

        // Return this (loaded from m3)
        return fromArray(m3);
    }

    /**
     * Multiplies receiver by given transform: [this] =  [aTrans] x [this]
     */
    public Matrix3D premultiply(Matrix3D aTransform)
    {
        // Get this float array, given float array and new float array
        double[] m2 = aTransform.mtx;
        double[] m3 = new double[16];

        // Perform multiplication
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    m3[i + j * 4] += m2[i + k * 4] * mtx[k + j * 4];

        // Return this (loaded from m3)
        return fromArray(m3);
    }

    /**
     * Translates by given x, y & z.
     */
    public Matrix3D translate(double x, double y, double z)
    {
        Matrix3D rm = new Matrix3D();
        rm.mtx[3 * 4 + 0] = x;
        rm.mtx[3 * 4 + 1] = y;
        rm.mtx[3 * 4 + 2] = z;
        return multiply(rm);
    }

    /**
     * Rotate x axis by given degrees.
     */
    public Matrix3D rotateX(double anAngle)
    {
        Matrix3D rm = new Matrix3D();
        double angle = Math.toRadians(anAngle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        rm.mtx[1 * 4 + 1] = cos;
        rm.mtx[2 * 4 + 2] = cos;
        rm.mtx[1 * 4 + 2] = sin;
        rm.mtx[2 * 4 + 1] = -sin;
        return multiply(rm);
    }

    /**
     * Rotate y axis by given degrees.
     */
    public Matrix3D rotateY(double anAngle)
    {
        Matrix3D rm = new Matrix3D();
        double angle = Math.toRadians(anAngle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        rm.mtx[0 * 4 + 0] = cos;
        rm.mtx[2 * 4 + 2] = cos;
        rm.mtx[0 * 4 + 2] = -sin;
        rm.mtx[2 * 4 + 0] = sin;
        return multiply(rm);
    }

    /**
     * Rotate z axis by given degrees.
     */
    public Matrix3D rotateZ(double anAngle)
    {
        Matrix3D rm = new Matrix3D();
        double angle = Math.toRadians(anAngle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        rm.mtx[0 * 4 + 0] = cos;
        rm.mtx[1 * 4 + 1] = cos;
        rm.mtx[0 * 4 + 1] = sin;
        rm.mtx[1 * 4 + 0] = -sin;
        return multiply(rm);
    }

    /**
     * Rotate about arbitrary axis.
     */
    public Matrix3D rotateAboutAxis(double anAngle, double aX, double aY, double aZ)
    {
        Matrix3D rm = new Matrix3D();
        double angle = Math.toRadians(anAngle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double t = 1 - cos;
        rm.mtx[0 * 4 + 0] = t * aX * aX + cos;
        rm.mtx[0 * 4 + 1] = t * aX * aY + sin * aZ;
        rm.mtx[0 * 4 + 2] = t * aX * aZ - sin * aY;
        rm.mtx[1 * 4 + 0] = t * aX * aY - sin * aZ;
        rm.mtx[1 * 4 + 1] = t * aY * aY + cos;
        rm.mtx[1 * 4 + 2] = t * aY * aZ + sin * aX;
        rm.mtx[2 * 4 + 0] = t * aX * aY + sin * aY;
        rm.mtx[2 * 4 + 1] = t * aY * aZ - sin * aX;
        rm.mtx[2 * 4 + 2] = t * aZ * aZ + cos;
        return multiply(rm);
    }

    /**
     * Rotate x,y,z with three Euler angles (same as rotateX(rx).rotateY(ry).rotateZ(rz)).
     */
    public Matrix3D rotateXYZ(double rx, double ry, double rz)
    {
        Matrix3D rm = new Matrix3D();
        double ax = Math.toRadians(rx);
        double ay = Math.toRadians(ry);
        double az = Math.toRadians(rz);
        double a = Math.cos(ax);
        double b = Math.sin(ax);
        double c = Math.cos(ay);
        double d = Math.sin(ay);
        double e = Math.cos(az);
        double f = Math.sin(az);
        double ad = a*d;
        double bd = b*d;

        rm.mtx[0 * 4 + 0] = c * e;
        rm.mtx[1 * 4 + 0] = -c * f;
        rm.mtx[2 * 4 + 0] = d;
        rm.mtx[0 * 4 + 1] = bd * e + a * f;
        rm.mtx[1 * 4 + 1] = -bd * f + a * e;
        rm.mtx[2 * 4 + 1] = -b * c;
        rm.mtx[0 * 4 + 2] = -ad * e + b * f;
        rm.mtx[1 * 4 + 2] = ad * f + b * e;
        rm.mtx[2 * 4 + 2] = a * c;
        return multiply(rm);
    }

    /**
     * Skew by the given degrees.
     */
    public Matrix3D scale(double aSX, double aSY, double aSZ)
    {
        Matrix3D rm = new Matrix3D();
        rm.mtx[0 * 4 + 0] = aSX;
        rm.mtx[1 * 4 + 1] = aSY;
        rm.mtx[2 * 4 + 2] = aSZ;
        return multiply(rm);
    }

    /**
     * Invert.
     */
    public final Matrix3D invert()
    {
        // If IDENTITY, just return
        if (this == IDENTITY)
            return this;

        double[] mat = toArray();
        double[] matInv = new Matrix3D().toArray();
        double determinant = 1;
        double factor;

        // Forward elimination
        for (int i = 0; i < 3; i++) {

            // Get pivot and pivotsize
            int pivot = i;
            double pivotsize = Math.abs(mat[i * 4 + i]);

            // Iterate
            for (int j = i + 1; j < 4; j++)
                if (pivotsize < Math.abs(mat[j * 4 + i])) {
                    pivot = j;
                    pivotsize = Math.abs(mat[j * 4 + i]);
                }

            // Test pivotsize
            if (pivotsize == 0)
                return fromArray(matInv);

            // Do something else
            if (pivot != i) {
                for (int j = 0; j < 4; j++) {
                    double tmp = mat[i * 4 + j];
                    mat[i * 4 + j] = mat[pivot * 4 + j];
                    mat[pivot * 4 + j] = tmp;
                    tmp = matInv[i * 4 + j];
                    matInv[i * 4 + j] = matInv[pivot * 4 + j];
                    matInv[pivot * 4 + j] = tmp;
                }
                determinant = -determinant;
            }

            // Something else
            for (int j = i + 1; j < 4; j++){
                factor = mat[j * 4 + i] / mat[i * 4 + i];
                for (int k = 0; k != 4; k++) {
                    mat[j * 4 + k] -= factor * mat[i * 4 + k];
                    matInv[j * 4 + k] -= factor * matInv[i * 4 + k];
                }
            }
        }

        // Backward substitution
        for (int i = 3; i >= 0; --i){
            if ((factor = mat[i * 4 + i]) == 0.0)
                return fromArray(matInv);
            for (int j = 0; j != 4; j++) {
                mat[i * 4 + j] /= factor;
                matInv[i * 4 + j] /= factor;
            }
            determinant *= factor;
            for (int j = 0; j != i; j++) {
                factor = mat[j * 4 + i];
                for (int k = 0; k != 4; k++) {
                    mat[j * 4 + k] -= factor * mat[i * 4 + k];
                    matInv[j * 4 + k] -= factor * matInv[i * 4 + k];
                }
            }
        }

        return fromArray(matInv);
    }

    /**
     * Transforms a given point (and returns it as a convenience).
     *
     * This pre-multiplies OpenGL style:  [ P' ] = [ Matrix ] x [ P ]
     *
     *     3 x 1          3 x 3            3 x 1              3 x 1
     *
     *    [ px' ]   [ m00 m10 m20 m30 ]   [ px ]   [ m00 * px + m10 * py + m20 * pz + m30,
     *    [ py' ] = [ m01 m11 m21 m31 ] x [ py ] =   m01 * px + m11 * py + m21 * pz + m31,
     *    [ pz' ]   [ m02 m12 m22 m32 ]   [ pz ]     m02 * px + m12 * py + m22 * pz + m32,
     *    [  w  ]   [ m03 m13 m23 m33 ]   [ 1 ]      m03 * px + m13 * py + m23 * pz + m33 (w) ]
     *
     * JOML looks like this:
     *
     *    double W = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
     *    double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) / W;
     *    double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) / W;
     *    double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) / W;
     */
    public final Point3D transformPoint(Point3D aPoint)
    {
        double x2 = mtx[0 * 4 + 0] * aPoint.x + mtx[1 * 4 + 0] * aPoint.y + mtx[2 * 4 + 0] * aPoint.z + mtx[3 * 4 + 0];
        double y2 = mtx[0 * 4 + 1] * aPoint.x + mtx[1 * 4 + 1] * aPoint.y + mtx[2 * 4 + 1] * aPoint.z + mtx[3 * 4 + 1];
        double z2 = mtx[0 * 4 + 2] * aPoint.x + mtx[1 * 4 + 2] * aPoint.y + mtx[2 * 4 + 2] * aPoint.z + mtx[3 * 4 + 2];
        double w =  mtx[0 * 4 + 3] * aPoint.x + mtx[1 * 4 + 3] * aPoint.y + mtx[2 * 4 + 3] * aPoint.z + mtx[3 * 4 + 3];
        aPoint.x = x2 / w;
        aPoint.y = y2 / w;
        aPoint.z = z2 / w;
        return aPoint;
    }

    /**
     * Transforms a given point (and returns it as a convenience).
     */
    public final Point3D transformXYZ(double aX, double aY, double aZ)
    {
        return transformPoint(new Point3D(aX, aY, aZ));
    }

    /**
     * Transforms a given point (and returns it as a convenience).
     */
    public final void transformXYZArray(float[] pointsArray, int aPointCount)
    {
        for (int i = 0; i < aPointCount; i++) {
            int pointsArrayIndex = i * 3;
            float x1 = pointsArray[pointsArrayIndex + 0];
            float y1 = pointsArray[pointsArrayIndex + 1];
            float z1 = pointsArray[pointsArrayIndex + 2];
            double x2 = mtx[0 * 4 + 0] * x1 + mtx[1 * 4 + 0] * y1 + mtx[2 * 4 + 0] * z1 + mtx[3 * 4 + 0];
            double y2 = mtx[0 * 4 + 1] * x1 + mtx[1 * 4 + 1] * y1 + mtx[2 * 4 + 1] * z1 + mtx[3 * 4 + 1];
            double z2 = mtx[0 * 4 + 2] * x1 + mtx[1 * 4 + 2] * y1 + mtx[2 * 4 + 2] * z1 + mtx[3 * 4 + 2];
            double w =  mtx[0 * 4 + 3] * x1 + mtx[1 * 4 + 3] * y1 + mtx[2 * 4 + 3] * z1 + mtx[3 * 4 + 3];
            pointsArray[pointsArrayIndex + 0] = (float) (x2 / w);
            pointsArray[pointsArrayIndex + 1] = (float) (y2 / w);
            pointsArray[pointsArrayIndex + 2] = (float) (z2 / w);
        }
    }

    /**
     * Transforms a given vector (and returns it as a convenience).
     */
    public final Vector3D transformVector(Vector3D aVector)
    {
        return transformVectorXYZ(aVector.x, aVector.y, aVector.z);
    }

    /**
     * Transforms a given vector (and returns it as a convenience).
     */
    public final Vector3D transformVectorXYZ(double vx, double vy, double vz)
    {
        double x2 = mtx[0 * 4 + 0] * vx + mtx[1 * 4 + 0] * vy + mtx[2 * 4 + 0] * vz;
        double y2 = mtx[0 * 4 + 1] * vx + mtx[1 * 4 + 1] * vy + mtx[2 * 4 + 1] * vz;
        double z2 = mtx[0 * 4 + 2] * vx + mtx[1 * 4 + 2] * vy + mtx[2 * 4 + 2] * vz;
        return new Vector3D(x2, y2, z2);
    }

    /**
     * Returns a double array for the transform.
     */
    public final double[] toArray()
    {
        return mtx.clone();
    }

    /**
     * Returns a double array for the transform.
     */
    public final double[] toArray(double[] anArray)
    {
        System.arraycopy(mtx, 0, anArray, 0, 16);
        return anArray;
    }

    /**
     * Loads the transform from a double array.
     */
    public final Matrix3D fromArray(double[] mat2)
    {
        System.arraycopy(mat2, 0, mtx, 0, 16);
        return this;
    }

    /**
     * Standard clone implementation.
     */
    public Matrix3D clone()
    {
        Matrix3D clone;
        try { clone = (Matrix3D) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone.mtx = mtx.clone();
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String mtxStr = Arrays.toString(mtx);
        return "Matrix3D { " + mtxStr + " }";
    }

    /**
     * Returns a simple perspective transform.
     */
    public static Matrix3D newPerspective(double d)
    {
        Matrix3D xfm = new Matrix3D();
        xfm.mtx[2 * 4 + 3] = 1 / d; //p.m[3][3] = 0;
        return xfm;
    }

    /**
     * Returns a perspective transform.
     *
     *     [ f / aspect     0                  0                                0              ]     [ px ]
     *     [    0           f                  0                                0              ]  x  [ py ]
     *     [    0           0     (far + near) / (near - far)    2 * far * near / (near - far) ]     [ pz ]
     *     [    0           0                 -1                                0              ]     [ w  ]
     *
     */
    public static Matrix3D newPerspective(double fieldOfViewY, double aspect, double nearZ, double farZ)
    {
        Matrix3D xfm = new Matrix3D();
        double f = 1d / Math.tan(Math.toRadians(fieldOfViewY / 2));
        double nearMinusFar = nearZ - farZ;

        // Set elements like OpenGL: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
        xfm.mtx[0 * 4 + 0] = f / aspect;
        xfm.mtx[1 * 4 + 1] = f;
        xfm.mtx[2 * 4 + 2] = (farZ + nearZ) / nearMinusFar;
        xfm.mtx[2 * 4 + 3] = -1;
        xfm.mtx[3 * 4 + 2] = 2 * farZ * nearZ / nearMinusFar;
        xfm.mtx[3 * 4 + 3] = 0;

        // Return
        return xfm;
    }

    /**
     * Returns a orthographic transform.
     *
     *     [ 2 / (r - l)     0            0              tx ]     [ px ]
     *     [    0       2 / (t - b)       0              ty ]  x  [ py ]
     *     [    0           0        -2 / (far - near)   tz ]     [ pz ]
     *     [    0           0           -1               0  ]     [ w  ]
     *
     */
    public static Matrix3D newOrtho(double left, double right, double bottom, double top, double nearZ, double farZ)
    {
        Matrix3D xfm = new Matrix3D();

        // Set elements like OpenGL: https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glOrtho.xml
        xfm.mtx[0 * 4 + 0] = 2 / (right - left);
        xfm.mtx[1 * 4 + 1] = 2 / (top - bottom);
        xfm.mtx[2 * 4 + 2] = -2 / (farZ - nearZ);

        // Set tx, ty, tz
        xfm.mtx[3 * 4 + 0] = - (right + left) / (right - left);
        xfm.mtx[3 * 4 + 1] = - (top + bottom) / (top - bottom);
        xfm.mtx[3 * 4 + 2] = - (farZ + nearZ) / (farZ - nearZ);

        // Return
        return xfm;
    }
}