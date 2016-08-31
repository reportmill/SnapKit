package snap.view;
import snap.util.*;
import snap.web.*;

/**
 * A custom class.
 */
public class ViewChecker {

/**
 * Check files.
 */
public static void main(String args[])
{
    WebURL url = WebURL.getURL("/Users/jeff/SnapCode/SnapStudio/src/studio/app");
    WebFile dir = url.getFile();
    
    for(WebFile file : dir.getFiles())
        if(file.getType().equals("snp")) {
            testFile(file); break; }
    System.exit(0);
}

/**
 * Tests a file.
 */
public static void testFile(WebFile aFile)
{
    ViewArchiver varch = new ViewArchiver(); ViewArchiver.setUseRealClass(false);
    View view = varch.getParentView(aFile);
    XMLElement xml = varch.writeObject(view);
    
    WebFile out0 = WebURL.getURL("/tmp/Out0.snp").createFile(false);
    out0.setBytes(aFile.getBytes()); out0.save();
    
    WebFile out1 = WebURL.getURL("/tmp/Out1.snp").createFile(false);
    out1.setBytes(xml.getBytes()); out1.save();
    
    View view2 = varch.getParentView(out1);
    XMLElement xml2 = varch.writeObject(view2);
    WebFile out2 = WebURL.getURL("/tmp/Out2.snp").createFile(false);
    out2.setBytes(xml2.getBytes()); out2.save();
    
    System.err.println("Writing " + aFile);
}

}