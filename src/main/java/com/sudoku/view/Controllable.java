package com.sudoku.view;

import com.sudoku.exception.*;
import java.io.IOException;

public interface Controllable {

  boolean[] getCatalog();

  int[][] getGame(char level) throws NotFoundException;

  void driveGames(String sourcePath) throws SolutionInvalidException;

  boolean[][] verifyGame(int[][] game);

  int[][] solveGame(int[][] game) throws InvalidGameException;

  void logUserAction(UserAction userAction) throws IOException;

  void updateCurrentGame(int[][] game) throws IOException;

  int[] undoLastMove() throws IOException;

  void markGameComplete(char level) throws IOException;
}