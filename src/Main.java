import java.io.File;
import java.util.Arrays;

import jxl.write.WriteException;

public class Main {

	public static void main(String[] args) throws Exception, WriteException {
		String StatFolderPath = Global.StatFolderPath;
		String path = Global.path;
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

				Arrays.sort(files);

				int start = excel.getRows() - 1;
				for (int j = start; j < files.length; j++) {
					if (files[j].isFile()) {
						System.out.println(files[j].getName());
						tool = new StatisticsTool(files[j].getAbsolutePath());
						tool.parseData();
						// tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();
						excel.addNewDay(files[j].getName(),
								tool.getFeaturesValues());
					} else {
						throw new Exception(
								"Make sure companies directories contain only files.");
					}
				}
				System.out
						.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				// excel.calcCorrel();
				excel.drawTable(20, 0, 0);

				excel.drawTable(25, 0, 0);

				excel.writeAndClose();
			}
	}
}
