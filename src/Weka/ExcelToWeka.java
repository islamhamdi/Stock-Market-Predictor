package Weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import Default.Global;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelToWeka {
	static int feature_num = 19;
	static int lags = 3;

	public static String inDir = "/home/mohamed/Dropbox/Stock Market Daily Data/Weka Companies";
	public static String outDir1 = "./price/";
	public static String outDir2 = "./volume/";

	public static void main(String[] args) throws Exception {
		run();
	}

	private static void run() throws IOException, ParseException {
		int sheetNo = 1;

		int[] curtype = { Global.volume_start_col, Global.price_start_col };
		String[] curDir = { outDir2, outDir1 };

		for (int k = 0; k < curDir.length; k++) {
			for (int index = 0; index < 3; index++) {
				String tmp[] = { "today", "tomorrow", "aftertomorrow" };
				int type = curtype[k] - index;
				String outDir = curDir[k];

				File statusDir = new File(outDir);
				if (!statusDir.exists())
					statusDir.mkdir();
				statusDir = new File(inDir);
				File[] files = statusDir.listFiles();
				for (File f : files) {
					if (f.isDirectory())
						continue;
					String st = f.getName();
					System.out.println(st);
					String companyNam = st.substring(0, st.indexOf("."));

					ExcelInterface excel = new ExcelInterface(f, type, sheetNo);
					BufferedWriter bw = new BufferedWriter(new FileWriter(
							outDir + companyNam + tmp[index] + ".arff"));

					// write relation name
					bw.write("@relation " + companyNam + "\n");

					// write attributes
					String[] fet = excel.getFeatures();
					for (int i = 0; i < fet.length - 1; i++) {
						if (i == 2 || i == 13)
							bw.write("@attribute " + fet[i] + " real\n");

					}

					bw.write("@attribute class {increase ,decrease}\n");

					// write tuples
					bw.write("@data\n");
					for (String[] tuple : excel.getTuples()) {
						if (tuple[19].equals("-1") || tuple[2].isEmpty())
							break;
						String s = "";
						for (int i = 0; i < tuple.length; i++)
							if (i == 2 || i == 13 || i == 19)
								s += "," + tuple[i];

						s = s.substring(1);

						bw.write(s + "\n");
					}

					bw.close();
				}
			}
		}
	}

	private static class ExcelInterface {
		private String[] features;
		private String[][] tuples;
		private File file;
		private int classColumn;
		private int SheetNO;

		public ExcelInterface(File file, int type, int SheetNO)
				throws IOException, ParseException {
			this.file = file;
			this.SheetNO = SheetNO;
			this.classColumn = type;
			readExcel();

		}

		public String[] getFeatures() {
			return features;
		}

		public String[][] getTuples() {
			return tuples;
		}

		public void readExcel() throws IOException, ParseException {
			File inputWorkbook = file;
			Workbook w;
			try {
				w = Workbook.getWorkbook(inputWorkbook);
				// Get the first sheet
				Sheet sheet = w.getSheet(SheetNO);

				String raws = sheet.getCell(Global.specialCell, 0)
						.getContents();

				int rawscnt = Integer.parseInt(raws);

				int h = rawscnt - lags - 1;

				Cell[] c = sheet.getRow(0);

				features = new String[feature_num + 1];

				for (int j = 0; j < feature_num; j++) {
					features[j] = c[j + 1].getContents();
				}

				tuples = new String[h][feature_num + 1];

				int index = 0;
				for (int i = lags + 1; i <= lags + h; i++) {
					c = sheet.getRow(i);
					for (int j = 1; j <= feature_num; j++)
						tuples[index][j - 1] = c[j].getContents();
					index++;
				}

				int cnt = 0;
				double mean = 0;
				for (int i = lags + 1; i <= lags + h; i++) {

					String s = sheet.getCell(classColumn, i).getContents();

					if (!s.isEmpty()) {
						cnt++;
						mean += Double.parseDouble(s);
					}
				}
				mean /= cnt;

				System.out.println(mean);
				index = 0;
				for (int i = lags + 1; i <= lags + h; i++) {
					String s = sheet.getCell(classColumn, i).getContents();
					String s2 = sheet.getCell(classColumn, i - 1).getContents();
					if (s.isEmpty()) {
						tuples[index][feature_num] = "-1";
						break;
					}

					double currprice = Double.parseDouble(s);
					double pastprice = Double.parseDouble(s2);

					if (currprice > pastprice)
						tuples[index][feature_num] = "increase";
					else
						tuples[index][feature_num] = "decrease";
					index++;
				}

			} catch (BiffException e) {
				e.printStackTrace();
			}

		}

	}
}
