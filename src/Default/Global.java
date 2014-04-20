package Default;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global {

	public static int start_row_t1 = 55;
	public static int start_row_t2 = 75;

	public static int start_col_t1 = 1;
	public static int start_col_t2 = 12;

	public static String[] sheets = { "Twitter", "StockTwits", "Combined Data",
			"Positive-Twitter", "Negative-Twitter", "Positive-StockTwits",
			"Negative-StockTwits" };

	public static int[] sheet_num = { 0, 1, 2, 3, 4, 5, 6 };

	// static String twitterDataPath =
	// "/home/islamhamdi/Desktop/TwitterStockData";
	// static String stockTwitDataPath =
	// "/home/islamhamdi/Desktop/StockTwitsData";
	// static String twitterDataExpandedPath =
	// "/home/islamhamdi/Desktop/TwitterStockDataExpanded";
	// static String stockTwitDataExpandedPath =
	// "/home/islamhamdi/Desktop/StockTwitsDataExpanded";
	// static String combinedDataPath = "/home/islamhamdi/Desktop/CombinedData";
	// static String StatFolderPath = "Statistics";
	// public static String historyPath =
	// "/home/islamhamdi/Dropbox/Stock Market Daily Data/historical prices/";
	// static String sentimentTwitterPath =
	// "/home/islamhamdi/Dropbox/Stock Market Daily Data/Sentiment-Twits/Twitter";
	// static String sentimentStockTwitPath =
	// "/home/islamhamdi/Dropbox/Stock Market Daily Data/Sentiment-Twits/StockTwits";

	// set 0 for twitter 1 for stockTwits
	static int files_to_run;

	static String startDate = "20-2-2014";

	static String combinedDataPath = "/home/mohamed/Dropbox/Stock Market Daily Data/Combined";
	static String twitterDataPath = "/home/mohamed/Dropbox/Stock Market Daily Data/Twitter";
	static String stockTwitDataPath = "/home/mohamed/Dropbox/Stock Market Daily Data/StockTwits";
	static String StatFolderPath = "/home/mohamed/Dropbox/Stock Market Daily Data/statistics";
	public static String historyPath = "/home/mohamed/Dropbox/Stock Market Daily Data/historical prices/";
	static String sentimentTwitterPath = "/home/mohamed/Dropbox/Stock Market Daily Data/Sentiment-Twits/Twitter";
	static String sentimentStockTwitPath = "/home/mohamed/Dropbox/Stock Market Daily Data/Sentiment-Twits/StockTwits";

	static int lag_var = 3;
	static int features_num = 16;

	static int start_of_norm_table = 45;

	static int price_start_col = features_num + lag_var + 2;
	static int volume_start_col = price_start_col + 2 * lag_var + 2;

	static int specialCell = 40;
	static int COLWIDTH = 15;
	public static String[] companies;

	static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

	public final static int THREAD_COUNT = 500;

	static boolean areEquals(String price_day, String day2)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date from = sdf.parse(price_day);
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyy");
		Date to = sdf2.parse(day2);
		return from.equals(to);
	}

	public static String seperated = "SeparatedData";
	public static String idMap = "@ user-id-map";
	public static String idSet = "@ tweet-id-set";
	public static String lineSeparator = "=8=7=6=5=";
	public static String companiesFolder = "Serialized Data";
	public static String data = "data";
	public static String status = "Tweet";

}
