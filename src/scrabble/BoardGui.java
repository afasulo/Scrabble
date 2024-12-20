package scrabble;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.TextAlignment;


public class BoardGui extends Canvas {
    private GraphicsContext gc;
    private GameBoard gameBoard;
    public static final int SQUARE_SIZE = 40;
    private static final Font LETTER_FONT = Font.font("Arial", FontWeight.BOLD, 24);
    private static final Font SCORE_FONT = Font.font("Arial", FontWeight.NORMAL, 10);

    public BoardGui(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        gc = this.getGraphicsContext2D();
        this.setHeight(gameBoard.getDimension() * SQUARE_SIZE);
        this.setWidth(gameBoard.getDimension() * SQUARE_SIZE);
    }

    public void draw() {
        int dimension = gameBoard.getDimension();
        DropShadow shadow = new DropShadow(4, 2, 2, Color.rgb(0, 0, 0, 0.3));

        // Draw the board
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                int x = col * SQUARE_SIZE;
                int y = row * SQUARE_SIZE;
                Tile currentTile = gameBoard.getTile(row, col);

                // Determine the fill color based on multipliers
                setFillColorForTile(currentTile);

                // Fill the square with rounded corners
                gc.fillRoundRect(x, y, SQUARE_SIZE, SQUARE_SIZE, 8, 8);

                // Draw the border around the square
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(1.5);
                gc.setEffect(shadow);
                gc.strokeRoundRect(x, y, SQUARE_SIZE, SQUARE_SIZE, 8, 8);
                gc.setEffect(null);

                // Draw the letter or multiplier text
                if (!currentTile.isEmpty()) {
                    drawLetter(currentTile, x, y);
                } else {
                    drawMultiplierText(currentTile, x, y);
                }
            }
        }
    }

    private void setFillColorForTile(Tile tile) {
        switch (tile.getMultiType()) {
            case 'L' -> gc.setFill(tile.getMultiplier() == 2 ? Color.LIGHTSKYBLUE : Color.MEDIUMBLUE);
            case 'W' -> gc.setFill(tile.getMultiplier() == 2 ? Color.PINK : Color.CRIMSON);
            default -> gc.setFill(Color.FLORALWHITE);
        }
    }

    private void drawLetter(Tile tile, int x, int y) {
        String letter = tile.getLetter().toLowerCase();
        gc.setFill(gc.getFill().equals(Color.MEDIUMBLUE) ? Color.WHITE : Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(letter, x + SQUARE_SIZE / 2, y + SQUARE_SIZE * 0.65);

        // Draw score
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        gc.fillText(String.valueOf(tile.getPointValue()), x + SQUARE_SIZE * 0.8, y + SQUARE_SIZE * 0.9);
    }

    private void drawMultiplierText(Tile tile, int x, int y) {
        if (tile.hasMulti()) {
            String multiplierText = tile.getMultiplier() + (tile.getMultiType() == 'W' ? "W" : "L");
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(multiplierText, x + SQUARE_SIZE / 2, y + SQUARE_SIZE / 2);
        }
    }

    public void setBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public GameBoard getBoard() {
        return gameBoard;
    }
}