package snap.webenv;
import snap.webapi.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple app to bounce balls around page.
 */
public class BallBounce {

    // The list of balls
    private List<Ball> _balls = new ArrayList<>();

    // The body element
    private HTMLElement _bodyElmt;

    // Whether animation is playing
    private int _playing = -1;

    // Whether mouse button is pressed
    private boolean _mouseDown;

    // Last add time
    private long _lastAddTime;

    /**
     * Constructor.
     */
    public BallBounce()
    {
        // Get document and body
        HTMLDocument doc = HTMLDocument.getDocument();
        _bodyElmt = doc.getBody();
        _bodyElmt.getStyle().setCssText("margin:0;");

        // Get main element and register mouse listeners
        _bodyElmt.addEventListener("mousedown", e -> mouseDown((MouseEvent) e));
        _bodyElmt.addEventListener("mousemove", e -> mouseMove((MouseEvent) e));
        _bodyElmt.addEventListener("mouseup", e -> _mouseDown = false);

        // Add Touch Listeners
        _bodyElmt.addEventListener("touchstart", e -> touchStart((TouchEvent) e));
        _bodyElmt.addEventListener("touchmove", e -> touchMove((TouchEvent) e));
        _bodyElmt.addEventListener("touchend", e -> _mouseDown = false);

        // Add clear balls button
        HTMLElement clearButton = doc.createElement("button");
        clearButton.getStyle().setCssText("position:fixed; top:8px; right:8px;");
        clearButton.setInnerText("Clear Balls");
        clearButton.addEventListener("click", e -> clearBalls());
        _bodyElmt.appendChild(clearButton);

        // Seed starter balls
        for (int i = 0; i < 10; i++)
            addBall(30, 30);
    }

    /**
     * Add ball.
     */
    public void addBall(double aX, double aY)
    {
        // Add ball
        Ball ball = new Ball(aX, aY);
        _bodyElmt.appendChild(ball._ballImage);
        _balls.add(ball);
        _lastAddTime = System.currentTimeMillis();
        play();
    }

    /**
     * Remove ball.
     */
    public void removeBall(Ball aBall)  { _bodyElmt.removeChild(aBall._ballImage); }

    /**
     * Clears balls.
     */
    public void clearBalls()
    {
        _balls.forEach(this::removeBall);
        _balls.clear();
        stop();
    }

    /**
     * Start animation.
     */
    public void play()
    {
        if (_playing >= 0) return;
        _playing = Window.setInterval(this::moveBalls, 25);
    }

    /**
     * Stop animation.
     */
    public void stop()
    {
        if (_playing < 0) return;
        Window.clearInterval(_playing);
        _playing = -1;
    }

    /**
     * Move balls.
     */
    private void moveBalls()  { _balls.forEach(Ball::moveBall); }

    /**
     * Handle mouse down event.
     */
    public void mouseDown(MouseEvent anEvent)
    {
        mouseDown(anEvent.getClientX(), anEvent.getClientY());
        anEvent.preventDefault(); // Stop browser from potentially dragging hit image
    }

    /**
     * Handle mouse down event.
     */
    public void mouseDown(double aX, double aY)
    {
        _mouseDown = true;
        addBall(aX, aY);
    }

    /**
     * Handle mouse move event.
     */
    public void mouseMove(MouseEvent anEvent)  { mouseMove(anEvent.getClientX(), anEvent.getClientY()); }

    /**
     * Handle mouse move event.
     */
    public void mouseMove(double aX, double aY)
    {
        // If not mouse drag, just return
        if (!_mouseDown) return;

        // If 100 milliseconds has passed, add ball
        long time = System.currentTimeMillis();
        if (time > _lastAddTime + 100)
            addBall(aX, aY);
    }

    /**
     * Called when body gets TouchStart.
     */
    public void touchStart(TouchEvent anEvent)
    {
        // Get event touches and first touch
        Touch[] touches = anEvent.getTouches();
        if (touches == null || touches.length == 0) return;
        Touch touch = touches[0];
        mouseDown(touch.getClientX(), touch.getClientY());
        anEvent.stopPropagation();
        anEvent.preventDefault();
    }

    /**
     * Called when body gets touchMove.
     */
    public void touchMove(TouchEvent anEvent)
    {
        // Get event touches and first touch
        Touch[] touches = anEvent.getTouches();
        if (touches == null || touches.length == 0) return;
        Touch touch = touches[0];
        mouseMove(touch.getClientX(), touch.getClientY());
        anEvent.stopPropagation();
        anEvent.preventDefault();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)  { new BallBounce(); }

    /**
     * An inner class to model ball.
     */
    public static class Ball {

        // Image for ball
        private HTMLImageElement _ballImage;

        // Location of ball
        private double _ballX, _ballY;

        // The ball velocity vector
        private double _ballVX = Math.random() * 10 - 5;
        private double _ballVY = Math.random() * 10 - 5;

        // The ball rotation
        private int _roll;

        /**
         * Create Ball.
         */
        public Ball(double aX, double aY)
        {
            _ballX = aX;
            _ballY = aY;
            _ballImage = (HTMLImageElement) HTMLDocument.getDocument().createElement("img");
            _ballImage.setSrc("https://reportmill.com/images/Ball32.png");
            _ballImage.getStyle().setCssText("position:absolute; left:" + aX + "px; top:" + aY + "px;");
        }

        /**
         * Moves the ball.
         */
        public void moveBall()
        {
            _ballX += _ballVX;
            _ballY += _ballVY;
            _roll += _ballVX > 0 ? 4 : -4;

            // If ball hits wall, reflect velocity vector
            if (_ballX < 0 || _ballX + 40 > Window.get().getInnerWidth()) _ballVX = -_ballVX;
            if (_ballY < 0 || _ballY + 40 > Window.get().getInnerHeight()) _ballVY = -_ballVY;

            // Move image
            _ballImage.getStyle().setProperty("left", _ballX + "px");
            _ballImage.getStyle().setProperty("top", _ballY + "px");
            _ballImage.getStyle().setProperty("transform", "rotate(" + _roll + "deg)");
        }
    }
}