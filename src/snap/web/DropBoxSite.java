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

    // Shared instance
    private static DropBoxSite _shared;

    // Header value
    private static String _atok;

    // Shared instances
    private static Map<String,WebSite> _dropBoxSites = new HashMap<>();

    // Constants
    private static final String DROPBOX_ROOT = "dbox://dbox.com";
    private static DateFormat JSON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // Constants for DropBox endpoints
    private static final String GET_METADATA = "https://api.dropboxapi.com/2/files/get_metadata";
    private static final String LIST_FOLDER = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CREATE_FOLDER = "https://api.dropboxapi.com/2/files/create_folder_v2";
    private static final String DELETE = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD = "https://content.dropboxapi.com/2/files/upload";
    private static final String GET_CONTENT = "https://content.dropboxapi.com/2/files/download";

    /**
     * Constructor.
     */
    private DropBoxSite()
    {
        super();

        // Create/set URL
        WebURL url = WebURL.getURL(DROPBOX_ROOT);
        setURL(url);
    }

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
        // Create http request
        HTTPRequest httpReq = createHttpRequestForEndpoint(GET_METADATA);
        addParamsToRequestAsJsonBody(httpReq, "path", aReq.getFilePath());

        // Get JSON response
        JSObject jsonResp = getJsonResponse(httpReq, aResp);
        if (jsonResp == null)
            return;

        // Get JSON response
        FileHeader fileHeader = createFileHeaderForJSON(jsonResp);
        aResp.setFileHeader(fileHeader);
    }

    /**
     * Get Directory listing for request.
     */
    protected void doGetDir(WebRequest aReq, WebResponse aResp)
    {
        // Create http request
        HTTPRequest httpReq = createHttpRequestForEndpoint(LIST_FOLDER);
        addParamsToRequestAsJsonBody(httpReq, "path", aReq.getFilePath());

        // Get JSON response
        JSObject jsonResp = getJsonResponse(httpReq, aResp);
        if (jsonResp == null)
            return;

        // Get Entries Node, complain if not array
        JSValue entriesNode = jsonResp.getValue("entries");
        if (!(entriesNode instanceof JSArray)) {
            aResp.setException(new Exception("DropBoxSite.doGetDir: Unexpected response: " + entriesNode.getValueAsString()));
            return;
        }

        // Get json for entries
        JSArray fileEntriesArray = (JSArray) jsonResp.getValue("entries");
        List<JSValue> fileEntries = fileEntriesArray.getValues();

        // Get file headers for JSON file entries and set in response
        List<FileHeader> fileHeaders = ListUtils.map(fileEntries, e -> createFileHeaderForJSON((JSObject) e));
        aResp.setFileHeaders(fileHeaders);
    }

    /**
     * Get file request.
     */
    protected void doGetFileContents(WebRequest aReq, WebResponse aResp)
    {
        // Create http request
        HTTPRequest httpReq = new HTTPRequest(GET_CONTENT);
        httpReq.addHeader("Authorization", "Bearer " + getAccess());
        addParamsToRequestAsJsonHeader(httpReq, "path", aReq.getFilePath());

        // Get response
        getHttpResponse(httpReq, aResp);
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
        // Create http request
        HTTPRequest httpReq = createHttpRequestForEndpoint(UPLOAD);
        httpReq.addHeader("Content-Type", "application/octet-stream");
        addParamsToRequestAsJsonHeader(httpReq, "path", aReq.getFilePath(), "mode", "overwrite");

        // Add bytes
        byte[] bytes = aReq.getSendBytes();
        httpReq.setBytes(bytes);

        // Get JSON response
        JSObject jsonResp = getJsonResponse(httpReq, aResp);
        if (jsonResp == null)
            return;

        // Get last modified date from JSON response and set in web response
        String lastModifiedDateStr = jsonResp.getStringValue("server_modified");
        if (lastModifiedDateStr != null && lastModifiedDateStr.endsWith("Z")) {
            Date lastModifiedDate = parseJsonDateString(lastModifiedDateStr);
            if (lastModifiedDate != null)
                aResp.setLastModTime(lastModifiedDate.getTime());
        }
        else System.err.println("DropBoxSite.doPutFile: Can't get save mod time: " + jsonResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPutDir(WebRequest aReq, WebResponse aResp)
    {
        // Create http request
        HTTPRequest httpReq = createHttpRequestForEndpoint(CREATE_FOLDER);
        addParamsToRequestAsJsonBody(httpReq, "path", aReq.getFilePath());

        // Get JSON response
        JSObject jsonResp = getJsonResponse(httpReq, aResp);
        if (jsonResp != null)
            System.out.println(jsonResp);
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Create http request
        HTTPRequest httpReq = createHttpRequestForEndpoint(DELETE);
        addParamsToRequestAsJsonBody(httpReq, "path", aReq.getFilePath());

        // Get response
        getHttpResponse(httpReq, aResp);
    }

    /**
     * Creates an HTTP request for given endpoint.
     */
    private static HTTPRequest createHttpRequestForEndpoint(String anEndpoint)
    {
        HTTPRequest httpReq = new HTTPRequest(anEndpoint);
        httpReq.addHeader("Authorization", "Bearer " + getAccess());
        httpReq.addHeader("Content-Type", "application/json");
        return httpReq;
    }

    /**
     * Returns the access token.
     */
    private static String getAccess()
    {
        if (_atok != null) return _atok;

        try {
            HTTPRequest httpReq = new HTTPRequest("https://get-dbox.jeff-b76.workers.dev");
            HTTPResponse httpResp = httpReq.getResponse();
            if (httpResp == null || httpResp.getCode() != HTTPResponse.OK) {
                System.err.println("DropBoxSite.getAccess: " + (httpResp != null ? httpResp.getMessage() : "null"));
                return null;
            }

            JSObject jsonResp = (JSObject) httpResp.getJSON();
            return _atok = jsonResp.getStringValue("access_token");
        }

        catch (Exception e) { System.err.println(e.getMessage()); return null; }
    }

    /**
     * Adds parameters to http request as JSON Header.
     */
    private static void addParamsToRequestAsJsonHeader(HTTPRequest httpReq, String ... thePairs)
    {
        // Create JSON Request and add pairs
        JSObject jsonReq = new JSObject();
        for (int i = 0; i < thePairs.length; i += 2)
            jsonReq.setNativeValue(thePairs[i], thePairs[i + 1]);

        // Add as header
        String jsonReqStr = jsonReq.toStringCompacted();
        jsonReqStr = jsonReqStr.replace("\"", "\\\"");
        jsonReqStr = jsonReqStr.replace("\\", "");
        httpReq.addHeader("Dropbox-API-Arg", jsonReqStr);
    }

    /**
     * Adds parameters to http request as JSON Body.
     */
    private static void addParamsToRequestAsJsonBody(HTTPRequest httpReq, String ... thePairs)
    {
        // Create JSON Request and add pairs
        JSObject jsonReq = new JSObject();
        for (int i = 0; i < thePairs.length; i += 2)
            jsonReq.setNativeValue(thePairs[i], thePairs[i + 1]);

        // Add as send-bytes
        String jsonReqStr = jsonReq.toString();
        httpReq.setBytes(jsonReqStr.getBytes());
    }

    /**
     * Sends the HTTP request and loads results into WebResponse and returns the JSON response.
     */
    private static JSObject getJsonResponse(HTTPRequest httpReq, WebResponse aResp)
    {
        // Get HTTP Response
        HTTPResponse httpResp = getHttpResponse(httpReq, aResp);
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK)
            return null;

        // Get JSON response
        JSObject jsonResp = (JSObject) httpResp.getJSON();
        if (jsonResp == null)
            aResp.setException(new Exception("DropBoxSite.getJsonResponse: null response"));

        // Return
        return jsonResp;
    }

    /**
     * Sends the HTTP request and loads results into WebResponse.
     */
    private static HTTPResponse getHttpResponse(HTTPRequest aReq, WebResponse aResp)
    {
        // Get response
        HTTPResponse httpResp;
        try { httpResp = aReq.getResponse(); }
        catch (Exception e) { aResp.setException(e); return null; }

        // Copy response
        aResp.copyResponse(httpResp);
        return httpResp;
    }

    /**
     * Returns a FileHeader for DropBox File Entry JSONNode.
     */
    private static FileHeader createFileHeaderForJSON(JSObject fileEntry)
    {
        // Get attributes
        String filePath = fileEntry.getStringValue("path_display");
        String tag = fileEntry.getStringValue(".tag");
        boolean isFile = tag.equals("file");

        // Create FileHeader
        FileHeader fileHeader = new FileHeader(filePath, !isFile);

        // Get additional file attributes
        if (isFile) {

            // Get/set size
            String sizeStr = fileEntry.getStringValue("size");
            long size = Long.parseLong(sizeStr);
            fileHeader.setSize(size);

            // Get/set last modified date
            String lastModifiedDateStr = fileEntry.getStringValue("server_modified");
            if (lastModifiedDateStr.endsWith("Z")) {
                Date lastModifiedDate = parseJsonDateString(lastModifiedDateStr);
                if (lastModifiedDate != null)
                    fileHeader.setLastModTime(lastModifiedDate.getTime());
            }
        }

        // Return
        return fileHeader;
    }

    /**
     * Parses a string as a JSON formatted date.
     */
    private static Date parseJsonDateString(String dateString)
    {
        try { return JSON_DATE_FORMAT.parse(dateString); }
        catch (Exception e) { System.err.println(e.getMessage()); return null; }
    }

    /**
     * Returns the shared DropboxSite.
     */
    public static DropBoxSite getShared()
    {
        if (_shared != null) return _shared;
        return _shared = new DropBoxSite();
    }

    /**
     * Returns shared instance.
     */
    public static WebSite getSiteForUrl(WebURL siteUrl)
    {
        // Get cached dropbox for email
        String siteUrlAddress = siteUrl.getString();
        WebSite projectSite = _dropBoxSites.get(siteUrlAddress);
        if (projectSite != null)
            return projectSite;

        // Get shared
        DropBoxSite dropboxSite = getShared();

        // Get project dir path and project dir
        String projectDirPath = siteUrl.getPath();
        WebFile projectDir = dropboxSite.createFileForPath(projectDirPath, true);

        // Get dir as site
        WebURL projectDirUrl = projectDir.getURL();
        projectSite = projectDirUrl.getAsSite();

        // Add to cache and return
        _dropBoxSites.put(siteUrlAddress, projectSite);
        return projectSite;
    }
}
