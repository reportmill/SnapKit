package snap.view;

/**
 * A class to represent a TabView tab.
 */
public class Tab {
    
    // The TabView
    TabView      _tabView;

    // The title
    String       _title;
    
    // The content
    View         _content;
    
/**
 * Returns the title.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title.
 */
public void setTitle(String aTitle)  { _title = aTitle; }

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)  { _content = aView; }

}