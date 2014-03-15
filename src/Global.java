import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global {

	static String path1 = "/home/islamhamdi/Desktop/TwitterStockData";
	static String path2 = "/home/islamhamdi/Desktop/StockTwitsData";
	static String StatFolderPath = "Statistics";
	static String historyPath = "/home/islamhamdi/Dropbox/Stock Market Daily Data/historical prices/";

	// set 0 for twitter 1 for stockTwits
	static int files_to_run;

	// static String path1 = "S:\\Dropbox\\Stock Market Daily Data\\Twitter";
	// static String path2 = "S:\\Dropbox\\Stock Market Daily Data\\StockTwits";

	// static String StatFolderPath =
	// "S:\\Dropbox\\Stock Market Daily Data\\statistics";
	// static String historyPath =
	// "S:\\Dropbox\\Stock Market Daily Data\\historical prices\\";

	// static String path1 =
	// "/media/CSED/Dropbox/Stock Market Daily Data/Twitter";
	// static String path2 =
	// "/media/CSED/Dropbox/Stock Market Daily Data/StockTwits";

	// static String StatFolderPath =
	// "/media/CSED/Dropbox/Stock Market Daily Data/statistics";
	// static String historyPath =
	// "/media/CSED/Dropbox/Stock Market Daily Data/historical prices/";

	static int lag_var = 3;
	static int volume_start_col = 50;
	static int price_start_col = 30;
	static int specialCell = 60;
	static int COLWIDTH = 12;
	public static String[] companies;

	public final static int THREAD_COUNT = 50;

	static boolean areEquals(String price_day, String day2)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date from = sdf.parse(price_day);
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyy");
		Date to = sdf2.parse(day2);
		return from.equals(to);
	}

}
