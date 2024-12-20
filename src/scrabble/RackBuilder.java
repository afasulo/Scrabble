package scrabble;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.FontWeight;


import java.util.List;
import java.util.ArrayList;

public class RackBuilder extends Canvas {
    private static final int TILE_SIZE = 40;
    private static final int TRAY_HEIGHT = 50;
    private static final int TRAY_WIDTH = 350;

    private Rack rack;
    private GraphicsContext gc;
    private List<Integer> playedTileIndices;
    private boolean isMakeMoveOn;
    private boolean lastTilePlayed;

    public RackBuilder(Rack rack) {
        this.rack = rack;
        this.gc = getGraphicsContext2D();
        this.setHeight(TRAY_HEIGHT);
        this.setWidth(TRAY_WIDTH);
        this.playedTileIndices = new ArrayList<>();
        this.isMakeMoveOn = false;
        this.lastTilePlayed = true;
    }

    public void draw() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        List<String> letters = rack.getLetters();
        for (int i = 0; i < letters.size(); i++) {
            drawTile(i * TILE_SIZE, 0, letters.get(i), i);
        }
    }

    private void drawTile(int x, int y, String letter, int index) {
        // Define colors
        Color tileColor = Color.ANTIQUEWHITE;
        Color borderColor = Color.GREEN;
        Color textColor = Color.BLACK;
        Color shadowColor = Color.rgb(0, 0, 0, 0.3);

        // Create shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(3);
        dropShadow.setOffsetY(3);
        dropShadow.setColor(shadowColor);

        // Draw tile background with rounded corners
        gc.setEffect(dropShadow);
        gc.setFill(tileColor);
        gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 10, 10);

        // Draw border
        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, TILE_SIZE, TILE_SIZE, 10, 10);

        // Remove effect for text
        gc.setEffect(null);

        // Draw letter
        gc.setFill(textColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(letter, x + TILE_SIZE / 2, y + TILE_SIZE * 0.65);

        // Draw score
        int score = rack.getAllTiles().get(index).getPointValue();
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        gc.fillText(String.valueOf(score), x + TILE_SIZE * 0.8, y + TILE_SIZE * 0.9);

        // Highlight played tiles
        if (playedTileIndices.contains(index)) {
            gc.setFill(Color.rgb(0, 255, 0, 0.3));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 10, 10);
        }
    }

    public void setRack(Rack rack) {
        this.rack = rack;
        draw();
    }


    public List<Integer> getPlayedTileIndices() {
        return playedTileIndices;
    }

    public boolean isMakeMoveOn() {
        return isMakeMoveOn;
    }

    public void setMakeMoveOn(boolean makeMoveOn) {
        this.isMakeMoveOn = makeMoveOn;
    }

    public boolean isLastTilePlayed() {
        return lastTilePlayed;
    }

    public void setLastTilePlayed(boolean lastTilePlayed) {
        this.lastTilePlayed = lastTilePlayed;
    }

}