package snap.pdf.read;
import java.util.List;
import snap.gfx.*;
import snap.pdf.*;

/**
 * A class to paint PDF operators.
 */
public class PagePainter {
    
    // The Painter
    Painter              _pntr;
    
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
    _pntr = aPntr;
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
        case "b": b(); break; // Closepath, fill, stroke
        case "c": c(); break; // Curveto
        case "cm": cm(); break; // Concat matrix
        case "f": f(); break; // Fill path
        case "F": f(); break; // Fill path (obsolete)
        case "h": h(); break; // Closepath
        case "l": l(); break; // lineto
        case "m": m(); break; // moveto
        case "q": q(); break; // gsave
        case "Q": Q(); break; // grestore
        case "re": re(); break; // grestore
        case "rg": rg(); break; // set rgb color
        case "RG": RG(); break; // set stroke rgb color
        case "s": s(); break; // Closepath, stroke
        case "S": S(); break; // Stroke
        case "w": w(); break; // Set linewidth
        default: System.out.println("Unsupported op: " + op);
    }
}

/**
 * Closepath, fill, stroke.
 */
void b()
{
    //if(tlen==1) path.setWindingRule(GeneralPath.WIND_NON_ZERO); // b
    //else if(tlen==2 && pageBytes[tstart+1] =='*') path.setWindingRule(GeneralPath.WIND_EVEN_ODD); // b*
    //else break;
    _path.close();
    _pntr.fill(_path);
    _pntr.setColor(_scolor);
    _pntr.draw(_path);
    _path.clear();
}

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
 * Concat matrix.
 */
void cm()
{
    Transform xfm = getTransform(_index);
    _pntr.transform(xfm);
}

/**
 * Fill.
 */
void f()
{
    //if(tlen==1) path.setWindingRule(GeneralPath.WIND_NON_ZERO);
    //else if(tlen==2 && pageBytes[tstart+1]=='*') path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
    _pntr.fill(_path);
    _path.clear();
}

/**
 * Closepath.
 */
void h()  { _path.close(); }

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
 * Set linewidth.
 */
void w()
{
    double lwidth = getFloat(_index-1);
    _pntr.setStroke(new Stroke(lwidth));
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

}