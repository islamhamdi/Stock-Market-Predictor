package Average;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import edu.stanford.nlp.util.logging.Color;

import jxl.Cell;
import jxl.CellType;
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

public class AVR {
	String[] f = { "RTID", "RTU", "TID", "TSUM", "UFRN", "THTG", "TURL",
			"UFLW", "UID", "NEG", "NEUT", "POS", "POS_NEG", "NUM_NODES",
			"NUM_EDGES", "NUM_CMP", "MAX_DIST" };
	int width = 7;
	int hight1 = 17;
	int hight2 = 136;

	int start_raw1 = 35;
	int start_raw2 = 57;

	int start_col1 = 2;
	int start_col2 = 13;

	double buff_price[][] = new double[hight1][width];
	double buff_volume[][] = new double[hight1][width];

	double buff_comb_price[][] = new double[hight2][width];
	double buff_comb_volume[][] = new double[hight2][width];

	WritableWorkbook workbook;
	WritableSheet excelSheet;

	String inputFolder = "/home/mohamed/Dropbox/Stock Market Daily Data/statistics/PlainStatistics";

	public static void main(String[] args) throws Exception {
		AVR avr = new AVR("avr.xls");

		for (int i = 0; i < 2; i++)
			avr.read(i);

		avr.close();
	}

	public void read(int sheetNum) throws Exception {
		excelSheet = workbook.getSheet(sheetNum);
		excelSheet.getSettings().setDefaultColumnWidth(15);

		File statusDir = new File(inputFolder);
		if (!statusDir.exists())
			throw new Exception("input folder doesnt exist");

		File[] files = statusDir.listFiles();
		for (int k = 0; k < files.length; k++) {
			File inputWorkbook = new File(files[k].getAbsolutePath());
			System.out.println(files[k].getName());
			Workbook w;
			try {
				w = Workbook.getWorkbook(inputWorkbook);
				// Get the first sheet
				Sheet sheet = w.getSheet(sheetNum);

				add_to_buff(sheet, buff_price, start_raw1, start_col1, width,
						hight1, files.length);

				add_to_buff(sheet, buff_volume, start_raw1, start_col2, width,
						hight1, files.length);

				// vol
				add_to_buff(sheet, buff_comb_volume, start_raw2, start_col1,
						width, hight2, files.length);
				// price
				add_to_buff(sheet, buff_comb_price, start_raw2, start_col2,
						width, hight2, files.length);

			} catch (BiffException e) {
				e.printStackTrace();
			}
		}

		int n = files.length;
		norm(buff_comb_price, n);
		norm(buff_comb_volume, n);
		norm(buff_price, n);
		norm(buff_volume, n);

		down(buff_price, 0, 0, 0);
		down(buff_volume, 10, 0, 1);
		down(buff_comb_price, 20, 1, 0);
		down(buff_comb_volume, 30, 1, 1);

	}

	private void norm(double[][] buff, int n) {

		for (int i = 0; i < buff.length; i++) {
			for (int j = 0; j < buff[0].length; j++) {
				buff[i][j] /= n;
			}
		}
	}

	private void add_to_buff(Sheet sheet, double[][] buff, int start_raw,
			int start_col, int width, int high, int n) throws Exception {
		for (int row = start_raw; row < start_raw + high; row++) {
			for (int col = start_col; col < start_col + width; col++) {
				Cell cell = sheet.getCell(col, row);
				if (cell.getType() == CellType.NUMBER) {
					double v = Double.parseDouble(cell.getContents());
					buff[row - start_raw][col - start_col] += v;
				} else {
					throw new Exception("INVALID");
				}
			}
		}

	}

	private void down(double[][] buff, int k, int ori, int p_V)
			throws RowsExceededException, WriteException {
		if (p_V == 0) {
			int pos = k + 1;
			for (int i = -3; i <= 3; i++) {
				addLabel(pos++, 0, "price(" + i + ")");
			}
		} else {
			int pos = k + 1;
			for (int i = -3; i <= 3; i++) {
				addLabel(pos++, 0, "volume(" + i + ")");
			}
		}
		if (ori == 0) {
			for (int i = 0; i < f.length; i++) {
				addLabel(k, i + 1, f[i]);
			}

		} else {
			int index = 1;
			for (int i = 0; i < f.length; i++) {
				for (int j = i + 1; j < f.length; j++) {
					addLabel(k, index++, f[i] + "+" + f[j]);
				}
			}

		}
		for (int i = 0; i < buff.length; i++) {
			for (int j = 0; j < buff[0].length; j++) {
				addNumber(j + k + 1, i + 1, buff[i][j]);
			}
		}

	}

	private void close() throws IOException, WriteException {
		workbook.write();
		workbook.close();
	}

	public AVR(String name) throws WriteException, IOException {
		File file = new File(name);
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));
		workbook = Workbook.createWorkbook(file, wbSettings);

		workbook.createSheet("Twitter", 0);
		workbook.createSheet("StockTwits", 1);
		// workbook.createSheet("Combined", 2);
	}

	private void addNumber(int column, int row, double v)
			throws WriteException, RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setWrap(true);
		excelSheet.addCell(new jxl.write.Number(column, row, v, cellFormat));
	}

	private void addLabel(int column, int row, String s) throws WriteException,
			RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setWrap(true);
		cellFormat.setBackground(Colour.GRAY_25);
		excelSheet.addCell(new Label(column, row, s, cellFormat));
	}

}
