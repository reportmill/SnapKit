package snap.swing;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.util.function.IntConsumer;
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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Get byte array output stream
            ImageIO.write(image, "jpg", byteArrayOutputStream);  // Write jpg image to output stream
            return byteArrayOutputStream.toByteArray();  // Return byte array output stream bytes
        }
        catch(Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Returns a PNG byte array for the given buffered image.
     */
    public static byte[] getBytesPNG(Image anImage)
    {
        // Catch exceptions
        try {
            BufferedImage image = getBufferedImage(anImage); // Get buffered image
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Get byte array output stream
            ImageIO.write(image, "png", byteArrayOutputStream); // Write png image to output stream
            return byteArrayOutputStream.toByteArray(); // Return output stream bytes
        }
        catch(Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Returns a buffered image for an AWT image with transparency.
     */
    public static BufferedImage getBufferedImage(Image anImage)
    {
        // If image is already a buffered image, just return it
        if (anImage instanceof BufferedImage)
            return (BufferedImage) anImage;

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
            BufferedImage image = (BufferedImage) anImage;
            int transparency = image.getTransparency();
            if ((transparency == BufferedImage.TRANSLUCENT && withAlpha) || (transparency == BufferedImage.OPAQUE && !withAlpha))
                return image;
        }

        // Get image width and height
        int imageW = anImage.getWidth(null);
        int imageH = anImage.getHeight(null);

        // Create new buffered image and draw old image in new buffered image
        BufferedImage bufferedImage = getBufferedImage(imageW, imageH, withAlpha);
        Graphics2D gfx2D = bufferedImage.createGraphics();
        gfx2D.drawImage(anImage, 0, 0, null);
        gfx2D.dispose();

        // Return
        return bufferedImage;
    }

    /**
     * Returns a compatible buffered image for width and height with given transparency.
     */
    public static BufferedImage getBufferedImage(int aWidth, int aHeight, boolean withAlpha)
    {
        // Get graphics configuration
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
        GraphicsConfiguration defaultConfig = graphicsDevice.getDefaultConfiguration();

        // Return buffered image
        return defaultConfig.createCompatibleImage(aWidth, aHeight, withAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    }

    /**
     * Emboss a source image according to a bump map, both in ARGB integer array form.
     * Bump map is assumed to to be (2*radius x 2*radius) pixels larger than the source
     * to compensate for edge conditions of both the blur and the emboss convolutions.
     * Code adapted from Graphics Gems IV - Fast Embossing Effects on Raster Image Data (by John Schlag)
     */
    public static void emboss(int[] srcPix, int[] bumpPix, int srcW, int srcH, int radius, double azimuth, double altitude)
    {
        // Get basic info
        int rad = Math.abs(radius);
        int bumpW = srcW + 2 * rad;
        int bumpH = srcH + 2 * rad;

        // Normalized light source vector
        double pixScale = 255.9;
        int Lx = (int) (Math.cos(azimuth) * Math.cos(altitude) * pixScale);
        int Ly = (int) (Math.sin(azimuth) * Math.cos(altitude) * pixScale);
        int Lz = (int) (Math.sin(altitude) * pixScale);

        // Constant z component of surface normal
        int Nz = 3 * 255 / rad;
        int Nz2 = Nz * Nz;
        int NzLz = Nz * Lz;
        int background = Lz;

        // Bump map is an ARGB image - Turn it into an array of ints representing heights (in range 0-255).
        int threadCount = srcH > 100 ? 8 : srcH > 50 ? 5 : srcH > 20 ? 2 : 1;
        int bumpPixLen = bumpW * bumpH;
        runInParallel(threadCount, i -> {
            int jMin = bumpPixLen / threadCount * i;
            int jMax = bumpPixLen / threadCount * (i + 1);
            if (i == threadCount - 1)
                jMax = bumpPixLen;
            for (int j = jMin; j < jMax; j++)
                bumpPix[j] = (bumpPix[j] >> 24) & 0xff;  // If single thread: jMin=0,jMax=bw*bh
        });

        // Break image up into some number of parts and run in parallel stream threads
        runInParallel(threadCount, i -> {
            int yMin = srcH / threadCount * i;
            int yMax = srcH / threadCount * (i + 1);
            if (i == threadCount - 1)
                yMax = srcH;
            int srcOff = yMin * srcW;
            int bumpOff = yMin * (srcW + 2 * rad) + bumpW * rad + rad; // If single thread: yMin=0,yMax=sh

            // Shade the pixels based on bump height & light source location
            for (int y = yMin; y < yMax; y++, bumpOff += 2 * rad) {
                for (int x = 0; x < srcW; x++) {

                    // Normal calculation from alpha sample of bump map of surrounding pixels
                    int b_0_0 = bumpPix[bumpOff - bumpW - 1];
                    int b_0_1 = bumpPix[bumpOff - bumpW];
                    int b_0_2 = bumpPix[bumpOff - bumpW + 1];
                    int b_1_0 = bumpPix[bumpOff - 1];
                    int b_1_2 = bumpPix[bumpOff + 1];
                    int b_2_0 = bumpPix[bumpOff + bumpW - 1];
                    int b_2_1 = bumpPix[bumpOff + bumpW];
                    int b_2_2 = bumpPix[bumpOff + bumpW + 1];
                    int Nx = b_0_0 + b_1_0 + b_2_0 - b_0_2 - b_1_2 - b_2_2;
                    int Ny = b_2_0 + b_2_1 + b_2_2 - b_0_0 - b_0_1 - b_0_2;

                    // If negative, negate everything
                    if (radius < 0) {
                        Nx = -Nx;
                        Ny = -Ny;
                    }

                    // Calculate shade: If normal isn't normal, calculate shade
                    int shade = background;
                    if (Nx != 0 || Ny != 0) {
                        int NdotL = Nx * Lx + Ny * Ly + NzLz;
                        if (NdotL < 0) shade = 0;
                        else shade = (int) (NdotL / Math.sqrt(Nx * Nx + Ny * Ny + Nz2));
                    }

                    // scale each sample by shade
                    int pixel = srcPix[srcOff]; //if (shade<Lz) p=0xff202020; else p=0xff808080; spix[off] = (0x010101*shade) | alpha;

                    // Recalculate components
                    int red = (((pixel & 0xff0000) * shade) >> 8) & 0xff0000;
                    int green = (((pixel & 0xff00) * shade) >> 8) & 0xff00;
                    int blue = (((pixel & 0xff) * shade) >> 8);
                    int alpha = pixel & 0xff000000; //((p>>8)*shade) & 0xff000000;

                    // Set new value and increment offsets (assumption is that rowbytes==width)
                    srcPix[srcOff] = red | green | blue | alpha;
                    srcOff++;
                    bumpOff++;
                }
            }
        });
    }

    /**
     * Convolves given source image into dest image.
     */
    public static void convolve(int[] srcPix, int[] destPix, int srcW, int srcH, float[] kern, int kernW)
    {
        // Get offset x/y and integer kernel
        int kernLen = kern.length;
        int kernH = kernLen / kernW;
        int dx = kernW / 2;
        int dy = kernH / 2;
        int[] kern2 = new int[kernLen];
        for (int i = 0; i < kernLen; i++)
            kern2[i] = (int) (kern[i] * 255 * 255);

        // Bump map is an ARGB image - Turn it into an array of ints representing heights (in range 0-255).
        int threadCount = srcH > 100 ? 8 : srcH > 50 ? 5 : srcH > 20 ? 2 : 1;
        runInParallel(threadCount, i -> {

            // Get yMin/yMax for thread part and calculate start offset
            int yMin = dy + (srcH - 2 * dy) / threadCount * i;
            int yMax = dy + (srcH - 2 * dy) / threadCount * (i + 1);
            if (i == threadCount - 1)
                yMax = srcH - dy;
            int srcOff = yMin * srcW + dx; // If single thread: yMin=dy,yMax=sh-dy

            // Iterate over pix map
            for (int y = yMin; y < yMax; y++, srcOff += dx * 2) {
                for (int x = dx, xMax = srcW - dx; x < xMax; x++, srcOff++)
                    destPix[srcOff] = getPixAverage(srcPix, srcW, srcOff, kern2, kernW, kernH, dx, dy);
            }
        });
    }

    /**
     * Returns the pixel value for given.
     */
    private static int getPixAverage(int[] srcPix, int srcW, int srcOff, int[] kern, int kernW, int kernH, int dx, int dy)
    {
        // Declare vars for weighted color component sums
        int sr = 0;
        int sg = 0;
        int sb = 0;
        int sa = 0;

        // Iterate over kernel matrix: get pix value for each and sum component value times kernel value
        for (int ky = 0, koff = 0; ky < kernH; ky++)
            for (int kx = 0; kx < kernW; kx++, koff++) {
                int pixel = srcPix[srcOff + (-dy + ky) * srcW + (-dx + kx)];
                if (pixel == 0)
                    continue;
                int kernVal = kern[koff];
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int alpha = (pixel >> 24) & 0xff;
                sr += red * kernVal;
                sg += green * kernVal;
                sb += blue * kernVal;
                sa += alpha * kernVal;
            }

        // Reconstruct pix int and return
        return (sr & 0xff0000) | ((sg >> 8) & 0xff00) | (sb >> 16) | ((sa << 8) & 0xff000000);
    }

    /**
     * Returns kernel for a Gaussian blur convolve.
     */
    public static float[] getGaussianKernel(int rx, int ry)
    {
        // Set kernel/loop variables
        int kernW = rx * 2 + 1;
        int kernH = ry * 2 + 1;
        int kernLength = kernW * kernH;
        float[] gaussianKernel = new float[kernLength];
        double devx = rx > 0 ? rx / 3d : .1;
        double devy = ry > 0 ? ry / 3d : .1; // This guarantees non zero values in the kernel
        double devxSqr2 = 2 * devx * devx;
        double devySqr2 = 2 * devy * devy;
        double devSqr2Pi = Math.max(devxSqr2, devySqr2) * Math.PI;
        double sum = 0;

        // Calculate/set kernal values and sum
        for (int i = 0; i < kernW; i++) {
            for (int j = 0; j < kernH; j++) {
                double kbase = -(j - ry) * (j - ry) / devySqr2 - (i - rx) * (i - rx) / devxSqr2;
                gaussianKernel[i * kernH + j] = (float) (Math.pow(Math.E, kbase) / devSqr2Pi);
                sum += gaussianKernel[i * kernH + j];
            }
        }

        // Calculate/set kernel values and sum
        //for (int y=0; y<h; y++) { for (int x=0; x<w; x++) {
        //    double kbase = -(y-ry)*(y-ry)/devySqr2 - (x-rx)*(x-rx)/devxSqr2;
        //    kern2[y*w+x] = (float)(Math.pow(Math.E, kbase)/devSqr2Pi); sum += kern[y*w+x]; }}

        // Make elements sum to 1
        for (int i = 0; i < kernLength; i++)
            gaussianKernel[i] /= sum;

        // Return
        return gaussianKernel;
    }

    /**
     * Runs the given int consumer in given number of threads.
     */
    private static void runInParallel(int threadCount, IntConsumer aConsumer)
    {
        if (threadCount == 1)
            aConsumer.accept(0);
        else IntStream.range(0, threadCount).parallel().forEach(aConsumer);
    }
}