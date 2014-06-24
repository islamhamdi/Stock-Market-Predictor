package Default;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.gephi.datalab.plugin.manipulators.values.ClearAttributeValue;

import Default.Main.VOL_PR;

import com.itextpdf.text.pdf.parser.Vector;

import jxl.Cell;
import jxl.NumberFormulaCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.BorderLineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Border;
import jxl.write.Colour;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class WriteExcel {

	// company path and name
	int temp_var = 0;
	private String path, CompanyName;

	// each feature represented by a column
	private String[] features;

	// book to write in
	private WritableWorkbook workbook;

	// current writable sheet
	private WritableSheet sheet;

	private int specialCol = Global.specialCell;
	private int lag_var = Global.lag_var;

	HashMap<String, VOL_PR> volume_price_table;

	private WritableSheet sheetSet[];

	private void openAllSheets() {

	}

	public void passFeatures(String[] features) throws IOException {
		this.features = features;
	}

	public void setOutputFile(String companyPath, String CompanyName)
			throws IOException {
		this.path = companyPath;
		this.CompanyName = CompanyName;
	}

	File file;

	public void createExcel(String start) throws Exception {
		file = new File(path);
		workbook = Workbook.createWorkbook(file);

		for (int i = 0; i < Global.sheets.length; i++)
			workbook.createSheet(Global.sheets[i], i);
		for (int i = 0; i < Global.sheets.length; i++) {
			sheet = workbook.getSheet(i);
			sheet.getSettings().setDefaultColumnWidth(Global.COLWIDTH);
			writeFeatures();
			// adddummyDays(start);
		}
		writeAndClose();
	}

	/*
	 * add dummy days at start of sheet with no features
	 */
	double v[] = new double[0];
	Date temp;

	private void adddummyDays(String start) throws Exception {
		Date date = Global.sdf.parse(start);
		int cnt = 0, i;
		for (i = 1; i < 20; i++) {
			temp = new Date(date.getTime() - TimeUnit.DAYS.toMillis(i));
			if (volume_price_table.containsKey(Global.sdf.format(temp))) {
				cnt++;
				if (cnt == lag_var)
					break;
			}
		}
		for (; i > 0; i--) {
			temp = new Date(date.getTime() - TimeUnit.DAYS.toMillis(i));
			if (volume_price_table.containsKey(Global.sdf.format(temp)))
				addNewDay(Global.sdf.format(temp), v, true);
		}

	}

	/*
	 * add dummy days at end of sheet with no features
	 */

	public void adddummyDaysAtEnd() throws Exception {
		temp_var = 0;
		int size = getRowsCnt();
		String lastDay = sheet.getCell(0, size - 1).getContents();
		Date date = Global.sdf.parse(lastDay);
		int cnt = 0, i;
		for (i = 1; i < 20; i++) {
			Date d = new Date(date.getTime() + TimeUnit.DAYS.toMillis(i));
			String day = Global.sdf.format(d);
			if (volume_price_table.containsKey(day)) {
				addNewDay(day, v, false);
				cnt++;
				if (cnt == Global.lag_var)
					break;
			}
		}
	}

	public void initializeExcelSheet(int sheetNum, String start)
			throws Exception {

		file = new File(path);
		Workbook myWorkbook = Workbook.getWorkbook(file);
		workbook = Workbook.createWorkbook(file, myWorkbook);
		sheet = workbook.getSheet(sheetNum);

		int r = sheet.getRows();
		if (r < lag_var)
			adddummyDays(start);

		myWorkbook.close();
	}

	public void writeFeatures() throws WriteException {
		addCaption(0, 0, "Day");
		for (int i = 0; i < features.length; i++) {
			addCaption(i + 1, 0, features[i]);
		}
		int pos = Global.price_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, 0, "price(" + i + ")");
		}
		pos = Global.volume_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, 0, "volume(" + i + ")");
		}
		addNumber(specialCol, 0, 1.0);
	}

	public int getRowsCnt() throws Exception {
		Cell cell = sheet.getCell(specialCol, 0);
		return (int) Double.parseDouble(cell.getContents());
	}

	void addNewDay(String day, double[] val, boolean add) throws Exception {
		VOL_PR ob = volume_price_table.get(day);

		int row = getRowsCnt();
		if (!add) {
			row += temp_var++;
		} else {
			addNumber(specialCol, 0, row + 1.0);

		}
		addCaption(0, row, day);
		int n = val.length;
		for (int j = 0; j < n; j++)
			addNumber(j + 1, row, val[j]);

		int start_price = Global.price_start_col;
		for (int i = -lag_var; i <= lag_var; i++) {
			if (row + i > 0)
				addNumber(start_price + i, row + i, ob.price);

		}
		int start_Volume = Global.volume_start_col;
		for (int i = -lag_var; i <= lag_var; i++) {
			if (row + i > 0)
				addNumber(start_Volume + i, row + i, ob.vol);

		}
	}

	void writeAndClose() throws WriteException, IOException {
		workbook.write();
		workbook.close();
	}

	StringBuffer buf = new StringBuffer();
	Formula formula;

	void calcCorrel(int col, int row, String ch1, String ch2, int start, int end)
			throws WriteException {
		buf.setLength(0);
		WritableCellFormat cellFormat = new WritableCellFormat();

		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		String s1 = ch1 + "" + start + ":" + ch1 + "" + end;
		String s2 = ch2 + "" + start + ":" + ch2 + "" + end;
		buf.append("CORREL(" + s1 + "," + s2 + ")");

		formula = new Formula(col, row, buf.toString(), cellFormat);

		sheet.addCell(formula);
	}

	private void addCaption(int column, int row, String s)
			throws RowsExceededException, WriteException {
		WritableCellFormat cellFormat = new WritableCellFormat();

		cellFormat.setWrap(true);
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setBackground(Colour.GRAY_25);
		sheet.addCell(new Label(column, row, s, cellFormat));

	}

	private void addNumber(int column, int row, Double d)
			throws WriteException, RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();

		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		sheet.addCell(new Number(column, row, d, cellFormat));
	}

	private void addLabel(int column, int row, String s) throws WriteException,
			RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat();

		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellFormat.setWrap(true);
		sheet.addCell(new Label(column, row, s, cellFormat));
	}

	// feature = 0 then price else volume
	public void drawTable1(int frow, int fcolumn, String tname, int lastRow,
			String[] a) throws Exception {

		addLabel(fcolumn, frow, "Features\\Lag");
		int r = frow + 1;
		for (int i = 0; i < features.length; i++) {
			addCaption(fcolumn, r++, features[i]);
		}

		int pos = fcolumn + 1;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, frow, tname + "(" + i + ")");
		}

		int cnt = 1;
		for (char ch2 = 'B'; ch2 <= 'T'; ch2++) {
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = a[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						lastRow);

			}
			cnt++;
		}
	}

	public void drawTable3(int frow, int fcolumn, String tname, int lastRow,
			String[] f) throws Exception {
		addLabel(fcolumn, frow, "Features\\Lag");
		int r = frow + 1;

		int t = 0;
		for (int i = 0; i < features.length; i++) {
			for (int j = i + 1; j < features.length; j++) {
				addCaption(fcolumn, r++, features[i] + "+" + features[j]);
				t++;
			}
		}

		int pos = fcolumn + 1;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, frow, tname + "(" + i + ")");
		}

		int cnt = 1;
		for (int c = 0; c < t; c++) {
			int a = Global.start_of_norm_table + Global.features_num + c + 3;
			String ch2 = Global.convert(a);
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = f[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						lastRow);
			}
			cnt++;
		}
	}

	// feature = 0 then price else volume
	public void drawDailyStepTable(int frow, int fcolumn, String tname,
			int lastRow, String[] a) throws Exception {
		int pos = fcolumn + 1;
		addCaption(pos++, frow, tname + "(0)");

		int cnt = 1;
		for (char ch2 = 'B'; ch2 <= 'T'; ch2++) {
			pos = fcolumn + 1;
			String ch1 = a[a.length / 2];
			calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2, lastRow);
			cnt++;
		}
	}

	public void drawTables() throws Exception {

		int lastRow = getRowsCnt();

		drawNormalizedTable();
		clear();

		int index = 0;
		for (int i = 7; i <= lastRow; i++) {
			int col = Global.start_col_t2 + (index++);
			addLabel(col + 1, Global.start_row_t1 - 1, sheet.getCell(0, i - 1)
					.getContents());
			drawDailyStepTable(Global.start_row_t1, col, "volume", i,
					Global.volume_cols);
		}

		index = 0;
		for (int i = 7; i <= lastRow; i++) {
			int col = Global.start_col_t2 + (index++);

			addLabel(col + 1, Global.start_row_t1 - 1, sheet.getCell(0, i - 1)
					.getContents());
			drawDailyStepTable(Global.start_row_t1 + 25, col, "price", i,
					Global.price_cols);
		}

		// drawTable1(Global.start_row_t1,
		// Global.start_col_t1,
		// "volume",
		// lastRow / 2, Global.volume_cols);
		// drawTable1(Global.start_row_t1, Global.start_col_t2, "price",
		// lastRow / 2, Global.price_cols);

		drawTable1(Global.start_row_t1, Global.start_col_t1, "volume", lastRow,
				Global.volume_cols);
		drawTable1(Global.start_row_t1 + 25, Global.start_col_t1, "price",
				lastRow, Global.price_cols);

		drawTable3(Global.start_row_t2, Global.start_col_t1, "volume", lastRow,
				Global.volume_cols);

		drawTable3(Global.start_row_t2, Global.start_col_t2, "price", lastRow,
				Global.price_cols);

	}

	private void clear() throws Exception {
		int raws = getRowsCnt() + 5;
		for (int column = 0; column < 200; column++)
			for (int r = raws + 1; r < raws + 200; r++)
				addLabel(column, r, "");
	}

	double[][] normTable;
	double max[];
	double min[];

	public void drawNormalizedTable() throws Exception {

		int raw_n = getRowsCnt() - lag_var;
		int width = Global.features_num + 1;

		normTable = new double[raw_n][width];
		max = new double[width];
		min = new double[width];

		for (int col = 1; col < width; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				String s = sheet.getCell(col, raw).getContents();
				if (s.isEmpty())
					s = "0";
				normTable[raw][col] = Double.parseDouble(s);
				max[col] = Math.max(max[col], normTable[raw][col]);
				min[col] = Math.min(min[col], normTable[raw][col]);
			}
		}

		for (int col = 1; col < width; col++)
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				normTable[raw][col] = (normTable[raw][col] - min[col])
						/ (max[col] - min[col]);
			}

		int start_col = Global.start_of_norm_table;

		ArrayList<ArrayList<Double>> temp = new ArrayList<>();
		for (int i = 0; i < normTable.length; i++) {
			temp.add(new ArrayList<Double>());
			for (int j = 1; j < normTable[0].length; j++)
				for (int j2 = j + 1; j2 < normTable[0].length; j2++)
					temp.get(i).add(normTable[i][j] + normTable[i][j2]);
		}

		for (int col = start_col; col < max.length + start_col; col++)
			for (int raw = lag_var + 1; raw < raw_n; raw++)
				if (col - start_col > 0)
					addNumber(col, raw, normTable[raw][col - start_col]);

		start_col = Global.start_of_norm_table + Global.features_num + 2;

		int size = temp.get(1).size();
		for (int col = start_col; col < size + start_col; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				double a = temp.get(raw).get(col - start_col);
				addNumber(col, raw, a);
			}
		}

	}

	public void set_price_vol_table(HashMap<String, VOL_PR> hs) {
		volume_price_table = hs;
	}

}