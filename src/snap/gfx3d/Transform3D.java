/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.Arrays;

/**
 * This class represents a 3D transform. 
 */
public class Transform3D implements Cloneable {
    
    // All of the transform components
    public double[] mtx = new double[] { 1, 0, 0, 0,  0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

    // Constant for Identity transform
    public static Transform3D  IDENTITY = new Transform3D();
    
    /**
     * Creates a Transform3D with the identity matrix.
     */
    public Transform3D()  { }

    /**
     * Creates a Transform3D with given translations.
     */
    public Transform3D(double aX, double aY, double aZ)
    {
        translate(aX, aY, aZ);
    }

    /**
     * Multiplies receiver by given transform: [this] = [this] x [aTrans]
     */
    public Transform3D multiply(Transform3D aTransform)
    {
        // Get this float array, given float array and new float array
        double[] m1 = mtx;
        double[] m2 = aTransform.mtx;
        double[] m3 = new double[16];

        // Perform multiplication
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    m3[i * 4 + j] += m1[i * 4 + k] * m2[k * 4 + j];

        // Return this (loaded from m3)
        return fromArray(m3);
    }

    /**
     * Translates by given x, y & z.
     */
    public Transform3D translate(double x, double y, double z)
    {
        //m[3][0] += x; m[3][1] += y; m[3][2] += z;
        Transform3D rm = new Transform3D();
        rm.mtx[3 * 4 + 0] = x;
        rm.mtx[3 * 4 + 1] = y;
        rm.mtx[3 * 4 + 2] = z;
        return multiply(rm);
    }

    /**
     * Rotate x axis by given degrees.
     */
    public Transform3D rotateX(double anAngle)
    {
        Transform3D rm = new Transform3D();
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
    public Transform3D rotateY(double anAngle)
    {
        Transform3D rm = new Transform3D();
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
    public Transform3D rotateZ(double anAngle)
    {
        Transform3D rm = new Transform3D();
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
     * Rotate x,y,z with three Euler angles (same as rotateX(rx).rotateY(ry).rotateZ(rz)).
     */
    public Transform3D rotateXYZ(double rx, double ry, double rz)
    {
        Transform3D rm = new Transform3D();
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
     * Scale by the given factors.
     */
    public Transform3D scale(double aScaleX, double aScaleY, double aScaleZ)
    {
        Transform3D rm = new Transform3D();
        rm.mtx[0 * 4 + 0] = aScaleX;
        rm.mtx[1 * 4 + 1] = aScaleY;
        rm.mtx[2 * 4 + 2] = aScaleZ;
        return multiply(rm);
    }

    /**
     * Skew by the given degrees.
     */
    public Transform3D skew(double skx, double sky)
    {
        Transform3D rm = new Transform3D();
        rm.mtx[2 * 4 + 0] = skx; //Math.toRadians(skx);
        rm.mtx[2 * 4 + 1] = sky; //Math.toRadians(sky);
        return multiply(rm);
    }

    /**
     * Returns a simple perspective transform.
     */
    public static Transform3D newPerspective(double d)
    {
        Transform3D xfm = new Transform3D();
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
    public static Transform3D newPerspective(double fieldOfViewY, double aspect, double nearZ, double farZ)
    {
        Transform3D xfm = new Transform3D();
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
     * Invert.
     */
    public Transform3D invert()
    {
        // If IDENTITY, just return
        if (this == IDENTITY)
            return this;

        double[] mat = toArray();
        double[] matInv = new Transform3D().toArray();
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
     * Transform3D currently post-multiplies transforms:
     *
     *         3 x 3             3 x 1                    3 x 1
     *
     *    [ m00 m10 m20 m30 ]   [ px ]   [ m00 * px + m10 * py + m20 * pz + m30 ]
     *    [ m01 m11 m21 m31 ] x [ py ] = [ m01 * px + m11 * py + m21 * pz + m31 ]
     *    [ m02 m12 m22 m32 ]   [ pz ]   [ m02 * px + m12 * py + m22 * pz + m32 ]
     *    [ m03 m13 m23 m33 ]   [ 1 ]    [ m03 * px + m13 * py + m23 * pz + m33 ] (w)
     *
     * But we want to get to pre-multiply OpenGL style:
     *
     *         1 x 3             3 x 3                    1 x 3
     *
     *    [ px py pz 1 ]   [ m00 m10 m20 m30 ]   [ m00 * px + m10 * py + m20 * pz + m30,
     *                   x [ m01 m11 m21 m31 ] =   m01 * px + m11 * py + m21 * pz + m31,
     *                     [ m02 m12 m22 m32 ]     m02 * px + m12 * py + m22 * pz + m32,
     *                     [ m03 m13 m23 m33 ]     m03 * px + m13 * py + m23 * pz + m33 (w) ]
     *
     * JOML looks like this:
     *
     *    double W = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
     *    double rx = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w))) / W;
     *    double ry = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w))) / W;
     *    double rz = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w))) / W;
     */
    public Point3D transformPoint(Point3D aPoint)
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
    public Point3D transformPoint(double aX, double aY, double aZ)
    {
        return transformPoint(new Point3D(aX, aY, aZ));
    }

    /**
     * Transforms a given vector (and returns it as a convenience).
     */
    public Vector3D transformVector(Vector3D aVector)
    {
        double x2 = mtx[0 * 4 + 0] * aVector.x + mtx[1 * 4 + 0] * aVector.y + mtx[2 * 4 + 0] * aVector.z;
        double y2 = mtx[0 * 4 + 1] * aVector.x + mtx[1 * 4 + 1] * aVector.y + mtx[2 * 4 + 1] * aVector.z;
        double z2 = mtx[0 * 4 + 2] * aVector.x + mtx[1 * 4 + 2] * aVector.y + mtx[2 * 4 + 2] * aVector.z;
        aVector.x = x2; aVector.y = y2; aVector.z = z2;
        return aVector;
    }

    /**
     * Returns a double array for the transform.
     */
    public double[] toArray()
    {
        return mtx.clone();
    }

    /**
     * Returns a double array for the transform.
     */
    public double[] toArray(double[] anArray)
    {
        System.arraycopy(mtx, 0, anArray, 0, 16);
        return anArray;
    }

    /**
     * Loads the transform from a double array.
     */
    public Transform3D fromArray(double[] mat2)
    {
        System.arraycopy(mat2, 0, mtx, 0, 16);
        return this;
    }

    /**
     * Standard clone implementation.
     */
    public Transform3D clone()
    {
        Transform3D copy = new Transform3D();
        return copy.fromArray(mtx);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String mtxStr = Arrays.toString(mtx);
        return "Transform3D { " + mtxStr + " }";
    }
}