package snap.web;
import java.io.*;
import java.util.*;
import snap.util.StringUtils;

/**
 * A WebSite for HTTP sources.
 */
public class HTTPSite extends WebSite {

/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "http"; }

/**
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws IOException
{
    // Fetch URL
    String urls = getURLString() + aPath;
    HTTPRequest req = new HTTPRequest(urls); req.setRequestMethod("HEAD");
    HTTPResponse resp  = req.getResponse();
    
    // Handle non-success response codes
    if(resp.getCode()==HTTPResponse.NOT_FOUND)
        return null; // throw new FileNotFoundException(aPath);
    if(resp.getCode()==HTTPResponse.UNAUTHORIZED)
        throw new AccessException();
    if(resp.getCode()!=HTTPResponse.OK)
        throw new IOException(resp.getMessage());
    
    // Create file, set bytes and return
    boolean isDir = StringUtils.getPathExtension(aPath).length()==0;
    FileHeader file = new FileHeader(aPath, isDir);
    file.setLastModifiedTime(resp.getLastModified());
    file.setSize(resp.getContentLength());
    return file;
}

/**
 * Gets file bytes.
 */
public byte[] getFileBytes(String aPath) throws IOException
{
    String urls = getURLString() + aPath;
    HTTPRequest req = new HTTPRequest(urls);
    HTTPResponse resp  = req.getResponse();
    return resp.getBytes();
}

/**
 * Returns files at path.
 */
public List <FileHeader> getFileHeaders(String aPath) throws IOException
{
    // Create files list
    List <FileHeader> files = getFilesFromHTML(aPath);
    if(files!=null)
        return files;
    
    // Gets files from html
    files = new ArrayList();
    
    // If ".index" file exists, load children
    WebFile indexFile = getFile(StringUtils.getPathChild(aPath, ".index"));
    if(indexFile!=null) {
        String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
        String fileEntries[] = indexFileString.split("\n");
        for(String fileEntry : fileEntries) {
            if(fileEntry.length()==0) continue;
            String fileInfo[] = fileEntry.split("\t");
            FileHeader file = new FileHeader(StringUtils.getPathChild(aPath, fileInfo[0]), false);
            files.add(file);
        }
    }
    
    // Return files
    return files;
}

/**
 * Returns files from HTML.
 */
List <FileHeader> getFilesFromHTML(String aPath) throws IOException
{
    byte bytes[] = getFileBytes(aPath);
    String text = new String(bytes);
    int htag = text.indexOf("<HTML>"); if(htag<0) return null;
    List <FileHeader> files = new ArrayList();
    
    for(int i=text.indexOf("HREF=\"", htag); i>0; i=text.indexOf("HREF=\"", i+8)) {
        int end = text.indexOf("\"", i+6); if(end<0) continue;
        String name = text.substring(i+6,end);
        if(name.length()<2 || !Character.isLetterOrDigit(name.charAt(0))) continue;
        boolean isDir = false; if(name.endsWith("/")) { isDir = true; name = name.substring(0, name.length()-1); }
        String path = StringUtils.getPathChild(aPath, name);
        FileHeader file = new FileHeader(path, isDir);
        file.setLastModifiedTime(System.currentTimeMillis());
        files.add(file);
    }
    return files;
}

/** WebSite method. */
protected long saveFileImpl(WebFile aFile) throws Exception
{
    String urls = getURLString() + aFile.getPath();
    HTTPRequest req = new HTTPRequest(urls);
    req.setBytes(aFile.getBytes());
    req.getResponse();
    return aFile.getLastModifiedTime();
}

/**
 * Override to return standard file for cache file.
 */
@Override
protected File getStandardFile(WebFile aFile)
{
    WebFile cfile = getLocalFile(aFile, false);
    return cfile.getStandardFile();
}

/**
 * Returns a local file for given file (with option to cache for future use).
 */
public WebFile getLocalFile(WebFile aFile, boolean doCache)
{
    WebFile cfile = getCacheFile(aFile.getPath());
    if(cfile.getExists() && cfile.getLastModifiedTime()>=aFile.getLastModifiedTime())
        return cfile;
    cfile.setBytes(aFile.getBytes());
    cfile.save();
    return cfile;
}

/**
 * Returns a cache file for path.
 */
public WebFile getCacheFile(String aPath)
{
    WebSite sbox = getSandbox();
    WebFile dfile = sbox.getFile("/Cache" + aPath);
    if(dfile==null) dfile = sbox.createFile("/Cache" + aPath, false);
    return dfile;
}

}