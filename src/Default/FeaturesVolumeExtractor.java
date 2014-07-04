package Default;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class FeaturesVolumeExtractor {
	private static String companiesDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/statistics/";
	private static String outputDirectory = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/PlotCompanies/";
	private static int featureIndex, sheetIndex;
	private static final String[] GRAPH_FEATURES_NAMES = Helper
			.getFeaturesList();

	public static void main(String[] args) throws IndexOutOfBoundsException,
			BiffException, IOException {

		for (int j = 0; j < Global.sheet_num.length; j++) {
			sheetIndex = Global.sheet_num[j];

			for (int i = 1; i <= GRAPH_FEATURES_NAMES.length; i++) {
				featureIndex = i;

				String sheetDirectory = outputDirectory
						+ Global.sheets[sheetIndex] + "/";
				createDirectory(sheetDirectory);

				run();
			}
		}
	}

	private static void run() throws IndexOutOfBoundsException, BiffException,
			IOException {
		File[] companiesList = new File(companiesDirectory).listFiles();

		for (File company : companiesList) {
			System.out.println("Read Company : " + company.getName());

			if (!company.getName().startsWith("$"))
				continue;

			double[] featureValue = getFeatureValues(company);
			double[] volumeValue = getVolumeValues(company);

			String companyName = company.getName().replace(".xls", "");

			writeOutputToCSVFile(featureValue, volumeValue, companyName);
		}
	}

	private static void writeOutputToCSVFile(double[] featureValue,
			double[] volumeValue, String companyName) throws IOException {

		String sheetDirectory = outputDirectory + Global.sheets[sheetIndex]
				+ "/";
		createDirectory(sheetDirectory + companyName);

		String featureName = GRAPH_FEATURES_NAMES[featureIndex - 1];
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(
				sheetDirectory + companyName + "/" + featureName + ".csv"));

		for (int i = 0; i < featureValue.length; i++) {
			csvWriter.write(featureValue[i] + "," + volumeValue[i] + "\n");
		}

		csvWriter.close();
	}

	private static double[] getVolumeValues(File company)
			throws IndexOutOfBoundsException, BiffException, IOException {

		Sheet excelSheet = Workbook.getWorkbook(company).getSheet(sheetIndex);
		int numberOfDays = (int) Double.parseDouble(excelSheet.getCell(
				Global.specialCell, 0).getContents());
		int volumeColume = Global.volume_start_col;
		double[] volumeValues = new double[numberOfDays];

		for (int i = 1; i <= numberOfDays; i++) {
			volumeValues[i - 1] = parseCell(excelSheet.getCell(volumeColume, i));
		}

		return volumeValues;
	}

	private static double[] getFeatureValues(File company)
			throws IndexOutOfBoundsException, BiffException, IOException {

		Sheet excelSheet = Workbook.getWorkbook(company).getSheet(sheetIndex);
		int numberOfDays = (int) Double.parseDouble(excelSheet.getCell(
				Global.specialCell, 0).getContents());
		double[] featureValues = new double[numberOfDays];

		for (int i = 1; i <= numberOfDays; i++) {
			featureValues[i - 1] = parseCell(excelSheet
					.getCell(featureIndex, i));
		}

		return featureValues;
	}

	private static void createDirectory(String directory) {
		File newDirectory = new File(directory);
		newDirectory.mkdir();
	}

	private static double parseCell(Cell cell) {
		String cellValue = cell.getContents();
		if (cellValue.isEmpty())
			return 0;
		else
			return Double.parseDouble(cellValue);
	}
}
