import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class AssimentOne {
	static String[] NameOfPerson = { "Ahmed", "Salem", "Ayman", "Hani", "Kamal", "Samir", "Hakam", "Fuad", "Ibrahim",
			"Khalid" };

	public static void main(String[] args) throws IOException, InvalidFormatException {
		int[][] Dislike_Matrix = ReadDislikeMatrix_FromFile("dislike.xlsx");
		if (Dislike_Matrix == null) {
			System.out.println("The read data Fram Dislike Matrix is failer.");
			return;
		}

		printDislike_Matrix(Dislike_Matrix);

		int[][] Non_Linear_Cost_Matrix = initializeNon_Linear_Cost_Matrix(Dislike_Matrix);

		System.out.println("Uniform Cost Search:");
		String[] Uniform_cost_Arrangment = Uniform_Cost_Search(Dislike_Matrix, Non_Linear_Cost_Matrix);
		printArrangmentWithCost(Uniform_cost_Arrangment);

		System.out.println("\nGreedy_Search Search:");
		String[] Greedy_Search_Cost_Arrangment = Greedy_Search(Dislike_Matrix);
		printArrangmentWithCost(Greedy_Search_Cost_Arrangment);

		System.out.println("\nA* Search:");
		String[] A_Star_Arrangment = A_Star_Search(Dislike_Matrix, Non_Linear_Cost_Matrix);
		printArrangmentWithCost(A_Star_Arrangment);
	}

	static int[][] ReadDislikeMatrix_FromFile(String filePath) throws IOException, InvalidFormatException {
		try (Workbook Data = WorkbookFactory.create(new FileInputStream(new File(filePath)))) {
			Sheet sheet = Data.getSheetAt(0);
			int[][] Dislike_Matrix = new int[NameOfPerson.length][NameOfPerson.length];

			for (int row = 0; row < NameOfPerson.length; row++) {
				Row excelRow = sheet.getRow(row);

				for (int col = 0; col < NameOfPerson.length; col++) {
					Cell cell = excelRow.getCell(col);
					if (cell != null) {
						if (cell.getCellType() == CellType.NUMERIC) {
							double percentage = cell.getNumericCellValue() * 100;
							Dislike_Matrix[row][col] = (int) Math.round(percentage);
						} else if (cell.getCellType() == CellType.STRING) {
							String cellValue = cell.getStringCellValue();
							if (cellValue.endsWith("%")) {
								double percentage = Double.parseDouble(cellValue.replaceAll("[^\\d.]", ""));
								Dislike_Matrix[row][col] = (int) Math.round(percentage);
							} else {

								Dislike_Matrix[row][col] = 0;
							}
						} else {

							Dislike_Matrix[row][col] = 0;
						}
					} else {

						Dislike_Matrix[row][col] = 0;
					}
				}
			}
			return Dislike_Matrix;
		}
	}

	public static int[][] initializeNon_Linear_Cost_Matrix(int[][] Dislike_Matrix) {
		int[][] Non_Linear_Cost_Matrix = new int[Dislike_Matrix.length][Dislike_Matrix[0].length];
		for (int i = 0; i < Dislike_Matrix.length; i++) {
			for (int j = 0; j < Dislike_Matrix[i].length; j++) {
				Non_Linear_Cost_Matrix[i][j] = Dislike_Matrix[i][j] * Dislike_Matrix[i][j];
			}
		}
		return Non_Linear_Cost_Matrix;
	}

	public static String[] Uniform_Cost_Search(int[][] Dislike_Matrix, int[][] Non_Linear_Cost_Matrix) {
		PriorityQueue<Node> Queue = new PriorityQueue<>();
		boolean[] explored = new boolean[NameOfPerson.length];

		int[] Total_Cost = new int[NameOfPerson.length];
		Arrays.fill(Total_Cost, Integer.MAX_VALUE);

		Queue.offer(new Node(NameOfPerson[0], 0));
		Total_Cost[getIndex(NameOfPerson[0])] = 0;

		while (!Queue.isEmpty()) {
			Node node = Queue.poll();
			String person = node.person;
			int cost = node.cost;

			if (getIndex(person) == NameOfPerson.length - 1) {
				return new String[] { Arrays.toString(extractArrangment(person, Dislike_Matrix)),
						"Total Cost: " + Total_Cost[getIndex(person)] };
			}

			for (int i = 0; i < NameOfPerson.length; i++) {
				if (!explored[i]) {
					int New_Cost = cost + Non_Linear_Cost_Matrix[getIndex(person)][i];
					if (New_Cost < Total_Cost[i]) {
						Queue.offer(new Node(NameOfPerson[i], New_Cost));
						Total_Cost[i] = New_Cost;
					}
				}
			}

			explored[getIndex(person)] = true;
		}

		return null;
	}

	public static String[] Greedy_Search(int[][] Dislike_Matrix) {
		List<String> Arrangment = new ArrayList<>();
		boolean[] explored = new boolean[NameOfPerson.length];
		String currentPerson = NameOfPerson[0];
		int Total_Cost = 0;

		while (Arrangment.size() < NameOfPerson.length) {
			Arrangment.add(currentPerson);
			explored[getIndex(currentPerson)] = true;
			int nextPersonIndex = findMinNeighbor(getIndex(currentPerson), explored, Dislike_Matrix);
			if (nextPersonIndex == -1) {
				break;
			}
			Total_Cost += Dislike_Matrix[getIndex(currentPerson)][nextPersonIndex];
			currentPerson = NameOfPerson[nextPersonIndex];
		}

		String[] Arrangment_To_Array = new String[Arrangment.size()];
		Arrangment_To_Array = Arrangment.toArray(Arrangment_To_Array);

		return new String[] { Arrays.toString(Arrangment_To_Array), "Total Cost: " + Total_Cost };
	}

	public static String[] A_Star_Search(int[][] Dislike_Matrix, int[][] Non_Linear_Cost_Matrix) {
		PriorityQueue<Node> Queue = new PriorityQueue<>();
		boolean[] explored = new boolean[Dislike_Matrix.length];
		int[] Cost = new int[Dislike_Matrix.length];
		int Total_Cost = 0;

		Queue.offer(new Node(NameOfPerson[0], 0));
		Cost[0] = 0;

		while (!Queue.isEmpty()) {
			Node node = Queue.poll();
			String person = node.person;
			int g = node.cost;

			if (allExplored(explored)) {
				return new String[] { Arrays.toString(extractArrangment(person, Dislike_Matrix)),
						"Total Cost: " + Total_Cost };
			}

			for (int i = 0; i < Dislike_Matrix.length; i++) {
				if (!explored[i]) {
					int New_Cost = g + Non_Linear_Cost_Matrix[getIndex(person)][i];
					Queue.offer(new Node(NameOfPerson[i], New_Cost));
					Cost[i] = New_Cost;
				}
			}

			explored[getIndex(person)] = true;
			Total_Cost += g;
		}

		return null;
	}

	public static int findMinNeighbor(int index, boolean[] explored, int[][] Dislike_Matrix) {
		int minNeighbor = -1;
		int minCost = Integer.MAX_VALUE;
		for (int i = 0; i < Dislike_Matrix.length; i++) {
			if (!explored[i] && Dislike_Matrix[index][i] < minCost) {
				minCost = Dislike_Matrix[index][i];
				minNeighbor = i;
			}
		}
		return minNeighbor;
	}

	public static boolean allExplored(boolean[] explored) {
		for (boolean b : explored) {
			if (!b)
				return false;
		}
		return true;
	}

	public static String[] extractArrangment(String startPerson, int[][] Dislike_Matrix) {
		String[] Arrangment = new String[Dislike_Matrix.length];
		int index = 0;
		String currentPerson = startPerson;
		boolean[] visited = new boolean[Dislike_Matrix.length];

		while (index < Dislike_Matrix.length) {
			Arrangment[index++] = currentPerson;
			visited[getIndex(currentPerson)] = true;
			int nextPersonIndex = findMinNeighbor(getIndex(currentPerson), visited, Dislike_Matrix);
			if (nextPersonIndex == -1) {
				break;
			}
			currentPerson = NameOfPerson[nextPersonIndex];
		}

		return Arrangment;
	}

	public static void printArrangmentWithCost(String[] ArrangmentWithCost) {
		System.out.println("Arrangment: " + ArrangmentWithCost[0]);
		System.out.println(ArrangmentWithCost[1]);
	}

	public static int getIndex(String person) {
		for (int i = 0; i < NameOfPerson.length; i++) {
			if (NameOfPerson[i].equals(person)) {
				return i;
			}
		}
		return -1;
	}

	public static void printDislike_Matrix(int[][] Dislike_Matrix) {
		System.out.println("Dislike Matrix:");
		for (int i = 0; i < Dislike_Matrix.length; i++) {
			for (int j = 0; j < Dislike_Matrix[i].length; j++) {
				System.out.print(Dislike_Matrix[i][j] + "\t");
			}
			System.out.println();
		}
	}

}
