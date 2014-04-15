package DailyDataCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class YahooFinanceDownloader {
	public static void downloadFile(String symbol) throws IOException {
		URL website = new URL("http://ichart.finance.yahoo.com/table.csv?s=" + symbol
				+ "&a=04&b=18&c=2012&d=03&e=.csv%2bHistorical%2bPrices&f=sl1d1t1c1ohgv&g=d&ignore=.csv");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("YahooData/" + symbol + ".csv");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		
		System.out.println("Finish Company : " + symbol);
	}
	
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(new File("TrackingCompanies.txt"));
		
		while(sc.hasNext()){
			String Company = sc.nextLine();
			downloadFile(Company.substring(1));
		}
	
		sc.close();
	}
}
