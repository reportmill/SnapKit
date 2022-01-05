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
    public Matrix3D invert()
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
    public Matrix3D fromArray(double[] mat2)
    {
        System.arraycopy(mat2, 0, mtx, 0, 16);
        return this;
    }

    /**
     * Standard clone implementation.
     */
    public Matrix3D clone()
    {
        Matrix3D copy = new Matrix3D();
        return copy.fromArray(mtx);
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
}