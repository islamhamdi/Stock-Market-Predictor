import java.io.File;
import java.io.IOException;

import jxl.write.WriteException;

public class Reader {
	private static String path = "/home/islamhamdi/Desktop/TwitterStockData";

	public static void main(String[] args) throws IOException, WriteException {

		File statusDir = new File(path);
		File[] folders = statusDir.listFiles();
		StatisticsTool tool = new StatisticsTool();

		String[] featureList = tool.getFeaturesList();
		WriteExcel test = new WriteExcel();

		for (int i = 0; i < folders.length; i++)
			if (folders[i].isDirectory()) {
				File dir = new File(path + "/" + folders[i].getName());
				File[] files = dir.listFiles();

				for (int j = 0; j < files.length; j++) {
					System.out.println(files[j].getName());
					System.out.println(">>>> " + files[j].isFile());

					if (files[j].isFile()) {
						tool = new StatisticsTool();
						tool.initialize(files[j].getAbsolutePath());
						tool.parseData();
						// tool.addSimilarityNodes();
						tool.buildActivityFeatures();
						tool.buildGraphFeatures();
						test.setOutputFile("output.xls", folders[i].getName(),
								featureList);
						test.write();
						test.addNewDay(files[j].getName(),
								tool.getFeatureValues());
						test.close();
					}
				}
			}
	}
}
