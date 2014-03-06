import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class WriteExcel {

	String path;
	String[] features;
	String CompanyName;
	WritableWorkbook workbook;
	WritableSheet sheet;
	int colPos = 17;

	public void passFeatures(String[] features) throws IOException {
		this.features = features;
	}

	public void setOutputFile(String companyPath, String CompanyName)
			throws IOException {
		this.path = companyPath;
		this.CompanyName = CompanyName;

	}

	public void createExcel() throws IOException, WriteException {
		File file = new File(path);
		workbook = Workbook.createWorkbook(file);
		workbook.createSheet("Report", 0);
		sheet = workbook.getSheet(0);
		writeFeatures();
		writeAndClose();
	}

	public void initializeExcelSheet() throws IOException, WriteException,
			BiffException {
		File file = new File(path);
		Workbook myWorkbook = Workbook.getWorkbook(file);
		workbook = Workbook.createWorkbook(file, myWorkbook);
		sheet = workbook.getSheet(0);
	}

	public int getCellValue() {
		Cell cell = sheet.getCell(colPos, 0);
		return (int) Double.parseDouble(cell.getContents());
	}

	public void writeFeatures() throws WriteException {
		int n = features.length;
		addCaption(0, 0, "Day");
		for (int i = 0; i < features.length; i++) {
			addCaption(i + 1, 0, features[i]);
		}
		addCaption(n + 1, 0, "price");
		addCaption(n + 2, 0, "Volume");
		addNumber(colPos, 0, 1.0);

	}

	public int getRows() throws Exception {
		// return sheet.getRows();
		return getCellValue();
	}

	void addNewDay(String day, double[] val) throws Exception {

		int row = sheet.getRows();

		addNumber(colPos, 0, row + 1.0);

		Label label = new Label(0, row, day);
		sheet.addCell(label);

		int n = val.length;
		for (int j = 0; j < n; j++)
			addNumber(j + 1, row, val[j]);

		double[] d = read(day);

		addNumber(n + 1, row, d[0]);
		addNumber(n + 2, row, d[1]);

	}

	static boolean areEquals(String price_day, String day2)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date from = sdf.parse(price_day);
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyy");
		Date to = sdf2.parse(day2);
		return from.equals(to);
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
			boolean found = false;
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell cell = sheet.getCell(0, i);
				String day2 = cell.getContents();
				if (areEquals(day2, dayx)) {
					found = true;
					volume = sheet.getCell(5, i).getContents();
					price = sheet.getCell(6, i).getContents();
					System.out.println("v=" + volume + ", price= " + price);
				}
			}

			if (!found) {
				price = "-1";
				volume = "-1";
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

	void calcCorrel(int col, int row, char ch1, char ch2, int end)
			throws WriteException {
		StringBuffer buf = new StringBuffer();
		String s1 = ch1 + "2:" + ch1 + "" + end;
		String s2 = ch2 + "2:" + ch2 + "" + end;
		buf.append("CORREL(" + s1 + "," + s2 + ")");
		Formula f = new Formula(col, row, buf.toString());
		sheet.addCell(f);
	}

	private void addCaption(int column, int row, String s)
			throws RowsExceededException, WriteException {
		sheet.addCell(new Label(column, row, s));
	}

	private void addNumber(int column, int row, Double d)
			throws WriteException, RowsExceededException {
		sheet.addCell(new Number(column, row, d));
	}

	private void addLabel(int column, int row, String s) throws WriteException,
			RowsExceededException {
		sheet.addCell(new Label(column, row, s));
	}

	// feature = 0 then price else volume
	public void drawTable(int fcolumn, int frow, int feature) throws Exception {

		addLabel(fcolumn, frow, "Features\\Lag");
		int r = frow + 1;
		for (int i = 0; i < features.length; i++) {
			addLabel(fcolumn, r++, features[i]);
		}

		// lag =0

		char[] f = new char[14];
		for (int i = 0; i < f.length; i++) {
			f[i] = (char) ('B' + i);
		}

		int lag = 0;

		addLabel(fcolumn + 1, frow, "lag(0)");

		char second;

		if (feature == 0)
			second = 'P';
		else
			second = 'Q';

		r = frow + 1;
		int size = getRows();
		for (int i = 0; i < f.length; i++) {
			calcCorrel(fcolumn + lag + 1, r++, f[i], second, size);
		}

	}

}