/**
 * @author Syukri Mullia Adil P
 * @author Martin Novela
 * @author Dhanang Hadhi Sasmita
 * @version 2016.10.11 7.57 PM
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

public class SudokuSolver {
	// Constants
	public static final int N_SUDOKU = Integer.parseInt(JOptionPane.showInputDialog("Insert Sudoku size (integer): "));
	public static final int SUDOKU_SIZE = N_SUDOKU * N_SUDOKU;

	public static final int ENTRY_SIZE = 25;
	public static final int ENTRY_FONT_SIZE = 20;
	public static final int TITLE_FONT_SIZE = 10 + N_SUDOKU * 3;
	public static final int BTN_FONT_SIZE = 15;

	public static final String FONT_NAME = "Consolas";

	public static final int SIDE_PANEL_WIDTH = 60;
	public static final int TITLE_PANEL_WIDTH = 30 + N_SUDOKU * 9;
	public static final int SOLVE_BTN_PANEL_WIDTH = 60;
	public static final int FRAME_WIDTH = 200 + ENTRY_SIZE * SUDOKU_SIZE;
	public static final int FRAME_HEIGHT = 240 + ENTRY_SIZE * SUDOKU_SIZE;

	public static final Color BG_COLOR = Color.DARK_GRAY;
	public static final Color TEXT_COLOR = Color.LIGHT_GRAY;
	public static final Color ENTRY_BTN_BG_COLOR = Color.WHITE;
	public static final Color ENTRY_TEXT_COLOR = Color.DARK_GRAY;
	public static final Color ENTRY_TEXT_COLOR_RED = Color.RED;
	public static final Color BTN_BG_COLOR = Color.WHITE;
	public static final Color BTN_TEXT_COLOR = Color.DARK_GRAY;

	public static final int SUBGRID_MARGIN = 5;
	public static final int ENTRY_MARGIN = 2;

	public static final long NUM_OF_LITERALS = (long) Math.pow(N_SUDOKU, 6);
	public static final int[][][] LITERALS = new int[SUDOKU_SIZE][SUDOKU_SIZE][SUDOKU_SIZE];

	public static final boolean MINISAT_INSTALLED = false;
	public static final String MINISAT_COMPILED_DIR = "./minisat/core/minisat";
	public static final String MINISAT_INPUT_FILE_NAME = "input.in";
	public static final String MINISAT_OUTPUT_FILE_NAME = "output.out";

	public static HashMap<String,Integer> filledEntries = new HashMap<String,Integer>();
	public static JButton[][] allEntries = new JButton[SUDOKU_SIZE][SUDOKU_SIZE];

	public static LinkedList<String> allClauses = new LinkedList<String>();
	public static int totalClause = 0;
	public static boolean solved = false;

	public static void main(String[] args) throws Exception {
		if (N_SUDOKU <= 0) {
			throw new Exception("Invalid sudoku size: " + N_SUDOKU); 
		}

		// Main frame
		JFrame mainFrame = new JFrame("DMS Sudoku Solver!");
		mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

		// Main panel
		JPanel mainPanel = new JPanel();
		BorderLayout mainPanelLayout = new BorderLayout();
		mainPanel.setLayout(mainPanelLayout);

		// Title texts
		JPanel titlePanelWrapper = new JPanel();
		titlePanelWrapper.setLayout(new GridLayout(1, 1));
		JLabel title = new JLabel(SUDOKU_SIZE + "x" + SUDOKU_SIZE + " Sudoku Solver");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(new Font(FONT_NAME, 1, TITLE_FONT_SIZE));
		title.setForeground(TEXT_COLOR);
		titlePanelWrapper.setBackground(BG_COLOR);
		titlePanelWrapper.add(title);

		// Title panel
		JPanel titlePanel = new JPanel();
		titlePanel.setPreferredSize(new Dimension(FRAME_WIDTH, TITLE_PANEL_WIDTH));
		titlePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		titlePanel.setBackground(BG_COLOR);
		titlePanel.add(titlePanelWrapper, gbc);

		// Sudoku panel
		JPanel sudokuPanel = new JPanel();
		GridLayout sudokuPanelLayout = new GridLayout(N_SUDOKU, N_SUDOKU);
		sudokuPanelLayout.setHgap(SUBGRID_MARGIN);
		sudokuPanelLayout.setVgap(SUBGRID_MARGIN);
		sudokuPanel.setLayout(sudokuPanelLayout);
		sudokuPanel.setBackground(BG_COLOR);

		int count = 1;
		for (int i = 0; i < (SUDOKU_SIZE); i++) {
			// Sub-grid panel
			JPanel subgridPanel = new JPanel();
			GridLayout subgridPanelLayout = new GridLayout(N_SUDOKU, N_SUDOKU);
			subgridPanelLayout.setHgap(ENTRY_MARGIN);
			subgridPanelLayout.setVgap(ENTRY_MARGIN);
			for (int j = 0; j < (SUDOKU_SIZE); j++) {
				final JButton entry = new JButton("");
				entry.setPreferredSize(new Dimension(ENTRY_SIZE, ENTRY_SIZE));
				entry.setFont(new Font(FONT_NAME, Font.PLAIN, ENTRY_FONT_SIZE));
				entry.setBorder(null);
				entry.setForeground(ENTRY_TEXT_COLOR_RED);
				entry.setBackground(ENTRY_BTN_BG_COLOR);
				entry.setOpaque(true);
				subgridPanel.add(entry);

				final int I_BASE = i;
				final int J_BASE = j;
				
				// To convert grid indices
				final int I_FINAL = N_SUDOKU * (i / N_SUDOKU) + (j / N_SUDOKU);
				final int J_FINAL = N_SUDOKU * (i % N_SUDOKU) + (j % N_SUDOKU);

				// To save the button in array
				allEntries[I_FINAL][J_FINAL] = entry;
				// To fill in all literals
				for (int k = 0; k < SUDOKU_SIZE; k++) {
					LITERALS[i][j][k] = count++;
				}

				class FillEntryButtonListener implements ActionListener {
					@Override
					public void actionPerformed(ActionEvent ae) {
						String entryChosenStr = (String) JOptionPane.showInputDialog(null, "Type \"c\" to clear", "Fill entry with:", JOptionPane.QUESTION_MESSAGE);
						boolean invalid = false;
						if (entryChosenStr != null) {
							try {
								int entryChosen = Integer.parseInt(entryChosenStr);
								if (entryChosen < 0 || entryChosen > (SUDOKU_SIZE)) {
									throw new Exception();
								}
							} catch (Exception e) {
								if (!entryChosenStr.equals("c")) {
									JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
									invalid = true;
								}
							}
						}
						if (!invalid) {
							entry.setForeground(ENTRY_TEXT_COLOR_RED);
							if (entryChosenStr.equals("c")) {
								entry.setText("");
								filledEntries.remove(I_FINAL + "," + J_FINAL);
								System.out.println(I_FINAL + "," + J_FINAL + " -> cleared. " + filledEntries.size() + " of " + SUDOKU_SIZE * SUDOKU_SIZE + " entryies filled out.");
							} else {
								entry.setText(entryChosenStr);	
								int entryChosen = Integer.parseInt(entryChosenStr);
								filledEntries.put(I_FINAL + "," + J_FINAL, entryChosen);
								System.out.println(I_FINAL + "," + J_FINAL + " -> " + entryChosen + ". " + filledEntries.size() + " of " + SUDOKU_SIZE * SUDOKU_SIZE + " entryies filled out.");
							}	
						}
					}
				}
				entry.addActionListener(new FillEntryButtonListener());
			}
			subgridPanel.setLayout(subgridPanelLayout);
			subgridPanel.setBackground(BG_COLOR);
			sudokuPanel.add(subgridPanel);
		}

		// Buttons panel		
		JPanel solvePanel = new JPanel();
		solvePanel.setPreferredSize(new Dimension(FRAME_WIDTH, SOLVE_BTN_PANEL_WIDTH));
		solvePanel.setLayout(new GridBagLayout());
		JButton solveButton = new JButton("Solve!");
		solveButton.setFont(new Font(FONT_NAME, Font.PLAIN, BTN_FONT_SIZE));
		solveButton.setForeground(BTN_TEXT_COLOR);
		solveButton.setBackground(BTN_BG_COLOR);
		solveButton.setOpaque(true);
		solvePanel.add(solveButton, new GridBagConstraints());
		solvePanel.setBackground(BG_COLOR);

		class SolveButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent ae) {
				// TODO: Solve the Sudoku
				if (!solved) {
					// 1.1: Each field contains at least one digit
					String clauses1 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							String clause = "";
							for (int k = 1; k <= SUDOKU_SIZE; k++) {
								clause += ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * j) + k) + " ";
							}
							clauses1 += clause + "0\n";
							totalClause++;
							System.out.print("Clause " + totalClause + ": " + clause);
						}
					}
					allClauses.add(clauses1);

					// 1.2: No field contains two digits
					String clauses2 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							for (int k1 = 1; k1 <= SUDOKU_SIZE; k1++) {
								for (int k2 = k1 + 1; k2 <= SUDOKU_SIZE; k2++) {
									String k1Str = "-" + ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * j) + k1);
									String k2Str = "-" + ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * j) + k2);
									String clause = k1Str + " " + k2Str + " 0\n";
									clauses2 += clause;
									totalClause++;
									System.out.print("Clause " + totalClause + ": " + clause);
								}
							}
						}
					}
					allClauses.add(clauses2);

					// 1.3: Each digit must occur at least once in each row
					String clauses3 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 1; j <= SUDOKU_SIZE; j++) {
							String clause = "";
							for (int k = 0; k < SUDOKU_SIZE; k++) {
								clause += ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * k) + j) + " ";
							}
							clauses3 += clause + "0\n";
							totalClause++;
							System.out.print("Clause " + totalClause + ": " + clause);
						}
					}
					allClauses.add(clauses3);

					// 1.4: 1.3 + and not more than once
					String clauses4 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 1; j <= SUDOKU_SIZE; j++) {
							for (int k1 = 0; k1 < SUDOKU_SIZE; k1++) {
								for (int k2 = k1 + 1; k2 < SUDOKU_SIZE; k2++) {
									String k1Str = "-" + ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * k1) + j);
									String k2Str = "-" + ((SUDOKU_SIZE * SUDOKU_SIZE * i) + (SUDOKU_SIZE * k2) + j);
									String clause = k1Str + " " + k2Str + " 0\n";
									clauses4 += clause;
									totalClause++;
									System.out.print("Clause " + totalClause + ": " + clause);
								}
							}
						}
					}
					allClauses.add(clauses4);

					// 1.5: Each digit must occur at least once in a column
					String clauses5 = "";
					for (int i = 1; i <= SUDOKU_SIZE * SUDOKU_SIZE; i++) {
						String clause = "";
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							clause += (SUDOKU_SIZE * SUDOKU_SIZE * j) + i + " ";
						}
						clauses5 += clause + "0\n";
						totalClause++;
						System.out.print("Clause " + totalClause + ": " + clause);
					}
					allClauses.add(clauses5);

					// 1.6: 1.5 + and not more than once
					String clauses6 = "";
					for (int i = 1; i <= SUDOKU_SIZE * SUDOKU_SIZE; i++) {
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							for (int k = j + 1; k <= SUDOKU_SIZE - 1; k++) {
								String k1Str = "-" + (SUDOKU_SIZE * SUDOKU_SIZE * j + i);
								String k2Str = "-" + (SUDOKU_SIZE * SUDOKU_SIZE * k + i);
								String clause = k1Str + " " + k2Str + " 0\n";
								clauses6 += clause;
								totalClause++;
								System.out.print("Clause " + totalClause + ": " + clause);
							}
						}
					}
					allClauses.add(clauses6);

					// 1.7: Each digit must occur at least once in a subgrid
					String clauses7 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							String clause = "";
							for (int k = 0; k < SUDOKU_SIZE; k++) {
								int jj = N_SUDOKU * (j / N_SUDOKU) + (k / N_SUDOKU);
								int kk = N_SUDOKU * (j % N_SUDOKU) + (k % N_SUDOKU);
								clause += LITERALS[kk][jj][i] + " ";
							}
							clauses7 += clause + "0\n";
							totalClause++;
							System.out.print("Clause " + totalClause + ": " + clause);
						}
					}
					allClauses.add(clauses7);

					// 1.8: 1.7 + and not more than once
					String clauses8 = "";
					for (int i = 0; i < SUDOKU_SIZE; i++) {
						for (int j = 0; j < SUDOKU_SIZE; j++) {
							for (int k1 = 0; k1 < SUDOKU_SIZE; k1++) {
								for (int k2 = k1 + 1; k2 < SUDOKU_SIZE; k2++) {
									int jj1 = N_SUDOKU * (j / N_SUDOKU) + (k1 / N_SUDOKU);
									int jj2 = N_SUDOKU * (j / N_SUDOKU) + (k2 / N_SUDOKU);
									int kk1 = N_SUDOKU * (j % N_SUDOKU) + (k1 % N_SUDOKU);
									int kk2 = N_SUDOKU * (j % N_SUDOKU) + (k2 % N_SUDOKU);
									String clause = "-" + LITERALS[kk1][jj1][i] + " -" + LITERALS[kk2][jj2][i] + " 0\n";
									clauses8 += clause;
									totalClause++;
									System.out.print("Clause " + totalClause + ": " + clause);
								}
							}
						}
					}
					allClauses.add(clauses8);
				}
				solved = true;

				// 2.1: Given clauses
				String givenClauses = "";
				for (Map.Entry<String,Integer> filledEntry : filledEntries.entrySet()) {
					String[] key = filledEntry.getKey().split(",");
					int value = filledEntry.getValue();
					int row = Integer.parseInt(key[0]);
					int col = Integer.parseInt(key[1]);
					String clause = (SUDOKU_SIZE * SUDOKU_SIZE * row + SUDOKU_SIZE * col + value) + " 0\n";
					givenClauses += clause;
					totalClause++;
					System.out.print("Given clause " + totalClause + ": " + clause);
				}
				
				// To write all clauses to a file input.in
				try {
					File inputFile = new File(MINISAT_INPUT_FILE_NAME);
					inputFile.createNewFile();
	
					BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile.getAbsoluteFile()));
					bw.write("p cnf " + NUM_OF_LITERALS + " " + totalClause + "\n");
					for (String clause : allClauses) {
						bw.write(clause);
					}
					bw.write(givenClauses);
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				

				// To run minisat and save the output to a file
				try {
					if (MINISAT_INSTALLED) {
						Process p = Runtime.getRuntime().exec("minisat " + MINISAT_INPUT_FILE_NAME + " " + MINISAT_OUTPUT_FILE_NAME);
						int exitVal = p.waitFor();
						System.out.println("Exited with status " + exitVal);	
					} else {
						Process p = Runtime.getRuntime().exec(MINISAT_COMPILED_DIR + " " + MINISAT_INPUT_FILE_NAME + " " + MINISAT_OUTPUT_FILE_NAME);
						int exitVal = p.waitFor();
						System.out.println("Exited with status " + exitVal);
					}					
				} catch (Exception e) {
					e.printStackTrace();
				}

				// To read the output file and process it
				try {
					File outputFile = new File(MINISAT_OUTPUT_FILE_NAME);

					BufferedReader br = new BufferedReader(new FileReader(outputFile));
					String satisfiability = br.readLine();
					if (satisfiability.equals("SAT")) {
						String[] results = br.readLine().split(" ");
						for (String res : results) {
							if (res.charAt(0) == '0') {
								break;
							}
							if (res.charAt(0) != '-') {
								int literal = Integer.parseInt(res) - 1;
								int row = literal / (SUDOKU_SIZE * SUDOKU_SIZE);
								int col = (literal - (row * SUDOKU_SIZE * SUDOKU_SIZE)) / SUDOKU_SIZE;
								int entry = (literal % SUDOKU_SIZE) + 1;
								// System.out.println(row + "," + col + " -> " + entry);
								if (!filledEntries.containsKey(row + "," + col)) {
									allEntries[row][col].setForeground(ENTRY_TEXT_COLOR);
									allEntries[row][col].setText(entry + "");
								}
								filledEntries.put(row + "," + col, entry);	
							}
						}
					} else {
						JOptionPane.showMessageDialog(null, "The Sudoku is unsatisfiable :(", "Ouch..", JOptionPane.WARNING_MESSAGE);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		solveButton.addActionListener(new SolveButtonListener());

		JPanel clearPanel = new JPanel();
		clearPanel.setPreferredSize(new Dimension(FRAME_WIDTH, SOLVE_BTN_PANEL_WIDTH));
		clearPanel.setLayout(new GridBagLayout());
		JButton clearButton = new JButton("Reset");
		clearButton.setFont(new Font(FONT_NAME, Font.PLAIN, BTN_FONT_SIZE));
		clearButton.setForeground(BTN_TEXT_COLOR);
		clearButton.setBackground(BTN_BG_COLOR);
		clearButton.setOpaque(true);
		clearPanel.add(clearButton, new GridBagConstraints());
		clearPanel.setBackground(BG_COLOR);

		class ResetButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent ae) {
				filledEntries.clear();
				for (int i = 0; i < allEntries.length; i++) {
					for (int j = 0; j < allEntries[i].length; j++) {
						allEntries[i][j].setText("");
					}
				}
			}
		}
		clearButton.addActionListener(new ResetButtonListener());

		JPanel buttonsPanelWrapper = new JPanel();
		buttonsPanelWrapper.setLayout(new GridLayout(1, 2));
		GridBagConstraints gbc2 = new GridBagConstraints();
		buttonsPanelWrapper.add(solvePanel, gbc2);
		buttonsPanelWrapper.add(clearPanel, gbc2);
		buttonsPanelWrapper.setBackground(BG_COLOR);

		// Left side-panel
		JPanel leftSidePanel = new JPanel();
		leftSidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH));
		leftSidePanel.setBackground(BG_COLOR);
		
		// Right side-panel
		JPanel rightSidePanel = new JPanel();
		rightSidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH));
		rightSidePanel.setBackground(BG_COLOR);

		// To add all panels to main panel
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(leftSidePanel, BorderLayout.WEST);
		mainPanel.add(rightSidePanel, BorderLayout.EAST);
		mainPanel.add(sudokuPanel, BorderLayout.CENTER);
		mainPanel.add(buttonsPanelWrapper, BorderLayout.SOUTH);

		// To add main panel to frame
		mainFrame.add(mainPanel);
		mainFrame.setVisible(true);
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
