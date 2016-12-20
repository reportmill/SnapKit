package snap.pdf.write;
import snap.gfx.*;
import snap.pdf.PDFWriter;
import snap.util.SnapUtils;

/**
 * PDFWriter utility methods for writing text. This would be a simple matter of using the PDF set-font and show-text
 * operators, except that we need to embed PDF type3 fonts (really char paths) for all chars printed in non-standard
 * fonts. We do this by tracking used chars in the PDFText.FontEntry class. Used chars in the ASCII range (0-255)
 * make up a base font "Font0", while chars beyond 255 get written out as separate PDF fonts for each block of 256
 * ("Font0.1", "Font0.2", etc.).
 */
public class PDFWriterText {

/**
 * Writes the given text run.
 */
public static void writeText(PDFWriter aWriter, TextBox aTextBox)
{
    // If we couldn't render all the text in bounds, perform clip
    //if(layout.endIndex()<layout.length()) { aWriter.print("0 0 "); aWriter.print(aTextBox.getWidth());
    //    aWriter.print(' '); aWriter.print(aTextBox.getHeight()); aWriter.println(" re W n"); }
    
    // Get page writer
    PDFPageWriter pwriter = aWriter.getPageWriter();
    
    // Flip coordinate system since pdf font transforms are flipped
    pwriter.gsave();
    pwriter.append("1 0 0 -1 0 ");
    pwriter.append(aTextBox.getHeight());
    pwriter.appendln(" cm");
    
    // Output PDF begin text operator
    pwriter.appendln("BT");

    // Run iteration variables
    PDFPageWriter underliner = null;
    
    // Get text shape and textLayout for text and start it
    TextBoxRun lastRun = null;

    // Iterate over runs
    for(TextBoxLine line : aTextBox.getLines())
    for(TextBoxRun run : line.getRuns()) {
        
        // If below text, bail
        if(line.getY()>aTextBox.getHeight()) break;
        
        // Write standard run
        writeRun(aWriter, aTextBox, line, run, lastRun);
        
        // If underlining, get PDFPageBuffer for underlining ops (to be added after End Text)
        TextStyle style = run.getStyle();
        if(style.isUnderlined()) {
            if(underliner == null) underliner = new PDFPageWriter(null, aWriter);
            underliner.setStrokeColor(style.getColor());
            underliner.setStrokeWidth((float)line.getUnderlineStroke());
            underliner.moveTo((float)run.getX(), (float)line.getBaseline() - (float)line.getUnderlineY());
            underliner.lineTo((float)run.getMaxX(), (float)line.getBaseline() - (float)line.getUnderlineY());
            underliner.appendln("S");
        }
        lastRun = run;
    }
    
    // End Text
    pwriter.appendln("ET");
    
    // Restore unflipped transform
    pwriter.grestore();
    
    // If there was any underlining, add underlining ops
    if(underliner != null)
        pwriter.append(underliner);
}

/**
 * Writes the given text run.
 */
public static void writeRun(PDFWriter aWriter, TextBox aText, TextBoxLine aLine, TextBoxRun aRun, TextBoxRun aLastRun)
{
    // Get pdf page
    PDFPageWriter pPage = aWriter.getPageWriter();
    TextStyle style = aRun.getStyle();
    TextStyle lastStyle = aLastRun!=null? aLastRun.getStyle() : null;
    
    // If colorChanged, have writer setFillColor
    if(lastStyle==null || !lastStyle.getColor().equals(style.getColor()))
        pPage.setFillColor(aRun.getColor());
        
    // Get last x & y
    double lastX = aLastRun==null? 0 : aLastRun.getX();
    double lastY = aLastRun==null? aText.getHeight() : aLastRun.getLine().getBaseline();
        
    // Set the current text point
    double runX = aRun.getX() - lastX;
    double runY = lastY - aLine.getBaseline(); // Flip y coordinate
    pPage.append(runX).append(' ').append(runY).appendln(" Td");
    
    // Get current run font, whether FontChanged and current font entry (base font entry for font, if font has changed)
    Font font = style.getFont();
    boolean fontChanged = lastStyle==null || !lastStyle.getFont().equals(style.getFont());
    PDFFontEntry fontEntry = fontChanged? aWriter.getFontEntry(font, 0) : aWriter.getFontEntry();
    
    // If char spacing has changed, set charSpace
    if(style.getCharSpacing() != (aLastRun==null? 0 : lastStyle.getCharSpacing())) {
        pPage.append(style.getCharSpacing());
        pPage.appendln(" Tc");
    }
    
    // If run outline has changed, configure text rendering mode
    if(!SnapUtils.equals(style.getBorder(), aLastRun==null? null : lastStyle.getBorder())) {
        Border border = style.getBorder();
        if(border==null)
            pPage.appendln("0 Tr");
        else {
            pPage.setStrokeColor(border.getColor());
            pPage.setStrokeWidth((float)border.getWidth());
            if(aRun.getColor().getAlpha()>0) {
                pPage.setFillColor(style.getColor());
                pPage.appendln("2 Tr");
            }
            else pPage.appendln("1 Tr");
        }
    }
    
    // Get length - just return if zero
    int length = aRun.length(); if(length==0) return;
    
    // Iterate over run chars
    for(int i=0; i<length; i++) { char c = aRun.charAt(i);
        
        // If char is less than 256, just mark it present in fontEntry chars
        if(c<256) {
            fontEntry._chars[c] = true;
            if(fontEntry.getCharSet()!=0) {
                fontChanged = true;
                fontEntry = aWriter.getFontEntry(font, 0);
            }
        }
        
        // If char beyond 255, replace c with its index in fontEntry uchars array (add it if needed)
        else {
            
            // Get index of chars
            int index = fontEntry._uchars.indexOf(c);
            
            // If char not found, add it
            if(index<0) {
                index = fontEntry._uchars.size();
                fontEntry._uchars.add(c);
            }
            
            // If char set changed, reset font entry
            if(fontEntry.getCharSet() != index/256 + 1) {
                fontChanged = true;
                fontEntry = aWriter.getFontEntry(font, index/256 + 1);
            }
            
            // Replace char with index
            c = (char)(index%256);
        }
        
        // If font changed, end current text show block, set new font, and start new text show block
        if(fontChanged) {
            if(i>0) pPage.appendln(") Tj");
            pPage.append('/'); pPage.append(fontEntry.getPDFName());
            pPage.append(' '); pPage.append(font.getSize()); pPage.appendln(" Tf");
            pPage.append('(');
            aWriter.setFontEntry(fontEntry);
            fontChanged = false;
        }
        
        // If first char, open paren
        else if(i==0)
            pPage.append('(');
        
        // Handle special chars for PDF string (might need to do backspace (\b) and form-feed (\f), too)
        if(c=='\t') { pPage.append("\\t"); continue; }  // Used to be if(aWriter.getIncludesNewlines())
        if(c=='\n') { pPage.append("\\n"); continue; }  // This too
        if(c=='\r') { pPage.append("\\r"); continue; }  // This too
        if(c=='(' || c==')' || c=='\\')
            pPage.append('\\');
            
        // Write the char
        pPage.append(c);
    }
    
    // If run is hyphenated, add hyphen
    if(aRun.isHyphenated()) pPage.append('-');
    
    // End last text show block
    pPage.appendln(") Tj");
}

}