package com.sudoku.model;

public enum DifficultyEnum {
  EASY(10, "easy"),
  MEDIUM(20, "medium"),
  HARD(25, "hard"),
  INITIAL(0, "incomplete"); // Used to save the 'Clean' state

  private final int cellsToRemove;
  private final String folderName;

  DifficultyEnum(int cellsToRemove, String folderName) {
    this.cellsToRemove = cellsToRemove;
    this.folderName = folderName;
  }

  public int getCellsToRemove() {
    return cellsToRemove;
  }

  public String getFolderName() {
    return folderName;
  }
}