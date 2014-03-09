import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global {
	static String path = "/home/islamhamdi/Desktop/TwitterStockData";
	static String StatFolderPath = "Statistics";
	static String historyPath = "/home/islamhamdi/Dropbox/Stock Market Daily Data/historical prices/";
	static int lag_var = 7;
	static int volume_start_col = 50;
	static int price_start_col = 30;
	static int specialCell = 60;
	static int COLWIDTH = 12;

	static boolean areEquals(String price_day, String day2)
			throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date from = sdf.parse(price_day);
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyy");
		Date to = sdf2.parse(day2);
		return from.equals(to);
	}

}
