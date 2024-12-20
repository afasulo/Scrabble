package scrabble;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

/**
 * Represents a ScoreBoard component for a Scrabble game.
 * This class extends StackPane and displays the scores for human and computer players,
 * as well as the number of remaining tiles.
 */
public class ScoreBoard extends StackPane {

    private Text humanScoreText;
    private Text computerScoreText;
    private Text remainingTilesText;
    private Rectangle background;

    // Constants for ScoreBoard dimensions and styling
    private static final int WIDTH = 250;
    private static final int HEIGHT = 150;
    private static final int PADDING = 10;
    private static final int CORNER_RADIUS = 15;

    /**
     * Constructs a new ScoreBoard instance.
     * Initializes and lays out all components of the scoreboard.
     */
    public ScoreBoard() {
        initializeComponents();
        layoutComponents();
        setMaxSize(WIDTH, HEIGHT);
    }

    /**
     * Initializes the background and score texts.
     */
    private void initializeComponents() {
        createBackground();
        initializeScoreTexts();
    }

    /**
     * Sets up the layout with the initialized components.
     */
    private void layoutComponents() {
        getChildren().addAll(background, humanScoreText, computerScoreText, remainingTilesText);
    }

    /**
     * Creates and styles the background of the scoreboard.
     */
    private void createBackground() {
        background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(CORNER_RADIUS);
        background.setArcHeight(CORNER_RADIUS);
        refreshBackgroundGradient(0, 0); // initial gradient setup

        // Add a drop shadow effect to the background
        DropShadow shadowEffect = new DropShadow();
        shadowEffect.setRadius(5.0);
        shadowEffect.setOffsetX(3.0);
        shadowEffect.setOffsetY(3.0);
        shadowEffect.setColor(Color.color(0.4, 0.4, 0.4));
        background.setEffect(shadowEffect);
    }

    /**
     * Initializes the text labels for scores and remaining tiles.
     */
    private void initializeScoreTexts() {
        humanScoreText = createScoreText("Human: 0", -50);
        computerScoreText = createScoreText("Computer: 0", 0);
        remainingTilesText = createScoreText("Tiles: 86", 50);
    }

    /**
     * Creates and styles individual score text.
     *
     * @param content The initial text content
     * @param verticalPosition The vertical position of the text
     * @return A styled Text object
     */
    private Text createScoreText(String content, double verticalPosition) {
        Text text = new Text(content);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        text.setTranslateY(verticalPosition);
        return text;
    }

    /**
     * Updates the human score display and refreshes the background gradient.
     *
     * @param score The new score for the human player
     */
    public void setHumanScore(int score) {
        humanScoreText.setText("Human: " + score);
        refreshBackgroundGradient(score, getComputerScore());
    }

    /**
     * Updates the computer score display and refreshes the background gradient.
     *
     * @param score The new score for the computer player
     */
    public void setComputerScore(int score) {
        computerScoreText.setText("Computer: " + score);
        refreshBackgroundGradient(getHumanScore(), score);
    }

    /**
     * Updates the remaining tiles display.
     *
     * @param tiles The number of remaining tiles
     */
    public void setRemainingTiles(int tiles) {
        remainingTilesText.setText("Tiles: " + tiles);
    }

    /**
     * Refreshes the background gradient based on current scores.
     *
     * @param humanScore The current human player score
     * @param computerScore The current computer player score
     */
    private void refreshBackgroundGradient(int humanScore, int computerScore) {
        // Calculate gradient colors based on the score ratio
        Color startColor = calculateGradientColor(Color.BLUE, Color.RED, calculateScoreRatio(humanScore, computerScore));
        Color endColor = calculateGradientColor(Color.RED, Color.BLUE, calculateScoreRatio(humanScore, computerScore));

        // Apply the new gradient to the background
        background.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, startColor),
                new Stop(1, endColor)));
    }

    /**
     * Calculates the score ratio for gradient interpolation.
     *
     * @param humanScore The current human player score
     * @param computerScore The current computer player score
     * @return The ratio of human score to total score
     */
    private double calculateScoreRatio(int humanScore, int computerScore) {
        if (humanScore + computerScore == 0) return 0.5; // Avoid division by zero
        return (double) humanScore / (humanScore + computerScore);
    }

    /**
     * Interpolates between two colors based on a given ratio.
     *
     * @param color1 The first color
     * @param color2 The second color
     * @param ratio The interpolation ratio
     * @return The interpolated color
     */
    private Color calculateGradientColor(Color color1, Color color2, double ratio) {
        double r = interpolateValue(color1.getRed(), color2.getRed(), ratio);
        double g = interpolateValue(color1.getGreen(), color2.getGreen(), ratio);
        double b = interpolateValue(color1.getBlue(), color2.getBlue(), ratio);
        return new Color(r, g, b, 1.0);
    }

    /**
     * Interpolates between two values based on a ratio.
     *
     * @param val1 The first value
     * @param val2 The second value
     * @param ratio The interpolation ratio
     * @return The interpolated value
     */
    private double interpolateValue(double val1, double val2, double ratio) {
        return val1 + (val2 - val1) * ratio;
    }

    /**
     * Retrieves the current human score from the text label.
     *
     * @return The current human score
     */
    private int getHumanScore() {
        return Integer.parseInt(humanScoreText.getText().split(": ")[1]);
    }

    /**
     * Retrieves the current computer score from the text label.
     *
     * @return The current computer score
     */
    private int getComputerScore() {
        return Integer.parseInt(computerScoreText.getText().split(": ")[1]);
    }
}