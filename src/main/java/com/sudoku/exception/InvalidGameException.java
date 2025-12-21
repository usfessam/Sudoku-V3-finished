package com.sudoku.exception;

public class InvalidGameException extends Exception {
  public InvalidGameException(String message) {
    super(message);
  }
}