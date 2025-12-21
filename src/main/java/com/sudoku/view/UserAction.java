package com.sudoku.view;

public class UserAction {
  private int row;
  private int col;
  private int newValue;
  private int oldValue;

  public UserAction(int row, int col, int newValue, int oldValue) {
    this.row = row;
    this.col = col;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public int getNewValue() {
    return newValue;
  }

  public int getOldValue() {
    return oldValue;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d, %d, %d)", row, col, newValue, oldValue);
  }
}