package snap.swing;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import snap.gfx.GradientPaint;
import snap.gfx.Color;

/**
 * An implementation of the java.awt.Paint interface for RMGradientFills.
 */
public class GradientPaintX implements PaintContext, Paint {

    // Shading axis
    private double  _x0, _y0, _x1, _y1;
    
    // Cached rgba values of the stops
    private int  _stop_colors[][];
    private float  _stop_positions[];
    
    // Cached values for shading loop
    private float  Ax, Ay, BAx, BAy, denom;
    
    // Whether linear
    private boolean  _linear = true;
    
    // Whether proportional
    private boolean  _proportional;
    
    // The ...
    private double  _maxRadius;

    /**
     * Constructor.
     */
    public GradientPaintX(GradientPaint aFill)
    {
        // Cache start/end points
        _x0 = aFill.getStartX(); _y0 = aFill.getStartY(); _x1 = aFill.getEndX(); _y1 = aFill.getEndY();

        // pull out the rgba components from the stop list
        GradientPaint.Stop stops[] = aFill.getStops(); int nstops = stops.length;
        _stop_colors = new int[nstops][4];
        _stop_positions = new float[nstops];
        for (int i=0; i<nstops; ++i) {
            Color c = stops[i].getColor();
            _stop_colors[i][0] = c.getAlphaInt();
            _stop_colors[i][1] = c.getRedInt();
            _stop_colors[i][2] = c.getGreenInt();
            _stop_colors[i][3] = c.getBlueInt();
            _stop_positions[i] = (float)stops[i].getOffset();
        }

        // Set linear
        _linear = aFill.getType()==GradientPaint.Type.LINEAR;

        // Set proportional
        _proportional = Math.abs(_x1-_x0)<2 && Math.abs(_y1-_y0)<2;
    }

    /**
     * createContext
     */
    public PaintContext createContext(ColorModel cm, Rectangle devBnds, Rectangle2D usrBnds, AffineTransform xform, RenderingHints hints)
    {
        // Get start/end x & y
        double x0 = _x0, y0 = _y0, x1 = _x1, y1 = _y1;
        if (_proportional) {
            double ubx = usrBnds.getX(), uby = usrBnds.getY(), ubw = usrBnds.getWidth(), ubh = usrBnds.getHeight();
            x0 = ubx + x0*ubw; y0 = uby + y0*ubh; x1 = ubx + x1*ubw; y1 = uby + y1*ubh;
        }

        // transform original line into device coords and recalculate values  //setDeviceTransform(xform,devBnds);
        float pts[] = { (float)(x0), (float)(y0), (float)(x1), (float)(y1) };
        xform.transform(pts, 0, pts, 0, 2);
        Ax = pts[0];
        Ay = pts[1];
        BAx = pts[2]-pts[0];
        BAy = pts[3]-pts[1];
        denom = BAx*BAx+BAy*BAy;
        _maxRadius = Math.sqrt(BAx*BAx+BAy*BAy);
        return this;
    }

    public Raster getRaster(int x, int y, int w, int h)
    {
        // Allocate an ARGB raster and pass the sample buffer to the shading implementation
        DataBufferInt dbuf = new DataBufferInt(w*h);
        WritableRaster r = Raster.createPackedRaster(dbuf, w,h,w, new int[]{0xff0000,0xff00,0xff,0xff000000}, new Point());
        int samples[] = dbuf.getData();

        if (_linear)
            doShading(samples, x, y, w, h);
        else doShadingRadial(samples, x, y, w, h);
        return r;
    }

    /**
     * Alpha & color definitions.
     */
    public int getTransparency()  { return TRANSLUCENT; }

    /**
     * ARGB.
     */
    public ColorModel getColorModel()  { return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000); }

    /**
     * Shading loop. Put in some meaningful comment here some day.
     */
    public void doShading(int argb_samples[], int x, int y, int w, int h)
    {
        // For every point P in the raster, find point t along AB where dotProduct(A-B, P-AB(t)) = 0
        for (int j=0, sindex=0; j<h; ++j) {
            double PAy = (y+j-Ay)*BAy;
            double PAx = x-Ax;
            for (int i=0; i<w; ++i) {
                float t = (float)(BAx*PAx+PAy)/denom; // t is the distance (0-1) along the gradient axis
                argb_samples[sindex] = getShadePixel(t);
                ++sindex; ++PAx;
            }
        }
    }

    /**
     * Shading loop radial.
     */
    public void doShadingRadial(int argb_samples[], int x, int y, int w, int h)
    {
        // For every point P in the raster, find distance to start of gradient axis, scaled by length of the axis.
        for (int j=0, sindex=0; j<h; j++) {
            double PAy2 = (y+j-Ay)*(y+j-Ay);
            double PAx = x-Ax;

            // Iterate over width - t is the distance (0-1) along the gradient axis
            for (int i=0; i<w; i++) {
                float t = (float)(Math.sqrt(PAx*PAx+PAy2)/_maxRadius);
                argb_samples[sindex] = getShadePixel(t);
                sindex++; PAx++;
            }
        }
    }

    /**
     * Returns the argb pixel value for the distance along the shading axis.
     */
    private final int getShadePixel(float t)
    {
        int nstops = _stop_positions.length;
        int pixel = 0;

        // Pixels beyond the endpoints of gradient axis use colors at endpoints (pdf calls this behavior 'extend')
        if (t<_stop_positions[0])
            t = _stop_positions[0];
        else if (t>_stop_positions[nstops-1])
            t = _stop_positions[nstops-1];

        // find the right stop color
        for (int k=1; k<nstops; ++k) {
            if (_stop_positions[k] >= t) {
                // scale t to stop range
                t = (t-_stop_positions[k-1])/(_stop_positions[k]-_stop_positions[k-1]);
                float ti = 1f - t;
                // calculate colors
                for (int csi = 0; csi<4; ++csi) {
                    // Linear interpolation between stops.
                    int sample = (int)(ti*_stop_colors[k-1][csi]+t*_stop_colors[k][csi]);
                    // sample is now an int in range 0-255, so no sign extension to worry about
                    pixel = (pixel<<8) | sample;
                }
                break;
            }
        }

        // Return the pixel value
        return pixel;
    }

    /**
     * PaintContext method.
     */
    public void dispose()  { }
}