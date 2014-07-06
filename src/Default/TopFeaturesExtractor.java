package Default;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class TopFeaturesExtractor {
	private static String inputDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Average/normal_average.xls";
	private static final int[] TABLE_START_COLUMN = { 0, 10, 20, 30 }, TABLE1_NUM_ROWS = { 19, 19, 171, 171 };
	private static final int NUMBER_OF_COLUMNS = 7, NUMBER_OF_SHEETS = 6, TOP = 5;

	public static void main(String[] args) throws IndexOutOfBoundsException, BiffException, IOException {
		Workbook normalWorkbook = getWorkBook(inputDirectory);

		for (int sheetIndex = 0; sheetIndex < NUMBER_OF_SHEETS; sheetIndex++) {
			System.out.println(">> Sheet : " + sheetIndex);
			for (int tableIndex = 0; tableIndex < TABLE_START_COLUMN.length; tableIndex++) {
				System.out.println("Table : " + tableIndex);
				
				Sheet excelSheet = normalWorkbook.getSheet(sheetIndex);
				Feature[] tableFeatures = readTable(excelSheet, TABLE_START_COLUMN[tableIndex], TABLE1_NUM_ROWS[tableIndex]);
				Arrays.sort(tableFeatures);
				
				for(int i = 0; i < TOP; i++){
					System.out.println(tableFeatures[i].toString());
				}
				
				System.out.println();
			}
		}

	}

	private static Feature[] readTable(Sheet excelSheet, int startColumn, int numOfRows) {
		Feature[] features = new Feature[numOfRows];
		for (int row = 1; row <= numOfRows; row++) {
			String name = excelSheet.getCell(startColumn, row).getContents();
			double[] featureValues = new double[NUMBER_OF_COLUMNS];
			for (int i = 1; i <= NUMBER_OF_COLUMNS; i++)
				featureValues[i - 1] = parse(excelSheet.getCell(startColumn + i, row));

			features[row - 1] = new Feature(name, featureValues);
		}
		return features;
	}

	private static double parse(Cell cell) {
		double v = Double.parseDouble(cell.getContents());
		return v;
	}

	private static Workbook getWorkBook(String directory) throws BiffException, IOException {
		File file = new File(directory);
		Workbook workbook = Workbook.getWorkbook(file);
		return workbook;
	}

	static class Feature implements Comparable<Feature> {
		String name;
		double[] value;

		public Feature(String name, double[] value) {
			this.value = value;
			this.name = name;
		}

		@Override
		public int compareTo(Feature f) {
			return Double.compare(f.value[NUMBER_OF_COLUMNS / 2], value[NUMBER_OF_COLUMNS / 2]);
		}

		@Override
		public String toString() {
			return name + " " + Arrays.toString(value);
		}
	}
}
