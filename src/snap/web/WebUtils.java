package snap.web;

/**
 * Utility methods for Web classes.
 */
public class WebUtils {

/**
 * Returns the common ancestor of this file and given file.
 */
public static WebFile getCommonAncestor(WebFile aFile1, WebFile aFile2)
{
    // Get this file directory and given file directory and return either if they are equal or root
    WebFile dir = aFile1.isDir()? aFile1 : aFile1.getParent();
    WebFile fileDir = dir.isRoot()? dir : aFile2.isDir()? aFile2 : aFile2.getParent();
    if(dir==fileDir)
        return dir;
    
    // Iterate up file's parents and return any equal to this file's directory
    for(WebFile file=fileDir.getParent(); !file.isRoot(); file=file.getParent())
        if(file==dir)
            return dir;
    
    // If not found, try again with directory parent
    return getCommonAncestor(dir.getParent(), fileDir);
}

/**
 * Sets a URL in a site.
 */
public static void setSiteURL(WebSite aSite, WebURL aURL)
{
    if(aSite.getURL()!=null) throw new RuntimeException("WebUtils.setURL(site,url): Oh no you don't");
    aSite.setURL(aURL);
}

}