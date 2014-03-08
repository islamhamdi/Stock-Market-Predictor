import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.apache.batik.dom.svg12.Global;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

public class Main {

	public static void main(String[] args) throws Exception, WriteException {
		String StatFolderPath = GLOBAL.StatFolderPath;
		String path = GLOBAL.path;
		File statDir = new File(StatFolderPath);
		if (!statDir.exists())
			statDir.mkdir();

		File statusDir = new File(path);
		File[] folders = statusDir.listFiles();
		String[] featuresList = Helper.getFeaturesList();
		StatisticsTool tool;

		WriteExcel excel = new WriteExcel();
		excel.passFeatures(featuresList);
		for (int i = 0; i < folders.length; i++)
			if (folders[i].isDirectory()) {

				String folderName = folders[i].getName();
				System.out.println("____" + folderName + "_____");

				String statfilePath = StatFolderPath + "/" + folderName
						+ ".xls";
				excel.setOutputFile(statfilePath, folderName);

				statDir = new File(statfilePath);
				if (!statDir.exists()) {
					excel.createExcel();
				}
				excel.initializeExcelSheet();

				File dir = new File(path + "/" + folderName);
				File[] files = dir.listFiles();

				HashSet<String> hs = getAvailableDays(folderName);
				int n = 0;
				for (int j = 0; j < files.length; j++) {
					if (hs.contains(files[j].getName()))
						n++;
				}

				myComp[] f = new myComp[n];
				int index = 0;
				for (int j = 0; j < files.length; j++) {
					if (hs.contains(files[j].getName()))
						f[index++] = new myComp(files[j]);
				}
				Arrays.sort(f);

				int start = excel.getRowsCnt() - GLOBAL.lag_var - 1;
				System.out.println(start);
				for (int j = start; j < f.length; j++) {
					if (f[j].file.isFile()) {
						System.out.println(f[j].file.getName());
						tool = new StatisticsTool(f[j].file.getAbsolutePath());
						tool.parseData();
						// tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();
						excel.addNewDay(f[j].file.getName(),
								tool.getFeaturesValues());
					} else {
						throw new Exception(
								"Make sure companies directories contain only files.");
					}
				}
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				excel.drawTables();
				excel.writeAndClose();
			}
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
		File inputWorkbook = new File(GLOBAL.historyPath + CompanyName + ".xls");
		Workbook w;
		w = Workbook.getWorkbook(inputWorkbook);
		Sheet sheet = w.getSheet(0);
		for (int i = 1; i < sheet.getRows(); i++) {
			Cell cell = sheet.getCell(0, i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String s = cell.getContents();
			Date from = sdf.parse(s);
			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
			hs.add(sdf2.format(from));
		}
		return hs;
	}

}
