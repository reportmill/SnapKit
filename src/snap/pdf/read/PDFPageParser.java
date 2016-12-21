/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.color.*;
import java.util.*;
import snap.gfx.*;
import snap.pdf.*;

/**
 * This class parses the page marking operators for a specific page number (it gets the
 * contents for that page from n PDFReader.)  It uses the various factory objects for
 * graphic object creation and a MarkupHandler to do the actual drawing.
 *
 *  Currently unsupported:
 *       - Ignores hyperlinks (annotations)
 *       - Type 1 & Type 3 fonts
 *       - Transparency blend modes other than /Normal
 *       ...
 */
public class PDFPageParser {
    
    // The PDF file that owns this parser
    PDFFile              _pfile;
    
    // The page index of the page being parsed
    PDFPage              _page;
    
    // The bounds of the area being parsed
    Rect                 _bounds;
    
    // The tokens of the page being parsed
    List                 _tokens;
    
    // The gstates of the page being parsed
    Stack                _gstates;
    
    // The current text object (gets created once and reused)
    PDFTextObject        _textObj;
    
/**
 * Creates a new page parser for a given PDF file and a page index.
 */
public PDFPageParser(PDFFile aPdfFile, int aPageIndex)
{
    // Cache given pdf file and page index
    _pfile = aPdfFile;
    _page = _pfile.getPage(aPageIndex);
    
    // Get Media box and crop box and set bounds of the area we're drawing into (interseciont of media and crop)
    Rect media = _page.getMediaBox();
    Rect crop = _page.getCropBox();
    _bounds = media.getIntersectRect(crop);

    // Create the gstate list and the default gstate
    _gstates = new Stack();
    _gstates.push(new PDFGState());
    
    // Initialize gstate to bounds
    getGState().trans.setToTranslation(-_bounds.getX(),-_bounds.getY());
    
    //TODO need to set transform for pages with "/Rotate" key
    //Transform  t = getGState().trans; t.setToTranslation(-_bounds.getX(),-_bounds.getY());
    //t.rotate(-Math.PI/2); t.translate(0,_bounds.getWidth());
    //TODO also need to make sure PDFPage returns right rect (ImageShape initialized from file) 
}

/**
 * Returns the page.
 */
public PDFPage getPage() { return _page; }

/**
 * Returns the token at the given index.
 */
private PageToken getToken(int index) { return (PageToken)_tokens.get(index); }

/**
 * Main entry point. Runs the lexer on the pdf content and passes the list of tokens to the parser. By separating out
 * a routine that operates on the list of tokens, we can implement Forms & patterns by recursively calling the parse
 * routine with a subset of the token list.
 */
public void parse()
{
    // Get page contents stream and stream bytes (decompressed/decoded)
    PDFStream pstream = getPage().getPageContentsStream(); if(pstream==null) return;
    byte pbytes[] = pstream.decodeStream();
    
    // Create top-level list of tokens and run the lexer to fill the list
    List pageTokens = PageToken.getTokens(pbytes);
 
    // Start the markup handler
    PDFMarkupHandler engine = _pfile.getMarkupHandler();
    engine.beginPage(_bounds.getWidth(), _bounds.getHeight());
    
    // Initialize a text object
    _textObj = new PDFTextObject(engine.getFontRenderContext());
    
    // Parse the tokens
    parse(pageTokens, pbytes);
}

/**
 * The meat and potatoes of the pdf parser. Translates the token list into a series of calls to either a Factory class,
 * which creates a Java2D object (like GeneralPath, Font, Image, GlyphVector, etc.), or the markup handler, which does
 * the actual drawing.
 */
public void parse(List tokenList, byte pageBytes[]) 
{
    // save away the factory callback handler objects
    PDFMarkupHandler engine = _pfile.getMarkupHandler();
    int compatibility_sections = 0;
    
    // Cache old tokens and set the token list that will be used by routines like getToken()
    // This routine is potentially recursive
    List oldTokens = _tokens; _tokens = tokenList;
    
    // Initialize current path
    // Note: in PDF, the path is not part of GState and so is not saved/restored by gstate operators
    GeneralPath path = null, future_clip=null;
    int numops = 0;                    // The number of operands available for the current operator
    //boolean swallowedToken, didDraw;   // for errors and operations that require multiple tokens
    
    // Get the current gstate
    PDFGState gs = getGState();
    Color acolor; ColorSpace cspace;
    
    // Iterate over page contents tokens
    for(int i=0, iMax=_tokens.size(); i<iMax; i++) {
        
        // Get the current loop token
        PageToken token = getToken(i);
        boolean swallowedToken = false, didDraw = false;
        
        if(token.type==PageToken.PDFOperatorToken) {
            int tstart = token.tokenLocation();
            int tlen = token.tokenLength();
            
            // Switch on first byte of the operator
            byte c = pageBytes[tstart];
            switch(c) {
            case 'b' : //closepath,fill,stroke (*=eostroke)   
                if(numops==0) {
                    if(tlen==1) path.setWindingRule(GeneralPath.WIND_NON_ZERO); // b
                    else if(tlen==2 && pageBytes[tstart+1] =='*') path.setWindingRule(GeneralPath.WIND_EVEN_ODD); // b*
                    else break;
                    path.closePath();
                    engine.fillPath(gs, path);
                    engine.strokePath(gs, path); didDraw = true; swallowedToken = true;
                }
                break;
            case 'B' : // fill,stroke (*=eostroke)
                if(numops==0 && (tlen==1 || (tlen==2 && (pageBytes[tstart+1]=='*')))) {
                    if(tlen==1) path.setWindingRule(GeneralPath.WIND_NON_ZERO);
                    else path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                    engine.fillPath(gs, path);
                    engine.strokePath(gs, path); didDraw = true; swallowedToken = true;
                }
                else if(numops==0 && tlen==2) {
                    if(pageBytes[tstart+1]=='T') { //BT - Begin text object
                        _textObj.begin(); swallowedToken = true; }
                    else if(pageBytes[tstart+1]=='X') { //BX start (possibly nested) compatibility section
                        ++compatibility_sections; swallowedToken = true; }
                    // BI - inline images
                    else if (pageBytes[tstart+1]=='I') {//BI
                        i = parseInlineImage(i+1, pageBytes); swallowedToken = true; }
                }
                else if(tlen==3 && pageBytes[tstart+2]=='C') {
                    if(pageBytes[tstart+1]=='D' || pageBytes[tstart+1]=='M')  //BDC, BMC
                        swallowedToken = true;
                } 
                break;
            case 'c' : // c, cm, cs
                if(tlen==1) { // Cureveto
                    if(numops==6) {
                        getPoint(i, gs.cp);
                        path.curveTo(getFloat(i-6), getFloat(i-5), getFloat(i-4), getFloat(i-3), gs.cp.x, gs.cp.y);
                        swallowedToken = true;
                    }
                }
                else if(tlen==2) {
                    c=pageBytes[tstart+1];
                    if ((c=='m') && (numops==6)) { // cm - Concat matrix
                        gs.trans.concatenate(getTransform(i)); swallowedToken = true; }
                    else if ((c=='s') && (numops==1)) { //cs - set non-stroke colorspace 
                        String space = getToken(i-1).nameString(pageBytes);
                        gs.colorSpace = PDFColorSpace.getColorspace(space, _pfile, _page); swallowedToken = true;
                    }
                }
                break;
            case 'C' : // CS stroke colorspace
                if(tlen==2 && pageBytes[tstart+1]=='S' && numops==1) {
                    String space = getToken(i-1).nameString(pageBytes);
                    gs.scolorSpace = PDFColorSpace.getColorspace(space, _pfile, _page); swallowedToken = true;
                }
                break;
            case 'd' : //setdash
                if(tlen==1 && numops==2) {
                    gs.lineDash = getFloatArray(i-2);
                    gs.dashPhase = getFloat(i-1);
                    gs.lineStroke = gs.createStroke();
                    swallowedToken=true;
                }
                // d0 & d1 are only available in charprocs streams
                break;
            case 'D' : // xobject Do [also DP]
                if(tlen==2) {
                    if(pageBytes[tstart+1]=='o' && numops==1) {
                        String iname=getToken(i-1).nameString(pageBytes);
                        Object xobj = getPage().getXObject(iname);
                        if(xobj instanceof Image) {
                            drawImage((Image)xobj); swallowedToken = true; }
                        else if(xobj instanceof PDFForm) {
                            executeForm((PDFForm)xobj); swallowedToken = true; }
                        else throw new PDFException("Error reading XObject");
                    }
                    else if(pageBytes[tstart+1]=='P') // DP marked content
                        swallowedToken = true;
                }
                break;
            case 'E' : // [also EI,EMC]
                if(tlen==2 && numops==0) {
                    if (pageBytes[tstart+1]=='T') {  //ET
                        _textObj.end(); swallowedToken = true; }
                    else if (pageBytes[tstart+1]=='X') { // EX
                        if(--compatibility_sections<0)
                            throw new PDFException("Unbalanced BX/EX operators");
                        swallowedToken = true;
                    }
                }
                else if ((tlen==3) && (pageBytes[tstart+1]=='M') && (pageBytes[tstart+2]=='C'))
                    swallowedToken = true;
                break;
            case 'f' : // fill (*=eofill)  
            case 'F' : // F is the same as f, but obsolete
                if(tlen==1) path.setWindingRule(GeneralPath.WIND_NON_ZERO);
                else if(tlen==2 && pageBytes[tstart+1]=='*') path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                else break;
                engine.fillPath(gs, path); didDraw = true; swallowedToken = true;
                break;
            case 'g' : // setgray
                if (tlen==1) {
                    cspace = PDFColorSpace.getColorspace("DeviceGray", _pfile, _page);
                    gs.color = getColor(cspace,i,numops); gs.colorSpace = cspace; swallowedToken = true;
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='s') && (numops==1)) { // gs
                    // Extended graphics state
                    Map exg = getPage().getExtendedGStateNamed(getToken(i-1).nameString(pageBytes));
                    readExtendedGState(gs, exg); swallowedToken = true;
                }
                break;
            case 'G' : // setgray
                if (tlen==1) {
                    cspace = PDFColorSpace.getColorspace("DeviceGray", _pfile, _page);
                    gs.scolor = getColor(cspace,i,numops); gs.scolorSpace = cspace; swallowedToken = true;
                }
                break;
            case 'h' : // closepath
                if ((tlen==1) && (numops==0)) {
                    path.closePath();
                    Point2D lastPathPoint = path.getCurrentPoint(); 
                    gs.cp.x = (float)lastPathPoint.getX();
                    gs.cp.y = (float)lastPathPoint.getY(); swallowedToken = true;
                }
                break;
            case 'i' : // setflat
                if ((tlen==1) && (numops==1)) {
                    gs.flatness = getFloat(i-1); swallowedToken = true; }
                break;
            case 'I' : // ID
                break;
            case 'j' : // setlinejoin
                if ((tlen==1) && (numops==1)) {
                    gs.lineJoin = getInt(i-1);
                    gs.lineStroke = gs.createStroke(); swallowedToken = true;
                }
                break;
            case 'J' : // setlinecap
                if ((tlen==1) && (numops==1)) {
                    gs.lineCap = getInt(i-1);
                    gs.lineStroke = gs.createStroke(); swallowedToken = true;
                }
                break;
            case 'k' : // setcmyk non-stroke
            case 'K' : // setcmyk stroke
                if(tlen==1) {
                    cspace = PDFColorSpace.getColorspace("DeviceCMYK", _pfile, _page);
                    acolor = getColor(cspace,i,numops);
                    if(c=='k') { gs.colorSpace = cspace; gs.color = acolor; }
                    else { gs.scolorSpace = cspace; gs.scolor = acolor; } // strokecolor
                    swallowedToken = true;
                }
                break;
                
            // lineto
            case 'l': if(tlen==1 && numops==2) { getPoint(i, gs.cp); path.lineTo(gs.cp.x, gs.cp.y);
                    swallowedToken = true; } break;
                    
            // moveto (creates a new path if there isn't one already, otherwise you get a subpath.)
            case 'm':
                if(tlen==1 && numops==2) {
                    getPoint(i, gs.cp);
                    if(path==null) path = new GeneralPath();
                    path.moveTo(gs.cp.x, gs.cp.y);
                    swallowedToken = true;
                }
                break;
                
            //setmiterlimit
            case 'M' :
                if (tlen==1) {
                    if (numops == 1) { 
                        gs.miterLimit = getFloat(i-1);
                        gs.lineStroke = gs.createStroke();
                        swallowedToken = true;
                    }
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='P') && (numops==1)) { // MP - Marked content point
                    swallowedToken = true; }
                break;
            case 'n' : //endpath
                // End path without fill or stroke - used for clearing the path after a clipping operation ( W n )
                if(tlen==1 && numops==0) {
                    didDraw = true; swallowedToken=true; }
                break;
                
            // gsave;
            case 'q': if(tlen==1 && numops==0) { gs = gsave(); swallowedToken = true; } break;
            
            // grestore
            case 'Q': if(tlen==1 && numops==0) { gs = grestore(); swallowedToken = true; } break;
            
            // re=rectangle, rg=setrgbcolor, ri=renderingintent
            case 'r' : 
                if (tlen==2) {
                    c=pageBytes[tstart+1];
                    if ((c=='e') && (numops==4)) { //x y w h re
                        // Add Rectangle
                        float x = getFloat(i-4), y = getFloat(i-3);
                        float w = getFloat(i-2), h = getFloat(i-1);
                        
                        // re either creates a new path or adds to the current one
                        if (path==null) 
                            path = new GeneralPath();
                        path.moveTo(x,y); path.lineTo(x+w,y); path.lineTo(x+w,y+h); path.lineTo(x,y+h);path.closePath();
                        // reset current point to start of rect. TODO: Check that this is what really happens in pdf
                        gs.cp.x = x; gs.cp.y = y; swallowedToken = true;
                    }
                    else if ((c=='i') && (numops==1)) {  //  /IntentName ri
                        gs.renderingIntent = PDFGState.getRenderingIntentID(getToken(i-1).nameValue(pageBytes));
                        swallowedToken=true;
                    }
                    else if (c=='g') { //r g b rg
                        cspace = PDFColorSpace.getColorspace("DeviceRGB", _pfile, _page);
                        gs.color = getColor(cspace,i,numops);
                        gs.colorSpace = cspace; swallowedToken = true;
                    }
                }
                break;
                
            // RG set stroke rgbcolor
            case 'R': 
                if((tlen==2) && (pageBytes[tstart+1]=='G')) {
                    cspace = PDFColorSpace.getColorspace("DeviceRGB", _pfile, _page);
                    gs.scolor = getColor(cspace,i,numops);
                    gs.scolorSpace = cspace; swallowedToken = true;
                }
                break;
            case 's' : 
                if(tlen==1) { // s
                    if(numops==0) { // closepath, stroke
                        path.closePath();
                        engine.strokePath(gs, path); didDraw = true; swallowedToken = true;
                    }
                }
                else if(pageBytes[tstart+1]=='c') { //sc, scn   setcolor in colorspace
                    if(tlen==2) {
                        gs.color = getColor(gs.colorSpace,i,numops); swallowedToken = true; }
                    else if(tlen==3 && pageBytes[tstart+2]=='n') { // scn
                        if(gs.colorSpace instanceof PDFPatternSpace && numops>=1) {
                            String pname = getToken(i-1).nameString(pageBytes);
                            PDFPattern pat = getPage().getPattern(pname);
                            gs.color = pat.getPaint();                            
                            // this is really stupid.  change this around
                            if ((pat instanceof PDFPatternTiling) && (gs.color==null)) {
                                // Uncolored tiling patterns require color values be passed.
                                // Note, however, that although you can draw an uncolored tiling pattern any number of
                                // times in different colors, we only do it once (after which it will be cached)
                                if (numops>1) {
                                    ColorSpace tileSpace=((PDFPatternSpace)gs.colorSpace).tileSpace;
                                    if (tileSpace==null)
                                        tileSpace=gs.colorSpace;
                                    gs.color = getColor(tileSpace,i-1, numops-1);
                                }
                                this.executePatternStream((PDFPatternTiling)pat);
                                gs.color = pat.getPaint();
                            }
                        }
                        else gs.color = getColor(gs.colorSpace,i,numops);
                        swallowedToken = true;
                    }
                }
                else if(tlen==2 && pageBytes[tstart+1]=='h') { //sh      && (numops==1?
                    String shadename = getToken(i-1).nameString(pageBytes);
                    java.awt.Paint oldPaint = gs.color;
                    PDFPatternShading shade = getPage().getShading(shadename);
                    gs.color = shade.getPaint();  //save away old color
                    // Get the area to fill.  If the shading specifies a bounds, use that, if not, use the clip.
                    // If there's no clip, fill the whole page.
                    GeneralPath shadearea;
                    if(shade.getBounds() != null)
                        shadearea = new GeneralPath(shade.getBounds());
                    else {
                        Rectangle2D r = new Rectangle2D.Double(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
                        shadearea = gs.clip!=null? (GeneralPath)gs.clip.clone() : new GeneralPath(r);
                        try { shadearea.transform(gs.trans.createInverse()); } // transform from page to user space
                        catch(NoninvertibleTransformException e) { throw new PDFException("Invalid user space xform"); }
                    }
                    engine.fillPath(gs, shadearea);
                    gs.color = oldPaint;  //restore the color
                    didDraw = true; swallowedToken = true;  // TODO:probably did draw... check this
                }
                break;
            case 'S' : // Very similar to above
                if (tlen==1) { // S - stroke
                    if(numops==0) {
                        engine.strokePath(gs, path); didDraw = true; swallowedToken = true; }
                }
                else if (pageBytes[tstart+1]=='C') {  // SC : strokecolor in normal colorspaces
                    if (tlen==2) {
                        gs.scolor = getColor(gs.scolorSpace, i, numops); swallowedToken = true; }
                    else if ((tlen==3) && (pageBytes[tstart+2]=='N')) { // SCN - TODO: deal with weird colorspaces
                        gs.scolor = getColor(gs.scolorSpace, i, numops); swallowedToken = true; }
                }
                break;
            case 'T' : // [T*, Tc, Td, TD, Tf, Tj, TJ, TL, Tm, Tr, Ts, Tw, Tz]
                // break text handling out
                if(tlen==2 && parseTextOperator(pageBytes[tstart+1], i, numops, gs, pageBytes)) 
                    swallowedToken = true;
                break;
            case '\'':
            case '\"':  // ' and " also handled by text routine
                if(tlen==1 && parseTextOperator(c, i, numops, gs, pageBytes))
                    swallowedToken = true;
                break;
            case 'v' : // Curveto (first control point is current point)
                if(tlen==1 && numops==4) {
                    double cp1x = gs.cp.x, cp1y = gs.cp.y;
                    Point cp2 = getPoint(i-2);
                    getPoint(i, gs.cp);
                    path.curveTo(cp1x, cp1y, cp2.x, cp2.y, gs.cp.x, gs.cp.y); swallowedToken = true;
                }
                break;
            case 'w' : // setlinewidth
                if((tlen==1) && (numops==1)) {
                    gs.lineWidth = getFloat(i-1);
                    gs.lineStroke = gs.createStroke(); swallowedToken = true;
                }
                break;
            case 'W' : // clip (*=eoclip)
                int wind;
                
                if(numops != 0) break;
                if(tlen==1) wind = GeneralPath.WIND_NON_ZERO; // W
                else if(tlen==2 && pageBytes[tstart+1]=='*') wind = GeneralPath.WIND_EVEN_ODD; // W*
                else break;
                
                // Somebody at Adobe's been smoking crack.
                // The clipping operation doesn't modify the clipping in the gstate.
                // Instead, the next path drawing operation will do that, but only AFTER it draws.  
                // So a sequence like 0 0 99 99 re W f will fill the rect first
                // and then set the clip path using the rect.
                // Because the W operation doesn't do anything, they had to introduce
                // the 'n' operation, which is a drawing no-op, in order to do a clip and not also draw the path.
                // You might think it would be safe to just reset the clip here,
                // since the path it will draw is the same as the path it will clip to.
                // However, there's at least one (admittedly obscure) case I can think
                // of where clip(path),draw(path)  is different from draw(path),clip(path): 
                //     W* f  %eoclip, nonzero-fill
                // Also note that Acrobat considers it an error to have a W that isn't
                // immediately followed by a drawing operation (f, f*, F, s, S, B, b, n)
                if(path != null) {
                    path.setWindingRule(wind);
                    future_clip = (GeneralPath)path.clone();
                 }
                swallowedToken=true;
                break;
            case 'y' : // curveto (final point replicated)
                if(tlen==1 && numops==4) {
                    Point cp1 = getPoint(i-2);
                    getPoint(i, gs.cp);
                    path.curveTo(cp1.x, cp1.y, gs.cp.x, gs.cp.y, gs.cp.x, gs.cp.y);
                    swallowedToken = true;
                }
                break;
            }
            
            // If we made it down here with swallowedToken==false, it's because there was no match, either because it
            // was an illegal token, or there were the wrong number of operands for the token.
            if(!swallowedToken) {
                // If we're in a compatibility section, just print a warning, since
                // we want to be alerted about anything that we don't currently support.
                if(compatibility_sections > 0) 
                    System.err.println("Warning - ignoring "+token.toString(pageBytes)+" in compatibility section");
                else throw new PDFException("Error in content stream. Token = "+token.toString(pageBytes));
            }
            
            numops=0; // everything was fine, reset the number of operands
        }
        
        // It wasn't an operator, so it must be an operand (comments are tossed by the lexer)
        else ++numops;
        
        // Catch up on that clipping.  Plus be anal and return an error, just like Acrobat.
        if(didDraw) {
            if(future_clip != null) {
                // Note that unlike other operators that change gstate, there is a specific call into markup handler
                // when clip changes. The markup handler can choose whether to respond to clipping change or whether
                // just to pull the clip out of the gstate when it draws.
                establishClip(future_clip, true); future_clip = null;
            }
            path = null;  // The current path and the current point are undefined after a draw
        }
        
        else {
            if(future_clip != null) { } // TODO: an error unless the last token was W or W*
        }
    }
    
    // restore previous token list
    _tokens = oldTokens;
}

public void executeForm(PDFForm f)
{
    Rectangle2D bbox = f.getBBox();
    AffineTransform xform = f.getTransform();
    
    // save the current gstate and set the transform in the newgstate
    PDFGState gs = gsave();
    gs.trans.concatenate(xform);
    
    // clip to the form bbox
    establishClip(new GeneralPath(bbox), true);
  
    // add the form's resources to the page resource stack
    getPage().pushResources(f.getResources(_pfile));
    parse(f.getTokens(this), f.getBytes());  // recurse back into the parser with a new set of tokens
    getPage().popResources();    // restore the old resources, gstate,ctm, & clip
    grestore();
}

/**
 * A pattern could execute its pdf over and over, like a form (above) but for performance reasons,
 * we only execute it once and cache a tile. To do this, we temporarily set the markup handler in the file to a new 
 * BufferedMarkupHander, add the pattern's resource dictionary and fire up the parser.
 */
public void executePatternStream(PDFPatternTiling pat)
{
    // Cache old handler, create image painter and set
    PDFMarkupHandler oldHandler = _pfile.getMarkupHandler();
    PDFMarkupHandler patHandler = PDFMarkupHandler.get();
    _pfile.setMarkupHandler(patHandler);
    
    // By adding the pattern's resources to page's resource stack, it means pattern will have access to resources
    // defined by the page.  I'll bet Acrobat doesn't allow you to do this, but it shouldn't hurt anything.
    getPage().pushResources(pat.getResources());
    
    // save the current gstate
    PDFGState gs = gsave();
    
    // Establish the pattern's transformation
    gs.trans.concatenate(pat.getTransform());
    
    // Begin the markup handler. TODO:probably going to have to add a translate by -x, -y of the bounds rect
    Rectangle2D prect = pat.getBounds();
    patHandler.beginPage(prect.getWidth(), prect.getHeight());
    
    // Get the pattern stream's tokens
    byte contents[] = pat.getContents();
    List <PageToken> tokens = PageToken.getTokens(contents);
    
    // Fire up parser
    parse(tokens, contents);
    
    // Get the image and set the tile.  All the resources can be freed up now
    pat.setTile(patHandler.getBufferedImage());
    
    // restore everything
    grestore();
    _pfile.setMarkupHandler(oldHandler);
}

// Handles all the text operations [T*,Tc,Td,TD,Tf,Tj,TJ,TL,Tm,Tr,Ts,Tw,Tz,'."]
// For the "T" operations, oper represents the second letter.
public boolean parseTextOperator(byte oper, int tindex, int numops, PDFGState gs, byte pageBytes[])
{
     boolean swallowedToken = false;
     
     switch(oper) {
         
         // T* - move to next line
         case '*': if (numops==0) { _textObj.positionText(0, -gs.tleading); swallowedToken = true; } break;
         
         // Tc - // Set character spacing
         case 'c': if(numops==1) { gs.tcs = getFloat(tindex-1); swallowedToken = true;  } break;
         
         // TD, td  - move relative to current line start (uppercase indicates to set leading to -ty)
         case 'D' :
         case 'd' : 
            if(numops==2) {
                float x = getFloat(tindex-2);
                float y = getFloat(tindex-1);
                _textObj.positionText(x,y);
                if(oper=='D') gs.tleading = -y;
                swallowedToken = true;
            }
            break;
            
        // Tf - Set font name and size
        case 'f' : 
            if(numops==2) {
                String fontalias = getToken(tindex-2).nameString(pageBytes); // name in dict is key, so lose leading /
                gs.font = getPage().getFontDictForAlias(fontalias);
                gs.fontSize = getFloat(tindex-1); swallowedToken = true;
            }
            break;
        
        // w c string " set word & charspacing, move to next line, show text
        case '"': if(numops!=3) break;
            gs.tws = getFloat(tindex-3); gs.tcs = getFloat(tindex-2); numops = 1; // fall through
             
        // ' - move to next line and show text
        case '\'': _textObj.positionText(0, -gs.tleading); // Fall through
        
        // Tj - Show text
        case 'j': 
            if(numops==1) {
                PageToken tok = getToken(tindex-1);
                int tloc = tok.tokenLocation(), tlen = tok.tokenLength();
                _textObj.showText(pageBytes, tloc, tlen, gs, _pfile); swallowedToken = true;
            }
            break;
            
        // TJ - Show text with spacing adjustment array
        case 'J': 
            if(numops==1) {
                List tArray = (List)(getToken(tindex-1).value);
                _textObj.showText(pageBytes, tArray, gs, _pfile); swallowedToken = true;
            }
            break;
            
        // TL -  set text leading
        case 'L': if(numops==1) { gs.tleading = getFloat(tindex-1); swallowedToken = true; } break;
         
        // Tm - set text matrix
        case 'm': 
            if(numops==6) {
                _textObj.setTextMatrix(getFloat(tindex-6), getFloat(tindex-5), getFloat(tindex-4),
                    getFloat(tindex-3), getFloat(tindex-2), getFloat(tindex-1));
                swallowedToken = true;
            }
            break;
            
        // Tr - set text rendering mode
        case 'r': if(numops==1) { gs.trendermode = getInt(tindex-1); swallowedToken = true; } break;
         
        // Ts - set text rise
        case 's': if(numops==1) { gs.trise = getFloat(tindex-1); swallowedToken = true; } break;
        
        // Tw - set text word spacing
        case 'w': if(numops==1) { gs.tws = getFloat(tindex-1); swallowedToken = true; } break;
         
        // Tz - horizontal scale factor
        case 'z': if(numops==1) { gs.thscale = getFloat(tindex-1)/100f; swallowedToken = true; } break;
     }

     return swallowedToken;
}

/** Returns the token at the given index as a float. */
private float getFloat(int i) { return getToken(i).floatValue(); }

/** Returns the token at the given index as an int. */
private int getInt(int i) { return getToken(i).intValue(); }

/** Returns the token at the given index as an array of floats */
private float[] getFloatArray(int i) 
{
    List <PageToken> ftokens = (List)(getToken(i).value);
    float farray[] = new float[ftokens.size()];
    for(int j=0, jMax=ftokens.size(); j<jMax; j++)  // We assume all tokens are floats
        farray[j] = ftokens.get(j).floatValue();
    return farray;
}

/** Returns a new point at the given index */
private Point getPoint(int i)
{
    double x = getToken(i-2).floatValue();
    double y = getToken(i-1).floatValue();
    return new Point(x,y);
}

/** Gets the token at the given index as a point. */
private void getPoint(int i, Point2D.Float pt)
{
    pt.x = getToken(i-2).floatValue();
    pt.y = getToken(i-1).floatValue();
}

/** Returns the token at the given index as a transform. */
private AffineTransform getTransform(int i)
{
    float a = getToken(i-6).floatValue(), b = getToken(i-5).floatValue();
    float c = getToken(i-4).floatValue(), d = getToken(i-3).floatValue();
    float tx = getToken(i-2).floatValue(), ty = getToken(i-1).floatValue();
    return new AffineTransform(a, b, c, d, tx, ty);
}

/**
 * The values for keys in inline images are limited to a small subset of names, numbers, arrays and maybe a dict.
 */
Object getInlineImageValue(PageToken token, byte pageBytes[])
{
    // Names (like /DeviceGray or /A85). Names can optionally be abbreviated.
    if(token.type==PageToken.PDFNameToken) { String abbrev = token.nameString(pageBytes); 
        for(int i=0, n=_inline_image_value_abbreviations.length; i<n; ++i) {
            if(_inline_image_value_abbreviations[i][0].equals(abbrev))
                return '/' + _inline_image_value_abbreviations[i][1]; }
        return '/' + abbrev;  // not found, so it's not an abbreviation.  We assume it's valid
    }
    
    // Numbers or bools
    else if(token.type==PageToken.PDFNumberToken || token.type==PageToken.PDFBooleanToken)
        return token.value;
        
    // An array of numbers or names (for Filter or Decode)
    else if(token.type==PageToken.PDFArrayToken) { List tokenarray = (List)token.value;
           List newarray = new ArrayList(tokenarray.size());
           for(int j=0, jMax=tokenarray.size(); j<jMax; ++j)     // recurse
               newarray.add(getInlineImageValue((PageToken)tokenarray.get(j), pageBytes));
           return newarray;
    }
    
    // Hex strings for indexed color spaces
    else if (token.type == PageToken.PDFStringToken)
        return token.byteArrayValue(pageBytes);
    
    // TODO: One possible key in an inline image is DecodeParms (DP). The normal decodeparms for an image is a dict.
    // The pdf spec doesn't give any information on the format of the dictionary.  Does it use the normal dictionary
    // syntax, or does it use the inline image key/value syntax? I have no idea, and I don't know how to generate a
    // pdf file that would have an inline image with a decodeparms dict.
    else { }
    throw new PDFException("Error parsing inline image dictionary");
}
        
/** 
 * Converts the tokens & data inside a BI/EI block into an image and draws it.
 * Returns the index of the last token consumed.
 */
public int parseInlineImage(int tIndex, byte[] pageBytes)
{
    Hashtable imageDict = new Hashtable();
    imageDict.put("Subtype", "/Image");

    // Get the inline image key/value pairs and create a normal image dictionary
    for(int i=tIndex, iMax=_tokens.size(); i<iMax; ++i) {
        PageToken token = getToken(i);

        // Handle NameToken: Translate key, get value, add translated key/value pair to the real dict
        if(token.type==PageToken.PDFNameToken) {
            String key = translateInlineImageKey(token.nameString(pageBytes));
            if (++i<iMax) {
                token = getToken(i);
                Object value = getInlineImageValue(token, pageBytes);
                imageDict.put(key,value);
            }
        }
        
        // The actual inline data. Create stream with dict & data and create image. The image does not get cached.
        // The only way an inline image would ever get reused is if it were inside a form xobject.
        // First get a colorspace object.  Inline images can use any colorspace a regular image can.
        // Create stream, tell imageFactory to create image and draw it
        else if (token.type==PageToken.PDFInlineImageData) {
            Object space = imageDict.get("ColorSpace");
            ColorSpace imageCSpace = space==null ? null : PDFColorSpace.getColorspace(space, _pfile, _page);
            PDFStream imageStream = new PDFStream(pageBytes, token.tokenLocation(), token.tokenLength(), imageDict);
            drawImage(PDFImage.getImage(imageStream, imageCSpace, _pfile));
            return i; // return token index
        }
    }
    
    // should only get here on an error (like running out of tokens or having bad key/value pairs)
    throw new PDFException("Syntax error parsing inline image dictionary");
}

/** map for translating inline image abbreviations into standard tokens */
static final String _inline_image_key_abbreviations[][] = {
    {"BPC", "BitsPerComponent"}, {"CS", "ColorSpace"}, {"D", "Decode"}, {"DP", "DecodeParms"},
    {"F", "Filter"}, {"H", "Height"}, {"IM", "ImageMask"}, {"I", "Interpolate"}, {"W", "Width"}};

/** Looks up an abbreviation in the above map. */
private String translateInlineImageKey(String abbreviation)
{
    for(int i=0, n=_inline_image_key_abbreviations.length; i<n; ++i) {
        if(_inline_image_key_abbreviations[i][0].equals(abbreviation))
            return _inline_image_key_abbreviations[i][1]; }
    return abbreviation; // not found, so it's not an abbreviation
}

static final String _inline_image_value_abbreviations[][] = {
    {"G", "DeviceGray"}, {"RGB", "DeviceRGB"}, {"CMYK", "DeviceCMYK"}, {"I", "Indexed"}, {"AHx", "ASCIIHexDecode"},
    {"A85", "ASCII85Decode"}, {"LZW", "LZWDecode"}, {"Fl", "FlateDecode"}, {"RL", "RunLengthDecode"},
    {"CCF", "CCITTFaxDecode"}, {"DCT", "DCTDecode"}
};

/** Establishes an image transform and tells markup engine to draw the image */
public void drawImage(Image im) 
{
    // In pdf, an image is defined as occupying the unit square no matter how many pixels wide or high
    // it is (image space goes from {0,0} - {1,1})
    // A pdf producer will scale up the ctm to get whatever size they want.
    // We remove the pixelsWide & pixelsHigh from the scale since awt image space goes from {0,0} - {width,height}
    // Also note that in pdf image space, {0,0} is at the upper-, left.  Since this is flipped from all the other
    // primatives, we also include a flip here for consistency.
    int pixWide = im.getWidth(null);
    int pixHigh = im.getHeight(null);
    if(pixWide<0 || pixHigh<0)
        throw new PDFException("Error loading image"); //This shouldn't happen

    AffineTransform ixform = new AffineTransform(1.0/pixWide, 0.0, 0.0, -1.0/pixHigh, 0, 1.0);
    _pfile.getMarkupHandler().drawImage(getGState(), im, ixform);
}

/**
 * Returns the last GState in the gstate list.
 */
private PDFGState getGState()  { return (PDFGState)_gstates.peek(); }

/**
 * Pushes a copy of the current gstate onto the gstate stack and returns the new gstate.
 */
private PDFGState gsave()
{
    PDFGState newstate = (PDFGState)getGState().clone();
    _gstates.push(newstate);
    return newstate;
}

/**
 * Pops the current gstate from the gstate stack and returns the restored gstate.
 */
private PDFGState grestore()
{
    // also calls into the markup handler if the change in gstate will cause the clipping path to change.
    GeneralPath currentclip = ((PDFGState)_gstates.pop()).clip;
    PDFGState gs = getGState();
    GeneralPath savedclip = gs.clip;
     if ((currentclip != null) && (savedclip != null)) {
        if (!currentclip.equals(savedclip))
            _pfile.getMarkupHandler().clipChanged(gs);
     }
     else if (currentclip != savedclip)
         _pfile.getMarkupHandler().clipChanged(gs);
     return gs;
}

/**
 * Called when the clipping path changes. The clip in the gstate is defined to be in page space.
 * Whenever clip is changed, we calculate new clip, which can be intersected with the old clip, and save it in gstate.
 * NB. This routine modifies the path that's passed in to it.
 */
public void establishClip(GeneralPath newclip, boolean intersect)
{
    PDFGState gs = getGState();
    
    // transform the new clip path into page space
    newclip.transform(gs.trans);
    
    // If we're adding a clip to an existing clip, calculate the intersection
    if (intersect && (gs.clip != null)) {
        Area clip_area = new Area(gs.clip);
        Area newclip_area = new Area(newclip);
        clip_area.intersect(newclip_area);
        newclip = new GeneralPath(clip_area);
    }
    gs.clip = newclip;
    
    // notify the markup handler of the new clip
    _pfile.getMarkupHandler().clipChanged(gs);
}

/**
 * Called with any of the set color operations to create new color instance from the values in the stream.
 * Currently considers having the wrong number of components an error.
 */
private Color getColor(ColorSpace space, int tindex, int numops)
{
    int n = space.getNumComponents();
    float varray[] = new float[n]; // how much of a performance hit is allocating this every time?

    if(numops != n)
        throw new PDFException("Wrong number of color components for colorspace");
    for(int i=0; i<n; ++i)
        varray[i] = getFloat(tindex-(n-i));
    return PDFColorSpace.createColor(space, varray);
}

static int getBlendModeID(String pdfName)
{
    if(pdfName.equals("/Normal") || pdfName.equals("/Compatible")) return PDFComposite.NormalBlendMode;
    if(pdfName.equals("/Multiply")) return PDFComposite.MultiplyBlendMode;
    if(pdfName.equals("/Screen")) return PDFComposite.ScreenBlendMode;
    if(pdfName.equals("/Overlay")) return PDFComposite.OverlayBlendMode;
    if(pdfName.equals("/Darken")) return PDFComposite.DarkenBlendMode;
    if(pdfName.equals("/Lighten")) return PDFComposite.LightenBlendMode;
    if(pdfName.equals("/ColorDodge")) return PDFComposite.ColorDodgeBlendMode;
    if(pdfName.equals("/ColorBurn")) return PDFComposite.ColorBurnBlendMode;
    if(pdfName.equals("/HardLight")) return PDFComposite.HardLightBlendMode;
    if(pdfName.equals("/SoftLight")) return PDFComposite.SoftLightBlendMode;
    if(pdfName.equals("/Difference")) return PDFComposite.DifferenceBlendMode;
    if(pdfName.equals("/Exclusion")) return PDFComposite.ExclusionBlendMode;
    if(pdfName.equals("/Hue")) return PDFComposite.HueBlendMode;
    if(pdfName.equals("/Saturation")) return PDFComposite.SaturationBlendMode;
    if(pdfName.equals("/Color")) return PDFComposite.ColorBlendMode;
    if(pdfName.equals("/Luminosity")) return PDFComposite.LuminosityBlendMode;
    throw new PDFException("Unknown blend mode name \""+pdfName+"\"");
}

/**
 * Pull out anything useful from an extended gstate dictionary
 */
void readExtendedGState(PDFGState gs, Map exgstate)
{
    boolean strokeChanged = false;
    boolean transparencyChanged = false;
    
    if(exgstate==null) return;
    
    // The dictionary will have been read in by PDFReader, so
    // elements will have been converted into appropriate types, like Integer, Float, List, etc.
    
    Iterator entries = exgstate.entrySet().iterator();
    while(entries.hasNext()) {
        Map.Entry entry=(Map.Entry)entries.next();
        String key = (String)entry.getKey();
        Object val = entry.getValue();
        
        //line width, line cap, line join, & miter limit
        if(key.equals("LW")) { gs.lineWidth = ((Number)val).floatValue(); strokeChanged = true; }
        else if(key.equals("LC")) { gs.lineCap = ((Number)val).intValue(); strokeChanged = true; }
        else if(key.equals("LJ")) { gs.lineJoin = ((Number)val).intValue(); strokeChanged = true; }
        else if(key.equals("ML")) { gs.miterLimit = ((Number)val).floatValue(); strokeChanged = true; }
     
        // Dash:       "/D  [ [4 2 5 5] 0 ]"
        else if(key.equals("D")) {
            List dash = (List)val;
            gs.dashPhase = ((Number)dash.get(1)).floatValue();
            List dashArray = (List)dash.get(0);
            int n = dashArray.size();
            gs.lineDash = new float[n];
            for(int i=0; i<n; ++i) gs.lineDash[i] = ((Number)dashArray.get(i)).floatValue();
            strokeChanged = true;
        }
    
        // Rendering intent
        else if(key.equals("RI"))
            gs.renderingIntent = PDFGState.getRenderingIntentID((String)val);
    
        // Transparency blending mode
        else if(key.equals("BM")) {
            int bm = PDFPageParser.getBlendModeID((String)val);
            if(bm != gs.blendMode) { gs.blendMode = bm; transparencyChanged = true; }
        }
        
        // Transparency - whether to treat alpha values as shape or transparency
        else if(key.equals("AIS")) {
            boolean ais = ((Boolean)val).booleanValue();
            if(ais != gs.alphaIsShape) { gs.alphaIsShape = ais; transparencyChanged=true; }
        }
        
        // Soft mask 
        else if(key.equals("SMask")) {
            if(val.equals("/None")) gs.softMask = null;
            else System.err.println("Soft mask being specified : "+val);
        }
        
        // Transparency - stroke alpha
        else if (key.equals("CA")) {
           float a = ((Number)val).floatValue();
           if(a != gs.salpha) { gs.alpha = a; transparencyChanged = true; }
        }
        
        // Transparency - nonstroke alpha
        else if (key.equals("ca")) {
            float a = ((Number)val).floatValue();
            if (a != gs.alpha) { gs.alpha = a; transparencyChanged = true; }
        }
        // Some other possible entries in this dict that are not currently handled include:
        // Font, BG, BG2, UCR, UCR2, OP, op, OPM, TR, TR2, HT, FL, SM, SA,TK
    }
   
    // cache a new stroke object
    if(strokeChanged)
        gs.lineStroke = gs.createStroke();
    
    // cache new composite objects if necessary
    if(transparencyChanged) {
        gs.composite = PDFComposite.createComposite(gs.colorSpace, gs.blendMode, gs.alphaIsShape, gs.alpha);
        gs.scomposite = PDFComposite.createComposite(gs.colorSpace, gs.blendMode, gs.alphaIsShape, gs.salpha);
    }
}

}