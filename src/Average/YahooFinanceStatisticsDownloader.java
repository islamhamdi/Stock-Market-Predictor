package Average;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YahooFinanceStatisticsDownloader {
	final static String TITLE_TABLE = "yfnc_mod_table_title1";
	final static String DATA_TABLE = "yfnc_datamodoutline1";

	static WritableSheet sheet;

	public static Table[] getStatisticsTables(String companyName) {
		String html = "http://finance.yahoo.com/q/ks?s=" + companyName;
		Table[] result = new Table[3];

		try {
			Document doc = Jsoup.connect(html).timeout(100 * 1000).get();
			Elements tableElements = doc.select("table");
			Elements tableRowElements = tableElements.select(":not(thead) tr");

			ArrayList<String[]> list = new ArrayList<String[]>();
			for (int i = 0; i < tableRowElements.size(); i++) {
				Elements a = tableRowElements.get(i).select("table");

				for (Element b : a) {
					if (b.className().equals(TITLE_TABLE)) {
						String title = b.text();

						list.add(null);
						list.add(new String[] { title, title });
					} else if (b.className().equals(DATA_TABLE)) {
						Elements c = b.select("td").get(0).select("tr")
								.select("td");
						int start = 0;
						if (c.size() % 2 == 1) // first element is title (skip
												// this element)
							start = 1;

						for (int d = start; d < c.size();) {
							String s1 = c.get(d++).text();
							String s2 = c.get(d++).text();

							list.add(new String[] { s1, s2 });
						}
					}
				}
			}

			int currentTable = 0;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == null) {
					// new table
					String title = list.get(++i)[0];

					ArrayList<String[]> data = new ArrayList<String[]>();
					for (i = i + 1; i < list.size(); i++)
						if (list.get(i) != null)
							data.add(list.get(i));
						else
							break;

					i--;
					result[currentTable++] = new Table(title, data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(new File("YahooKeyCompanies.txt"));
		String company[] = new String[66];
		for(int i = 0; i < 100; i++)
			sc.nextLine();
		
		for (int i = 0; i < company.length; i++) {
			company[i] = sc.nextLine().substring(1);
		}
		sc.close();

		File file = new File("CompaniesData3.xls");
		WritableWorkbook workbook = Workbook.createWorkbook(file);
		workbook.createSheet("data", 0);
		sheet = workbook.getSheet(0);

		Table[] result = getStatisticsTables(company[0]);

		int column = 1, row = 0;
		for (int i = 0; i < result.length; i++) {
			Table table = result[i];
			String[] tuple = table.getHeader();
			for (int j = 0; j < tuple.length; j++) {
				addLabel(column++, row, tuple[j]);
			}

		}

		row = 1;
		for (int c = 0; c < company.length; c++) {
			System.out.println(c + " Start Company : " + company[c]);
			
			result = getStatisticsTables(company[c]);
			System.out.println(company[c]);
			column = 1;

			addLabel(0, row, company[c]);
			for (int i = 0; i < result.length; i++) {
				Table table = result[i];
				String[] tuple = table.getTuple();

				for (int j = 0; j < tuple.length; j++) {
					addLabel(column++, row, tuple[j]);
				}

			}
			row++;
		}
		workbook.write();
		workbook.close();
	}


	private static void addLabel(int column, int row, String s)
			throws WriteException, RowsExceededException {
		sheet.addCell(new Label(column, row, s));
	}

	public static class Table {
		public String title;
		public ArrayList<String[]> data;

		public Table(String title, ArrayList<String[]> data) {
			this.title = title;
			this.data = data;
		}

		@Override
		public String toString() {
			String result = "TITLE : " + title + "\n\n";
			for (int i = 0; i < data.size(); i++)
				result += data.get(i)[0] + " >> " + data.get(i)[1] + "\n";

			result += "\n";
			return result;
		}

		public String[] getTuple() {
			String[] tuple = new String[data.size()];
			for (int i = 0; i < data.size(); i++)
				tuple[i] = data.get(i)[1];

			return tuple;
		}

		public String[] getHeader() {
			String[] tuple = new String[data.size()];
			for (int i = 0; i < data.size(); i++)
				tuple[i] = data.get(i)[0];

			return tuple;
		}

	}
}

// Table 1 : yfnc_modtitlew1
