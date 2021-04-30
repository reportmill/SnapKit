package snap.viewx;
import snap.geom.HPos;
import snap.gfx.Font;
import snap.text.TextLineStyle;
import snap.text.TextStyle;
import snap.util.KeyChain;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.view.*;

/**
 * A DevPane to show the console.
 */
public class DevPaneExceptions extends ViewOwner {

    // The ConsoleView
    private TextView  _textView;

    /**
     * Shows the given exception.
     */
    public void showException(Exception anExc)
    {
        anExc.fillInStackTrace();
        String str = StringUtils.getStackTraceString(anExc);
        _textView.addChars(str);
    }

    @Override
    protected View createUI()
    {
        // Create Header
        Label headerText = new Label("Exception was thrown:");
        headerText.setFont(Font.Arial14.getBold());
        Button closeButton = new Button("X");
        closeButton.setName("CloseButton");
        closeButton.setLeanX(HPos.RIGHT);
        closeButton.setPadding(3,6,3,6);

        // Create HeaderView
        RowView headerView = new RowView();
        headerView.addChild(headerText);
        headerView.addChild(closeButton);

        // Create TextView
        _textView = new TextView();
        _textView.setGrowHeight(true);
        //setFirstFocus(_textView);

        // Create BoxView for ScrollView
        ColView boxView = new ColView();
        boxView.setFillWidth(true);
        boxView.setPadding(10, 10, 10, 10);
        boxView.setSpacing(5);
        boxView.addChild(headerView);
        boxView.addChild(_textView);
        return boxView;
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        if (anEvent.equals("CloseButton"))
            runLater(() -> DevPane.setDevPaneShowing(getUI(), false));
    }
}
