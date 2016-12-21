/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.Hashtable;

/**
 * This class handles drawing images as tiles, for when allocating a BufferedImage for the entire pdf image would be
 * too expensive. It implements ImageConsumer, and assumes that it will be passes an ImageProducer that knows how to
 * generate itself top-down/left-right.
 * 
 * It keeps a BufferedImage only as large as a single tile.  When drawImage() is called, the imageProducer is told to
 * generate the image data.  As soon as a single tile is filled, it is drawn and the Buffer is reused for the next tile.
 * We could cut out the imageProducer middle-man and just go directly from the codec...
 */
public class PDFTiledImage implements ImageConsumer {

    // The color model
    ColorModel _colorModel;
    BufferedImage _tile;
    int _height, _width;
    int _currentTileY;
    Graphics2D _destination;

/**
 * Returns the height of the tile (it's a strip)
 */
public int getTileHeight()  { return 128; }

/**
 * Returns the width of the entire image.
 */
public int getWidth()  { return _width; }

/**
 * Returns the height of the entire image.
 */
public int getHeight()  { return _height; }

/**
 * ImageConsumer method.  Releases the tile.
 */
public void imageComplete(int status)
{
    // If there's anything left in the tile, draw it
    int tileHeight = getTileHeight();
    int remainder = _currentTileY%tileHeight;
    
    if (remainder>0) {
        int tileIndex = (_currentTileY / tileHeight)-1;
        int destY = tileIndex*tileHeight;
        _destination.drawImage(_tile, 0, destY, _width, destY+remainder, 0, 0, _width, remainder, null);
    }
    _tile = null;
}

public void setHints(int hintflags)  { }


public void setColorModel(ColorModel model)  { _colorModel = model; }

/**
 * ImageConsumer method.  Assumption is that this will be called before any calls to setPixels()
 */
public void setDimensions(int width, int height)
{
    _width = width; _height = height;
    // This is a lame attempt to hide our meshing problems.
    Rectangle2D r = new Rectangle2D.Float(0f,0f, _width, _height);
    _destination.setColor(Color.WHITE); _destination.fill(r);
    _destination.setColor(Color.BLACK); _destination.draw(r);
}

public WritableRaster getTileBuffer()
{
    if(_tile==null) {
        // It's really a strip, not a tile. The buffer is actually 1 pixel higher than the tile size.
        // This is for an attempt to deal with the meshing problems
        _tile = new BufferedImage(_width, getTileHeight()+1, BufferedImage.TYPE_BYTE_BINARY, 
                           (IndexColorModel)_colorModel);
        _currentTileY = 0; // The next scanline goes at the top of the tile
    }
    // probably should cache this, too
    return _tile.getRaster();
}

public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize)
{
    WritableRaster r = getTileBuffer();
    
    // make sure we're right about what we are assuming (some people might call that an Assertion)
    if ((y != _currentTileY) || (x != 0) || (w!=_width) || (h!=1))
        throw new IndexOutOfBoundsException("Scanlines arriving out of order");
    
    // Get the y index in the tile for this scanline
    int tileHeight = getTileHeight();
    int tileY = _currentTileY % tileHeight;
    // make sure to fill up to the extra meshing scanline
    if ((tileY==0) && (y!=0))
        tileY=tileHeight;
    
    // push the scanline into the tile
    r.setDataElements(x, tileY, w, h, pixels);
    
    // if we've filled up the tile, draw it
    if (tileY == tileHeight) {
        int tileIndex = (_currentTileY / tileHeight)-1;
        _destination.drawImage(_tile, x, tileIndex*tileHeight, null);
        r.setDataElements(x,0,w,h,pixels); // experiment to help meshing.  copy last scanline in tile to the first
    }
    
    // get ready for the next scanline
    ++_currentTileY;
}

public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize)  { }

public void setProperties(Hashtable props)  { }

/**
 * The main entry point. Images are drawn at 0,0 so destination should have the image transform applied to it.
 */
public void drawImage(ImageProducer ip, Graphics2D destination)
{
    _destination = destination;
    _tile = null; _currentTileY = 0;  // reset everything
    ip.requestTopDownLeftRightResend(this);  // tell the producer to start
}

public static void drawTiledImage(ImageProducer ip, Graphics2D aGfx)
{
    new PDFTiledImage().drawImage(ip, aGfx);
}

// Just for fitting in to all the places that require an Image to be passed around.
// All it does is hold an instance of the imageProducer.
public static class TiledImageProxy extends Image
{
    ImageProducer _source;
    
    public TiledImageProxy(ImageProducer producer)  { _source = producer; }
    
    public void drawImage(Graphics2D destination) { PDFTiledImage.drawTiledImage(_source, destination); }
    
    // forward to the producer
    public int getHeight(ImageObserver observer)  { return ((PDFCCITTFaxProducer)_source).getCodec().getHeight(); }
    public int getWidth(ImageObserver observer)  { return ((PDFCCITTFaxProducer)_source).getCodec().getWidth(); }
    
    /** other Image methods don't do anything */
    public void flush()  { }
    public Graphics getGraphics()  { return null; }
    public Object getProperty(String name, ImageObserver observer)  { return null; }
    public ImageProducer getSource()  { return null; }
}

}