/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.*;

/**
 * Bezier curve fitting code adapted from 
 *   "An Algorithm for Automatically Fitting Digitized Curves", by Philip J. Schneider
 *   "Graphics Gems", 1990 - Andrew S. Glassner, ed.
 */
public class PathFitCurves {

    /**
     * Takes a path with a bunch of line-to segments and replaces them with curves.
     */
    public static void fitCurveFromPointIndex(Path aPath, int anIndex)
    {
        // Copy the points to be fit (remove duplicates that are adjacent or within one point)
        List points = new ArrayList(aPath.getPointCount()-anIndex);
        for (int i=anIndex, iMax=aPath.getPointCount(); i<iMax; i++) { Point point = aPath.getPoint(i);

            // Remove last point if equal to point 2 points back, otherwise don't add if equal to last point
            int j = points.size();
            if (j>1 && point.equals(points.get(j-2)))
                points.remove(j-1);
            else if (j==0 || !point.equals(points.get(j-1)))
                points.add(point);
        }

        // Fit curves to the discrete line segments added since mouse down
        if (points.size()>1) {
            Point pointArray[] = (Point[]) points.toArray(new Point[0]);
            List<Point> beziers = FitCurve(pointArray, pointArray.length, 40);

            // Remove the discrete line segments added since mouse down
            while (aPath.getPointCount() > anIndex)
                aPath.removeLastSeg();

            // Add new curves to self and register animation at each one
            for (int i=0, iMax=beziers.size()/3; i<iMax; i++) {
                Point cp0 = beziers.get(i*3+1);
                Point cp1 = beziers.get(i*3+2);
                Point p1 = beziers.get(i*3+3);
                aPath.curveTo(cp0.x, cp0.y, cp1.x, cp1.y, p1.x, p1.y);
            }
        }
    }

    /**
     * Returns a curve fit path for given points.
     */
    public static Shape getCurveFitPathForPoints(Point[] thePoints)
    {
        List<Point> beziers = FitCurve(thePoints, thePoints.length, 40);

        // Create new path and add curves
        Path2D path = new Path2D();
        for (int i=0, iMax=beziers.size()/3; i<iMax; i++) {
            Point cp0 = beziers.get(i*3+1);
            Point cp1 = beziers.get(i*3+2);
            Point p1 = beziers.get(i*3+3);
            path.curveTo(cp0.x, cp0.y, cp1.x, cp1.y, p1.x, p1.y);
        }

        // Return path
        return path;
    }

    /**
     * Fit a Bezier curve to a set of digitized points.
     */
    private static List <Point> FitCurve(Point d[], int nPts, double error)
    {
        // Approximate unit tangents at endpoints of digitized curve
        Point tHat1 = V2Normalize(V2Sub(d[1],d[0])), tHat2 = V2Normalize(V2Sub(d[nPts-2],d[nPts-1]));

        // Call FitCubic on the whole range of points (recursively) and add bezier points to list
        List <Point> beziers = new ArrayList();
        FitCubic(beziers, d, 0, nPts - 1, tHat1, tHat2, error);
        return beziers;
    }

    /**
     * Fit a Bezier curve to a (sub)set of digitized points.
     */
    private static void FitCubic(List beziers, Point d[], int first, int last, Point tHat1, Point tHat2, double error)
    {
        // Use heuristic if region only has two points in it
        int pcount = last - first + 1;
        if (pcount==2) {
            double dist = d[last].getDistance(d[first])/3;
            Point bezCurve[] = new Point[] { d[first], null, null, d[last] };
            tHat1 = V2Scale(tHat1, dist);
            bezCurve[1] = V2Add(bezCurve[0], tHat1);
            tHat2 = V2Scale(tHat2, dist);
            bezCurve[2] = V2Add(bezCurve[3], tHat2);
            ConcatBezierCurve(beziers, bezCurve);
            return;
        }

        // Parameterize points, and attempt to fit curve
        double u[] = ChordLengthParameterize(d, first, last);
        Point bezCurve[] = GenerateBezier(d, first, last, u, tHat1, tHat2);

        // Find max deviation of points to fitted curve
        double result[] = ComputeMaxError(d, first, last, bezCurve, u);
        double maxError = result[0];
        int splitPoint = (int)result[1];

        if (maxError < error) {
            ConcatBezierCurve(beziers, bezCurve);
            return;
        }

        // If error not too large, try some reparameterization and iteration (4 times?)
        double iterationError = error*error;      // Error below which you try iterating
        if (maxError < iterationError) {
            for (int i=0; i<4; i++) {
                double u2[] = Reparameterize(d, first, last, u, bezCurve);
                bezCurve = GenerateBezier(d, first, last, u2, tHat1, tHat2);
                double result2[] = ComputeMaxError(d, first, last, bezCurve, u2);
                maxError = result2[0];
                splitPoint = (int)result2[1];

                if (maxError<error) {
                    ConcatBezierCurve(beziers, bezCurve);
                    return;
                }
                u = u2;
            }
        }

        // Fitting failed -- split at max error point and fit recursively
        Point tHatCenter = ComputeCenterTangent(d, splitPoint); // Unit tangent vector at splitPoint
        FitCubic(beziers, d, first, splitPoint, tHat1, tHatCenter, error);
        tHatCenter = V2Negate(tHatCenter);
        FitCubic(beziers, d, splitPoint, last, tHatCenter, tHat2, error);
    }

    /**
     * Use least-squares method to find Bezier control points for region.
     */
    private static Point[] GenerateBezier(Point d[], int first, int last, double uPrime[], Point tHat1, Point tHat2)
    {
        // Compute the A's
        int pcount = last - first + 1; // Number of pts in sub-curve
        Point A[][] = new Point[pcount][2];   // Precomputed rhs for eqn
        for (int i=0; i<pcount; i++) {
            A[i][0] = V2Scale(tHat1, B1(uPrime[i]));
            A[i][1] = V2Scale(tHat2, B2(uPrime[i]));
        }

        // Load Matrix C & X
        double C[][] = {{0,0},{0,0}}, X[] = {0,0};
        for (int i=0; i<pcount; i++) {
            C[0][0] += V2Dot(A[i][0], A[i][0]);
            C[0][1] += V2Dot(A[i][0], A[i][1]);
            C[1][0] = C[0][1];
            C[1][1] += V2Dot(A[i][1], A[i][1]);

            Point tmp = V2Sub(d[first + i], V2Add(V2Scale(d[first], B0(uPrime[i])), V2Add(V2Scale(d[first], B1(uPrime[i])),
                V2Add(V2Scale(d[last], B2(uPrime[i])), V2Scale(d[last], B3(uPrime[i]))))));

            X[0] += V2Dot(A[i][0], tmp); X[1] += V2Dot(A[i][1], tmp);
        }

        // Compute the determinants of C and X
        double det_C0_C1 = C[0][0] * C[1][1] - C[1][0] * C[0][1];
        double det_C0_X  = C[0][0] * X[1]    - C[0][1] * X[0];
        double det_X_C1  = X[0]    * C[1][1] - X[1]    * C[0][1];

        // Finally, derive alpha values, left and right
        if (det_C0_C1 == 0.0)
            det_C0_C1 = (C[0][0] * C[1][1]) * 10e-12;
        double alpha_l = det_X_C1 / det_C0_C1;
        double alpha_r = det_C0_X / det_C0_C1;

        // If alpha negative, use the Wu/Barsky heuristic (see text)
        if (alpha_l < 0.0 || alpha_r < 0.0) {
            double dist = d[last].getDistance(d[first])/ 3.0;
            Point bezCurve2[] = new Point[] { d[first], null, null, d[last] };
            tHat1 = V2Scale(tHat1, dist);
            bezCurve2[1] = V2Add(bezCurve2[0], tHat1);
            tHat2 = V2Scale(tHat2, dist);
            bezCurve2[2] = V2Add(bezCurve2[3], tHat2);
            return bezCurve2;
        }

        // First and last control points of Bezier curve are positioned exactly at first and last data points.
        // Control points 1 and 2 are positioned an alpha distance out on the tangent vectors, left & right, respectively
        Point bezCurve2[] = new Point[] { d[first], null, null, d[last] };
        tHat1 = V2Scale(tHat1, alpha_l);
        bezCurve2[1] = V2Add(bezCurve2[0], tHat1);
        tHat2 = V2Scale(tHat2, alpha_r);
        bezCurve2[2] = V2Add(bezCurve2[3], tHat2);
        return bezCurve2;
    }

    // Given set of points & their parameterization, find better parameterization
    private static double[] Reparameterize(Point d[], int first, int last, double u[], Point bezCurve[])
    {
        int pcount = last-first+1;
        double uPrime[] = new double[pcount];                            // New parameter values
        for (int i=first; i<=last; i++)
            uPrime[i-first] = NewtonRaphsonRootFind(bezCurve, d[i], u[i-first]);
        return uPrime;
    }

    // Use Newton-Raphson iteration to find better root.
    private static double NewtonRaphsonRootFind(Point bc[], Point P, double u)
    {
        // Compute Q(u) and generate control vertices for Q' & Q''
        Point Q_u = Bezier(3, bc, u), Q1[] = new Point[3], Q2[] = new Point[2];           // Q' and Q''
        for (int i=0; i<=2; i++)
            Q1[i] = new Point((bc[i+1].x - bc[i].x)*3f, (bc[i+1].y - bc[i].y)*3f);
        for (int i=0; i<=1; i++)
            Q2[i] = new Point((Q1[i+1].x - Q1[i].x)*2f, (Q1[i+1].y - Q1[i].y)*2f);

        // Compute Q'(u) and Q''(u)
        Point Q1_u = Bezier(2, Q1, u);
        Point Q2_u = Bezier(1, Q2, u);

        // Compute f(u)/f'(u) and return improved U: u = u - f(u)/f'(u)
        double numerator = (Q_u.x - P.x)*(Q1_u.x) + (Q_u.y - P.y)*(Q1_u.y);
        double denominator = (Q1_u.x)*(Q1_u.x) + (Q1_u.y)*(Q1_u.y) + (Q_u.x - P.x)*(Q2_u.x) + (Q_u.y - P.y)*(Q2_u.y);
        return u - (numerator/denominator);
    }

    // Evaluate a Bezier curve at a particular parameter value using triangle computation
    private static Point Bezier(int degree, Point V[], double t)
    {
        Point vtmp[] = new Point[4];
        for (int i=0; i<=degree; i++)
            vtmp[i] = new Point(V[i]);
        for (int i=1; i<=degree; i++) {
            for (int j = 0; j <= degree - i; j++) {
                vtmp[j].x = (1 - t) * vtmp[j].x + t * vtmp[j + 1].x;
                vtmp[j].y = (1 - t) * vtmp[j].y + t * vtmp[j + 1].y;
            }
        }
        return vtmp[0];
    }

    // Approximate unit tangents at center of digitized curve
    private static Point ComputeCenterTangent(Point d[], int center)
    {
        Point V1 = V2Sub(d[center-1], d[center]);
        Point V2 = V2Sub(d[center], d[center+1]);
        Point tHatCenter = new Point((V1.x + V2.x)/2f, (V1.y + V2.y)/2f);
        tHatCenter = V2Normalize(tHatCenter);
        return tHatCenter;
    }

    // Assign parameter values to points using relative dist between points
    private static double[] ChordLengthParameterize(Point d[], int first, int last)
    {
        double u[] = new double[(last-first+1)];
        u[0] = 0.0;
        for (int i=first+1; i<=last; i++)
            u[i-first] = u[i-first-1] + d[i].getDistance(d[i-1]);
        for (int i=first+1; i<=last; i++)
            u[i-first] /= u[last-first];
        return u;
    }

    // Find the maximum squared distance of digitized points to fitted curve.
    private static double[] ComputeMaxError(Point d[], int first, int last, Point bezCurve[], double u[])
    {
        double maxDist = 0.0; // Maximum & current error
        int splitPoint = (last - first + 1)/2;

        for (int i=first+1; i<last; i++) {

            // Get point on curve and vector from point to curve
            Point p = Bezier(3, bezCurve, u[i-first]);
            Point v = V2Sub(p, d[i]);
            double dist = V2SquaredLength(v);
            if (dist >= maxDist) {
                maxDist = dist;
                splitPoint = i;
            }
        }

        return new double[] { maxDist, splitPoint };
    }

    // Bezier multipliers
    private static double B0(double u) { return Math.pow(1-u,3); }
    private static double B1(double u) { return 3*u*Math.pow(1-u,2); }
    private static double B2(double u) { return 3*u*u*(1-u); }
    private static double B3(double u) { return u*u*u; }

    // Vector add, subtract, negate, scale and normalize
    private static Point V2Add(Point a, Point b) { return new Point(a.x + b.x, a.y + b.y); }
    private static Point V2Sub(Point a, Point b) { return new Point(a.x - b.x, a.y - b.y); }
    private static Point V2Negate(Point v) { return new Point(-v.x, -v.y); }
    private static Point V2Scale(Point v, double s) { return new Point(v.x*s, v.y*s); }
    private static Point V2Normalize(Point v)
    {
        v = v.clone();
        double l = V2Len(v);
        if (l!=0) { v.x/=l; v.y/=l; }
        return v;
    }

    // Vector length, squared length, normalized vector and vector dot product
    private static double V2Len(Point a) { return Math.sqrt(V2SquaredLength(a)); }
    private static double V2SquaredLength(Point a) { return (a.x*a.x)+(a.y*a.y); };
    private static double V2Dot(Point a, Point b) { return (a.x*b.x)+(a.y*b.y); }

    // Add Bezier curve points to list.
    private static void ConcatBezierCurve(List beziers, Point bezCurve[])
    {
        if (beziers.size()==0)
            beziers.add(bezCurve[0]);
        beziers.add(bezCurve[1]);
        beziers.add(bezCurve[2]);
        beziers.add(bezCurve[3]);
    }
}