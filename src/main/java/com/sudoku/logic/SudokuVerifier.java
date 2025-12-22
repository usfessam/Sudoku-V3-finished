package com.sudoku.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sudoku.model.VerificationState;

public class SudokuVerifier {

  public VerificationResult verify(int[][] board) {
    // 1. Check for rule violations FIRST (Rows, Cols, Boxes)
    List<String> invalidPositions = new ArrayList<>();

    // Check all rows
    for (int row = 0; row < 9; row++) {
      List<String> rowViolations = checkRow(board, row);
      invalidPositions.addAll(rowViolations);
    }

    // Check all columns
    for (int col = 0; col < 9; col++) {
      List<String> colViolations = checkColumn(board, col);
      invalidPositions.addAll(colViolations);
    }

    // Check all 3x3 subgrids
    for (int boxRow = 0; boxRow < 3; boxRow++) {
      for (int boxCol = 0; boxCol < 3; boxCol++) {
        List<String> boxViolations = checkBox(board, boxRow, boxCol);
        invalidPositions.addAll(boxViolations);
      }
    }

    // 2. Decision Logic:
    // If we found ANY violations, the board is INVALID (regardless of zeros)
    if (!invalidPositions.isEmpty()) {
      return new VerificationResult(VerificationState.INVALID, invalidPositions);
    }

    // 3. If no violations, THEN check if it's incomplete
    if (hasZeros(board)) {
      return new VerificationResult(VerificationState.INCOMPLETE, new ArrayList<>());
    }

    // 4. If neither, it is VALID
    return new VerificationResult(VerificationState.VALID, invalidPositions);
  }

  private boolean hasZeros(int[][] board) {
    for (int row = 0; row < 9; row++) {
      for (int col = 0; col < 9; col++) {
        if (board[row][col] == 0) {
          return true;
        }
      }
    }
    return false;
  }

  private List<String> checkRow(int[][] board, int row) {
    List<String> violations = new ArrayList<>();
    Set<Integer> seen = new HashSet<>();

    for (int col = 0; col < 9; col++) {
      int value = board[row][col];
      if (value != 0) {
        if (!seen.add(value)) {
          violations.add(row + "," + col);
        }
      }
    }
    return violations;
  }

  private List<String> checkColumn(int[][] board, int col) {
    List<String> violations = new ArrayList<>();
    Set<Integer> seen = new HashSet<>();

    for (int row = 0; row < 9; row++) {
      int value = board[row][col];
      if (value != 0) {
        if (!seen.add(value)) {
          violations.add(row + "," + col);
        }
      }
    }
    return violations;
  }

  private List<String> checkBox(int[][] board, int boxRow, int boxCol) {
    List<String> violations = new ArrayList<>();
    Set<Integer> seen = new HashSet<>();

    int startRow = boxRow * 3;
    int startCol = boxCol * 3;

    for (int row = startRow; row < startRow + 3; row++) {
      for (int col = startCol; col < startCol + 3; col++) {
        int value = board[row][col];
        if (value != 0) {
          if (!seen.add(value)) {
            violations.add(row + "," + col);
          }
        }
      }
    }
    return violations;
  }

  public static class VerificationResult {
    private final VerificationState state;
    private final List<String> invalidPositions;

    public VerificationResult(VerificationState state, List<String> invalidPositions) {
      this.state = state;
      this.invalidPositions = invalidPositions;
    }

    public VerificationState getState() {
      return state;
    }

    public List<String> getInvalidPositions() {
      return invalidPositions;
    }

    public String toFormattedString() {
      if (state == VerificationState.VALID) {
        return "valid";
      } else if (state == VerificationState.INCOMPLETE) {
        return "incomplete";
      } else {
        return "invalid " + String.join(" ", invalidPositions);
      }
    }
  }
}