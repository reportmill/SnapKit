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
    WebURL url = WebURL.getURL("/Users/jeff/SnapCode/SnapCode/src/snap/app");
    WebFile dir = url.getFile();
    
    for(WebFile file : dir.getFiles())
        if(file.getType().equals("snp")) {
            testFile(file); }
    System.exit(0);
}

/**
 * Tests a file.
 */
public static void testFile(WebFile aFile)
{
    System.err.println("Writing " + aFile);
    
    // Read original file
    ViewArchiver varch = new ViewArchiver(); ViewArchiver.setUseRealClass(false);
    View view = varch.getParentView(aFile);
    
    // Write to new version
    XMLElement xml = varch.writeObject(view);
    byte bytes1[] = xml.getBytes();
    
    // Read new version and write again
    View view2 = varch.getParentView(bytes1);
    XMLElement xml2 = varch.writeObject(view2);
    byte bytes2[] = xml2.getBytes();
    
    // Write original file
    WebFile out0 = WebURL.getURL("/tmp/Out0/" + aFile.getName()).createFile(false);
    out0.setBytes(aFile.getBytes()); out0.save();
    
    // Write new file
    WebFile out1 = WebURL.getURL("/tmp/Out1/" + aFile.getName()).createFile(false);
    out1.setBytes(bytes1); out1.save();
    
    // Write new file again
    WebFile out2 = WebURL.getURL("/tmp/Out2/" + aFile.getName()).createFile(false);
    out2.setBytes(bytes2); out2.save();
}

}