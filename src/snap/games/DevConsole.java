package snap.games;
import snap.gfx.Color;
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
    public GameView getGameView()  { return _gameController.getGameView(); }

    /**
     * Starts the game.
     */
    public void runGame(ViewEvent anEvent)
    {
        View runBtn = anEvent.getView();
        runBtn.setText("Pause");
        runBtn.setName("PauseButton");
        getView("ActButton").setDisabled(true);
        getGameView().start();
    }

    /**
     * Pauses the game.
     */
    public void pauseGame(ViewEvent anEvent)
    {
        getGameView().stop();
        View pauseBtn = anEvent.getView();
        pauseBtn.setText("Run");
        pauseBtn.setName("RunButton");
        getView("ActButton").setDisabled(false);
    }

    /**
     * Sets the game speed.
     */
    public void setGameSpeed(ViewEvent anEvent)
    {
        getGameView().setFrameRate(anEvent.getFloatValue() * 100);
    }

    /**
     * Reset GameView.
     */
    public void resetGameView()
    {
        getGameView().stop();
        View pauseBtn = getView("PauseButton");
        if (pauseBtn != null) {
            pauseBtn.setText("Run");
            pauseBtn.setName("RunButton");
            getView("ActButton").setDisabled(false);
        }

        _gameController.resetGameView();
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
        ColView colView = new ColView();
        colView.addChild(scrollView);
        colView.addChild(createToolbar());
        colView.setBorder(Color.GRAY, 1);

        // Return
        return colView;
    }

    /**
     * Create Toolbar.
     */
    protected View createToolbar()  { return UILoader.loadViewForString(TOOL_BAR_UI); }

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
        switch (anEvent.getName()) {

            // Handle ActButton
            case "ActButton" -> _gameController.stepGame();

            // Handle RunButton, PauseButton
            case "RunButton" -> runGame(anEvent);
            case "PauseButton" -> pauseGame(anEvent);

            // Handle ResetButton
            case "ResetButton" -> resetGameView();

            // Handle SpeedSlider
            case "SpeedSlider" -> setGameSpeed(anEvent);
        }

        // Shouldn't need this
        getGameView().requestFocus();
    }

    /**
     * TOOL_BAR_UI.
     */
    private static String TOOL_BAR_UI = """
        <RowView Align="CENTER" Padding="18,25,18,25" Spacing="15">
          <Button Name="ActButton" PrefWidth="70" Text="Act" />
          <Button Name="RunButton" PrefWidth="70" Text="Run" />
          <Button Name="ResetButton" PrefWidth="70" Text="Reset" />
          <Separator PrefWidth="40" />
          <Label LeanX="CENTER" Font="Arial 14" Text="Frame Rate:" />
          <Slider Name="SpeedSlider" PrefWidth="180" Max="1" />
          <TextField Name="SpeedText" PrefWidth="40" Align="CENTER" />
        </RowView>
        """;
}
