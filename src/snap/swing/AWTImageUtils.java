package snap.swing;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

/**
 * Utility methods for AWT Images.
 */
public class AWTImageUtils {

    /**
     * Returns a JPeg byte array for the given buffered image.
     */
    public static byte[] getBytesJPEG(Image anImage)
    {
        // Catch exceptions
        try {
            BufferedImage image = getBufferedImage(anImage, false); // Get buffered image
            ByteArrayOutputStream out = new ByteArrayOutputStream(); // Get byte array output stream
            ImageIO.write(image, "jpg", out);  // Write jpg image to output stream
            return out.toByteArray();  // Return byte array output stream bytes
        } catch(Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Returns a PNG byte array for the given buffered image.
     */
    public static byte[] getBytesPNG(Image anImage)
    {
        // Catch exceptions
        try {
            BufferedImage image = getBufferedImage(anImage); // Get buffered image
            ByteArrayOutputStream out = new ByteArrayOutputStream(); // Get byte array output stream
            ImageIO.write(image, "png", out); // Write png image to output stream
            return out.toByteArray(); // Return output stream bytes
        } catch(Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Returns a buffered image for an AWT image with transparency.
     */
    public static BufferedImage getBufferedImage(Image anImage)
    {
        // If image is already a buffered image, just return it
        if (anImage instanceof BufferedImage)
            return (BufferedImage)anImage;

        // Return buffered image for image with transparency
        return getBufferedImage(anImage, true);
    }

    /**
     * Returns a buffered image for an AWT image.
     */
    public static BufferedImage getBufferedImage(Image anImage, boolean withAlpha)
    {
        // If image is already a buffered image with given transparency, just return it
        if (anImage instanceof BufferedImage) {
            BufferedImage image = (BufferedImage)anImage;
            if ((image.getTransparency()==BufferedImage.TRANSLUCENT && withAlpha) ||
                (image.getTransparency()==BufferedImage.OPAQUE && !withAlpha))
                return image;
        }

        // Get image width and height
        int w = anImage.getWidth(null);
        int h = anImage.getHeight(null);

        // Create new buffered image, draw old image in new buffered image and return
        BufferedImage bi = getBufferedImage(w, h, withAlpha);
        Graphics2D g = bi.createGraphics();
        g.drawImage(anImage, 0, 0, null);
        g.dispose();
        return bi;
    }

    /**
     * Returns a compatible buffered image for width and height with given transparency.
     */
    public static BufferedImage getBufferedImage(int aWidth, int aHeight, boolean withAlpha)
    {
        // Get graphics configuration
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        // Return buffered image
        return gc.createCompatibleImage(aWidth, aHeight, withAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    }

    /**
     * Emboss a source image according to a bump map, both in ARGB integer array form.
     * Bump map is assumed to to be (2*radius x 2*radius) pixels larger than the source
     * to compensate for edge conditions of both the blur and the emboss convolutions.
     * Code adapted from Graphics Gems IV - Fast Embossing Effects on Raster Image Data (by John Schlag)
     */
    public static void emboss(int spix[], int bpix[], int sw, int sh, int radius, double az, double alt)
    {
        // Get basic info
        int rad = Math.abs(radius);
        int bw = sw + 2*rad, bh = sh + 2*rad;

        // Normalized light source vector
        double pixScale = 255.9;
        int Lx = (int)(Math.cos(az) * Math.cos(alt) * pixScale);
        int Ly = (int)(Math.sin(az) * Math.cos(alt) * pixScale);
        int Lz = (int)(Math.sin(alt) * pixScale);

        // Constant z component of surface normal
        int Nz = 3*255/rad, Nz2 = Nz*Nz;
        int NzLz = Nz*Lz, background = Lz;

        // Bump map is an ARGB image - Turn it into an array of ints representing heights (in range 0-255).
        int pcount = sh>100 ? 8 : sh>50 ? 5 : sh>20 ? 2 : 1, blen = bw*bh;
        IntStream.range(0,pcount).parallel().forEach(i -> {
            int jMin = blen/pcount*i, jMax = blen/pcount*(i+1); if (i==pcount-1) jMax = blen;
            for (int j=jMin;j<jMax;j++) bpix[j] = (bpix[j]>>24) & 0xff;  // If single thread: jMin=0,jMax=bw*bh
        });

        // Break image up into some number of parts and run in parallel stream threads
        IntStream.range(0,pcount).parallel().forEach(i -> {
            int yMin = sh/pcount*i, yMax = sh/pcount*(i+1); if (i==pcount-1) yMax = sh;
            int soff = yMin*sw, boff = yMin*(sw+2*rad) + bw*rad + rad; // If single thread: yMin=0,yMax=sh

        // Shade the pixels based on bump height & light source location
        for (int y=yMin; y<yMax; y++, boff+=2*rad) { for (int x=0; x<sw; x++) {

            // Normal calculation from alpha sample of bump map of surrounding pixels
            int b_0_0 = bpix[boff-bw-1], b_0_1 = bpix[boff-bw], b_0_2 = bpix[boff-bw+1];
            int b_1_0 = bpix[boff-1],                           b_1_2 = bpix[boff+1];
            int b_2_0 = bpix[boff+bw-1], b_2_1 = bpix[boff+bw], b_2_2 = bpix[boff+bw+1];
            int Nx = b_0_0 + b_1_0 + b_2_0 - b_0_2 - b_1_2 - b_2_2;
            int Ny = b_2_0 + b_2_1 + b_2_2 - b_0_0 - b_0_1 - b_0_2;

            // If negative, negate everything
            if (radius<0) { Nx = -Nx; Ny = -Ny; }

            // Calculate shade: If normal isn't normal, calculate shade
            int shade = background;
            if (Nx!=0 || Ny!=0) {
                int NdotL = Nx*Lx + Ny*Ly + NzLz;
                if (NdotL<0) shade = 0;
                else shade = (int)(NdotL / Math.sqrt(Nx*Nx + Ny*Ny + Nz2));
            }

            // scale each sample by shade
            int p = spix[soff]; //if (shade<Lz) p=0xff202020; else p=0xff808080; spix[off] = (0x010101*shade) | alpha;

            // Recalculate components
            int r = (((p&0xff0000)*shade)>>8) & 0xff0000;
            int g = (((p&0xff00)*shade)>>8) & 0xff00;
            int b = (((p&0xff)*shade)>>8);
            int a = p & 0xff000000; //((p>>8)*shade) & 0xff000000;

            // Set new value and increment offsets (assumption is that rowbytes==width)
            spix[soff] = r | g | b | a; soff++; boff++;
        }}});
    }

    /**
     * Convolves given source image into dest image.
     */
    public static void convolve(int spix[], int dpix[], int sw, int sh, float kern[], int kw)
    {
        // Get offset x/y and integer kernel
        int klen = kern.length, kh = klen/kw, dx = kw/2, dy = kh/2;
        int kern2[] = new int[klen]; for (int i=0;i<klen;i++) kern2[i] = (int)(kern[i]*255*255);

        // Bump map is an ARGB image - Turn it into an array of ints representing heights (in range 0-255).
        int pcount = sh>100 ? 8 : sh>50 ? 5 : sh>20 ? 2 : 1;
        IntStream.range(0,pcount).parallel().forEach(i -> {

            // Get yMin/yMax for thread part and calculate start offset
            int yMin = dy + (sh-2*dy) / pcount*i;
            int yMax = dy + (sh-2*dy) / pcount*(i+1);
            if (i==pcount-1) yMax = sh-dy;
            int off = yMin*sw + dx; // If single thread: yMin=dy,yMax=sh-dy

            // Iterate over pix map
            for (int y=yMin;y<yMax;y++,off+=dx*2)
                for (int x=dx,xMax=sw-dx;x<xMax;x++,off++)
                    dpix[off] = getPixAverage(spix, sw, sh, off, kern2, kw, kh, dx, dy);
        });
    }

    /**
     * Returns the pixel value for given.
     */
    private static final int getPixAverage(int spix[], int sw, int sh, int off, int kern[], int kw, int kh, int dx, int dy)
    {
        // Declare vars for weighted color component sums
        int sr = 0, sg = 0, sb = 0, sa = 0;

        // Iterate over kernel matrix: get pix value for each and sum component value times kernel value
        for (int ky=0, koff=0;ky<kh;ky++) for (int kx=0;kx<kw;kx++,koff++) {
            int p = spix[off + (-dy+ky)*sw + (-dx+kx)]; if (p==0) continue;
            int kval = kern[koff];
            int r = (p>>16) & 0xff;
            int g = (p>>8) & 0xff;
            int b = p & 0xff;
            int a = (p>>24) & 0xff;
            sr += r*kval; sg += g*kval; sb += b*kval; sa += a*kval;
        }

        // Reconstruct pix int and return
        return (sr&0xff0000) | ((sg>>8) & 0xff00) | (sb>>16) | ((sa<<8) & 0xff000000);
    }

    /**
     * Returns kernel for a Gaussian blur convolve.
     */
    public static float[] getGaussianKernel(int rx, int ry)
    {
        // Set kernel/loop variables
        int kw = rx*2 + 1;
        int kh = ry*2 + 1;
        int klen = kw*kh;
        float[] kern = new float[klen];
        double devx = rx>0 ? rx/3d : .1;
        double devy = ry>0 ? ry/3d : .1; // This guarantees non zero values in the kernel
        double devxSqr2 = 2*devx*devx;
        double devySqr2 = 2*devy*devy;
        double devSqr2Pi = Math.max(devxSqr2,devySqr2)*Math.PI;
        double sum = 0;

        // Calculate/set kernal values and sum
        for (int i=0; i<kw; i++) { for (int j=0; j<kh; j++) {
            double kbase = -(j-ry)*(j-ry)/devySqr2 - (i-rx)*(i-rx)/devxSqr2;
            kern[i*kh+j] = (float)(Math.pow(Math.E, kbase)/devSqr2Pi);
            sum += kern[i*kh+j];
        }}

        // Calculate/set kernal values and sum
        //for (int y=0; y<h; y++) { for (int x=0; x<w; x++) {
        //    double kbase = -(y-ry)*(y-ry)/devySqr2 - (x-rx)*(x-rx)/devxSqr2;
        //    kern2[y*w+x] = (float)(Math.pow(Math.E, kbase)/devSqr2Pi); sum += kern[y*w+x]; }}

        // Make elements sum to 1 and return
        for (int i=0; i<klen; i++) kern[i] /= sum;
        return kern;
    }
}