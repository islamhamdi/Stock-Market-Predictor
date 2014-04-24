package Default;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

public class Main {

	static String path;
	static int sheetNum;
	static String statPath = Global.StatFolderPath;
	static WriteExcel excel;
	static HashSet<String> currAvailableDays;

	public static void main(String[] args) throws Exception, WriteException {

		currAvailableDays = getAvailableDays("$BBRY");

		// 0 Twitter - 1 StockTwits - 2 Combined - 3 PosTwitter - 4 NegTwitter -
		// 5 PosStockTwit - 6 NegStockTwit
		Global.files_to_run = Global.sheet_num[5];

		// preprocessUrlExpansion();
		path = Global.dataPaths[Global.files_to_run];
		sheetNum = Global.files_to_run;

		File statDir = new File(statPath);
		if (!statDir.exists())
			statDir.mkdir();

		File statusDir = new File(path);
		File[] folders = statusDir.listFiles();
		String[] featuresList = Helper.getFeaturesList();
		StatisticsTool tool;
		excel = new WriteExcel();
		System.out.println("Start ");
		excel.passFeatures(featuresList);
		HashSet<String> avCompanies = getAvailableCompanies();

		set_price_vol_columns();
		myComp[] f;
		for (int i = 0; i < folders.length; i++) {
			String folderName = folders[i].getName();

			if (folders[i].isDirectory() && avCompanies.contains(folderName)) {
				System.out.println("____" + folderName + "_____");

				f = getFileList(folderName);

				if (f.length < 2)
					continue;

				openExcelWriter(folderName, f[0].file.getName());

				int start = excel.getRowsCnt() - Global.lag_var - 1;

				System.out.println("ROWS CNT = " + excel.getRowsCnt());

				boolean someThingNew = false;
				for (int j = start; j < f.length; j++) {

					if (f[j].file.isFile()) {
						System.out.println(f[j].file.getName());
						tool = new StatisticsTool(folderName,
								f[j].file.getName(),
								f[j].file.getAbsolutePath());
						tool.parseData();
						tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();

						double[] a = tool.getFeaturesValues();
						if (a == null)
							continue;
						someThingNew = true;
						excel.addNewDay(f[j].file.getName(), a, true);
					} else {
						throw new Exception(
								"Make sure companies directories contain only files.");
					}
				}
				System.out.println(">>>>>>>>>>>>>>>>>>>>>next>>>>>>>>>>>>>>");
				excel.adddummyDaysAtEnd();
				if (someThingNew)
					excel.drawTables();
				excel.writeAndClose();
			}

		}
	}

	private static void set_price_vol_columns() {
		int lag_var = Global.lag_var;
		int k = 0;
		int pos = Global.price_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			Global.price_cols[k++] = Global.convert(++pos);
		}
		k = 0;
		pos = Global.volume_start_col - lag_var;
		for (int i = -lag_var; i <= lag_var; i++) {
			Global.volume_cols[k++] = Global.convert(++pos);
		}
	}

	private static void preprocessUrlExpansion() throws InterruptedException {
		String sourcePath = null, destPath = null;

		if (Global.files_to_run == Global.sheet_num[0]) {
			// sourcePath = Global.twitterDataPath;
			// destPath = Global.twitterDataExpandedPath;
		} else {
			// sourcePath = Global.stockTwitDataPath;
			// destPath = Global.stockTwitDataExpandedPath;
		}

		File statusDir = new File(sourcePath);
		File[] folders = statusDir.listFiles();
		URLExpander urlExpander;
		for (int i = 0; i < folders.length; i++) {
			String folderName = folders[i].getName();
			if (folders[i].isDirectory()) {
				String destinationPath = destPath + "/" + folderName;
				File destDir = new File(destinationPath);
				if (!destDir.exists())
					destDir.mkdir();
				urlExpander = new URLExpander(sourcePath + "/" + folderName,
						destinationPath);
				// System.out.println(destinationPath);
				urlExpander.startURLExpander();
				while (!urlExpander.isTerminated())
					;
			}
		}
	}

	private static void openExcelWriter(String folderName, String start)
			throws Exception {
		String statfilePath = statPath + "/" + folderName + ".xls";
		excel.setOutputFile(statfilePath, folderName);
		File statDir = new File(statfilePath);
		HashMap<String, VOL_PR> hs = price_volume_table(folderName);

		excel.set_price_vol_table(hs);

		if (!statDir.exists()) {
			excel.createExcel(start);
		}
		excel.initializeExcelSheet(sheetNum, start);

	}

	private static myComp[] getFileList(String folderName) throws Exception {
		File dir = new File(path + "/" + folderName);
		File[] files = dir.listFiles();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date start = sdf.parse(Global.startDate);

		int n = 0;
		for (int j = 0; j < files.length; j++) {
			Date cur = sdf.parse(files[j].getName());

			if (currAvailableDays.contains(files[j].getName())
					&& cur.after(start))
				n++;
		}

		myComp[] f = new myComp[n];
		int index = 0;
		for (int j = 0; j < files.length; j++) {
			Date cur = sdf.parse(files[j].getName());

			if (currAvailableDays.contains(files[j].getName())
					&& cur.after(start))
				f[index++] = new myComp(files[j]);
		}
		Arrays.sort(f);

		return f;
	}

	static class myComp implements Comparable<myComp> {
		File file;
		DateFormat f = new SimpleDateFormat("dd-MM-yyyy");
		String name;

		public myComp(File f) {
			file = f;
			name = f.getName();
		}

		@Override
		public int compareTo(myComp o) {
			try {
				return f.parse(name).compareTo(f.parse(o.name));
			} catch (ParseException e) {
				return 0;
			}
		}

	}

	public static HashSet<String> getAvailableDays(String CompanyName)
			throws Exception {
		HashSet<String> hs = new HashSet<String>();
		File inputWorkbook = new File(Global.historyPath + CompanyName + ".xls");
		Workbook w;
		w = Workbook.getWorkbook(inputWorkbook);
		Sheet sheet = w.getSheet(0);
		for (int i = 1; i < sheet.getRows(); i++)
			hs.add(sheet.getCell(0, i).getContents());
		w.close();
		return hs;
	}

	static HashSet<String> getAvailableCompanies() {
		File dir = new File(Global.historyPath);
		File[] files = dir.listFiles();
		HashSet<String> hs = new HashSet<>();
		for (int i = 0; i < files.length; i++) {
			hs.add(files[i].getName().replace(".xls", ""));
		}
		return hs;
	}

	public static HashMap<String, VOL_PR> price_volume_table(String CompanyName)

	throws IOException, ParseException {

		HashMap<String, VOL_PR> output = new HashMap<>();
		File inputWorkbook = new File(Global.historyPath + CompanyName + ".xls");
		Workbook w;
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			Sheet sheet = w.getSheet(0);
			// Loop over first 10 column and lines
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell cell = sheet.getCell(0, i);
				String day = cell.getContents();
				String volume = sheet.getCell(5, i).getContents();
				String price = sheet.getCell(6, i).getContents();
				double v = Double.parseDouble(volume);
				double p = Double.parseDouble(price);
				output.put(day, new VOL_PR(v, p));
			}
			w.close();

		} catch (BiffException e) {
			e.printStackTrace();
		}
		return output;
	}

	static class VOL_PR {
		double vol;
		double price;

		public VOL_PR(double v, double p) {
			vol = v;
			price = p;
		}
	}

}
