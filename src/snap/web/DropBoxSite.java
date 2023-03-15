/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Class to work with DropBox.
 */
public class DropBoxSite extends WebSite {

    // The Email for this DropBox
    private String  _email;

    // The Site path
    private String  _sitePath;

    // Constants for DropBox endpoints
    private static final String GET_METADATA = "https://api.dropboxapi.com/2/files/get_metadata";
    private static final String LIST_FOLDER = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CREATE_FOLDER = "https://api.dropboxapi.com/2/files/create_folder_v2";
    private static final String DELETE = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD = "https://content.dropboxapi.com/2/files/upload";
    private static final String GET_CONTENT = "https://content.dropboxapi.com/2/files/download";

    // Header value
    private static String _atok = "";

    // Date format
    private static DateFormat _fmt = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // Shared instances
    private static Map<String,DropBoxSite>  _dropBoxSites = new HashMap<>();

    // Default email
    private static String  _defaultEmail;

    // Constants
    private static final String DEFAULT_EMAIL = "DefaultDropBoxEmail";

    /**
     * Constructor.
     */
    private DropBoxSite(String anEmail)
    {
        // Set ivars
        _email = anEmail;
        _sitePath = getPathForEmail(anEmail);

        // Create/set URL
        String DROPBOX_ROOT = "dbox://dbox.com";
        String urls = DROPBOX_ROOT + _sitePath;
        WebURL url = WebURL.getURL(urls);
        setURL(url);
    }

    /**
     * Returns the DropBox email.
     */
    public String getEmail()  { return _email; }

    /**
     * Handles getting file info, contents or directory files.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Always do Head
        doHead(aReq, aResp);
        if (isHead)
            return;

        // If error, just return
        if (aResp.getCode() != WebResponse.OK)
            return;

        // If directory, get files
        FileHeader fhdr = aResp.getFileHeader();
        if (fhdr.isDir())
            doGetDir(aReq, aResp);

        // Otherwise, get contents
        else doGetFileContents(aReq, aResp);
    }

    /**
     * Get Head for request.
     */
    protected void doHead(WebRequest aReq, WebResponse aResp)
    {
        // If no email, set response to not found
        if (_email == null || _email.length() == 0) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Create Request
        HTTPRequest httpReq = new HTTPRequest(GET_METADATA);
        httpReq.addHeader("Authorization", "Bearer " + _atok);
        httpReq.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dropBoxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpReq, false, "path", dropBoxPath);

        // Get HTTP Response
        HTTPResponse httpResp = getResponseHTTP(httpReq, aResp);
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK)
            return;

        // Get JSON response
        JSObject json = (JSObject) httpResp.getJSON();
        if (json == null)
            return;

        // Get JSON response
        FileHeader fileHeader = createFileHeaderForJSON(json);
        fileHeader.setPath(aReq.getURL().getPath());
        aResp.setFileHeader(fileHeader);
    }

    /**
     * Get Directory listing for request.
     */
    protected void doGetDir(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest httpRequest = new HTTPRequest(LIST_FOLDER);
        httpRequest.addHeader("Authorization", "Bearer " + _atok);
        httpRequest.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dropBoxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpRequest, false, "path", dropBoxPath);

        // Get HTTP Response
        HTTPResponse httpResp = getResponseHTTP(httpRequest, aResp);
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK)
            return;

        // Get JSON response
        JSObject json = (JSObject) httpResp.getJSON();
        if (json == null) {
            System.err.println("DropBoxSite.doGetDir: null response");
            return;
        }

        // Get Entries Node, complain if not array
        JSValue entriesNode = json.getValue("entries");
        if (!(entriesNode instanceof JSArray)) {
            System.err.println("DropBoxSite.doGetDir: Unexpected response: " + entriesNode.getValueAsString());
            return;
        }

        // Get json for entries
        JSArray entriesArray = (JSArray) json.getValue("entries");
        List<JSValue> entryNodes = entriesArray.getValues();

        // Get FileHeader List for json entries
        List<FileHeader> fileHeaders = ListUtils.map(entryNodes, e -> createFileHeaderForJSON((JSObject) e));

        // Strip SitePath from FileHeaders
        for (FileHeader fileHeader : fileHeaders) {
            String filePath = fileHeader.getPath().substring(_sitePath.length());
            fileHeader.setPath(filePath);
        }

        // Set FileHeaders
        aResp.setFileHeaders(fileHeaders);
    }

    /**
     * Get file request.
     */
    protected void doGetFileContents(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest httpReq = new HTTPRequest(GET_CONTENT);
        httpReq.addHeader("Authorization", "Bearer " + _atok);

        // Add path param as JSON header
        String dropBoxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpReq, true, "path", dropBoxPath);

        // Get response
        getResponseHTTP(httpReq, aResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        WebFile file = aReq.getFile();
        if (file.isFile())
            doPutFile(aReq, aResp);
        else doPutDir(aReq, aResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPutFile(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest httpReq = new HTTPRequest(UPLOAD);
        httpReq.addHeader("Authorization", "Bearer " + _atok);
        httpReq.addHeader("Content-Type", "application/octet-stream");

        // Add path param as JSON header
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpReq, true, "path", dboxPath, "mode", "overwrite");

        // Add bytes
        byte[] bytes = aReq.getSendBytes();
        httpReq.setBytes(bytes);

        // Get HTTP Response
        HTTPResponse httpResp = getResponseHTTP(httpReq, aResp);
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK) {
            System.err.println("DropBoxSite.putFile: " + (httpResp != null ? httpResp.getMessage() : "null"));
            return;
        }

        // Get JSON response
        JSObject json = (JSObject) httpResp.getJSON();
        if (json != null) {
            String mod = json.getStringValue("server_modified");
            if (mod != null && mod.endsWith("Z")) {
                try {
                    Date date = _fmt.parse(mod);
                    aResp.setModTime(date.getTime());
                    System.out.println("Save ModTime: " + date);
                }
                catch (Exception e) { System.err.println(e); }
            }
            else System.err.println("DropBoxSite.doPutFile: Can't get save mod time: " + json);
        }
    }

    /**
     * Handle a PUT request.
     */
    protected void doPutDir(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest httpReq = new HTTPRequest(CREATE_FOLDER);
        httpReq.addHeader("Authorization", "Bearer " + _atok);
        httpReq.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dropBoxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpReq, false, "path", dropBoxPath);

        // Get HTTP Response
        HTTPResponse httpResp = getResponseHTTP(httpReq, aResp);
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK) {
            System.err.println("DropBoxSite.createFolder: " + (httpResp != null ? httpResp.getMessage() : "null"));
            return;
        }

        // Get JSON response
        JSValue json = httpResp.getJSON();
        if (json!=null)
            System.out.println(json);
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest httpReq = new HTTPRequest(DELETE);
        httpReq.addHeader("Authorization", "Bearer " + _atok);
        httpReq.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dropBoxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(httpReq, false, "path", dropBoxPath);

        // Get response
        getResponseHTTP(httpReq, aResp);
    }

    /**
     * Returns the dropbox path for URL.
     */
    private String getDropBoxPathForURL(WebURL aURL)
    {
        String filePath = aURL.getPath();
        return _sitePath + (filePath.length() > 1 ? filePath : "");
    }

    /**
     * Adds a JSON Header to given HTTP Request.
     */
    private static void addParamsToRequestAsJSON(HTTPRequest aReq, boolean asHeader, String ... thePairs)
    {
        // Create JSON Request and add pairs
        JSObject jsonReq = new JSObject();
        for (int i = 0; i < thePairs.length; i += 2)
            jsonReq.setNativeValue(thePairs[i], thePairs[i + 1]);

        // Add as header
        if (asHeader) {
            String jsonReqStr = jsonReq.toStringCompacted();
            jsonReqStr = jsonReqStr.replace("\"", "\\\"");
            jsonReqStr = jsonReqStr.replace("\\", "");
            aReq.addHeader("Dropbox-API-Arg", jsonReqStr);
        }

        // Add as send-bytes
        else {
            String jsonReqStr = jsonReq.toString();
            aReq.setBytes(jsonReqStr.getBytes());
        }
    }

    /**
     * Sends the HTTP request and loads results into WebResponse.
     */
    private static HTTPResponse getResponseHTTP(HTTPRequest aReq, WebResponse aResp)
    {
        // Get response
        HTTPResponse httpResp;
        try { httpResp = aReq.getResponse(); }
        catch (Exception e)
        {
            aResp.setException(e);
            return null;
        }

        // Copy response
        aResp.copyResponse(httpResp);
        return httpResp;
    }

    /**
     * Returns a FileHeader for DropBox File Entry JSONNode.
     */
    private static FileHeader createFileHeaderForJSON(JSObject aFileEntryNode)
    {
        // Get attributes
        String filePath = aFileEntryNode.getStringValue("path_display");
        String tag = aFileEntryNode.getStringValue(".tag");
        boolean isFile = tag.equals("file");

        // Create FileHeader
        FileHeader fileHeader = new FileHeader(filePath, !isFile);

        // Get additional file attributes
        if (isFile) {

            // Get/set size
            String sizeStr = aFileEntryNode.getStringValue("size");
            long size = Long.parseLong(sizeStr);
            fileHeader.setSize(size);

            // Get/set ModTime
            String mod = aFileEntryNode.getStringValue("server_modified");
            if (mod.endsWith("Z")) {
                try {
                    Date date = _fmt.parse(mod);
                    fileHeader.setModTime(date.getTime());
                }
                catch (Exception e) { System.err.println(e); }
            }
        }

        // Return FileHeader
        return fileHeader;
    }

    /**
     * Returns a path for email. E.G.: jack@abc.com = /com/abc/jack.
     * We're storing files at DropBox in this format.
     */
    private String getPathForEmail(String anEmail)
    {
        // Get email name
        int domainIndex = anEmail != null ? anEmail.indexOf('@') : -1;
        if (domainIndex < 0)
            return "unknown";
        String emailName = anEmail.substring(0, domainIndex).replace('.', '_');

        // Get email domain parts
        String domainName = anEmail.substring(domainIndex + 1);
        String[] dirs = domainName.split("\\.");

        // Add domain parts
        String path = "/";
        for (int i = dirs.length - 1; i >= 0; i--)
            path += dirs[i] + '/';

        // Add name and return
        path += emailName;
        return path;
    }

    /**
     * Returns shared instance.
     */
    public static DropBoxSite getSiteForEmail(String anEmail)
    {
        // Get cached dropbox for email
        String email = anEmail != null ? anEmail.toLowerCase() : null;
        DropBoxSite dropBoxSite = _dropBoxSites.get(email);
        if (dropBoxSite != null || _atok == null)
            return dropBoxSite;

        // Otherwise, create and set
        //System.setProperty("javax.net.debug","all");
        dropBoxSite = new DropBoxSite(anEmail);
        _dropBoxSites.put(email, dropBoxSite);
        return dropBoxSite;
    }

    /**
     * Returns the default email.
     */
    public static String getDefaultEmail()
    {
        if (_defaultEmail != null) return _defaultEmail;
        return _defaultEmail = Prefs.getDefaultPrefs().getString(DEFAULT_EMAIL);
    }

    /**
     * Sets the default email.
     */
    public static void setDefaultEmail(String aString)
    {
        // If already set, just return
        if (Objects.equals(aString, getDefaultEmail())) return;

        // Set and clear RecentFiles
        _defaultEmail = aString;

        // Update Prefs
        Prefs.getDefaultPrefs().setValue(DEFAULT_EMAIL, _defaultEmail);
    }
}
