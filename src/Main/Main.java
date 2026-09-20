package Main;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import Main.Config.*;

/**
 * Main class that initializes and controls the Pong game.
 * 
 * Responsible for:
 * - Setting up the game window and rendering context
 * - Initializing paddles, ball, and AI
 * - Managing the update and render loops
 * - Handling keyboard input for the left paddle
 * - Controlling game state during scoring sequences
 * 
 * The game runs at 120 FPS for rendering and 60 FPS for game logic updates.
 */
public class Main {

    /** Graphics rendering engine */
    protected static GraphicalRender graphicalRender;
    /** Left player's paddle */
    public static Paddle leftPaddle;
    /** Right player's paddle (AI controlled) */
    public static Paddle rightPaddle;

    /**
     * Entry point for the Pong game.
     * 
     * Initializes the game window, sets up all game entities (paddles, ball),
     * starts the rendering and update loops, and attaches keyboard listeners.
     * 
     * @param args Command line arguments (not used)
     * @throws Exception If initialization fails
     */
    public static void main(String[] args) throws Exception {
        Screen screen = new Screen(new Dimension(300, 300), true, false, true);

        graphicalRender = new GraphicalRender();
        GraphicalRender.writeToContext = screen;
        Paddle.screenArea = screen.getContentSize();
        Paddle.setDistanceFromWall(10);
        Paddle.LeftPaddle = new Config().new Left(10, 60);
        Paddle.RightPaddle = new Config().new Right(10, 60);

        Paddle.LeftPaddle.resetToCenter();
        Paddle.RightPaddle.resetToCenter();
        AI.reset();

        Ball.init();

        screen.startRenderLoop(1000 / 120);
        screen.setVisible(true);
        startUpdateLoop(1000 / 60);

        screen.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == Input.LeftPaddleUp) {
                    Paddle.LeftPaddle.input[0] = true;
                }
                if (e.getKeyCode() == Input.LeftPaddleDown) {
                    Paddle.LeftPaddle.input[1] = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == Input.LeftPaddleUp) {
                    Paddle.LeftPaddle.input[0] = false;
                }
                if (e.getKeyCode() == Input.LeftPaddleDown) {
                    Paddle.LeftPaddle.input[1] = false;
                }
            }
        });

        screen.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Window gained focus, resuming update loop");
                    isPaused = false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                System.out.println("Window lost focus, pausing update loop");
                isPaused = true;
            }

        });

    }

    /**
     * Tracks whether the game is currently paused (e.g., during score display, or
     * inactive screen)
     */
    private static boolean isPaused = false;

    /**
     * Triggers the scoring sequence animation and pause.
     * 
     * When called:
     * 1. Pauses the game
     * 2. Displays the score for 500ms
     * 3. Resets paddle and ball positions to center
     * 4. Shows reset state for 1000ms
     * 5. Resumes the game
     * 
     * Executes asynchronously in a separate thread to avoid blocking the game loop.
     */
    public static void triggerScoringSequence() {
        isPaused = true;
        new Thread(() -> {
            try {
                System.out.println("PAUSE: Showing score for 500ms");
                Thread.sleep(500); // Show score
                System.out.println("PAUSE: Resetting positions");
                Paddle.LeftPaddle.resetToCenter();
                Paddle.RightPaddle.resetToCenter();
                AI.reset();
                Ball.resetToCenter();
                System.out.println("PAUSE: Showing reset for 1000ms");
                Thread.sleep(1000); // Show reset
                System.out.println("PAUSE: Resuming game");
                isPaused = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** Executor service for the game update loop */
    private static final ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor();
    /** Future object representing the scheduled update loop task */
    private static ScheduledFuture<?> updateFuture;

    /**
     * Starts the game update loop at a fixed rate.
     * 
     * The update loop runs the game's physics and logic updates at the specified
     * interval.
     * Updates include paddle movement, ball physics, AI logic, and rendering.
     * 
     * @param periodMillis The period between updates in milliseconds (typically
     *                     1000/60 = ~16.67ms for 60 FPS)
     */
    public static void startUpdateLoop(long periodMillis) {
        stopUpdateLoop();

        updateFuture = updateExecutor.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(Main::Update);
        }, 0, periodMillis, TimeUnit.MILLISECONDS);
        System.out.println("Starting Update Loop " + updateFuture.hashCode());
    }

    /**
     * Stops the game update loop.
     * 
     * Cancels the scheduled update tasks and cleans up resources.
     * Safe to call multiple times or when no loop is running.
     */
    public static void stopUpdateLoop() {
        if (updateFuture != null && !updateFuture.isDone()) {
            System.out.println("Stopping Update Loop " + updateFuture.hashCode());

            updateFuture.cancel(false);
        }
        updateFuture = null;

    }

    /**
     * Checks if the game update loop is currently running.
     * 
     * @return true if the update loop is active, false otherwise
     */
    public static boolean isUpdateLoopRunning() {
        return updateFuture != null && !updateFuture.isDone();
    }

    /**
     * Main game update method called every game tick.
     * 
     * In order, performs:
     * 1. Renders the current game state
     * 2. Skips logic updates if game is paused
     * 3. Updates left paddle position based on player input
     * 4. Updates ball physics (movement, collisions, scoring)
     * 5. Updates AI paddle logic
     * 6. Updates right paddle position
     */
    public static void Update() {
        graphicalRender.postImage();
        if (isPaused) {
            return;
        }
        Paddle.LeftPaddle.Move(Paddle.organizeInput(Paddle.LeftPaddle.input));
        Ball.Move();
        AI.update();
        Paddle.calculateDynamicSpeed();
        Paddle.RightPaddle.Move(Paddle.organizeInput(Paddle.RightPaddle.input));

    }
}
