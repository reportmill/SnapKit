package snap.games;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.view.View;

/**
 * This stage view class delegates to a simpler stage object.
 */
public class ProxyStageView extends StageView {

    // The stage
    private Stage _stage;

    // The stage class name
    private String _stageClassName;

    /**
     * Constructor.
     */
    public ProxyStageView()
    {
        super();
        _stage = new Stage();
        _stage._stageView = this;
    }

    /**
     * Constructor.
     */
    public ProxyStageView(Stage stage)
    {
        super();
        _stage = stage;
        _stageClassName = stage.getClass().getName();
    }

    /**
     * Returns the stage.
     */
    public Stage getStage()  { return _stage; }

    /**
     * Override to clear actors list.
     */
    @Override
    public void addChild(View aChild, int anIndex)
    {
        super.addChild(aChild, anIndex);
        _stage._actors = null;
    }

    /**
     * Override to clear actors list.
     */
    @Override
    public View removeChild(int anIndex)
    {
        View child = super.removeChild(anIndex);
        _stage._actors = null;
        return child;
    }

    /**
     * Override to forward to stage.
     */
    @Override
    protected void stepGameFrame()
    {
        super.stepGameFrame();
        _stage.stepGameFrame();
    }

    /**
     * Override to handle PenActors.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        super.paintFront(aPntr);

        // Iterate over children and paint pen paths
        for (Actor child : _stage.getActors()) {
            if (child instanceof PenActor penActor) {
                penActor.paintPen(aPntr);
                aPntr.setStroke(Stroke.Stroke1);
            }
        }
    }

    /**
     * Override to handle stage.
     */
    @Override
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXMLView(anArchiver);
        xml.setName("StageView");

        // Archive stage class name
        if (_stageClassName != null && !_stageClassName.equals(Stage.class.getName()))
            xml.add("StageClass", _stageClassName);

        // Return
        return xml;
    }

    /**
     * Override to handle stage.
     */
    @Override
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Create Actor class
        _stageClassName = anElement.getAttributeValue("StageClass");
        if (_stageClassName != null) {
            _stage = (Stage) ProxyActorView.getInstanceForClassName(anArchiver.getOwnerClass(), _stageClassName);
            if (_stage == null) _stage = new Stage();
            _stage._stageView = this;
        }

        super.fromXMLView(anArchiver, anElement);
    }
}
