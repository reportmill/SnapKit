package snap.pdf;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A custom class.
 */
public class TestPDF {

public static void main(String args[])
{
    DocView doc = new DocView();
    PageView page = new PageView(); page.setPrefSize(792,612);
    doc.setPage(page);
    
    // Add Rect
    RectView rect = new RectView(320,320,200,200); rect.setFill(Color.RED); rect.setBorder(Color.BLUE,2);
    page.addChild(rect);
    
    // Add Text
    TextView text = new TextView(); text.setBounds(380,80,300,200); text.setWrapText(true); text.setRotate(-20);
    text.setText("Why is the world in love again, why are they marching hand in hand?");
    text.setFont(new Font("Arial Bold", 24)); text.setBorder(Color.GREEN, 1); text.setFill(new Color("#AACCEE33"));
    text.setRich(true);
    text.getTextBox().setStyleValue(TextStyle.UNDERLINE_KEY, 1, 20, 30);
    page.addChild(text);
    
    // Add Image
    ImageView iview = new ImageView("/Users/jeff/DesktopStack/Images/Daisy.jpg");
    iview.setBounds(36, 36, 320, 480); iview.setOpacity(.8);
    page.addChild(iview, 0);
    
    byte bytes[] = new PDFWriter().getBytes(doc);
    SnapUtils.writeBytes(bytes, "/tmp/test.pdf");
    GFXEnv.getEnv().openURL("/tmp/test.pdf");
}

}