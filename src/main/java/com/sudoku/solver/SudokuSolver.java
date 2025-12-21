package com.sudoku.solver;

import com.sudoku.logic.SudokuVerifier;
import com.sudoku.model.VerificationState;
import com.sudoku.exception.InvalidGameException;
import java.util.ArrayList;
import java.util.List;

public class SudokuSolver {
  private final SudokuVerifier verifier;

  public SudokuSolver(SudokuVerifier verifier) {
    this.verifier = verifier;
  }

  public int[][] solve(int[][] board) throws InvalidGameException {
    List<int[]> emptyCells = findEmptyCells(board);

    if (emptyCells.size() != 5) {
      throw new InvalidGameException(
          "Solver requires exactly 5 empty cells, found: " + emptyCells.size());
    }

    PermutationIterator iterator = new PermutationIterator(5);

    while (iterator.hasNext()) {
      int[] combination = iterator.next();

      if (isValidCombination(board, emptyCells, combination)) {
        return buildSolution(emptyCells, combination);
      }
    }

    throw new InvalidGameException("No valid solution found for this board");
  }

  private List<int[]> findEmptyCells(int[][] board) {
    List<int[]> emptyCells = new ArrayList<>();

    for (int row = 0; row < 9; row++) {
      for (int col = 0; col < 9; col++) {
        if (board[row][col] == 0) {
          emptyCells.add(new int[] { row, col });
        }
      }
    }

    return emptyCells;
  }

  private boolean isValidCombination(int[][] board, List<int[]> emptyCells, int[] combination) {
    // Flyweight: Temporarily fill cells
    for (int i = 0; i < emptyCells.size(); i++) {
      int row = emptyCells.get(i)[0];
      int col = emptyCells.get(i)[1];
      board[row][col] = combination[i];
    }

    SudokuVerifier.VerificationResult result = verifier.verify(board);
    boolean isValid = (result.getState() == VerificationState.VALID);

    // Restore zeros
    for (int i = 0; i < emptyCells.size(); i++) {
      int row = emptyCells.get(i)[0];
      int col = emptyCells.get(i)[1];
      board[row][col] = 0;
    }

    return isValid;
  }

  private int[][] buildSolution(List<int[]> emptyCells, int[] combination) {
    int[][] solution = new int[emptyCells.size()][3];

    for (int i = 0; i < emptyCells.size(); i++) {
      solution[i][0] = emptyCells.get(i)[0];
      solution[i][1] = emptyCells.get(i)[1];
      solution[i][2] = combination[i];
    }

    return solution;
  }
}