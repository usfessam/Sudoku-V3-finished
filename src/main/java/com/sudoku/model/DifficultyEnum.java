package com.sudoku.model;

public enum DifficultyEnum {
  EASY(10),
  MEDIUM(20),
  HARD(25);

  private final int cellsToRemove;

  DifficultyEnum(int cellsToRemove) {
    this.cellsToRemove = cellsToRemove;
  }

  public int getCellsToRemove() {
    return cellsToRemove;
  }

  public String getFolderName() {
    return this.name().toLowerCase();
  }
}