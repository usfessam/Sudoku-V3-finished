package com.sudoku.model;

public class Game {
  private int[][] board;

  public Game(int[][] board) {
    // IMPORTANT: Use reference, not deep copy
    this.board = board;
  }

  public int[][] getBoard() {
    return board;
  }

  public void setBoard(int[][] board) {
    this.board = board;
  }

  public int getCell(int row, int col) {
    return board[row][col];
  }

  public void setCell(int row, int col, int value) {
    board[row][col] = value;
  }
}