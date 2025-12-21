package com.sudoku.solver;

import java.util.Iterator;

public class PermutationIterator implements Iterator<int[]> {
  private final int numPositions;
  private final int[] currentCombination;
  private boolean hasNext;

  private static final int MAX_VALUE = 9;

  public PermutationIterator(int numPositions) {
    if (numPositions <= 0 || numPositions > 5) {
      throw new IllegalArgumentException("Solver is bounded to exactly 5 empty cells");
    }

    this.numPositions = numPositions;
    this.currentCombination = new int[numPositions];

    // Initialize to [1, 1, 1, 1, 1]
    for (int i = 0; i < numPositions; i++) {
      currentCombination[i] = 1;
    }

    this.hasNext = true;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public int[] next() {
    if (!hasNext) {
      throw new IllegalStateException("No more combinations");
    }

    int[] result = currentCombination.clone();
    advance();

    return result;
  }

  private void advance() {
    int position = numPositions - 1;

    while (position >= 0) {
      if (currentCombination[position] < MAX_VALUE) {
        currentCombination[position]++;
        return;
      } else {
        currentCombination[position] = 1;
        position--;
      }
    }

    hasNext = false;
  }
}