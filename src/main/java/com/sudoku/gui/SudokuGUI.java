package com.sudoku.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sudoku.adapter.ControllerAdapter;
import com.sudoku.controller.SudokuController;
import com.sudoku.exception.InvalidGameException;
import com.sudoku.exception.NotFoundException;
import com.sudoku.exception.SolutionInvalidException;
import com.sudoku.view.Controllable;
import com.sudoku.view.UserAction;

public class SudokuGUI extends JFrame {
  private Controllable controller;
  private JTextField[][] cells;
  private JButton verifyButton;
  private JButton solveButton;
  private JButton undoButton;
  
  // DATA
  private int[][] currentBoard;
  private int[][] initialBoard; // Holds the clean puzzle state
  private char currentDifficulty;

  public SudokuGUI() {
    this.controller = new ControllerAdapter(new SudokuController());

    setTitle("Sudoku Game - Lab 10");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 700);
    setLocationRelativeTo(null);

    initializeGame();
  }

  private void initializeGame() {
    boolean[] catalog = controller.getCatalog();
    boolean hasCurrent = catalog[0];
    boolean hasAllModes = catalog[1];

    try {
      if (hasCurrent) {
        int response = JOptionPane.showConfirmDialog(this,
            "An unfinished game was found. Do you want to continue it?",
            "Resume Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
          loadGame('c'); // Resume
        } else {
          if (hasAllModes) askDifficulty();
          else askForSourceFile();
        }
      } else if (hasAllModes) {
        askDifficulty();
      } else {
        askForSourceFile();
      }
    } catch (Exception e) {
      showError("Initialization failed: " + e.getMessage());
      System.exit(1);
    }
  }

  private void askDifficulty() {
    String[] options = { "Easy", "Medium", "Hard" };
    int choice = JOptionPane.showOptionDialog(this, "Select difficulty level:",
        "Choose Difficulty", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, options, options[0]);

    try {
      switch (choice) {
        case 0: loadGame('e'); break;
        case 1: loadGame('m'); break;
        case 2: loadGame('h'); break;
        default: System.exit(0);
      }
    } catch (NotFoundException e) {
      showError("Failed to load game: " + e.getMessage());
      System.exit(1);
    }
  }

  private void askForSourceFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select Solved Sudoku File");
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      try {
        controller.driveGames(path);
        JOptionPane.showMessageDialog(this, "Games generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        askDifficulty();
      } catch (SolutionInvalidException e) {
        showError("Invalid source solution: " + e.getMessage());
        System.exit(1);
      }
    } else {
      System.exit(0);
    }
  }

  private void loadGame(char level) throws NotFoundException {
    // 1. LOAD DATA FIRST (Do not touch UI yet)
    currentBoard = controller.getGame(level);
    currentDifficulty = level;

    // 2. DETERMINE INITIAL STATE
    if (level == 'c') {
        // If resuming, try to load the 'initial' state we saved
        try {
            // Use 'i' to get the clean board from storage
            initialBoard = controller.getGame('i'); 
        } catch (Exception e) {
            // Fallback: If initial.txt missing, everything is locked
            initialBoard = currentBoard; 
        }
    } else {
        // If new game, the loaded board IS the initial board
        initialBoard = currentBoard;
    }

    // 3. BUILD UI NOW (Data is ready)
    buildUI();
  }

  private void buildUI() {
    getContentPane().removeAll(); // Clear old UI
    setLayout(new BorderLayout());

    // Create the grid using the loaded data
    JPanel gridPanel = createGridPanel();
    add(gridPanel, BorderLayout.CENTER);

    JPanel controlPanel = createControlPanel();
    add(controlPanel, BorderLayout.SOUTH);

    revalidate();
    repaint();
    setVisible(true);
  }

  private JPanel createGridPanel() {
    JPanel panel = new JPanel(new GridLayout(9, 9, 2, 2));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.setBackground(Color.BLACK);

    cells = new JTextField[9][9];

    for (int row = 0; row < 9; row++) {
      for (int col = 0; col < 9; col++) {
        JTextField cell = new JTextField();
        cell.setHorizontalAlignment(JTextField.CENTER);
        cell.setFont(new Font("Arial", Font.BOLD, 20));

        int currentValue = currentBoard[row][col];
        // Check the INITIAL board to see if this cell was originally empty
        int initialValue = (initialBoard != null) ? initialBoard[row][col] : currentValue;

        if (initialValue != 0) {
          // It was part of the original puzzle -> LOCK IT
          cell.setText(String.valueOf(currentValue));
          cell.setEditable(false);
          cell.setBackground(Color.LIGHT_GRAY);
        } else {
          // It was originally empty -> USER CAN EDIT
          if (currentValue != 0) {
             cell.setText(String.valueOf(currentValue));
          } else {
             cell.setText("");
          }
          cell.setBackground(Color.WHITE);
          cell.setEditable(true);

          final int r = row;
          final int c = col;

          cell.addFocusListener(new FocusAdapter() {
            private String oldValue = "";
            @Override
            public void focusGained(FocusEvent e) { oldValue = cell.getText(); }
            @Override
            public void focusLost(FocusEvent e) {
              String newValue = cell.getText().trim();
              if (!newValue.equals(oldValue)) {
                int oldVal = oldValue.isEmpty() ? 0 : Integer.parseInt(oldValue);
                int newVal = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
                currentBoard[r][c] = newVal;
                try {
                  UserAction action = new UserAction(r, c, newVal, oldVal);
                  controller.logUserAction(action);
                  controller.updateCurrentGame(currentBoard);
                } catch (IOException ex) {
                  showError("Failed to log move: " + ex.getMessage());
                }
                updateSolveButton();
              }
            }
          });
          
          cell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
              char c = e.getKeyChar();
              if (!Character.isDigit(c) || c == '0') { e.consume(); }
            }
          });
        }

        int top = (row % 3 == 0) ? 3 : 1;
        int left = (col % 3 == 0) ? 3 : 1;
        cell.setBorder(BorderFactory.createMatteBorder(top, left, 1, 1, Color.BLACK));

        cells[row][col] = cell;
        panel.add(cell);
      }
    }
    return panel;
  }

  // --- Standard Control Panel Logic ---
  private JPanel createControlPanel() {
    JPanel panel = new JPanel(new FlowLayout());
    verifyButton = new JButton("Verify");
    solveButton = new JButton("Solve");
    undoButton = new JButton("Undo");

    verifyButton.addActionListener(e -> verifyGame());
    solveButton.addActionListener(e -> solveGame());
    undoButton.addActionListener(e -> undoMove());

    updateSolveButton();
    panel.add(verifyButton);
    panel.add(solveButton);
    panel.add(undoButton);
    return panel;
  }

  private void updateSolveButton() {
    int emptyCount = 0;
    for (int row = 0; row < 9; row++) {
      for (int col = 0; col < 9; col++) {
        if (currentBoard[row][col] == 0) emptyCount++;
      }
    }
    solveButton.setEnabled(emptyCount == 5);
  }

  private void verifyGame() {
    boolean[][] validCells = controller.verifyGame(currentBoard);
    boolean isComplete = true;
    for (int[] row : currentBoard) {
        for (int val : row) if (val == 0) isComplete = false;
    }

    boolean hasInvalid = false;
    for (int row = 0; row < 9; row++) {
      for (int col = 0; col < 9; col++) {
        if (!validCells[row][col]) {
          cells[row][col].setBackground(Color.RED);
          hasInvalid = true;
        } else if (cells[row][col].isEditable()) {
          cells[row][col].setBackground(Color.WHITE);
        }
      }
    }

    if (isComplete) {
      if (!hasInvalid) {
        try {
          controller.markGameComplete(currentDifficulty);
          JOptionPane.showMessageDialog(this, "Congratulations! Puzzle solved correctly!", "Success", JOptionPane.INFORMATION_MESSAGE);
          System.exit(0);
        } catch (IOException e) {
          showError("Failed to mark game complete: " + e.getMessage());
        }
      } else {
        showError("Board is complete but contains errors!");
      }
    } else {
      if (hasInvalid) showError("Board contains errors (marked in red)");
      else JOptionPane.showMessageDialog(this, "Board is valid so far. Keep going!", "Verification", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void solveGame() {
    try {
      int[][] solution = controller.solveGame(currentBoard);
      for (int[] entry : solution) {
        int row = entry[0];
        int col = entry[1];
        int value = entry[2];
        currentBoard[row][col] = value;
        cells[row][col].setText(String.valueOf(value));
        cells[row][col].setBackground(Color.GREEN);
      }
      JOptionPane.showMessageDialog(this, "Solution found and applied!", "Solved", JOptionPane.INFORMATION_MESSAGE);
    } catch (InvalidGameException e) {
      showError("Cannot solve: " + e.getMessage());
    }
  }

  private void undoMove() {
    try {
      int[] moveData = controller.undoLastMove();
      if (moveData == null) {
        showError("No moves to undo");
        return;
      }
      int x = moveData[0], y = moveData[1], oldValue = moveData[3];
      currentBoard[x][y] = oldValue;
      if (oldValue == 0) cells[x][y].setText("");
      else cells[x][y].setText(String.valueOf(oldValue));
      controller.updateCurrentGame(currentBoard);
      updateSolveButton();
    } catch (IOException e) {
      showError("Undo failed: " + e.getMessage());
    }
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new SudokuGUI());
  }
}