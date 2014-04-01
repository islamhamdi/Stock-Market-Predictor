package Default;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global {

	static String twitterDataPath = "/home/islamhamdi/Desktop/TwitterStockData";
	static String stockTwitDataPath = "/home/islamhamdi/Desktop/StockTwitsData";
	static String twitterDataExpandedPath = "/home/islamhamdi/Desktop/TwitterStockDataExpanded";
	static String stockTwitDataExpandedPath = "/home/islamhamdi/Desktop/StockTwitsDataExpanded";
	static String combinedDataPath = "/home/islamhamdi/Desktop/CombinedData";
	static String StatFolderPath = "Statistics";
	static String historyPath = "/home/islamhamdi/Dropbox/Stock Market Daily Data/historical prices/";

	// set 0 for twitter 1 for stockTwits
	static int files_to_run;
	
	static String startDate = "20-2-2014";

	// static String twitterDataPath =
	// "/home/mohamed/Dropbox/Stock Market Daily Data/Twitter";
	// static String stockTwitDataPath =
	// "/home/mohamed/Dropbox/Stock Market Daily Data/StockTwits";
	// static String StatFolderPath =
	// "/home/mohamed/Dropbox/Stock Market Daily Data/statistics";
	// static String historyPath =
	// "/home/mohamed/Dropbox/Stock Market Daily Data/historical prices/";

	static int lag_var = 3;
	static int features_num = 17;

	static int start_of_norm_table = 70;

	static int price_start_col = features_num + lag_var + 1;
	static int volume_start_col = price_start_col + 2 * lag_var + 1;

	static int specialCell = 60;
	static int COLWIDTH = 15;
	public static String[] companies;

	static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

	public final static int THREAD_COUNT = 500;
	public final static int TWITTER_DATA = 0;
	public final static int STOCK_TWITS_DATA = 1;

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
