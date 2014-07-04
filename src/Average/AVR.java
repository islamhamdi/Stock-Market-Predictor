package Average;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import Default.Global;

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

	static HashMap<Integer, ArrayList<String>> keyMap = new HashMap<>();

	String[] f = { "RTID", "RTU", "TID", "TSUM", "UFRN", "THTG", "UFLW", "UID",
			"NEG", "POS", "POS_NEG", "NUM_NODES", "NUM_EDGES", "NUM_CMP",
			"MAX_DIST", "AVG_DEGREE", "GRAPH_DENSITY", "AVG_PATH_LEN",
			"MODULARITY" };

	int width = 7;
	int hight1 = f.length;
	int hight2 = 120;

	int start_raw1 = Global.start_row_t1 + 1;
	int start_raw2 = Global.start_row_t2 + 1;

	int start_col1 = Global.start_col_t1 + 1;
	int start_col2 = Global.start_col_t2 + 1;

	double buff_price[][] = new double[hight1][width];
	double buff_volume[][] = new double[hight1][width];

	double buff_comb_price[][] = new double[hight2][width];
	double buff_comb_volume[][] = new double[hight2][width];

	WritableWorkbook workbook;
	WritableSheet excelSheet;

	static String inputFolder = "/home/mohamed/Dropbox/Stock Market Daily Data/plain";
	static String outputFolder = "/home/mohamed/Dropbox/Stock Market Daily Data/Average/";

	// static String inputFolder = "/home/mohamed/Desktop/plain";
	// static String inputFolder =
	// "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/statistics/plain/";

	public static void main(String[] args) throws Exception {
		// read hashMap

		keyMap = readHashMap("");

		for (int k = 0; k <= 21; k++) {
			File[] ss = ReadCurrMode(k);
			for (int i = 0; i < ss.length; i++)
				System.out.print(" " + ss[i].getName());
			System.out.println();
			AVR avr = new AVR(outputFolder + Global.mode_names[k] + ".xls");
			for (int i = 0; i < 7; i++)
				avr.read(i, k);

			avr.close();
		}
	}

	private static HashMap<Integer, ArrayList<String>> readHashMap(
			String directory) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(directory + Global.kEYMAP);
			ObjectInputStream ois = new ObjectInputStream(fin);
			@SuppressWarnings("unchecked")
			HashMap<Integer, ArrayList<String>> map = (HashMap<Integer, ArrayList<String>>) ois
					.readObject();
			ois.close();
			return map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void read(int sheetNum, int mode) throws Exception {

		excelSheet = workbook.getSheet(sheetNum);
		excelSheet.getSettings().setDefaultColumnWidth(15);

		File[] files = ReadCurrMode(mode);
		for (int k = 0; k < files.length; k++) {
			name = files[k].getName();
			System.out.println(name);
			File inputWorkbook = new File(files[k].getAbsolutePath());
			Workbook w;
			try {
				w = Workbook.getWorkbook(inputWorkbook);
				// Get the first sheet
				Sheet sheet = w.getSheet(sheetNum);

				// vol
				add_to_buff(sheet, buff_volume, start_raw1, start_col1, width,
						hight1, files.length);

				// price
				add_to_buff(sheet, buff_price, start_raw1 + 25, start_col1,
						width, hight1, files.length);

				// vol
				add_to_buff(sheet, buff_comb_volume, start_raw2, start_col1,
						width, hight2, files.length);

				// // price
				add_to_buff(sheet, buff_comb_price, start_raw2, start_col2,
						width, hight2, files.length);
				w.close();
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

	public String name = "";

	private static File[] ReadCurrMode(int mode) throws Exception {
		File statusDir = new File(inputFolder);
		if (!statusDir.exists())
			throw new Exception("input folder doesnt exist");

		File[] files = statusDir.listFiles();
		if (mode == 0)
			return files;
		ArrayList<File> result = new ArrayList<File>();
		ArrayList<String> keyStatFiles = keyMap.get(mode);
		for (File f : files) {
			String fileName = f.getName();
			String companyName = fileName.substring(1, fileName.indexOf('.'));
			if (keyStatFiles.contains(companyName))
				result.add(f);
		}

		File[] ans = new File[result.size()];
		for (int i = 0; i < result.size(); i++)
			ans[i] = result.get(i);

		return ans;
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
				int r = row - start_raw;
				int c = col - start_col;
				try {

					Cell cell = sheet.getCell(col, row);
					if (cell.getType() == CellType.NUMBER) {
						double v = Double.parseDouble(cell.getContents());
						buff[r][c] += v;
					} else {
						buff[r][c] += 0.5;

						System.err.print(" check " + sheet.getName() + " "
								+ row + " ," + col);
						// System.err.println(row + " ," + col);
						// System.err.println(cell.getContents());
						// throw new Exception("INVALID");
					}
				} catch (Exception e) {
					buff[r][c] += 0.2;
					// System.out.println(row + " " + col + sheet.getName());
					// System.out.println(name);
					// System.exit(0);
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
			for (int i = 0; i < f.length; i++)
				for (int j = i + 1; j < f.length; j++)
					addLabel(k, index++, f[i] + "+" + f[j]);

		}
		for (int i = 0; i < buff.length; i++)
			for (int j = 0; j < buff[0].length; j++)
				addNumber(j + k + 1, i + 1, buff[i][j]);

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

		String[] sheetname = { "Twitter", "StockTwits", "Combined",
				"Positive-Twitter", "Negative-Twitter", "Positive-StockTwits",
				"Negative-StockTwits" };

		for (int i = 0; i < sheetname.length; i++)
			workbook.createSheet(sheetname[i], i);

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