package Default;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class FeatureValuesCollector {
	private static String inputDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Average/";
	public static String[] mode_names = Global.mode_names;

	public static void main(String[] args) throws BiffException, IOException {
		int row = 2, col = 1;
		
		for (int i = 1; i < mode_names.length; i++) {
			String fileDirectory = inputDirectory + mode_names[i] + ".xls";
			Workbook workBook = getWorkBook(fileDirectory);

			Sheet twitterSheet = workBook.getSheet(Global.twitter);
			double twitterValue = parse(twitterSheet.getCell(col, row));

			Sheet stocktwitSheet = workBook.getSheet(Global.stocktwits);
			double stocktwitsValue = parse(stocktwitSheet.getCell(col, row));

			System.out.printf("%s %.2f %.2f\n", mode_names[i], twitterValue , stocktwitsValue);
		}
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

}
