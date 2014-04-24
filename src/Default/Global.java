package Default;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global {

	public static final int twitter = 0;
	public static final int stocktwits = 1;
	public static final int combined = 2;
	public static final int postwitter = 3;
	public static final int negtwitter = 4;
	public static final int posstocktwits = 5;
	public static final int negstocktwits = 6;

	public static int min_tweets_perFile = 3;

	public static String userName = "mohamed";

	public static int start_row_t1 = 55;
	public static int start_row_t2 = 75;

	public static int start_col_t1 = 1;
	public static int start_col_t2 = 12;

	public static int[] sheet_num = { 0, 1, 2, 3, 4, 5, 6 };

	public static String[] sheets = { "Twitter", "StockTwits", "Combined Data",
			"Positive-Twitter", "Negative-Twitter", "Positive-StockTwits",
			"Negative-StockTwits" };

	public static String[] dataPaths = {
			// Twitter Data Path
			"/home/" + userName + "/Dropbox/Stock Market Daily Data/Twitter",
			// StockTwits Data Path
			"/home/" + userName + "/Dropbox/Stock Market Daily Data/StockTwits",
			// Combined Data Path
			"/home/" + userName + "/Dropbox/Stock Market Daily Data/Combined",
			// Twitter Positive Data Path
			"/home/"
					+ userName
					+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/Twitter/positive",
			// Twitter Negative Data Path
			"/home/"
					+ userName
					+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/Twitter/negative",
			// StockTwits Positive Data Path
			"/home/"
					+ userName
					+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/StockTwits/positive",
			// StockTwits Negative Data Path
			"/home/"
					+ userName
					+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/StockTwits/negative", };

	// Statistics + history prices paths
	public static String StatFolderPath = "/home/" + userName
			+ "/Dropbox/Stock Market Daily Data/statistics";
	// static String StatFolderPath = "Statistics";
	public static String historyPath = "/home/" + userName
			+ "/Dropbox/Stock Market Daily Data/historical prices/";
	// public static String historyPath =
	// "/home/islamhamdi/Desktop/HistoryPath/";

	static String sentimentTwitterPath = "/home/" + userName
			+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/Twitter";
	static String sentimentStockTwitPath = "/home/" + userName
			+ "/Dropbox/Stock Market Daily Data/Sentiment-Twits/StockTwits";

	// set 0 for twitter 1 for stockTwits
	static int files_to_run;

	static String startDate = "20-2-2014";

	public static int lag_var = 3;
	static int features_num = 16;

	static int start_of_norm_table = 45;

	static int price_start_col = features_num + lag_var + 2;
	static int volume_start_col = price_start_col + 2 * lag_var + 2;

	static int specialCell = 40;
	static int COLWIDTH = 15;
	public static String[] companies;

	static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

	static String[] price_cols = new String[2 * lag_var + 1];
	static String[] volume_cols = new String[2 * lag_var + 1];

	public final static int THREAD_COUNT = 500;

	public static String seperated = "SeparatedData";
	public static String idMap = "@ user-id-map";
	public static String idSet = "@ tweet-id-set";
	public static String lineSeparator = "=8=7=6=5=";
	public static String companiesFolder = "Serialized Data";
	public static String data = "data";
	public static String status = "Tweet";

	static String convert(int a) {
		int k = 1;
		while (a >= k) {
			a -= k;
			k *= 26;
		}
		k /= 26;
		String s = "";

		while (k > 0) {
			s += ((char) ('A' + (a / k)));
			a %= k;
			k /= 26;
		}
		return s;
	}

}
