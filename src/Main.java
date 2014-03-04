import java.io.File;
import java.io.IOException;

import jxl.write.WriteException;

public class Main {
	private static String path = "/home/islamhamdi/Desktop/TwitterStockData";

	public static void main(String[] args) throws IOException, WriteException {

		File statusDir = new File(path);
		File[] folders = statusDir.listFiles();
		StatisticsTool tool = new StatisticsTool();

		String[] featureList = tool.getFeaturesList();
		for (int i = 0; i < folders.length; i++)
			if (folders[i].isDirectory()) {

				System.out.println(folders[i].getName());
				File dir = new File(path + "/" + folders[i].getName());
				File[] files = dir.listFiles();

				WriteExcel excelSheet = new WriteExcel();
				excelSheet.setOutputFile(folders[i].getName() + ".xls",
						folders[i].getName(), featureList);
				excelSheet.initializeExcelSheet();

				for (int j = 0; j < files.length; j++) {

					if (files[j].isFile()) {
						System.out.println(files[j]);
						tool = new StatisticsTool();
						tool.initialize(files[j].getAbsolutePath());
						tool.parseData();
						// tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();
						excelSheet.addNewDay(files[j].getName(),
								tool.getFeatureValues());
					}
				}
				System.out
						.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				excelSheet.calcCorrel();
				excelSheet.writeAndClose();
			}
	}
}
