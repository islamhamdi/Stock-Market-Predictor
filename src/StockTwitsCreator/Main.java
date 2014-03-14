package StockTwits;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		GLOBAL.companies = new String[169];

		BufferedReader br = new BufferedReader(new FileReader(
				"TrackingCompanies.txt"));
		for (int i = 0; i < GLOBAL.companies.length; i++) {
			GLOBAL.companies[i] = br.readLine();
		}
		br.close();

		String directory = "./Data/";

		CompanySeparator cs = new CompanySeparator();
		cs.Seperate(directory);

		StatusWriter writer = new StatusWriter();
		writer.Write(directory);
	}
}
