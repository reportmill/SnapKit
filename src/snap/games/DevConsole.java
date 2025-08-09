package snap.games;
import snap.gfx.Color;
import snap.util.SnapUtils;
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
        _gameController.setAutoPlay(false);
        _gameController.addPropChangeListener(pc -> resetLater(), GameController.Playing_Prop);
    }

    /**
     * Returns the game view.
     */
    public GameView getGameView()  { return _gameController.getGameView(); }

    /**
     * Returns whether game is playing.
     */
    public boolean isPlaying()  { return _gameController.isPlaying(); }

    /**
     * Plays the game.
     */
    public void playGame()  { _gameController.playGame(); }

    /**
     * Pauses the game.
     */
    public void pauseGame()  { _gameController.stopGame(); }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        // Get game controller UI view and set to grow
        View gameControllerUI = _gameController.getUI();
        gameControllerUI.setGrowWidth(true);
        gameControllerUI.setGrowHeight(true);

        // Create border view and add ScrollView, toolBar
        ColView colView = new ColView();
        colView.addChild(gameControllerUI);
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
     * Override to maximize window when in browser.
     */
    @Override
    protected void initWindow(WindowView aWindow)
    {
        if (SnapUtils.isWebVM)
            aWindow.setMaximized(true);
    }

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
        setViewValue("SpeedSlider", _gameController.getFrameRate() / 100);
        setViewValue("SpeedText", Math.round(_gameController.getFrameRate()));
    }

    /**
     * Respond to UI changes.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle StepButton
            case "StepButton" -> _gameController.stepGameFrame();

            // Handle PlayButton, PauseButton
            case "PlayButton" -> playGame();
            case "PauseButton" -> pauseGame();

            // Handle ResetButton
            case "ResetButton" -> _gameController.resetGameView();

            // Handle SpeedSlider, SpeedText
            case "SpeedSlider" -> _gameController.setFrameRate(anEvent.getFloatValue() * 100);
            case "SpeedText" -> _gameController.setFrameRate(anEvent.getFloatValue());
        }

        // Shouldn't need this
        getGameView().requestFocus();
    }

    /**
     * Toolbar UI.
     */
    private static String TOOL_BAR_UI = """
        <RowView Align="CENTER" Padding="18,25,18,25" Spacing="15">
          <Button Name="StepButton" PrefWidth="70" Text="Step" />
          <Button Name="PlayButton" PrefWidth="70" Text="Play" />
          <Button Name="PauseButton" PrefWidth="70" Text="Pause" />
          <Button Name="ResetButton" PrefWidth="70" Text="Reset" />
          <Separator PrefWidth="40" PrefHeight="20" Vertical="true" />
          <Label LeanX="CENTER" Font="Arial 14" Text="Frame Rate:" />
          <Slider Name="SpeedSlider" PrefWidth="180" GrowWidth="true" Max="1" />
          <TextField Name="SpeedText" PrefWidth="40" Align="CENTER" />
        </RowView>
        """;
}
