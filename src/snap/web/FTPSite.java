package snap.web;
import java.util.*;
import snap.util.StringUtils;

/**
 * A data source built on top of FTP.
 */
public class FTPSite extends WebSite {

    // The FTP client
    FTPClient       _ftpClient;
    
/**
 * Returns the ftp client.
 */
protected FTPClient getFTPClient()  { return _ftpClient!=null? _ftpClient : (_ftpClient=createFTPClient()); }

/**
 * Creates the FTPClient.
 */
protected FTPClient createFTPClient()
{
    FTPClient ftp = new FTPClient();
    ftp.setHost(getHostName());
    ftp.setUserName(getUserName());
    ftp.setPassword(getPassword());
    return ftp;
}

// Override to clear ftp client
public void setUserName(String aName) { super.setUserName(aName); _ftpClient = null; }
public void setPassword(String aPassword)  { super.setPassword(aPassword); _ftpClient = null; }

/**
 * Returns the file at path.
 */
protected FileHeader getFileHeader(String aPath)
{
    // Make sure we're connected
    FTPClient ftpc = getFTPClient(); try { ftpc.setConnected(true); }
    catch(Exception e) { throw new AccessException(this, e); }
    
    // Get path
    String ftpPath = getFtpPath(aPath);
    
    // Get file info for path
    FTPClient.FileInfo fileInfo;
    try { fileInfo = ftpc.getFileInfo(ftpPath); }
    catch(Exception e) { throw new RuntimeException(e); }
    if(fileInfo==null) return null;
    
    FileHeader file = new FileHeader(aPath, fileInfo.directory);
    file.setLastModifiedTime(fileInfo.lastModified);
    return file;
}

/**
 * Returns files at path.
 */
public List <FileHeader> getFileHeaders(String aPath)
{
    // Make sure we're connected
    FTPClient ftpc = getFTPClient(); try { ftpc.setConnected(true); }
    catch(Exception e) { throw new AccessException(this, e); }
    
    // Get FtpPath
    String ftpPath = getFtpPath(aPath);
    
    // Get file infos for path from ftp client
    List <FTPClient.FileInfo> fileInfos;
    try { fileInfos = ftpc.getFileInfos(ftpPath); }
    catch(Exception e) { throw new RuntimeException(e); }
    
    // Create files from FTP file info
    List <FileHeader> files = new ArrayList();
    for(FTPClient.FileInfo finfo : fileInfos) {
        if(finfo.name.equals(".")) continue;
        FileHeader file = new FileHeader(StringUtils.getPathChild(aPath, finfo.name), finfo.directory);
        file.setLastModifiedTime(finfo.lastModified);
        files.add(file);
    }
    
    // Return files
    return files;
}

/**
 * Gets file bytes.
 */
public byte[] getFileBytes(String aPath)
{
    // Make sure we're connected
    FTPClient ftpc = getFTPClient(); try { ftpc.setConnected(true); }
    catch(Exception e) { throw new AccessException(this, e); }
    
    // Get bytes
    String ftpPath = getFtpPath(aPath);
    try { return ftpc.getBytes(ftpPath); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Writes file bytes.
 */
public long saveFileImpl(WebFile aFile) throws Exception
{
    // Make sure we're connected
    FTPClient ftpc = getFTPClient(); try { ftpc.setConnected(true); }
    catch(Exception e) { throw new AccessException(this, e); }
    
    // Get path and FtpPath
    String path = aFile.getPath(), ftpPath = getFtpPath(path);
    
    // If file is directory (that doesn't exist), mkdir
    if(aFile.isDir()) {
        if(!aFile.getLoaded().getExists())
            ftpc.mkdir(ftpPath); }
    
    // Otherwise write bytes
    else ftpc.setBytes(ftpPath, aFile.getBytes());
    
    // Return ModifiedTime
    FTPClient.FileInfo fileInfo = ftpc.getFileInfo(ftpPath);
    return fileInfo.lastModified;
}

/**
 * Deletes file.
 */
public void deleteFileImpl(WebFile aFile) throws Exception
{
    // Make sure we're connected
    FTPClient ftpc = getFTPClient(); try { ftpc.setConnected(true); }
    catch(Exception e) { throw new AccessException(this, e); }
    
    // Get path and FtpPath
    String path = aFile.getPath(), ftpPath = getFtpPath(path);
    
    // If directory, rmdir, otherwise delete file
    if(aFile.isDir())
        ftpc.rmdir(ftpPath);
    else ftpc.delete(ftpPath);
}

/**
 * Returns the path relative to Site path.
 */
protected String getFtpPath(String aPath)  { String path = getPath(); return path!=null? path + aPath : aPath; }

}