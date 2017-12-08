package snap.pdf.read;
import java.util.List;
import snap.gfx.*;
import snap.pdf.*;

/**
 * A class to paint PDF operators.
 */
public class PagePainter {
    
    // The Page
    PDFPage              _page;
    
    // The Painter
    Painter              _pntr;
    
    // The Text renderer
    PageText             _text = new PageText(this);
    
    // The tokens
    List <PageToken>     _tokens;
    
    // The current token index
    int                  _index;
    
    // The current path
    Path                 _path = new Path();
    
    // The stroke color
    Color                _scolor = Color.BLACK;

/**
 * Paints the given page.
 */
public void paint(PDFPage aPage, Painter aPntr)
{
    // Get page contents stream and stream bytes (decompressed/decoded)
    _page = aPage;
    PDFStream pstream = aPage.getPageContentsStream(); if(pstream==null) return;
    byte pbytes[] = pstream.decodeStream();
    
    // Create top-level list of tokens and run the lexer to fill the list
    List tokens = PageToken.getTokens(pbytes);
    paint(tokens, aPntr);
}

/**
 * Paints the given tokens.
 */
public void paint(List <PageToken> theTokens, Painter aPntr)
{
    _tokens = theTokens;
    _pntr = _text._pntr = aPntr;
    paint();
}

/**
 * Returns the token at given index.
 */
PageToken getToken(int anIndex)  { return _tokens.get(anIndex); }

/**
 * Top level paint.
 */
protected void paint()
{
    for(int i=0,iMax=_tokens.size();i<iMax;i++) { PageToken tok = getToken(i); _index = i;
        if(tok.type==PageToken.PDFOperatorToken)
            paintOp(tok, i);
    }
}

/**
 * Paints the given op token at given index.
 */
protected void paintOp(PageToken aToken, int anIndex)
{
    String op = aToken.getString();
    
    switch(op) {
        case "b": b(); break; // Closepath, fill and stroke path
        case "b*": b_x(); break; // Closepath, fill and stroke path (even-odd rule)
        case "B": B(); break; // Fill and stroke path
        case "B*": B_x(); break; // Fill and stroke path (even-odd rule)
        case "BT": BT(); break; // Begin text
        case "c": c(); break; // Curveto
        case "cm": cm(); break; // Concat matrix
        case "ET": ET(); break; // End text
        case "f": f(); break; // Fill path
        case "f*": f_x(); break; // Fill path
        case "F": f(); break; // Fill path (obsolete)
        case "g": g(); break; // Set gray for nonstroking
        case "G": G(); break; // Set gray for stroking
        case "h": h(); break; // Closepath
        case "l": l(); break; // lineto
        case "m": m(); break; // moveto
        case "n": n(); break; // End path
        case "q": q(); break; // gsave
        case "Q": Q(); break; // grestore
        case "re": re(); break; // grestore
        case "rg": rg(); break; // set rgb color for nonstroking
        case "RG": RG(); break; // set rgb color for stroking
        case "s": s(); break; // Closepath, stroke
        case "S": S(); break; // Stroke
        //case "sc": sc(); break; // Set color nonstroking
        //case "SC": SC(); break; // Set color stroking
        case "T*": T_x(); break; // Text move to
        case "Tc": Tc(); break; // Set char spacing
        case "Td": Td(); break; // Text move to
        case "TD": TD(); break; // Text move to
        case "Tf": Tf(); break; // Set font
        case "Tj": Tj(); break; // Show text
        case "TJ": TJ(); break; // Show text (list)
        case "TL": TL(); break; // Set text leading
        case "Tm": Tm(); break; // Set text matrix
        case "Ts": Ts(); break; // Set text rise
        case "Tw": Tw(); break; // Set word spacing
        case "Tz": Tz(); break; // Set text horizontal scale
        case "w": w(); break; // Set linewidth
        default: System.out.println("Unsupported op: " + op);
    }
}

/**
 * Closepath, fill, stroke.
 */
void b()  { _path.close(); B(); }

/**
 * Fill and stroke path.
 */
void B()
{
    //_path.setWindingRule(GeneralPath.WIND_NON_ZERO);
    _pntr.fill(_path);
    _pntr.setColor(_scolor);
    _pntr.draw(_path);
    _path.clear();
}

/**
 * Closepath, fill, stroke (even-odd rule).
 */
void b_x()  { _path.close(); B_x(); }

/**
 * Fill and stroke path (even-odd rule).
 */
void B_x()
{
    //_path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
    _pntr.fill(_path);
    _pntr.setColor(_scolor);
    _pntr.draw(_path);
    _path.clear();
}

/**
 * BeginText.
 */
void BT()  { _text.begin(); }

/**
 * Curveto.
 */
void c()
{
    double xc0 = getFloat(_index-6), yc0 = getFloat(_index-5);
    double xc1 = getFloat(_index-4), yc1 = getFloat(_index-3);
    double x1 = getFloat(_index-2), y1 = getFloat(_index-1);
    _path.curveTo(xc0, yc0, xc1, yc1, x1, y1);
}

/**
 * Set colorspace
 */
void cs()
{
    //String space = getToken(_index-1).getName();
    //gs.colorSpace = PDFColorSpace.getColorspace(space, _pfile, _page);
}

/**
 * Set stroke colorspace
 */
void CS()
{
    //String space = getToken(_index-1).getName();
    //gs.scolorSpace = PDFColorSpace.getColorspace(space, _pfile, _page);
}

/**
 * Concat matrix.
 */
void cm()
{
    Transform xfm = getTransform(_index);
    _pntr.transform(xfm);
}

/**
 * BeginText.
 */
void ET()  { _text.end(); }

/**
 * Fill.
 */
void f()
{
    //_path.setWindingRule(GeneralPath.WIND_NON_ZERO);
    _pntr.fill(_path);
    _path.clear();
}

/**
 * Fill path (even-odd rule).
 */
void f_x()
{
    //_path.setWindingRule(WIND_EVEN_ODD);
    _pntr.fill(_path);
    _path.clear();
}

/**
 * Set gray for nonstroking.
 */
void g()
{
    //ColorSpace cspace = PDFColorSpace.getColorspace("DeviceGray", _pfile, _page);
    //gs.color = getColor(cspace,_index,numops); gs.colorSpace = cspace;
    _pntr.setColor(getGray(_index));
}

/**
 * Set gray for stroking
 */
void G()
{
    //ColorSpace cspace = PDFColorSpace.getColorspace("DeviceGray", _pfile, _page);
    //gs.scolor = getColor(cspace,_index,numops); gs.scolorSpace = cspace;
    _scolor = getGray(_index);
}

/**
 * Extended graphics state
 */
void gs()
{
    //Map exg = getExtendedGStateNamed(getToken(_index-1).getName());
    //readExtendedGState(gs, exg);
}

/**
 * Closepath
 */
void h()
{
    _path.close();
    //Point2D lastPt = path.getCurrentPoint(); gs.cp.x = (float)lastPt.getX(); gs.cp.y = (float)lastPt.getY();
}

/**
 * Set flatness
 */
void i()  { } //gs.flatness = getFloat(_index-1);

/**
 * ID
 */
void ID()  { }

/**
 * Set linejoin
 */
void j()  { } //gs.lineJoin = getInt(_index-1); gs.lineStroke = gs.createStroke();

/**
 * Set linecap
 */
void J()  { } //gs.lineCap = getInt(_index-1); gs.lineStroke = gs.createStroke();

/**
 * Lineto.
 */
void l()
{
    double x = getFloat(_index-2), y = getFloat(_index-1);
    _path.lineTo(x,y);
}

/**
 * Moveto.
 */
void m()
{
    double x = getFloat(_index-2), y = getFloat(_index-1);
    _path.moveTo(x,y);
}

/**
 * End path: End path without fill or stroke - used for clearing the path after a clipping operation ( W n )
 */
void n()  { _path.clear(); }

/**
 * GSave.
 */
void q()  { _pntr.save(); }

/**
 * GRestore.
 */
void Q()  { _pntr.restore(); }

/**
 * Rectangle.
 */
void re()
{
    float x = getFloat(_index-4), y = getFloat(_index-3);
    float w = getFloat(_index-2), h = getFloat(_index-1);
    _path.moveTo(x,y); _path.lineTo(x+w,y); _path.lineTo(x+w,y+h); _path.lineTo(x,y+h); _path.close();
}

/**
 * Set color.
 */
void rg()
{
    //cspace = PDFColorSpace.getColorspace("DeviceRGB", _pfile, _page);
    Color color = getColor(_index); //cspace,i,numops);
    _pntr.setPaint(color);
    //gs.colorSpace = cspace;
}

/**
 * Set stroke color.
 */
void RG()
{
    //cspace = PDFColorSpace.getColorspace("DeviceRGB", _pfile, _page);
    _scolor = getColor(_index); //cspace,i,numops);
}

/**
 * Closepath, stroke.
 */
void s()
{
    _path.close();
    _pntr.setColor(_scolor);
    _pntr.draw(_path);
    _path.clear();
}

/**
 * Stroke.
 */
void S()
{
    _pntr.setColor(_scolor);
    _pntr.draw(_path);
    _path.clear();
}

/**
 * Set color nonstroking.
 */
void sc()
{
    Color color = getColor(_index); //cspace,i,numops);
    _pntr.setPaint(color);
}

/**
 * Set color stroking.
 */
void SC()
{
    _scolor = getColor(_index); //cspace,i,numops);
}

/**
 * Move to next line
 */
void T_x()
{
    _text.positionText(0, -_text._leading); //_text.positionText(0, -gs.tleading);
    //System.out.println("Move to next line");
}

/**
 * Set character spacing
 */
void Tc()  { _text._charSpc = getFloat(_index-1); } //gs.tcs = getFloat(_index-1);

/**
 * Text move relative to current line start
 */
void Td()
{
    float x = getFloat(_index-2);
    float y = getFloat(_index-1);
    _text.positionText(x, y); //_text.positionText(x,y);
}

/**
 * Text move relative to current line start (uppercase indicates to set leading to -ty)
 */
void TD()
{
    float x = getFloat(_index-2);
    float y = getFloat(_index-1);
    _text.positionText(x,y); //_text.positionText(x,y);
    _text._leading = -y;
}

/**
 * Set font.
 */
void Tf()
{
    String fontalias = getToken(_index-2).getName(); // name in dict is key, so lose leading /
    float fsize = getFloat(_index-1);
    //gs.font = _page.getFontDictForAlias(fontalias);
    Font font = Font.Arial12.deriveFont(fsize);
    _pntr.setFont(font);
}

/**
 * Show Text.
 */
void Tj()
{
    PageToken tok = getToken(_index-1);
    String str = tok.getString();
    _text.showText(str); //pageBytes, tloc, tlen, gs, _pfile, _pntr);
}

/**
 * Show text.
 */
void TJ()
{
    PageToken token = getToken(_index-1);
    List <PageToken> tokens = (List)token.value;
    _text.showText(tokens);
}

/**
 * Set text leading
 */
void TL()  { _text._leading = getFloat(_index-1); }

/**
 * Set text matrix.
 */
void Tm()
{
    float a = getFloat(_index-6), b = getFloat(_index-5), c = getFloat(_index-4), d = getFloat(_index-3);
    float tx = getFloat(_index-2), ty = getFloat(_index-1);
    _text.setTextMatrix(a, b, c, d, tx, ty);
}

/**
 * Set text rise
 */
void Ts()
{
    _text._rise = getFloat(_index-1);
    if(_text._rise!=0) System.out.println("PagePainter: Non-zero rise");
}

/**
 * Set text word spacing
 */
void Tw()  { _text._wordSpc = getFloat(_index-1); }

/**
 * Set text horizontal scale
 */
void Tz()  { _text._horScale = getFloat(_index-1)/100f; }

/**
 * Set linewidth.
 */
void w()
{
    double lwidth = getFloat(_index-1);
    _pntr.setStroke(new Stroke(lwidth));
}

/**
 * Clip.
 */
void W()
{
    //int wind = GeneralPath.WIND_NON_ZERO; // if ( W* ) wind = GeneralPath.WIND_EVEN_ODD;
                
    // Somebody at Adobe's been smoking crack. The clipping operation doesn't modify the clipping in the gstate.
    // Instead, the next path drawing operation will do that, but only AFTER it draws.  
    // So a sequence like 0 0 99 99 re W f will fill the rect first and then set the clip path using the rect.
    // Because the W operation doesn't do anything, they had to introduce the 'n' operation, which is a drawing no-op,
    // in order to do a clip and not also draw the path. You might think it would be safe to just reset the clip here,
    // since the path it will draw is the same as the path it will clip to. However, there's at least one (admittedly
    // obscure) case I can think of where clip(path),draw(path)  is different from draw(path),clip(path): 
    //     W* f  %eoclip, nonzero-fill
    // Note Also, Acrobat considers it an error to have a W not immediately followed by drawing op (f,f*,F,s,S,B,b,n)
    //if(_path != null) {
    //    _path.setWindingRule(wind);
    //    future_clip = (GeneralPath)path.clone(); }
}

/**
 * Returns float value for token at given index.
 */
float getFloat(int i)  { return getToken(i).floatValue(); }

/**
 * Returns transform value for the 6 tokens before given index.
 */
Transform getTransform(int i)
{
    float a = getFloat(i-6), b = getFloat(i-5), c = getFloat(i-4), d = getFloat(i-3);
    float tx = getFloat(i-2), ty = getFloat(i-1);
    return new Transform(a, b, c, d, tx, ty);
}

/**
 * Called with any of the set color operations to create new color instance from the values in the stream.
 * Currently considers having the wrong number of components an error.
 */
Color getColor(int ind)  //ColorSpace space
{
    int n = 3; //space.getNumComponents();
    float comp[] = new float[n]; // how much of a performance hit is allocating this every time?
    for(int i=0; i<n; ++i) comp[i] = getFloat(ind-(n-i));
    //return PDFColorSpace.createColor(space, varray);
    return new Color(comp[0], comp[1], comp[2]);
}

/**
 * Called with any of the set color operations to create new color instance from the values in the stream.
 * Currently considers having the wrong number of components an error.
 */
Color getGray(int ind)  { float g = getFloat(ind-1); return new Color(g); }

}