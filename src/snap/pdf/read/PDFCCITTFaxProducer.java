/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/** 
 * This class is an image producer that uses the RBCodecCCITTFaxDecoder to produce the bits of a 1bpp monochrome image.
 * This is used as an optimization for when decoding the entire image at once would be too expensive.
 * Instead of holding on to all the bytes of a decompressed 8bpp BufferedImage,
 * this class holds on to the compressed bit stream and produces the image as needed.
 */
public class PDFCCITTFaxProducer extends OutputStream implements ImageProducer {

    // All the consumers
    List _consumers = new ArrayList();
    
    // When producing, the subset of all consumers who get notifications
    ImageConsumer _currentConsumers[];
    
    // The codec ( and the data )
    SnapDecodeCCITTFax _ccittFaxDecoder;

    // The color model
    IndexColorModel _colorModel;
    
    // The current scanline being produced
    int _scanlineIndex;

/**
 * Create a new image producer for the codec.
 */
public PDFCCITTFaxProducer(SnapDecodeCCITTFax decoder)
{
    super();
    _ccittFaxDecoder = decoder;
    // create the color model.  It's the same for all pixels
    byte clut[];
    if (_ccittFaxDecoder.blackIsOne())
        clut = new byte[]{(byte)0xff, (byte)0x00};
    else
        clut = new byte[]{(byte)0,(byte)0xff};
    
    _colorModel = new IndexColorModel(1,2,clut,clut,clut);
}

/** ImageProducer method - add a new consumer */
public void addConsumer(ImageConsumer ic)
{
    // just add the consumer to the list
    // We wait until startProduction() to do any work.
    if (!isConsumer(ic))
        _consumers.add(ic);
}

/** ImageProducer method - check to see if we are sending bits to ic */
public boolean isConsumer(ImageConsumer ic)
{
    return _consumers.indexOf(ic) != -1;
}

/** ImageProducer method */
public void removeConsumer(ImageConsumer ic)
{
    _consumers.remove(ic);
}

/** ImageProducer method - kicks things off. */
public void startProduction(ImageConsumer ic)
{
    addConsumer(ic);
    // set the list of target consumers to everybody
    _currentConsumers = (ImageConsumer[])_consumers.toArray(new ImageConsumer[_consumers.size()]);
    produce();
}

/** ImageProducer method - send all the bits to the given consumer */
public void requestTopDownLeftRightResend(ImageConsumer ic)
{
    // Set the target consumers to just the one
    _currentConsumers = new ImageConsumer[] {ic};
    produce();
}

/** The blood & guts */
public void produce()
{
    int nconsumers = _currentConsumers.length;
    int i, status;
    
    // Tell everyone about the basic information
    for(i=0; i<nconsumers; ++i) {
        _currentConsumers[i].setHints(ImageConsumer.COMPLETESCANLINES |
                              ImageConsumer.SINGLEFRAME | 
                              ImageConsumer.TOPDOWNLEFTRIGHT |
                              ImageConsumer.SINGLEPASS);
        _currentConsumers[i].setColorModel(_colorModel);
        _currentConsumers[i].setDimensions(_ccittFaxDecoder.getWidth(), _ccittFaxDecoder.getHeight());

   }
    
    // Reset the y value
    _scanlineIndex = 0;
    
    // launch the decoder, which will call our OutputStream methods while decompressing.
    try {
        _ccittFaxDecoder.decodeStream(this);
        if (_scanlineIndex != _ccittFaxDecoder.getHeight()) {
            System.err.println("CCITTFaxDecode read "+_scanlineIndex+" scanlines.  Expecting "+_ccittFaxDecoder.getHeight());
            status = ImageConsumer.IMAGEERROR;
        }
        else
            status = ImageConsumer.STATICIMAGEDONE;
    }
    catch (Exception e) {
        e.printStackTrace();
        status = ImageConsumer.IMAGEERROR;
    }
   
    // Tell everyone we're done
    for(i=0; i<nconsumers; ++i) {
        _currentConsumers[i].imageComplete(status);
    }
    
}

/**
 * returns the decoder, should you need it.
 */
public SnapDecodeCCITTFax getCodec() { return _ccittFaxDecoder; }

/* These Outputstream methods are used as 'callbacks' from the decoder.  
 * When it writes a scanline, we'll send it to the consumer.
 */
public void write(byte[] b) throws IOException
{
    int scanlinesize = _ccittFaxDecoder.getWidth();
    
    // send the scanline to everyone
    for(int i=0, n=_currentConsumers.length; i<n; ++i)
        _currentConsumers[i].setPixels(0, _scanlineIndex, scanlinesize, 1, _colorModel, 
                b, 0, scanlinesize);
    // bump the y
    ++_scanlineIndex;
}

public void flush() throws IOException
{
}

// see RBCodeCCITTFaxDecoder for a treatise on why this is especially stupid
public void write(int b) throws IOException
{
    throw new IOException("blackIsOne not implemented yet");   
}

}
