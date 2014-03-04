import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class WriteExcel {

	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private String inputFile;
	String companyName;
	String[] features;

	public void setOutputFile(String inputFile, String companyName,
			String[] features) throws IOException {
		this.inputFile = inputFile;
		Workbook.createWorkbook(new File(inputFile));
		this.companyName = companyName;
		this.features = features;
	}

	WritableWorkbook workbook;
	WritableSheet sheet;

	public void initializeExcelSheet() throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet(this.companyName, 0);
		sheet = workbook.getSheet(0);
		// createLabel(excelSheet);
		writeFeatures();
	}

	private void writeFeatures() throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		int n = features.length;
		addCaption(0, 0, "Day");
		for (int i = 0; i < features.length; i++) {
			addCaption(i + 1, 0, features[i]);
		}
		addCaption(n + 1, 0, "price");
		addCaption(n + 2, 0, "Volume");
	}

	private void createLabel() throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		// Write a few headers
		addCaption(0, 0, "feature 1");
		addCaption(1, 0, "volume");
		calcCorrel();
	}

	void addNewDay(String day, double[] val) throws RowsExceededException,
			WriteException, IOException {

		int row = sheet.getRows();
		Label label = new Label(0, row, day);
		sheet.addCell(label);

		for (int j = 0; j < val.length; j++)
			addNumber(j + 1, row, (int) val[j]);

	}

	void writeAndClose() throws WriteException, IOException {
		workbook.write();
		workbook.close();
	}

	void calcCorrel() throws WriteException {
		int curRow = sheet.getRows();
		addLabel(0, curRow, "correlation");
		StringBuffer buf = new StringBuffer();
		buf.append("CORREL(A1:A10,B1:B10)");
		Formula f = new Formula(1, curRow, buf.toString());
		sheet.addCell(f);
		buf = new StringBuffer();
	}

	private void addCaption(int column, int row, String s)
			throws RowsExceededException, WriteException {
		sheet.addCell(new Label(column, row, s, timesBoldUnderline));
	}

	private void addNumber(int column, int row, Integer integer)
			throws WriteException, RowsExceededException {
		sheet.addCell(new Number(column, row, integer));
	}

	private void addLabel(int column, int row, String s) throws WriteException,
			RowsExceededException {
		sheet.addCell(new Label(column, row, s, times));
	}
}