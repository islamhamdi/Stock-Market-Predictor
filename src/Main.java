import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.batik.dom.svg12.Global;

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

				int n = files.length;

				myComp[] f = new myComp[n];
				for (int j = 0; j < n; j++) {
					f[j] = new myComp(files[j]);
				}
				Arrays.sort(f);

				int start = excel.getRowsCnt() - GLOBAL.lag_var - 1;
				System.out.println(start);
				for (int j = start; j < n; j++) {
					
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
				System.out
						.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				// excel.calcCorrel();
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
}
