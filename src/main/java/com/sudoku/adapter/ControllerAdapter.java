package com.sudoku.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sudoku.exception.InvalidGameException;
import com.sudoku.exception.NotFoundException;
import com.sudoku.exception.SolutionInvalidException;
import com.sudoku.facade.Viewable;
import com.sudoku.model.Catalog;
import com.sudoku.model.DifficultyEnum;
import com.sudoku.model.Game;
import com.sudoku.view.Controllable;
import com.sudoku.view.UserAction;

public class ControllerAdapter implements Controllable {
  private final Viewable controller;

  public ControllerAdapter(Viewable controller) {
    this.controller = controller;
  }

  @Override
  public boolean[] getCatalog() {
    Catalog catalog = controller.getCatalog();
    return new boolean[] {
        catalog.hasCurrent(),
        catalog.hasAllModes()
    };
  }

  @Override
  public int[][] getGame(char level) throws NotFoundException {
    Game game;
    // Map characters to Enums
    switch (Character.toLowerCase(level)) {
      case 'e': game = controller.getGame(DifficultyEnum.EASY); break;
      case 'm': game = controller.getGame(DifficultyEnum.MEDIUM); break;
      case 'h': game = controller.getGame(DifficultyEnum.HARD); break;
      case 'c': game = controller.getCurrentGame(); break;
      case 'i': // NEW: Fetch the initial clean board
         try {
           game = controller.getGame(DifficultyEnum.INITIAL);
         } catch (NotFoundException e) {
           // If initial doesn't exist, fallback to current
           game = controller.getCurrentGame();
         }
         break;
      default:
        throw new NotFoundException("Invalid difficulty level: " + level);
    }
    return game.getBoard();
  }

  // --- Keep the rest of the file exactly as it was ---
  @Override
  public void driveGames(String sourcePath) throws SolutionInvalidException {
    try {
      int[][] board = loadBoardFromFile(sourcePath);
      Game sourceGame = new Game(board);
      controller.driveGames(sourceGame);
    } catch (IOException e) {
      throw new SolutionInvalidException("Failed to load source file: " + e.getMessage());
    }
  }

  @Override
  public boolean[][] verifyGame(int[][] game) {
    Game gameObj = new Game(game);
    String result = controller.verifyGame(gameObj);

    boolean[][] validCells = new boolean[9][9];
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        validCells[i][j] = true;
      }
    }

    if (result.startsWith("invalid")) {
      String[] parts = result.split(" ");
      for (int i = 1; i < parts.length; i++) {
        String[] coords = parts[i].split(",");
        if (coords.length == 2) {
          int row = Integer.parseInt(coords[0]);
          int col = Integer.parseInt(coords[1]);
          validCells[row][col] = false;
        }
      }
    }
    return validCells;
  }

  @Override
  public int[][] solveGame(int[][] game) throws InvalidGameException {
    Game gameObj = new Game(game);
    return controller.solveGame(gameObj);
  }

  @Override
  public void logUserAction(UserAction userAction) throws IOException {
    controller.logUserAction(userAction.toString());
  }

  @Override
  public void updateCurrentGame(int[][] game) throws IOException {
    Game gameObj = new Game(game);
    controller.updateCurrentGame(gameObj);
  }

  @Override
  public int[] undoLastMove() throws IOException {
    return controller.undoLastMove();
  }

  @Override
  public void markGameComplete(char level) throws IOException {
    DifficultyEnum difficulty;
    switch (level) {
      case 'e': difficulty = DifficultyEnum.EASY; break;
      case 'm': difficulty = DifficultyEnum.MEDIUM; break;
      case 'h': difficulty = DifficultyEnum.HARD; break;
      default: throw new IOException("Invalid difficulty level: " + level);
    }
    controller.markGameComplete(difficulty);
  }

  private int[][] loadBoardFromFile(String path) throws IOException {
    int[][] board = new int[9][9];
    java.util.List<String> lines = Files.readAllLines(Paths.get(path));
    if (lines.size() < 9) throw new IOException("Invalid board file");
    for (int row = 0; row < 9; row++) {
      String line = lines.get(row).trim();
      String[] values = line.split("\\s+");
      if (values.length != 9) throw new IOException("Invalid row length");
      for (int col = 0; col < 9; col++) {
        board[row][col] = Integer.parseInt(values[col]);
      }
    }
    return board;
  }
}