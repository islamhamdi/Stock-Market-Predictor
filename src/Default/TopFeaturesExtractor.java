package Default;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Border;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class TopFeaturesExtractor {
	private static String inputDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Average/normal_average.xls";
	private static String outputDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Average/topFeatures.xls";

	private static final int[] VOLUME_TABLE_START_COLUMN = { 10, 30 };
	private static final int[] PRICE_TABLE_START_COLUMN = { 0, 20 };
	private static final int[] TABLE_NUM_ROWS = { 19, 171 };
	private static final int NUMBER_OF_COLUMNS = 7, NUMBER_OF_SHEETS = 7, TOPN = 5;

	public static void main(String[] args) throws IndexOutOfBoundsException, BiffException, IOException, WriteException {
		Workbook normalWorkbook = getWorkBook(inputDirectory);
		WritableWorkbook outputWorkbook = createNewWorkbook(outputDirectory);

		for (int sheetIndex = 0; sheetIndex < NUMBER_OF_SHEETS; sheetIndex++) {
			System.out.println(">> Sheet : " + sheetIndex);
			Sheet normalSheet = normalWorkbook.getSheet(sheetIndex);

			Feature[] top10FeaturesVolume = getTopNFeature(normalSheet, VOLUME_TABLE_START_COLUMN);
			printFeatures("VOLUME FEATURES : ", top10FeaturesVolume);

			Feature[] top10FeaturesPrice = getTopNFeature(normalSheet, PRICE_TABLE_START_COLUMN);
			printFeatures("PRICE FEATURES : ", top10FeaturesPrice);

			WritableSheet outputSheet = outputWorkbook.getSheet(sheetIndex);
			outputSheet.getSettings().setDefaultColumnWidth(20);
			writeWorkbookSheet(outputSheet, top10FeaturesPrice, "Price", 0);
			writeWorkbookSheet(outputSheet, top10FeaturesVolume, "Volume", 10);
		}

		closeWorkBook(outputWorkbook);
	}

	private static void writeWorkbookSheet(WritableSheet sheet, Feature[] features, String title, int startColume)
			throws RowsExceededException, WriteException {
		for (int col = startColume + 1, index = -3; col < startColume + NUMBER_OF_COLUMNS + 1; col++, index++)
			addLabel(sheet, col, 0, title + "(" + index + ")");

		for (int row = 1; row <= features.length; row++) {
			addLabel(sheet, startColume, row, features[row - 1].name);

			for (int col = 1; col <= NUMBER_OF_COLUMNS; col++)
				addNumber(sheet, col + startColume, row, features[row - 1].value[col - 1]);
		}
	}

	private static void addNumber(WritableSheet sheet, int column, int row, double v) throws WriteException,
			RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setWrap(true);
		sheet.addCell(new jxl.write.Number(column, row, v, cellFormat));
	}

	private static void addLabel(WritableSheet sheet, int column, int row, String s) throws WriteException,
			RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setWrap(true);
		cellFormat.setBackground(Colour.GRAY_25);
		sheet.addCell(new Label(column, row, s, cellFormat));
	}

	private static void closeWorkBook(WritableWorkbook workbook) throws IOException, WriteException {
		workbook.write();
		workbook.close();
	}

	private static WritableWorkbook createNewWorkbook(String directory) throws IOException {
		File file = new File(directory);
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));
		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

		String[] sheetname = { "Twitter", "StockTwits", "Combined", "Positive-Twitter", "Negative-Twitter",
				"Positive-StockTwits", "Negative-StockTwits" };

		for (int i = 0; i < sheetname.length; i++)
			workbook.createSheet(sheetname[i], i);

		return workbook;
	}

	private static void printFeatures(String title, Feature[] features) {
		System.out.println(title);
		for (int i = 0; i < features.length; i++)
			System.out.println(features[i].toString());
		System.out.println();
	}

	private static Feature[] getTopNFeature(Sheet excelSheet, int[] startCol) {
		Feature[] topFeatures = new Feature[TOPN * 2];
		for (int tableIndex = 0; tableIndex < VOLUME_TABLE_START_COLUMN.length; tableIndex++) {
			Feature[] tableFeatures = readTable(excelSheet, startCol[tableIndex], TABLE_NUM_ROWS[tableIndex]);
			Arrays.sort(tableFeatures);

			for (int i = 0; i < TOPN; i++)
				topFeatures[tableIndex * TOPN + i] = tableFeatures[i];
		}

		Arrays.sort(topFeatures);

		return topFeatures;
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
			String formatedValues = format(value);
			return name + formatedValues;
		}

		private String format(double[] value) {
			String s = "";
			for (int i = 0; i < value.length; i++)
				s += " " + String.format("%.2f", value[i]);
			return s;
		}
	}
}
