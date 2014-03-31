package Default;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gephi.datalab.plugin.manipulators.values.ClearAttributeValue;

import com.itextpdf.text.pdf.parser.Vector;

import jxl.Cell;
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
	private String path, CompanyName;

	// each feature represented by a column
	private String[] features;

	// book to write in
	private WritableWorkbook workbook;

	// current writable sheet
	private WritableSheet sheet;

	private int specialCol = Global.specialCell;
	private int lag_var = Global.lag_var;

	private String[] price_cols = new String[2 * lag_var + 1];
	private String[] volume_cols = new String[2 * lag_var + 1];

	public void passFeatures(String[] features) throws IOException {
		this.features = features;
	}

	public void setOutputFile(String companyPath, String CompanyName)
			throws IOException {
		this.path = companyPath;
		this.CompanyName = CompanyName;
	}

	public void createExcel() throws Exception {
		File file = new File(path);
		workbook = Workbook.createWorkbook(file);

		workbook.createSheet("Twitter", 0);
		workbook.createSheet("StockTwits", 1);
		workbook.createSheet("Combined Data", 2);

		for (int i = 0; i < 3; i++) {
			sheet = workbook.getSheet(i);
			sheet.getSettings().setDefaultColumnWidth(Global.COLWIDTH);
			writeFeatures();
			adddummyDays();
		}

		writeAndClose();
	}

	/*
	 * add dummy days at start of sheet with no features
	 */
	private void adddummyDays() throws Exception {
		String Start = Global.startDate;
		Date date = Global.sdf.parse(Start);
		double v[] = new double[0];
		int cnt = 0, i;
		for (i = 1; i < 20; i++) {
			Date d = new Date(date.getTime() - TimeUnit.DAYS.toMillis(i));
			double[] D = read(Global.sdf.format(d));
			if (D[0] != 0)
				cnt++;
			if (cnt == lag_var)
				break;
		}
		for (; i > 0; i--) {
			Date d = new Date(date.getTime() - TimeUnit.DAYS.toMillis(i));
			addNewDay(Global.sdf.format(d), v);
		}

	}

	/*
	 * add dummy days at end of sheet with no features
	 */

	public void adddummyDaysAtEnd() throws Exception {
		int size = getRowsCnt();
		String lastDay = sheet.getCell(0, size - 1).getContents();
		Date date = Global.sdf.parse(lastDay);
		double v[] = new double[0];

		int cnt = 0, i;
		for (i = 1; i < 20; i++) {
			Date d = new Date(date.getTime() + TimeUnit.DAYS.toMillis(i));
			String day = Global.sdf.format(d);
			double[] D = read(day);
			if (D[0] != 0 && addNewDay(day, v))
				cnt++;
			if (cnt == Global.lag_var)
				break;
		}
	}

	public void initializeExcelSheet(int sheetNum) throws IOException,
			WriteException, BiffException {

		File file = new File(path);
		Workbook myWorkbook = Workbook.getWorkbook(file);
		workbook = Workbook.createWorkbook(file, myWorkbook);
		sheet = workbook.getSheet(sheetNum);

		int k = 0;
		int pos = Global.price_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			price_cols[k++] = convert(++pos);
		}
		k = 0;
		pos = Global.volume_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			volume_cols[k++] = convert(++pos);
		}
	}

	public void writeFeatures() throws WriteException {
		addCaption(0, 0, "Day");
		for (int i = 0; i < features.length; i++) {
			addCaption(i + 1, 0, features[i]);
		}
		int k = 0;
		int pos = Global.price_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, 0, "price(" + i + ")");
			price_cols[k++] = convert(pos);
		}
		k = 0;
		pos = Global.volume_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, 0, "volume(" + i + ")");
			volume_cols[k++] = convert(pos);
		}

		addNumber(specialCol, 0, 1.0);
	}

	public int getRowsCnt() throws Exception {
		Cell cell = sheet.getCell(specialCol, 0);
		return (int) Double.parseDouble(cell.getContents());
	}

	boolean addNewDay(String day, double[] val) throws Exception {
		double[] d = read(day);
		if (d[0] == 0) {
			return false;
		}
		int row = getRowsCnt();
		addNumber(specialCol, 0, row + 1.0);
		addCaption(0, row, day);
		int n = val.length;
		for (int j = 0; j < n; j++)
			addNumber(j + 1, row, val[j]);

		int start_price = Global.price_start_col;
		for (int i = -lag_var; i <= lag_var; i++) {
			if (row + i > 0)
				addNumber(start_price + i, row + i, d[0]);

		}
		int start_Volume = Global.volume_start_col;
		for (int i = -lag_var; i <= lag_var; i++) {
			if (row + i > 0)
				addNumber(start_Volume + i, row + i, d[1]);

		}
		return true;
	}

	public double[] read(String dayx) throws IOException, ParseException {
		File inputWorkbook = new File(Global.historyPath + CompanyName + ".xls");
		Workbook w;
		String volume = "0", price = "0";
		try {

			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			Sheet sheet = w.getSheet(0);
			// Loop over first 10 column and lines
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell cell = sheet.getCell(0, i);
				String day2 = cell.getContents();
				if (Global.areEquals(day2, dayx)) {
					volume = sheet.getCell(5, i).getContents();
					price = sheet.getCell(6, i).getContents();
				}
			}
		} catch (BiffException e) {
			e.printStackTrace();
		}

		double a = Double.parseDouble(price);
		double b = Double.parseDouble(volume);

		return new double[] { a, b };
	}

	void writeAndClose() throws WriteException, IOException {
		workbook.write();
		workbook.close();
	}

	void calcCorrel(int col, int row, String ch1, String ch2, int start, int end)
			throws WriteException {
		// TODO

		end -= (lag_var + 1);
		WritableCellFormat cellFormat = new WritableCellFormat();

		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		StringBuffer buf = new StringBuffer();
		String s1 = ch1 + "" + start + ":" + ch1 + "" + end;
		String s2 = ch2 + "" + start + ":" + ch2 + "" + end;
		buf.append("CORREL(" + s1 + "," + s2 + ")");
		Formula f = new Formula(col, row, buf.toString(), cellFormat);
		sheet.addCell(f);
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
	public void drawTable1() throws Exception {
		int frow = getRowsCnt() + 5, fcolumn = 1;

		addLabel(fcolumn, frow, "Features\\Lag");
		int r = frow + 1;
		for (int i = 0; i < features.length; i++) {
			addCaption(fcolumn, r++, features[i]);
		}

		int pos = fcolumn + 1;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, frow, "price(" + i + ")");
		}

		int cnt = 1;
		for (char ch2 = 'B'; ch2 < 'S'; ch2++) {
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = price_cols[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						getRowsCnt());

			}
			cnt++;
		}
	}

	public void drawTable2() throws Exception {

		int frow = getRowsCnt() + 5, fcolumn = lag_var * 4;
		addLabel(fcolumn, frow, "Features\\Lag");
		int r = frow + 1;
		for (int i = 0; i < features.length; i++) {
			addCaption(fcolumn, r++, features[i]);
		}

		int pos = fcolumn + 1;
		for (int i = -lag_var; i <= lag_var; i++) {
			addCaption(pos++, frow, "volume(" + i + ")");
		}

		int cnt = 1;
		for (char ch2 = 'B'; ch2 < 'S'; ch2++) {
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = volume_cols[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						getRowsCnt());
			}
			cnt++;
		}
	}

	public void drawTable3() throws Exception {
		int frow = getRowsCnt() + features.length + 10, fcolumn = 1;
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
			addCaption(pos++, frow, "volume(" + i + ")");
		}

		int cnt = 1;
		for (int c = 0; c < t; c++) {
			int a = Global.start_of_norm_table + Global.features_num + c + 3;
			String ch2 = convert(a);
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = volume_cols[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						getRowsCnt());
			}
			cnt++;
		}
	}

	// feature = 0 then price else volume
	public void drawTable4() throws Exception {
		int frow = getRowsCnt() + features.length + 10, fcolumn = lag_var * 4;
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
			addCaption(pos++, frow, "price(" + i + ")");
		}

		int cnt = 1;
		for (int c = 0; c < t; c++) {
			int a = Global.start_of_norm_table + Global.features_num + c + 3;
			String ch2 = convert(a);
			int index = 0;
			pos = fcolumn + 1;
			for (int i = -lag_var; i <= lag_var; i++) {
				String ch1 = price_cols[index++];
				calcCorrel(pos++, frow + cnt, ch1, ch2 + "", lag_var + 2,
						getRowsCnt());
			}
			cnt++;
		}
	}

	public void drawTables() throws Exception {
		drawNormalizedTable();
		clear();
		drawTable1();
		drawTable2();
		drawTable3();
		drawTable4();
		
	}

	private void clear() throws Exception {
		int raws = getRowsCnt();
		for (int column = 0; column < 100; column++) {
			for (int r = raws + 1; r < raws + 200; r++) {
				addLabel(column, r, "");
			}
		}
	}

	public void drawNormalizedTable() throws Exception {

		int INF = Integer.MAX_VALUE;
		int raw_n = getRowsCnt() - lag_var;
		System.out.println(">##" + raw_n);
		int width = Global.features_num + 1;

		double[][] d = new double[raw_n][width];
		double max[] = new double[width];
		double min[] = new double[width];

		for (int col = 1; col < width; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				String s = sheet.getCell(col, raw).getContents();
				if (!s.equals(""))
					d[raw][col] = Double.parseDouble(s);
				else {
					System.out.println("Warninng");
					d[raw][col] = INF;
				}
				if (d[raw][col] != INF) {
					max[col] = Math.max(max[col], d[raw][col]);
					min[col] = Math.min(min[col], d[raw][col]);
				}
			}
		}

		for (int col = 1; col < width; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				if (d[raw][col] != INF)
					d[raw][col] = (d[raw][col] - min[col])
							/ (max[col] - min[col]);
			}
		}

		int start_col = Global.start_of_norm_table;

		ArrayList<ArrayList<Double>> ar = new ArrayList<>();
		for (int i = 0; i < d.length; i++) {
			ar.add(new ArrayList<Double>());
			for (int j = 1; j < d[0].length; j++) {
				for (int j2 = j + 1; j2 < d[0].length; j2++) {
					ar.get(i).add(d[i][j] + d[i][j2]);
				}
			}
		}

		for (int col = start_col; col < max.length + start_col; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				if (col - start_col > 0 && d[raw][col - start_col] != INF)
					addNumber(col, raw, d[raw][col - start_col]);
			}
		}

		start_col = Global.start_of_norm_table + Global.features_num + 2;

		int size = ar.get(1).size();
		for (int col = start_col; col < size + start_col; col++) {
			for (int raw = lag_var + 1; raw < raw_n; raw++) {
				double a = ar.get(raw).get(col - start_col);
				addNumber(col, raw, a);
			}
		}

	}

	String convert(int a) {
		int k = 1;
		while (a >= k) {
			a -= k;
			k *= 26;
		}
		k /= 26;
		String s = "";

		while (k > 0) {
			s += ((char) ('A' + (a / k)));
			a %= k;
			k /= 26;
		}
		return s;
	}

}