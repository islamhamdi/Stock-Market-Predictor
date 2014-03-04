import java.io.File;

import jxl.write.WriteException;

public class Main {
	private static String path = "/home/islamhamdi/Desktop/TwitterStockData";

	public static void main(String[] args) throws Exception, WriteException {

		File statusDir = new File(path);
		File[] folders = statusDir.listFiles();
		String[] featuresList = Helper.getFeaturesList();
		StatisticsTool tool;

		for (int i = 0; i < folders.length; i++)
			if (folders[i].isDirectory()) {

				System.out.println("_______________" + folders[i].getName()
						+ "_______________");
				File dir = new File(path + "/" + folders[i].getName());
				File[] files = dir.listFiles();

				WriteExcel excelSheet = new WriteExcel();
				excelSheet.setOutputFile(folders[i].getName() + ".xls",
						folders[i].getName(), featuresList);
				excelSheet.initializeExcelSheet();

				for (int j = 0; j < files.length; j++) {

					if (files[j].isFile()) {
						tool = new StatisticsTool(files[j].getAbsolutePath());
						tool.parseData();
						// tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();
						excelSheet.addNewDay(files[j].getName(),
								tool.getFeaturesValues());
					} else {
						throw new Exception(
								"Make sure companies directories contain only files.");
					}
				}
				System.out
						.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				excelSheet.calcCorrel();
				excelSheet.writeAndClose();
			}
	}
}
