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
    public double[][] m = new double[][] { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };

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
     * Multiplies receiver by given transform.
     */
    public Transform3D multiply(Transform3D aTransform)
    {
        // Get this float array, given float array and new float array
        double[][] m1 = m;
        double[][] m2 = aTransform.m;
        double[][] m3 = new double[4][4];

        // Perform multiplication
        for (int i=0; i<4; i++)
            for (int j=0; j<4; j++)
                for (int k=0; k<4; k++)
                    m3[i][j] += m1[i][k]*m2[k][j];

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
        rm.m[3][0] = x;
        rm.m[3][1] = y;
        rm.m[3][2] = z;
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
        rm.m[1][1] = cos;
        rm.m[2][2] = cos;
        rm.m[1][2] = sin;
        rm.m[2][1] = -sin;
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
        rm.m[0][0] = cos;
        rm.m[2][2] = cos;
        rm.m[0][2] = -sin;
        rm.m[2][0] = sin;
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
        rm.m[0][0] = cos;
        rm.m[1][1] = cos;
        rm.m[0][1] = sin;
        rm.m[1][0] = -sin;
        return multiply(rm);
    }

    /**
     * Rotate about arbitrary axis.
     */
    public Transform3D rotate(Vector3D anAxis, double anAngle)
    {
        Transform3D rm = new Transform3D();
        double angle = Math.toRadians(anAngle);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double t = 1 - cos;
        rm.m[0][0] = t * anAxis.x * anAxis.x + cos;
        rm.m[0][1] = t * anAxis.x * anAxis.y + sin * anAxis.z;
        rm.m[0][2] = t * anAxis.x * anAxis.z - sin * anAxis.y;
        rm.m[1][0] = t * anAxis.x * anAxis.y - sin * anAxis.z;
        rm.m[1][1] = t * anAxis.y * anAxis.y + cos;
        rm.m[1][2] = t * anAxis.y * anAxis.z + sin * anAxis.x;
        rm.m[2][0] = t * anAxis.x * anAxis.y + sin * anAxis.y;
        rm.m[2][1] = t * anAxis.y * anAxis.z - sin * anAxis.x;
        rm.m[2][2] = t * anAxis.z * anAxis.z + cos;
        return multiply(rm);
    }

    /**
     * Rotate x,y,z with three Euler angles (same as rotateX(rx).rotateY(ry).rotateZ(rz)).
     */
    public Transform3D rotate(double rx, double ry, double rz)
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

        rm.m[0][0] = c * e;
        rm.m[1][0] = -c * f;
        rm.m[2][0] = d;
        rm.m[0][1] = bd * e + a * f;
        rm.m[1][1] = -bd * f + a * e;
        rm.m[2][1] = -b * c;
        rm.m[0][2] = -ad * e + b * f;
        rm.m[1][2] = ad * f + b * e;
        rm.m[2][2] = a * c;
        return multiply(rm);
    }

    /**
     * Returns a matrix whose axes are aligned with the world (screen) coordinate system.
     * All rotations & skews are removed, and perspective is replaced by uniform scaling.
     */
    public Transform3D worldAlign(Point3D originPt)
    {
       Point3D tp = transform(originPt.clone());
       double w = m[2][3] * originPt.z + m[3][3];

       for (int i=0; i<4; ++i)
           for (int j=0; j<4; ++j)
               m[i][j] = i==j ? (i<2 ? 1f/w : 1) : 0;
       m[3][0] = tp.x - originPt.x/w;
       m[3][1] = tp.y - originPt.y/w;
       m[3][2] = tp.z - originPt.z/w;
       return this;
    }

    /**
     * Skew by the given degrees.
     */
    public Transform3D skew(double skx, double sky)
    {
        Transform3D rm = new Transform3D();
        rm.m[2][0] = skx; //Math.toRadians(skx);
        rm.m[2][1] = sky; //Math.toRadians(sky);
        return multiply(rm);
    }

    /**
     * Apply perspective transform.
     */
    public Transform3D perspective(double d)
    {
        Transform3D xfm = new Transform3D();
        xfm.m[2][3] = 1/d; //p.m[3][3] = 0;
        return multiply(xfm);
    }

    /**
     * Invert.
     */
    public Transform3D invert()
    {
        // If IDENTITY, just return
        if (this == IDENTITY)
            return this;

        double[][] mat = toArray();
        double[][] matInv = new Transform3D().toArray();
        double determinant = 1;
        double factor;

        // Forward elimination
        for (int i=0; i<3; i++) {

            // Get pivot and pivotsize
            int pivot = i;
            double pivotsize = Math.abs(mat[i][i]);

            // Iterate
            for (int j=i+1; j<4; j++)
                if (pivotsize < Math.abs(mat[j][i])) {
                    pivot = j;
                    pivotsize = Math.abs(mat[j][i]);
                }

            // Test pivotsize
            if (pivotsize == 0)
                return fromArray(matInv);

            // Do something else
            if (pivot != i) {
                for (int j=0; j<4; j++) {
                    double tmp = mat[i][j];
                    mat[i][j] = mat[pivot][j];
                    mat[pivot][j] = tmp;
                    tmp = matInv[i][j];
                    matInv[i][j] = matInv[pivot][j];
                    matInv[pivot][j] = tmp;
                }
                determinant = -determinant;
            }

            // Something else
            for (int j=i+1; j<4; j++){
                factor = mat[j][i] / mat[i][i];
                for (int k=0; k!=4; k++) {
                    mat[j][k] -= factor * mat[i][k];
                    matInv[j][k] -= factor * matInv[i][k];
                }
            }
        }

        // Backward substitution
        for (int i=3; i>=0; --i){
            if ((factor = mat[i][i])==0.0)
                return fromArray(matInv);
            for (int j=0; j!=4; j++) {
                mat[i][j] /= factor;
                matInv[i][j] /= factor;
            }
            determinant *= factor;
            for (int j=0; j!=i; j++) {
                factor = mat[j][i];
                for (int k=0; k!=4; k++) {
                    mat[j][k] -= factor * mat[i][k];
                    matInv[j][k] -= factor * matInv[i][k];
                }
            }
        }

        return fromArray(matInv);
    }

    /**
     * Transforms a given point (and returns it as a convenience).
     */
    public Point3D transform(Point3D aPoint)
    {
        double x2 = m[0][0] * aPoint.x + m[1][0] * aPoint.y + m[2][0] * aPoint.z + m[3][0];
        double y2 = m[0][1] * aPoint.x + m[1][1] * aPoint.y + m[2][1] * aPoint.z + m[3][1];
        double z2 = m[0][2] * aPoint.x + m[1][2] * aPoint.y + m[2][2] * aPoint.z + m[3][2];
        double w =  m[0][3] * aPoint.x + m[1][3] * aPoint.y + m[2][3] * aPoint.z + m[3][3];
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
        return transform(new Point3D(aX, aY, aZ));
    }

    /**
     * Transforms a given vector (and returns it as a convenience).
     */
    public Vector3D transform(Vector3D aVector)
    {
        double x2 = m[0][0] * aVector.x + m[1][0] * aVector.y + m[2][0] * aVector.z;
        double y2 = m[0][1] * aVector.x + m[1][1] * aVector.y + m[2][1] * aVector.z;
        double z2 = m[0][2] * aVector.x + m[1][2] * aVector.y + m[2][2] * aVector.z;
        aVector.x = x2; aVector.y = y2; aVector.z = z2;
        return aVector;
    }

    /**
     * Returns a float array for the transform.
     */
    public double[][] toArray()
    {
        return m.clone();
    }

    /**
     * Loads the transform flom a float array.
     */
    public Transform3D fromArray(double mat2[][])
    {
        for (int i=0; i<4; i++)
            for (int j=0; j<4; j++)
                m[i][j] = mat2[i][j];
        return this;
    }

    /**
     * Standard clone implemenation.
     */
    public Transform3D clone()
    {
        Transform3D copy = new Transform3D();
        return copy.fromArray(m);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "Transform3D { " +
            Arrays.toString(m) +
            " }";
    }
}