package Average;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YahooFinanceStatisticsDownloader {
	final static String TITLE_TABLE = "yfnc_mod_table_title1";
	final static String DATA_TABLE = "yfnc_datamodoutline1";

	public static Table[] getStatisticsTables(String companyName) {
		String html = "http://finance.yahoo.com/q/ks?s=" + companyName;
		Table[] result = new Table[3];

		try {
			Document doc = Jsoup.connect(html).get();
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
						Elements c = b.select("td").get(0).select("tr").select("td");
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

	public static void main(String[] args) {
		Table[] result = getStatisticsTables("AAPL");

		for (int i = 0; i < result.length; i++)
			System.out.println(result[i].toString());
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
	}
}

// Table 1 : yfnc_modtitlew1

