package Historical_Prices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.BorderLineStyle;
import jxl.write.Border;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import Default.Global;

public class YahooFinanceDownloader {

	static String history = Global.historyPath;
	static WritableSheet sheet;

	public static void downloadFile(String symbol) throws Exception {
		URL website = new URL(
				"http://ichart.finance.yahoo.com/table.csv?s="
						+ symbol
						+ "&a=01&b=01&c=2014&d=03&e=.csv%2bHistorical%2bPrices&f=sl1d1t1c1ohgv&g=d&ignore=.csv");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("YahooData/" + symbol
				+ ".csv");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();

		File file = new File(history+symbol+".xls");
		WritableWorkbook workbook = Workbook.createWorkbook(file);
		workbook.createSheet("Twitter", 0);
		sheet = workbook.getSheet(0);

		Scanner scanner = new Scanner(new File("YahooData/" + symbol + ".csv"));

		int row = 0;
		String[] s = scanner.nextLine().split(",");
		for (int i = 0; i < s.length; i++) {
			addLabel(i, row, s[i]);
		}
		while (scanner.hasNext()) {
			row++;
			s = scanner.nextLine().split(",");
			addLabel(0, row, s[0]);
			for (int i = 1; i < s.length; i++) {
				addNumber(i, row, Double.parseDouble(s[i]));
			}
		}
		scanner.close();
		workbook.write();
		workbook.close();
		System.out.println("Finish Company : " + symbol);
	}

	private static void addNumber(int column, int row, Double d)
			throws WriteException, RowsExceededException {
		sheet.addCell(new Number(column, row, d));
	}

	private static void addLabel(int column, int row, String s)
			throws WriteException, RowsExceededException {
		sheet.addCell(new Label(column, row, s));
	}

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(new File("candidates.txt"));

		while (sc.hasNext()) {
			String Company = sc.nextLine();
			downloadFile(Company.substring(1));
		}

		sc.close();
	}
}
