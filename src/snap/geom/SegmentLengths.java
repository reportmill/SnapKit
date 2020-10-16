package snap.geom;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to calculate arc length of a Quad and Cubic.
 */
class SegmentLengths {

    /**
     * Returns the arc length of a quad up to parametric value t
     */
    public static double getArcLengthQuad(double x0, double y0, double cpx, double cpy, double x1, double y1)
    {
        RMFunc alen = getArcLengthFunctionQuad(x0, y0, cpx, cpy, x1, y1);
        return alen.f(1);
    }

    /**
     * Returns the arc length of a cubic up to parametric value t
     */
    public static double getArcLengthCubic(double x0, double y0, double cp0x, double cp0y, double cp1x, double cp1y, double x1, double y1)
    {
        RMFunc alen = getArcLengthFunctionCubic(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1);
        return alen.f(1);
    }

    /**
     * Returns the arc length of the segment up to parametric value t
     */
    public static double getArcLength(Segment aSeg, double start, double end)
    {
        RMFunc alen = getArcLengthFunction(aSeg);
        double l = alen.f(end);
        if (start > 0)
            l -= alen.f(start);
        return l;
    }

    /**
     * Returns parametric point t that corresponds to a given length along the curve.
     * l is in the range [0-1] (ie. percentage of total arclength)
     */
    public double getParameterForLength(Segment aSeg, double l)
    {
        // NB: This uses the exact solution to get the position. JavaFX uses interpolated solution.
        // Might be better to use the interpolated solution here, too, in case things don't quite match up.
        RMFunc solution = getInverseArcLengthFunction(aSeg);
        return solution.f(l);
    }

    /**
     * Returns a set of polynomials to approximate the arclength->parameter curve
     */
    public List<RMCurveFit.Piece> getInverseArcLengthCurve(Segment aSeg)
    {
        // 'inverse' now represents a function where f(x) returns bezier parameter t for given length along curve.
        // The length is in the range 0-1 and can be thought of as percentage of the entire curve.
        RMFunc inverse = getInverseArcLengthFunction(aSeg);

        // Fit one or more polynomials to this curve
        return RMCurveFit.nevilleFit(inverse, 0, 1, null);
    }

    /**
     * Returns the arc length of the segment up to parametric value t
     */
    private static RMFunc getArcLengthFunction(Segment aSeg)
    {
        if (aSeg instanceof Cubic) {
            Cubic c = (Cubic) aSeg;
            return getArcLengthFunctionCubic(c.x0, c.y0, c.cp0x, c.cp0y, c.cp1x, c.cp1y, c.x1, c.y1);
        }
        if (aSeg instanceof Quad) {
            Quad q = (Quad) aSeg;
            return getArcLengthFunctionQuad(q.x0, q.y0, q.cpx, q.cpy, q.x1, q.y1);
        }
        throw new RuntimeException("SegmentLength.getArcLengthFunction: Unsupported Segement Class: " + aSeg);
    }

    /**
     * Returns an RMFunc which calculates t for a percentage length along the curve.
     */
    private static RMFunc getInverseArcLengthFunction(Segment aSeg)
    {
        // Get the arclength function
        RMFunc alen = getArcLengthFunction(aSeg);

        // Scale it to the range 0-1
        RMFunc scaled = new RMCurveFit.ScaledFunc(alen);

        // return the inverse of the scaled function
        return new RMCurveFit.InverseFunc(scaled);
    }

    /**
     * Returns an MathFunc which calculates the arclength of a Quad up to t.
     */
    private static RMFunc getArcLengthFunctionQuad(double x0, double y0, double cpx, double cpy, double x1, double y1)
    {
        // Arc length of parametric curve is defined by:
        //   len = Integral[0,t, Sqrt[(dx/dt)^2 + (dy/dt)^2]]
        // We calculate dx/dt and dy/dt by integrating the first level of the de Castlejau algorithm
        //   d(t)/dt = 1,  d(1-t)/dy = -1
        final double cx[] = { cpx - x0, x1 - cpx};
        final double cy[] = { cpy - y0, y1 - cpy};

        // create function for 2*Sqrt[(dx/dt)^2 + (dy/dt)^2]
        final RMFunc integrand = new RMFunc() {
            public double f(double t)
            {
                double ti = 1 - t;
                double dxdt = (cx[0] * ti + cx[1] * t);
                double dydt = (cy[0] * ti + cy[1] * t);
                return 2 * Math.sqrt(dxdt * dxdt + dydt * dydt);
            }
        };

        // return the integration function
        return new RMFunc() {
            public double f(double t)
            {
                return integrand.integrate(0, t, 100);
            }

            public double fprime(double t, int level)
            {
                return level == 1 ? integrand.f(t) : super.fprime(t, level);
            }
        };
    }

    /**
     * Returns an MathFunc which calculates the arclength of a Cubic up to t.
     */
    private static RMFunc getArcLengthFunctionCubic(double x0, double y0, double cp0x, double cp0y, double cp1x, double cp1y, double x1, double y1)
    {
        // Arc length of parametric curve is defined by:
        //   len = Integral[0,t, Sqrt[(dx/dt)^2 + (dy/dt)^2]]
        // We calculate dx/dt and dy/dt by integrating the first level of the de Castlejau algorithm
        //   d(t)/dt = 1,  d(1-t)/dy = -1
        final double cx[] = { cp0x - x0, (cp1x - cp0x)*2, x1 - cp1x };
        final double cy[] = { cp1y - y0, (cp1y - cp0y)*2, y1 - cp1y };

        // create function for 3*Sqrt[(dx/dt)^2 + (dy/dt)^2]
        final RMFunc integrand = new RMFunc() {
            public double f(double t) {
                double ti = 1-t;
                double dxdt = (cx[0]*ti*ti+cx[1]*t*ti+cx[2]*t*t);
                double dydt = (cy[0]*ti*ti+cy[1]*t*ti+cy[2]*t*t);
                return 3*Math.sqrt(dxdt*dxdt + dydt*dydt);
            }
        };

        return new RMFunc() {
            public double f(double t) { return integrand.integrate(0,t,100); }
            public double fprime(double t, int level) {
                return level==1 ? integrand.f(t) : super.fprime(t,level);
            }
        };
    }

    /**
     * A class used to specify an arbitrary mathematical function.
     */
    public abstract static class RMFunc {

        // the function
        public abstract double f(double x);

        /**
         * nth derivative of function at x.
         * Base class calculates it numerically, but you could override this if you know the exact form.
         * order=1 for first derivative, 2 for second derivative, etc.
         */
        public double fprime(double x, int order)
        {
            if (order <= 0) return f(x);
            double epsilon = 1e-10;
            return (fprime(x + epsilon, order - 1) - fprime(x, order - 1)) / epsilon;
        }

        /**
         * Numerical integration of a function in the interval [start, end].
         * Uses composite Simpson's method.
         * (override if you know the exact form)
         */
        public double integrate(double start, double end, int npts)
        {
            if (end < start)
                return -integrate(end, start, npts);

            // make sure n is even
            int nintervals = npts + (npts % 2 == 1 ? 1 : 0);
            // get size of each interval
            double h = (end - start) / nintervals;
            // Simpson's method:
            //   I = h/3 * (f(start) + 4*Sum[f,start+h,end-h,2h] + 2*Sum[f,start+2h,end-2h,2h] + f(end))

            // start with the endpoints
            double integral = f(start) + f(end);
            // calculate sums of even & odd terms
            double point = start + h;
            double odds = f(point);
            double evens = 0;
            while (nintervals > 2) {
                point += h;
                evens += f(point);
                point += h;
                odds += f(point);
                nintervals -= 2;
            }
            integral += 4 * odds + 2 * evens;
            return h * integral / 3;
        }

        /**
         * Uses Newton's method to find numerical solution to f(x)=a.
         * (override if you know the exact solution)
         */
        public double solve(double a)
        {
            // use a as initial guess
            double newX = a;
            double guess;
            double limit = 1e-10;
            int maxiters = 1000;

            // Newton's method:  newx = oldx - f(oldx)/f'(oldx)
            do {
                guess = newX;
                newX -= (f(guess) - a) / fprime(guess, 1);
            }
            // loop until guess has settled on an answer within limit
            while (Math.abs(guess - newX) > limit && --maxiters > 0);

            // if we seem to have gotten stuck, accept whatever we're at if it's reasonable
            // otherwise report an error
            if ((maxiters == 0) && (Math.abs(f(guess) - a) > limit / 2))
                // perhaps a little severe
                throw new RuntimeException("Can't converge on solution.");

            //testing - average seems to converge in about 3 or 4 iterations
            //System.err.println("DELETEME: solved in "+(1001-maxiters)+" iterations");

            return guess;
        }
    }

    /**
     * This class creates an approximating polygon for a given RMFunction.
     * The polygon is expressed as a series of sample points that can be interpolated between using Neville's method
     * to quickly obtain the value of any function.
     * <p>
     * This is used for path animation, to map the complex curve which maps the arclength of the bezier path segment
     * to the bezier parameter value.
     * <p>
     * The real function is the solution to an expression which would be prohibitively expensive to try to evaluate
     * inside an animation loop, so instead we create a polynomial approximation and use that instead.
     */
    public static class RMCurveFit {

        // An individual polynomial which covers the curve in the range start-end.
        // The output of NevilleFit() routine is a list of these objects.
        public static class Piece {
            public double start;
            public double end;
            public double xsamples[];
            public double ysamples[];

            public Piece(double s, double e, double x[], double y[])
            {
                start = s; end = e;
                xsamples = x;
                ysamples = y;
            }
        }

        /**
         * A function whose value is determined by interpolating through a set
         * of sample points using Neville's Method.
         */
        public static class NevilleFunc extends RMFunc {
            public double xsamples[];
            public double ysamples[];
            public double p[];

            public NevilleFunc() { }

            public void setSamples(double x[], double y[])
            {
                xsamples = x;
                ysamples = y;
                p = new double[xsamples.length];
            }

            public double f(double x)
            {
                int i, j;
                int samples = xsamples.length;

                for (i = 0; i < samples; ++i)
                    p[i] = ysamples[i];
                for (j = 1; j < samples; ++j)
                    for (i = 0; i < samples - j; ++i)
                        p[i] = (p[i] * (xsamples[i + j] - x) + p[i + 1] * (x - xsamples[i])) / (xsamples[i + j] - xsamples[i]);
                return p[0];
            }
        }

        /**
         * A function whose value is the inverse of another function
         * ie.  realFunc.f(x)=y ==> inverseFunc.f(y)=x
         */
        public static class InverseFunc extends RMFunc {
            RMFunc _realFunc;

            public InverseFunc(RMFunc real)
            {
                _realFunc = real;
            }

            public double f(double x)
            {
                return _realFunc.solve(x);
            }

            public double fprime(double x, int order)
            {
                return order == 1 ? 1 / _realFunc.fprime(x, 1) : super.fprime(x, order);
            }
        }

        /**
         * A function scaled such that f(1) == 1
         */
        public static class ScaledFunc extends RMFunc {
            RMFunc _realFunc;
            double len;

            public ScaledFunc(RMFunc real)
            {
                _realFunc = real;
                len = _realFunc.f(1);
            }

            public double f(double x)
            {
                return _realFunc.f(x) / len;
            }

            public double fprime(double x, int order)
            {
                return _realFunc.fprime(x, order) / len;
            }
        }

        /**
         * Returns Chebyshev nodes for interpolating polynomial of order n.
         * These are the 'best' values at which to sample the curve, in order to minimaize the error.
         */
        public static double[] cheby(int n)
        {
            double x[] = new double[n];
            // calculate scale factor so nodes cover entire interval [0-1]
            double c = Math.cos(Math.PI / (2 * n));
            double b = (c + 1) / (2 * c);
            double a = (2 - b * (c + 1)) / (1 - c);

            // get nodes.  These are the x values of the (x,y) samples that minimize the error of the interpolation.
            for (int i = 1; i <= n; ++i) {
                double node = (1 + (b - a) * Math.cos((2 * i - 1) * Math.PI / (2 * n))) / 2;
                x[n - i] = node;
            }
            return x;
        }

        /**
         * NevilleFit -
         * This routine tries to fit an interpolating polygon to an arbitrary function.
         * Neville's method takes a set of points and calculates new points
         * by interpolating between the neighboring samples, and then interpolating
         * between the interpolations, etc, etc.
         * This method tries to create a set of points such that, when plugged into
         * Neville's method, will approximate the curve with minimal error.
         * <p>
         * It first tries to create a linear->5th degree polynomial (corresponding to two
         * to six sample points), and then calculates the maximum error of that
         * polynomial to the real curve.  If the error is too great, it subdivides
         * at the maximum error point and recurses.
         * <p>
         * The final result is a piecewise list of polynomials, expressed as sample points.
         */
        public static List<Piece> nevilleFit(RMFunc func, double start, double end, List<Piece> pieceList)
        {
            int i, j, samples = 2;
            int outsamples = 64;
            double maxerr = 0;
            double maxerrpt = -1;
            double x[];
            double y[];
            double newysample = 0;
            NevilleFunc interp = new NevilleFunc();

            double error_limit = 0.0004;

            if (end <= start)
                return pieceList;

            if (pieceList == null)
                pieceList = new ArrayList<>();

            // try 2-6 nodes (linear to quintic polygon)
            do {
                // Get the nodes (interpolation points) for the new polygon
                x = cheby(samples);
                y = new double[samples];
                // calculate y value at each node
                for (i = 0; i < samples; ++i) {
                    // map Chebychev nodes to the domain (cheby() routine maps them to 0-1)
                    x[i] = start + (end - start) * x[i];
                    // get the sample for the given node
                    y[i] = func.f(x[i]);
                }

                // initialize Neville interpolation function with the samples
                interp.setSamples(x, y);

                double xx = 0;
                double yy;
                double avgerr = 0;
                maxerr = 0;

                // Now that we have a polygon, see how good a fit it is
                for (int outi = 0; outi < outsamples; ++outi) {
                    // x value in range
                    xx = start + (end - start) * ((double) outi) / (outsamples - 1);
                    // do the interpolation
                    yy = interp.f(xx);

                    // get the actual value
                    double actual = func.f(xx);

                    // calculate squared error and keep track of worst fit point
                    double esq = (yy - actual) * (yy - actual);
                    avgerr += esq;
                    if (esq > maxerr) {
                        maxerr = esq;
                        maxerrpt = xx;
                    }
                }

                // try again with next higher order curve, up to 5th degree
                ++samples;
            }
            while (maxerr > error_limit && samples <= 5);

            // if we didn't succeed, subdivide and try again
            if (maxerr > error_limit) {
                pieceList = nevilleFit(func, start, maxerrpt, pieceList);
                return nevilleFit(func, maxerrpt, end, pieceList);
            } else {
                // If the fit is good, save the samples
                pieceList.add(new Piece(start, end, x, y));
                return pieceList;
            }
        }
    }
}
