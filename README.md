# Scrabble Game

This project implements a two-player Scrabble game where a human player competes against a computer opponent. The game features a graphical user interface built with JavaFX and includes separate components for score checking and move solving.
<img width="1400" alt="scrabble_gui" src="https://github.com/user-attachments/assets/96795c5b-a0cc-4cef-8558-f9031d7a25ee" />

## Features

- Two-player game: Human vs. Computer
- Graphical user interface using JavaFX
- Customizable computer player difficulty (Easy, Medium, Hard)
- Score tracking and display
- Tile placement with drag-and-drop functionality
- Word validation using a provided dictionary
- Special tile effects (double/triple letter/word scores)
- Separate solver component for finding optimal moves
- Score checking component for validating plays

## Setup

1. Ensure you have Java 8 or higher installed on your system.
2. Clone this repository or download the source code.
3. Compile the Java files or use the provided JAR files.

## Running the Components

The project consists of three main components, each with its own JAR file and specific way to provide input:

### 1. Score Checker

To run the score checker:

```
java -jar scorechecker.jar sowpods.txt < example_score_input.txt > your_output.txt
```

- `sowpods.txt`: The dictionary file to use for word validation.
- Input is redirected from `example_score_input.txt`.
- Output is redirected to `your_output.txt`.

### 2. Main Scrabble Game

To run the main Scrabble game with GUI:

```
java -jar scrabble.jar sowpods.txt scrabble_board.txt
```

- `sowpods.txt`: The dictionary file to use for word validation.
- `scrabble_board.txt`: The initial board configuration file.

### 3. Solver

To run the solver:

```
java -jar solver.jar sowpods.txt scrabble_tiles.txt example_input.txt > your_solver_output.txt
```

- `sowpods.txt`: The dictionary file to use for word validation.
- `scrabble_tiles.txt`: The file containing tile configurations and scores.
- `example_input.txt`: The input file with the board state and available tiles.
- Output is redirected to `your_solver_output.txt`.

Note: If you don't provide an input file for the solver, it will read from standard input.

## How to Play

1. Run the main Scrabble game as described above.
2. The game board will be displayed along with your tile rack.
3. To make a move:
   - Click the "Place Tiles" button.
   - Choose the play direction (Horizontal or Vertical).
   - Click on a tile in your rack, then click on the board to place it.
   - Repeat for each tile you want to play.
   - Click "Make Move" to submit your word.
4. Use the "Undo Last Move" button to remove the last placed tile.
5. Click "Forfeit Turn" if you can't make a move (not allowed on the first turn).
6. The computer will automatically make its move after you.
7. The game ends when all tiles are used or no more valid moves can be made.

## Computer Player Difficulty

You can adjust the computer player's difficulty using the dropdown menu:
- Easy: The computer will play suboptimal moves.
- Medium: The computer will play moderately strong moves.
- Hard: The computer will always try to play the highest-scoring move possible.

## Implementation Details

- The game uses a Trie data structure for efficient word lookup and validation.
- A backtracking algorithm is employed to generate possible moves for the computer player.
- The `ScrabbleMoveGenerator` class handles move generation and scoring.
- The GUI is implemented using JavaFX, with custom components for the game board and tile rack.

## Files and Classes

- `GameGui.java`: Main class for the graphical user interface
- `GameManager.java`: Manages the game state and flow
- `GameBoard.java`: Represents the Scrabble board
- `ComputerPlayer.java`: Implements the AI for the computer player
- `Dictionary.java`: Handles word validation
- `ScrabbleMoveGenerator.java`: Generates and evaluates possible moves
- `Solver.java`: Standalone component for finding optimal moves
- `ScoreChecker.java`: Validates and scores plays

## Known Issues

- There are some issues with solver.jar that I don't have energy or time to fix. Sometimes my solver finds a better move though... 8-)
- The game currently does not support loading custom dictionaries at runtime.
- There is no option to exchange tiles during a turn.

## Future Improvements

- Implement network play for human vs. human matches
- Add support for custom dictionaries
- Improve the GUI with animations and sound effects

## Contributors

