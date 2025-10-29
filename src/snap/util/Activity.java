package snap.util;
import snap.props.PropObject;
import snap.props.PropSet;
import java.util.Objects;

/**
 * This class models the progress of arbitrary tasks/activities.
 */
public class Activity extends PropObject {

    // A string to describe task
    private String _title;

    // Whether task count and run time is indeterminate
    private boolean _indeterminate;

    // The total number of tasks
    protected int _taskCount;

    // The current of task index
    protected int _taskIndex;

    // Whether monitor has been cancelled
    private boolean _cancelled;

    // Whether monitor has finished
    private boolean _finished;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String Indeterminate_Prop = "Indeterminate";
    public static final String TaskCount_Prop = "TaskCount";
    public static final String TaskIndex_Prop = "TaskIndex";
    public static final String Cancelled_Prop = "Cancelled";
    public static final String Finished_Prop = "Finished";

    /**
     * Constructor.
     */
    public Activity()
    {
        super();
    }

    /**
     * Returns a string to describe task.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets a string to describe task.
     */
    public void setTitle(String aValue)
    {
        if (Objects.equals(aValue, _title)) return;
        firePropChange(Title_Prop, _title, _title = aValue);
    }

    /**
     * Returns whether task timing is indeterminate.
     */
    public boolean isIndeterminate()  { return _indeterminate; }

    /**
     * Sets whether task timing is indeterminate.
     */
    public void setIndeterminate(boolean aValue)
    {
        if (aValue == isIndeterminate()) return;
        firePropChange(Indeterminate_Prop, _indeterminate, _indeterminate = aValue);
    }

    /**
     * Returns the total number of tasks.
     */
    public int getTaskCount()  { return _taskCount; }

    /**
     * Sets the total number of tasks.
     */
    protected void setTaskCount(int aValue)
    {
        if (aValue == _taskCount) return;
        firePropChange(TaskCount_Prop, _taskCount, _taskCount = aValue);
    }

    /**
     * Returns the current task index.
     */
    public int getTaskIndex()  { return _taskIndex; }

    /**
     * Sets the current task index.
     */
    protected void setTaskIndex(int aValue)
    {
        if (aValue == _taskIndex) return;
        firePropChange(TaskIndex_Prop, _taskIndex, _taskIndex = aValue);
    }

    /**
     * Returns whether the user asked the process to stop working.
     */
    public boolean isCancelled()  { return _cancelled; }

    /**
     * Sets whether the user asked the process to stop working.
     */
    public void setCancelled(boolean aValue)
    {
        if (aValue == _cancelled) return;
        firePropChange(Cancelled_Prop, _cancelled, _cancelled = aValue);
    }

    /**
     * Returns whether monitor has finished.
     */
    public boolean isFinished()  { return _finished; }

    /**
     * Sets whether monitor has finished.
     */
    public void setFinished(boolean aValue)
    {
        if (aValue == _finished) return;
        firePropChange(Finished_Prop, _finished, _finished = aValue);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // Title, Cancelled, Finished
        aPropSet.addPropNamed(Title_Prop, String.class, null);
        aPropSet.addPropNamed(Cancelled_Prop, boolean.class, false);
        aPropSet.addPropNamed(Finished_Prop, boolean.class, false);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // Title, Cancelled, Finished
            case Title_Prop -> getTitle();
            case Cancelled_Prop -> isCancelled();
            case Finished_Prop -> isFinished();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Title, Cancelled, Finished
            case Title_Prop -> setTitle(Convert.stringValue(aValue));
            case Cancelled_Prop -> setCancelled(Convert.boolValue(aValue));
            case Finished_Prop -> setFinished(Convert.boolValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }
}
