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
     * Returns whether game is playing.
     */
    public boolean isPlaying()  { return getGameView().isPlaying(); }

    /**
     * Plays the game.
     */
    public void playGame()
    {
        getGameView().start();
    }

    /**
     * Pauses the game.
     */
    public void pauseGame()
    {
        getGameView().stop();
    }

    /**
     * Sets the game frame rate.
     */
    public void setFrameRate(double aValue)
    {
        getGameView().setFrameRate(aValue);
    }

    /**
     * Reset GameView.
     */
    public void resetGameView()
    {
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
        // Update StepButton enabled
        setViewEnabled("StepButton", !isPlaying());

        // Update PlayButton, PauseButton visible
        setViewVisible("PlayButton", !isPlaying());
        setViewVisible("PauseButton", isPlaying());

        // Update SpeedSlider, SpeedText
        setViewValue("SpeedSlider", getGameView().getFrameRate() / 100);
        setViewValue("SpeedText", Math.round(getGameView().getFrameRate()));
    }

    /**
     * Respond to UI changes.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle StepButton
            case "StepButton" -> _gameController.stepGame();

            // Handle PlayButton, PauseButton
            case "PlayButton" -> playGame();
            case "PauseButton" -> pauseGame();

            // Handle ResetButton
            case "ResetButton" -> resetGameView();

            // Handle SpeedSlider, SpeedText
            case "SpeedSlider" -> setFrameRate(anEvent.getFloatValue() * 100);
            case "SpeedText" -> setFrameRate(anEvent.getFloatValue());
        }

        // Shouldn't need this
        getGameView().requestFocus();
    }

    /**
     * TOOL_BAR_UI.
     */
    private static String TOOL_BAR_UI = """
        <RowView Align="CENTER" Padding="18,25,18,25" Spacing="15">
          <Button Name="StepButton" PrefWidth="70" Text="Step" />
          <Button Name="PlayButton" PrefWidth="70" Text="Play" />
          <Button Name="PauseButton" PrefWidth="70" Text="Pause" />
          <Button Name="ResetButton" PrefWidth="70" Text="Reset" />
          <Separator PrefWidth="40" />
          <Label LeanX="CENTER" Font="Arial 14" Text="Frame Rate:" />
          <Slider Name="SpeedSlider" PrefWidth="180" Max="1" />
          <TextField Name="SpeedText" PrefWidth="40" Align="CENTER" />
        </RowView>
        """;
}
