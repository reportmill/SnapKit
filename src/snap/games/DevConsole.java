package snap.games;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;

/**
 * A class to provide developer controls to a GameController.
 */
public class DevConsole extends ViewOwner {

    // The GameController
    private GameController _gameController;

    /**
     * Constructor.
     */
    public DevConsole(GameController gameController)
    {
        super();
        _gameController = gameController;
    }

    /**
     * Returns the game view.
     */
    public GameView getGameView()
    {
        return _gameController.getGameView();
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        GameView gameView = getGameView();
        gameView.setAutoStart(false);

        // Configure ScrollView to hold game view
        ScrollView scrollView = new ScrollView(getGameView());
        scrollView.setBorder(null);

        // Create border view and add ScrollView, toolBar
        BorderView borderView = new BorderView();
        borderView.setFont(Font.Arial12);
        borderView.setFill(ViewUtils.getBackFill());
        borderView.setCenter(scrollView);
        borderView.setBottom(createToolbar());
        borderView.setBorder(Color.GRAY, 1);

        // Return
        return borderView;
    }

    /**
     * Create UI.
     */
    protected View createToolbar()
    {
        // Create tool bar items
        Button actBtn = new Button("Act");
        actBtn.setName("ActButton");
        actBtn.setPrefSize(70, 20);
        Button runBtn = new Button("Run");
        runBtn.setName("RunButton");
        runBtn.setPrefSize(70, 20);
        Button resetBtn = new Button("Reset");
        resetBtn.setName("ResetButton");
        resetBtn.setPrefSize(70, 20);
        Separator sep = new Separator();
        sep.setPrefWidth(40);
        sep.setVisible(false);
        Label speedLbl = new Label("Frame Rate:");
        speedLbl.setLeanX(HPos.CENTER);
        speedLbl.setFont(Font.Arial14);
        Slider speedSldr = new Slider();
        speedSldr.setName("SpeedSlider");
        speedSldr.setPrefWidth(180);
        TextField speedText = new TextField();
        speedText.setName("SpeedText");
        speedText.setPrefWidth(40);
        speedText.setAlignX(HPos.CENTER);

        // Create toolbar
        RowView toolBar = new RowView();
        toolBar.setAlign(Pos.CENTER);
        toolBar.setPadding(18, 25, 18, 25);
        toolBar.setSpacing(15);
        toolBar.setChildren(actBtn, runBtn, resetBtn, sep, speedLbl, speedSldr, speedText);
        return toolBar;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        setViewValue("SpeedSlider", getGameView().getFrameRate() / 100);
        setViewValue("SpeedText", Math.round(getGameView().getFrameRate()));
    }

    /**
     * Respond to UI changes.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ActButton
        if (anEvent.equals("ActButton"))
            getGameView().doAct();

        // Handle RunButton
        if (anEvent.equals("RunButton")) {
            View runBtn = anEvent.getView();
            runBtn.setText("Pause");
            runBtn.setName("PauseButton");
            getView("ActButton").setDisabled(true);
            getGameView().start();
        }

        // Handle PauseButton
        if (anEvent.equals("PauseButton")) {
            getGameView().stop();
            View pauseBtn = anEvent.getView();
            pauseBtn.setText("Run");
            pauseBtn.setName("RunButton");
            getView("ActButton").setDisabled(false);
        }

        // Handle ResetButton
        if (anEvent.equals("ResetButton"))
            _gameController.resetGameView();

        // Handle SpeedSlider
        if (anEvent.equals("SpeedSlider"))
            getGameView().setFrameRate(anEvent.getFloatValue() * 100);

        // Shouldn't need this
        getGameView().requestFocus();
    }
}
