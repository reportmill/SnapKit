/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.*;
import snap.view.*;

/**
 * A TaskRunner implementation that runs success/failed/finished on application thread.
 */
public abstract class TaskRunnerPanel<T> extends TaskRunner<T> {

    /**
     * Constructor.
     */
    public TaskRunnerPanel()
    {
        super();
    }

    /**
     * Constructor for given view and title.
     */
    public TaskRunnerPanel(View aView, String aTitle)
    {
        super(new TaskMonitorPanel(aView, aTitle));
    }

    /**
     * Override to show exception in dialog box (and potentially hide JFXTaskMonitor dialog box).
     */
    public void failure(Exception e)
    {
        // Hide task monitor panel
        TaskMonitor monitor = getMonitor();
        TaskMonitorPanel monitorPanel = monitor instanceof TaskMonitorPanel ? (TaskMonitorPanel) monitor : null;
        if (monitorPanel != null)
            monitorPanel.hide();

        // Show error panel?
        //String title = getName() + " Error";
        //DialogBox dialogBox = new DialogBox(title);
        //dialogBox.setErrorMessage(e.toString());
        //View monitorView = monitorPanel != null ? monitorPanel._view : null;
        //dialogBox.showMessageDialog(monitorView);
    }
}