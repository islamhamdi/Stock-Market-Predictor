package Average;

import java.io.File;

import jxl.Workbook;

public class Main {

	static int sheets_num = 6;
	static String inputFolder = "/home/mohamed/Dropbox/Stock Market Daily Data/statistics/plain";

	public static void main(String[] args) throws Exception {

		File statusDir = new File(inputFolder);
		if (!statusDir.exists())
			throw new Exception("input folder doesnt exist");
		File[] files = statusDir.listFiles();

		Workbook[] workbooks = new Workbook[files.length];
		File inputWorkbook;

		for (int k = 0; k < files.length; k++) {
			inputWorkbook = new File(files[k].getAbsolutePath());
			workbooks[k] = Workbook.getWorkbook(inputWorkbook);
		}

		AVR avr = new AVR(workbooks, "avr.xls");
		for (int i = 0; i <= sheets_num; i++)
			avr.read(i);

		avr.close();
	}
}
