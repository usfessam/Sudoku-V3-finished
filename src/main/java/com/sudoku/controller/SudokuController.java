package com.sudoku.controller;

import com.sudoku.facade.Viewable;
import com.sudoku.model.*;
import com.sudoku.exception.*;
import com.sudoku.logic.*;
import com.sudoku.solver.*;
import com.sudoku.storage.StorageManager;
import java.io.IOException;

public class SudokuController implements Viewable {
  private final SudokuVerifier verifier;
  private final SudokuSolver solver;
  private final GameGenerator generator;
  private final StorageManager storage;

  public SudokuController() {
    this.verifier = new SudokuVerifier();
    this.solver = new SudokuSolver(verifier);
    this.storage = new StorageManager();
    this.generator = new GameGenerator(verifier, storage);
  }

  @Override
  public Catalog getCatalog() {
    boolean hasCurrent = storage.hasCurrentGame();
    boolean hasAllModes = storage.hasAllDifficulties();
    return new Catalog(hasCurrent, hasAllModes);
  }

  @Override
  public Game getGame(DifficultyEnum level) throws NotFoundException {
    try {
      int[][] board = storage.loadGame(level);
      return new Game(board);
    } catch (IOException e) {
      throw new NotFoundException("No game found for difficulty: " + level);
    }
  }

  @Override
  public Game getCurrentGame() throws NotFoundException {
    try {
      int[][] board = storage.loadCurrentGame();
      return new Game(board);
    } catch (IOException e) {
      throw new NotFoundException("No current game found");
    }
  }

  @Override
  public void driveGames(Game sourceGame) throws SolutionInvalidException {
    try {
      generator.generateFromSolved(sourceGame.getBoard());
    } catch (IOException e) {
      throw new SolutionInvalidException("Failed to generate games: " + e.getMessage());
    }
  }

  @Override
  public String verifyGame(Game game) {
    SudokuVerifier.VerificationResult result = verifier.verify(game.getBoard());
    return result.toFormattedString();
  }

  @Override
  public int[][] solveGame(Game game) throws InvalidGameException {
    return solver.solve(game.getBoard());
  }

  @Override
  public void logUserAction(String userAction) throws IOException {
    String cleaned = userAction.replaceAll("[()]", "").trim();
    String[] parts = cleaned.split(",");

    if (parts.length == 4) {
      int x = Integer.parseInt(parts[0].trim());
      int y = Integer.parseInt(parts[1].trim());
      int newVal = Integer.parseInt(parts[2].trim());
      int oldVal = Integer.parseInt(parts[3].trim());

      storage.logMove(x, y, newVal, oldVal);
    }
  }

  @Override
  public void updateCurrentGame(Game game) throws IOException {
    storage.saveCurrentGame(game.getBoard());
  }

  @Override
  public int[] undoLastMove() throws IOException {
    return storage.undoLastMove();
  }

  @Override
  public void markGameComplete(DifficultyEnum difficulty) throws IOException {
    storage.deleteGame(difficulty);
    storage.clearCurrentGame();
  }
}