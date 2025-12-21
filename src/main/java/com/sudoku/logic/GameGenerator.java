package com.sudoku.logic;

import com.sudoku.model.DifficultyEnum;
import com.sudoku.model.VerificationState;
import com.sudoku.exception.SolutionInvalidException;
import com.sudoku.storage.StorageManager;
import com.sudoku.util.RandomPairs;
import java.io.IOException;
import java.util.List;

public class GameGenerator {
  private final SudokuVerifier verifier;
  private final StorageManager storage;

  public GameGenerator(SudokuVerifier verifier, StorageManager storage) {
    this.verifier = verifier;
    this.storage = storage;
  }

  public void generateFromSolved(int[][] sourceSolution) throws SolutionInvalidException, IOException {
    // MUST verify source solution first
    SudokuVerifier.VerificationResult result = verifier.verify(sourceSolution);

    if (result.getState() != VerificationState.VALID) {
      throw new SolutionInvalidException(
          "Source solution is " + result.getState() + ": " + result.toFormattedString());
    }

    // Generate all three difficulty levels at once
    RandomPairs randomPairs = new RandomPairs();

    // Generate Easy
    int[][] easyBoard = cloneBoard(sourceSolution);
    removeCells(easyBoard, randomPairs.generateDistinctPairs(DifficultyEnum.EASY.getCellsToRemove()));
    storage.saveGame(DifficultyEnum.EASY, easyBoard);

    // Generate Medium
    int[][] mediumBoard = cloneBoard(sourceSolution);
    removeCells(mediumBoard, randomPairs.generateDistinctPairs(DifficultyEnum.MEDIUM.getCellsToRemove()));
    storage.saveGame(DifficultyEnum.MEDIUM, mediumBoard);

    // Generate Hard
    int[][] hardBoard = cloneBoard(sourceSolution);
    removeCells(hardBoard, randomPairs.generateDistinctPairs(DifficultyEnum.HARD.getCellsToRemove()));
    storage.saveGame(DifficultyEnum.HARD, hardBoard);
  }

  private void removeCells(int[][] board, List<int[]> positions) {
    for (int[] pos : positions) {
      int row = pos[0];
      int col = pos[1];
      board[row][col] = 0;
    }
  }

  private int[][] cloneBoard(int[][] original) {
    int[][] clone = new int[9][9];
    for (int row = 0; row < 9; row++) {
      System.arraycopy(original[row], 0, clone[row], 0, 9);
    }
    return clone;
  }
}