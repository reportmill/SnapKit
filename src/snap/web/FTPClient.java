/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import snap.util.FilePathUtils;
import sun.net.ftp.*;

/**
 * A class to establish an FTP connection to get and retrieve files.
 */
public class FTPClient {

    // The host to connect to
    String         _host;
    
    // The user name of the connection
    String         _userName;
    
    // The password of the connection
    String         _password;
    
    // The current working directory found at login
    String         _loginPath;
    
    // The current working directory path
    String         _cwd;
    
    // The Sun FTPClient
    FtpClient      _ftpClient;

/**
 * Returns the host.
 */
public String getHost()  { return _host; }

/**
 * Sets the host.
 */
public void setHost(String aHost)  { _host = aHost; }

/**
 * Returns the user name of the connection.
 */
public String getUserName()  { return _userName; }

/**
 * Sets the user name of the connection.
 */
public void setUserName(String aUser)  { _userName = aUser; }

/**
 * Returns the password of the connection.
 */
public String getPassword()  { return _password; }

/**
 * Sets the password of the connection.
 */
public void setPassword(String aPassword)  { _password = aPassword; }

/**
 * Returns the current working directory found at login.
 */
public String getLoginPath()  { return _loginPath; }

/**
 * Returns the absolute path given any path (absolute, relative to root path, or relative to cwd).
 * A path is absolute if it starts with root path.
 * A path is relative to root path if it starts with "/" but not root path.
 * A path is relative to current directory if it doesn't start with "/".
 */
public String getAbsolutePath(String aPath)
{
    // If path starts with "/", make sure it starts with root path
    String path = aPath;
    if(path.startsWith("/")) {
        if(!path.startsWith(getLoginPath()))
            path = FilePathUtils.getChild(getLoginPath(), path);
    }
    
    // If relative path, append current path
    else path = FilePathUtils.getChild(getCurrentDirectory(), path);
    
    // Standardize path and return
    path = FilePathUtils.getStandardized(path);
    return path;
}

/**
 * Returns whether client is connected.
 */
public boolean isConnected()  { return _ftpClient!=null; }

/**
 * Sets whether client is connected.
 */
public void setConnected(boolean aValue) throws FtpProtocolException, IOException
{
    // If value already set, just return (try a no-op if connected, so we can try re-connect on failure/timeout)
    if(aValue==isConnected())
        try { if(aValue) _ftpClient.noop(); return; }
        catch(Exception e) { _ftpClient = null; }
    
    // If no UserName or Password, throw authentication exception
    if(getUserName()==null || getUserName().length()==0)
        throw new RuntimeException("No UserName provided");
    if(getPassword()==null || getPassword().length()==0)
        throw new RuntimeException("No Password provided");
    if(getHost()==null || getHost().length()==0)
        throw new RuntimeException("No Host provided");
    
    // Catch exceptions
    try {

        // If connecting, connect
        if(aValue) {
                
            // Log connection attempt
            println("Connecting to host " + getHost());
            
            // Get new client and login with user/password
            _ftpClient = FtpClient.create(getHost());
            _ftpClient.login(getUserName(), getPassword().toCharArray());
            
            // Log welcome message
            if(_ftpClient.getWelcomeMsg()!=null)
                println(_ftpClient.getWelcomeMsg().trim());
            else println("User " + getUserName() + " login OK");
            
            // Get login path
            _loginPath = _cwd = FilePathUtils.getStandardized(_ftpClient.getWorkingDirectory());
            
            // Set binary mode
            _ftpClient.setBinaryType();
        }
            
        // Otherwise, disconnect
        else { _ftpClient.close(); _ftpClient = null; }
    }
    
    // Handle disconnect failure
    catch(FtpProtocolException e) { _ftpClient = null; throw e; }
    catch(IOException e) { _ftpClient = null; throw e; }
}

/**
 * Returns the current directory.
 */
public String getCurrentDirectory()  { return _cwd; }

/**
 * Changes directory to given path.
 */
public void setCurrentDirectory(String aPath) throws FtpProtocolException, IOException
{
    // Get path (if already set, just return)
    String path = getAbsolutePath(aPath);
    
    // Change to directory
    println("CD " + aPath); // Log change
    _ftpClient.changeDirectory(path);
    _cwd = path;
}

/**
 * Returns the bytes for a path.
 */
public byte[] getBytes(String aPath) throws FtpProtocolException, IOException
{
    // Log byte transfer
    println("GET " + aPath);
    
    // Get input stream, create buffer, read bytes into buffer
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    InputStream stream = _ftpClient.getFileStream(aPath);
    byte buffer[] = new byte[8192];
    while(true) {
        int bytesRead = stream.read(buffer, 0, buffer.length);
        if(bytesRead>=0)
            byteStream.write(buffer, 0, bytesRead);
        else break;
    }
        
    // Close stream
    stream.close();
    
    // Return bytes
    return byteStream.toByteArray();
}

/**
 * Sets the bytes for a path.
 */
public void setBytes(String aPath, byte theBytes[]) throws FtpProtocolException, IOException
{
    // Log byte transfer
    println("PUT " + aPath + " (" + theBytes.length + " bytes)");
    
    // Get output stream, write bytes and close
    OutputStream out = _ftpClient.putFileStream(aPath);
    out.write(theBytes, 0, theBytes.length);
    out.close();
}

/**
 * Deletes the file at given path.
 */
public void delete(String aPath) throws FtpProtocolException, IOException
{
    String path = getAbsolutePath(aPath);
    _ftpClient.deleteFile(path);
}

/**
 * Returns the bytes length.
 */
public FileInfo getFileInfo(String aPath) throws FtpProtocolException, IOException
{
    List <FileInfo> fileInfos = getFileInfos(aPath);
    return fileInfos.size()>0? fileInfos.get(0) : null;
}

/**
 * Returns the bytes length.
 */
public List <FileInfo> getFileInfos(String aPath) throws FtpProtocolException, IOException
{
    // Get FtpDirEntry iterator for file(s) at path
    String path = getAbsolutePath(aPath);
    
    // Get files - FILE_ACTION_NOT_TAKEN seems to indicate that file doesn't exist
    Iterator <FtpDirEntry> iterator;
    try { iterator = _ftpClient.listFiles(path); }
    catch(FtpProtocolException e) {
        FtpReplyCode code = e.getReplyCode();
        if(code==FtpReplyCode.FILE_ACTION_NOT_TAKEN) return new ArrayList(); // If unavailable, return empty
        System.err.println(e.getReplyCode());
        throw e;
    }
    
    // Create FileInfo list for FtpDirEntry(s)
    List <FileInfo> fileInfos = new ArrayList();
    while(iterator.hasNext()) { FtpDirEntry entry = iterator.next();
        if(entry.getName().equals("..")) continue;
        FileInfo finfo = new FileInfo();
        finfo.name = entry.getName();
        finfo.directory = entry.getType()==FtpDirEntry.Type.DIR;
        finfo.lastModified = entry.getLastModified().getTime();
        finfo.size = entry.getSize();
        fileInfos.add(finfo);
    }

    // Return FileInfos
    return fileInfos;
}

/**
 * Creates a directory for given path.
 */
public void mkdir(String aPath) throws FtpProtocolException, IOException
{
    String path = getAbsolutePath(aPath);
    _ftpClient.makeDirectory(path);
}

/**
 * Removes a remote directory.
 */
public void rmdir(String aPath) throws FtpProtocolException, IOException
{
    String path = getAbsolutePath(aPath);
    _ftpClient.removeDirectory(path);
}

/**
 * File Info for an FTP file listing.
 */
public class FileInfo {

    // The file name
    String      name;
    
    // Whether file is directory
    boolean     directory;
    
    // The file size
    long        size;
    
    // Last modified
    long        lastModified;
}

/**
 * Prints a message with a newline.
 */
public void println(String aMessage) { System.out.println(aMessage); }

/**
 * Test.
 */
public static void main2(String args[]) throws Exception
{
    FTPClient ftp = new FTPClient();
    ftp.setHost(args[0]); ftp.setUserName(args[1]); ftp.setPassword(args[2]);
    ftp.setConnected(true);
    List <FileInfo> finfos = ftp.getFileInfos("support");
    for(FileInfo fi : finfos) System.out.println("\t" + fi.name + " (" + fi.size + ")" + (fi.directory? " dir" : ""));
    byte bytes[] = ftp.getBytes("/index.html"); String string = new String(bytes);
    System.out.println(bytes.length>0? string : "Empty file");
    bytes = ftp.getBytes("/product/index.html"); string = new String(bytes);
    System.out.println(bytes.length>0? string : "Empty file");
    ftp.setConnected(false);
}

}