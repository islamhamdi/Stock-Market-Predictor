package Default;

import java.text.SimpleDateFormat;

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

	public static String globalPath = "/home/" + userName
			+ "/Dropbox/Stock Market Daily Data";

	public static String[] dataPaths = {
			// Twitter Data Path
			globalPath + "/Twitter",
			// StockTwits Data Path
			globalPath + "/StockTwits",
			// Combined Data Path
			globalPath + "/Combined",
			// Twitter Positive Data Path
			globalPath + "/Sentiment-Twits/Twitter/positive",
			// Twitter Negative Data Path
			globalPath + "/Sentiment-Twits/Twitter/negative",
			// StockTwits Positive Data Path
			globalPath + "/Sentiment-Twits/StockTwits/positive",
			// StockTwits Negative Data Path
			globalPath + "/Sentiment-Twits/StockTwits/negative", };

	// Statistics + history prices paths
	public static String StatFolderPath = globalPath + "/statistics/";
	// static String StatFolderPath = "Statistics";
	public static String historyPath1 = globalPath + "/historical prices1/";

	public static String historyPath2 = globalPath + "/historical prices2/";

	static String sentimentTwitterPath = globalPath
			+ "/Sentiment-Twits/Twitter";
	static String sentimentStockTwitPath = globalPath
			+ "/Sentiment-Twits/StockTwits";

	// set 0 for twitter 1 for stockTwits
	static int files_to_run;

	static String startDate = "20-2-2014";

	public static int lag_var = 3;
	static int features_num = 16;

	static int start_of_norm_table = 45;

	public static int price_start_col = features_num + lag_var + 2;
	public static int volume_start_col = price_start_col + 2 * lag_var + 2;

	public static int specialCell = 40;
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

	// Modes

	public static String[] mode_names = { "normal_average", "Market_Cap_low",
			"Market_Cap_Medium", "Market_Cap_high", "Peg_Ratio_low",
			"Peg_Ratio_medium", "Peg_Ratio_high", "Enterprise_Value_low",
			"Enterprise_Value_medium", "Enterprise_Value_high",
			"Gross_Profit_low", "Gross_Profit_medium", "Gross_Profit_high",
			"Current_Ration_low", "Current_Ration_medium",
			"Current_Ration_high", "Beta_low", "Beta_medium", "Beta_high",
			"Float_low", "Float_medium", "Float_high" };

	public static int normal_average = 0;

	public static int Market_Cap_low = 1;
	public static int Market_Cap_Medium = 2;
	public static int Market_Cap_high = 3;

	public static int Peg_Ratio_low = 4;
	public static int Peg_Ratio_medium = 5;
	public static int Peg_Ratio_high = 6;

	public static int Enterprise_Value_low = 7;
	public static int Enterprise_Value_medium = 8;
	public static int Enterprise_Value_high = 9;

	public static int Gross_Profit_low = 10;
	public static int Gross_Profit_medium = 11;
	public static int Gross_Profit_high = 12;

	public static int Current_Ration_low = 13;
	public static int Current_Ration_medium = 14;
	public static int Current_Ration_high = 15;

	public static int Beta_low = 16;
	public static int Beta_medium = 17;
	public static int Beta_high = 18;

	public static int Float_low = 19;
	public static int Float_medium = 20;
	public static int Float_high = 21;

	public static String kEYMAP = "KeyStatisticsMap";

}
