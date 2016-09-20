/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * A WebPage subclass for ClassFile (assuming they return a component).
 */
public class ClassPage extends WebPage {

    // An instance of the SnapClass
    Object            _instance;
    
    // The WebPage for the instance, if available
    WebPage           _instPage;

/**
 * Returns the dedicated instance of the ClassFile class for this page.
 */
public Object getInstance()  { return _instance!=null? _instance : (_instance=getInstanceImpl()); }

/**
 * Returns the dedicated instance of the ClassFile class for this page.
 */
private Object getInstanceImpl()
{
    if(getURL().getQueryValue("id")!=null)
        return getURLInstance(getURL());
    Class cls = ClassUtils.getClass(getURL().getFile()); //getFile()
    Object obj = ClassUtils.newInstance(cls);
    WebURL url = getInstanceURL(obj);
    setURL(url);
    return obj;
}

/**
 * Returns the dedicated instance of ClassFile class for this page cast to given class (or null).
 */
public <T> T getInstance(Class<T> aClass)  { return ClassUtils.getInstance(getInstance(), aClass); }

/**
 * Creates UI panel for this file.
 */
protected View createUI()
{
    // If InstancePage available, return it
    WebPage ipage = getInstancePage();
    if(ipage!=null && ipage!=this)
        return ipage.getUI();
    
    // Handle Java class for views (if null, replace with label)
    Object view = getInstance();
    if(view!=null) { View n = getView(view);
        if(n==null) n = (View)Key.getValue(view, "View");
        if(n!=null) return n; }

    // Return Label with complaint
    return new Label("No page found for class " + getFile().getName());
}

/**
 * Returns a WebPage for instance, if available.
 */
public WebPage getInstancePage()  { return _instPage!=null? _instPage : (_instPage=getInstancePageImpl()); }

/**
 * Returns a WebPage, if available.
 */
protected WebPage getInstancePageImpl()
{
    // Handle JFXOwner
    Object obj = getInstance();
    if(obj instanceof WebPage)
        return (WebPage)obj;
    if(obj instanceof ViewOwner)
        return new ViewOwnerPage((ViewOwner)obj);
    
    Object page = Key.getValue(obj, "Page");
    if(page instanceof WebPage) { WebPage wpage = (WebPage)page;
        wpage.setURL(getURL());
        return wpage;
    }
    
    // Return null since object not recognized
    return this;
}

}