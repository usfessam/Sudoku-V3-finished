package com.sudoku.storage;

import com.sudoku.model.DifficultyEnum;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
  private static final String BASE_DIR = "sudoku_games";
  private static final String CURRENT_DIR = "current";
  private static final String LOG_FILE = "game.log";
  private static final String GAME_FILE = "game.txt";

  private final Path basePath;

  public StorageManager() {
    this.basePath = Paths.get(BASE_DIR);
    initializeDirectories();
  }

  private void initializeDirectories() {
    try {
      Files.createDirectories(basePath);
      Files.createDirectories(basePath.resolve(CURRENT_DIR));
      Files.createDirectories(basePath.resolve(DifficultyEnum.EASY.getFolderName()));
      Files.createDirectories(basePath.resolve(DifficultyEnum.MEDIUM.getFolderName()));
      Files.createDirectories(basePath.resolve(DifficultyEnum.HARD.getFolderName()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize storage directories", e);
    }
  }

  public void saveGame(DifficultyEnum difficulty, int[][] board) throws IOException {
    Path difficultyPath = basePath.resolve(difficulty.getFolderName());

    int index = 1;
    Path filePath;
    do {
      filePath = difficultyPath.resolve("game_" + index + ".txt");
      index++;
    } while (Files.exists(filePath));

    writeBoardToFile(board, filePath);
  }

  public void saveCurrentGame(int[][] board) throws IOException {
    Path currentPath = basePath.resolve(CURRENT_DIR).resolve(GAME_FILE);
    writeBoardToFile(board, currentPath);
  }

  public int[][] loadCurrentGame() throws IOException {
    Path currentPath = basePath.resolve(CURRENT_DIR).resolve(GAME_FILE);
    if (!Files.exists(currentPath)) {
      throw new FileNotFoundException("No current game found");
    }
    return readBoardFromFile(currentPath);
  }

  public int[][] loadGame(DifficultyEnum difficulty) throws IOException {
    Path difficultyPath = basePath.resolve(difficulty.getFolderName());
    List<Path> gameFiles = listGameFiles(difficultyPath);

    if (gameFiles.isEmpty()) {
      throw new FileNotFoundException("No games found for difficulty: " + difficulty);
    }

    Path gameFile = gameFiles.get(0);
    int[][] board = readBoardFromFile(gameFile);

    saveCurrentGame(board);

    return board;
  }

  public void deleteGame(DifficultyEnum difficulty) throws IOException {
    Path difficultyPath = basePath.resolve(difficulty.getFolderName());
    List<Path> gameFiles = listGameFiles(difficultyPath);

    if (!gameFiles.isEmpty()) {
      Files.deleteIfExists(gameFiles.get(0));
    }
  }

  public void clearCurrentGame() throws IOException {
    Path currentPath = basePath.resolve(CURRENT_DIR);
    Files.deleteIfExists(currentPath.resolve(GAME_FILE));
    Files.deleteIfExists(currentPath.resolve(LOG_FILE));
  }

  public boolean hasCurrentGame() {
    Path currentPath = basePath.resolve(CURRENT_DIR).resolve(GAME_FILE);
    return Files.exists(currentPath);
  }

  public boolean hasAllDifficulties() {
    try {
      for (DifficultyEnum diff : DifficultyEnum.values()) {
        Path diffPath = basePath.resolve(diff.getFolderName());
        if (listGameFiles(diffPath).isEmpty()) {
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void writeBoardToFile(int[][] board, Path filePath) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
      for (int row = 0; row < 9; row++) {
        for (int col = 0; col < 9; col++) {
          writer.write(String.valueOf(board[row][col]));
          if (col < 8)
            writer.write(" ");
        }
        writer.newLine();
      }
    }
  }

  private int[][] readBoardFromFile(Path filePath) throws IOException {
    int[][] board = new int[9][9];

    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
      for (int row = 0; row < 9; row++) {
        String line = reader.readLine();
        if (line == null) {
          throw new IOException("Invalid board file: insufficient rows");
        }

        String[] values = line.trim().split("\\s+");
        if (values.length != 9) {
          throw new IOException("Invalid board file: row " + row + " has " + values.length + " values");
        }

        for (int col = 0; col < 9; col++) {
          board[row][col] = Integer.parseInt(values[col]);
        }
      }
    }

    return board;
  }

  private List<Path> listGameFiles(Path directory) throws IOException {
    List<Path> gameFiles = new ArrayList<>();

    if (Files.exists(directory)) {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.txt")) {
        for (Path entry : stream) {
          gameFiles.add(entry);
        }
      }
    }

    return gameFiles;
  }

  public void logMove(int x, int y, int newValue, int oldValue) throws IOException {
    Path logPath = basePath.resolve(CURRENT_DIR).resolve(LOG_FILE);
    String logEntry = String.format("%d,%d,%d,%d%n", x, y, newValue, oldValue);

    Files.write(logPath, logEntry.getBytes(),
        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  public int[] undoLastMove() throws IOException {
    Path logPath = basePath.resolve(CURRENT_DIR).resolve(LOG_FILE);

    if (!Files.exists(logPath)) {
      return null;
    }

    List<String> lines = Files.readAllLines(logPath);
    if (lines.isEmpty()) {
      return null;
    }

    String lastLine = lines.get(lines.size() - 1);
    String[] parts = lastLine.split(",");

    if (parts.length != 4) {
      throw new IOException("Invalid log entry format");
    }

    int[] moveData = new int[4];
    for (int i = 0; i < 4; i++) {
      moveData[i] = Integer.parseInt(parts[i].trim());
    }

    lines.remove(lines.size() - 1);
    Files.write(logPath, lines);

    return moveData;
  }
}