/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.*;
import snap.view.*;

/**
 * A TaskRunner implementation that runs success/failed/finished on application thread.
 */
public abstract class TaskRunnerPanel <T> extends TaskRunner <T> {

/**
 * Creates a new TaskRunnerPanel.
 */
public TaskRunnerPanel()  { }

/**
 * Creates a new TaskRunnerPanel for given monitor.
 */
public TaskRunnerPanel(TaskMonitor aTM)  { super(aTM); }

/**
 * Creates a new TaskRunnerPanel for given monitor.
 */
public TaskRunnerPanel(View aView, String aTitle)  { super(new TaskMonitorPanel(aView, aTitle)); }

/**
 * Override to show exception in dialog box (and potentially hide JFXTaskMonitor dialog box).
 */
public void failure(Exception e)
{
    TaskMonitorPanel jtm = getMonitor() instanceof TaskMonitorPanel? (TaskMonitorPanel)getMonitor() : null;
    View view = jtm!=null? jtm._view : null;
    if(jtm!=null) jtm.hide();
    DialogBox db = new DialogBox("Checkout Error"); db.setErrorMessage(e.toString());
    db.showMessageDialog(view!=null? view : null); e.printStackTrace();
}

/**
 * Runs the success method.
 */
protected void invokeFinished()
{
    ViewEnv.getEnv().runLater(() -> TaskRunnerPanel.super.invokeFinished());
}

}