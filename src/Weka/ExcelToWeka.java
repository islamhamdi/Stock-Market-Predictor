package Weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelToWeka {
	static int lags = 3;

	public static String inDir = "/home/mohamed/Dropbox/Stock Market Daily Data/statistics";
	public static String outDir = "./WekaOutput/";

	public static void main(String[] args) throws Exception {
		run();
	}

	private static void run() throws IOException, ParseException {
		File statusDir = new File(outDir);
		if (!statusDir.exists())
			statusDir.mkdir();
		statusDir = new File(inDir);
		File[] files = statusDir.listFiles();
		for (File f : files) {
			String st = f.getName();
			String companyNam = st.substring(0, st.indexOf("."));
			System.out.println("Read File : " + companyNam);

			ExcelInterface excel = new ExcelInterface(f);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outDir
					+ companyNam + ".arff"));

			// write relation name
			bw.write("@relation " + companyNam + "\n");

			// write attributes
			for (String s : excel.getFeatures())
				bw.write("@attribute " + s + " real\n");

			// write tuples
			bw.write("@data\n");
			for (double[] tuple : excel.getTuples()) {
				String s = "";
				for (double d : tuple)
					s += "," + d;
				s = s.substring(1);

				bw.write(s + "\n");
			}

			bw.close();
		}
	}

	private static class ExcelInterface {
		private String[] features;
		private double[][] tuples;
		private File file;

		public ExcelInterface(File file) throws IOException, ParseException {
			this.file = file;
			readExcel();
		}

		public String[] getFeatures() {
			return features;
		}

		public double[][] getTuples() {
			return tuples;
		}

		public void readExcel() throws IOException, ParseException {
			File inputWorkbook = file;
			Workbook w;
			try {

				w = Workbook.getWorkbook(inputWorkbook);
				// Get the first sheet
				Sheet sheet = w.getSheet(0);

				String raws = sheet.getCell(60, 0).getContents();

				int rawscnt = Integer.parseInt(raws);

				int h = rawscnt - 2 * lags - 1;
				Cell[] c = sheet.getRow(0);
				int width = sheet.getRow(lags + 1).length;
				features = new String[width - 1];

				for (int j = 0; j < features.length; j++) {
					features[j] = c[j + 1].getContents();
				}

				tuples = new double[h][width - 1];
				int index = 0;
				for (int i = lags + 1; i <= lags + h; i++) {
					c = sheet.getRow(i);
					for (int j = 1; j < tuples[0].length; j++) {
						String str = c[j].getContents();
						tuples[index][j - 1] = Double.parseDouble(str);
					}
					index++;
				}
			} catch (BiffException e) {
				e.printStackTrace();
			}

		}

	}
}
