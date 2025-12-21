package com.sudoku.facade;

import com.sudoku.model.Catalog;
import com.sudoku.model.Game;
import com.sudoku.model.DifficultyEnum;
import com.sudoku.exception.*;
import java.io.IOException;

public interface Viewable {

  Catalog getCatalog();

  Game getGame(DifficultyEnum level) throws NotFoundException;

  Game getCurrentGame() throws NotFoundException;

  void driveGames(Game sourceGame) throws SolutionInvalidException;

  String verifyGame(Game game);

  int[][] solveGame(Game game) throws InvalidGameException;

  void logUserAction(String userAction) throws IOException;

  void updateCurrentGame(Game game) throws IOException;

  int[] undoLastMove() throws IOException;

  void markGameComplete(DifficultyEnum difficulty) throws IOException;
}