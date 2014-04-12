package DataHandling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;

import twitter4j.Status;

public class DataCombiner {
	private static String TwitterDir = "./Twitter Data/";
	private static String StockTwitsDir = "./Stocktwits Data/";
	private static String CombinedDir = "./Combined Data/";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static Date startDate;

	private static boolean areAfter(String day1) {
		try {
			Date from = dateFormat.parse(day1);
			return from.after(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		startDate = dateFormat.parse("25-12-2014");
		run();
	}

	private static void run() throws IOException {
		HashSet<String> combinedCompaniesNames = new HashSet<String>();

		// combine companies names
		combinedCompaniesNames.addAll(getFileList(TwitterDir));
		combinedCompaniesNames.addAll(getFileList(StockTwitsDir));

		// combine files
		for (String companyName : combinedCompaniesNames) {
			System.out.println("Read Company : " + companyName);

			HashSet<String> combinedFilesNames = new HashSet<String>();

			// read company twiter files
			if (isExists(TwitterDir + companyName)) {
				HashSet<String> twitterFiles = getFileList(TwitterDir + companyName);
				combinedFilesNames.addAll(twitterFiles);
			}

			// read company stocktwits files
			if (isExists(StockTwitsDir + companyName)) {
				HashSet<String> stocktwtisFiles = getFileList(StockTwitsDir + companyName);
				combinedFilesNames.addAll(stocktwtisFiles);
			}

			File newDir = new File(CombinedDir + companyName);
			newDir.mkdir();

			for (String fileName : combinedFilesNames) {
				System.out.println("read file : " + fileName);

				ArrayList<Status> twitterList = getStatusList(TwitterDir + companyName + "/" + fileName);
				ArrayList<Status> stocktwitsList = getStatusList(StockTwitsDir + companyName + "/" + fileName);

				WriteLists(twitterList, stocktwitsList, CombinedDir + companyName + "/" + fileName);
			}
		}
	}

	private static void WriteLists(ArrayList<Status> twitter, ArrayList<Status> stocktwits, String dir)
			throws IOException {
		FileOutputStream fout = new FileOutputStream(dir);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		for (Status s : twitter)
			oos.writeObject(s);

		oos.flush();
		oos.reset();

		for (Status s : stocktwits)
			oos.writeObject(s);

		oos.close();
		fout.close();

	}

	private static ArrayList<Status> getStatusList(String dir) throws IOException {
		ArrayList<Status> list = new ArrayList<Status>();
		if (isExists(dir)) {
			FileInputStream fiut = new FileInputStream(dir);
			ObjectInputStream ois = new ObjectInputStream(fiut);

			while (true) {
				try {
					Status s = (Status) ois.readObject();
					list.add(s);
				} catch (Exception e) {
					break;
				}
			}

			ois.close();
			fiut.close();
		}
		return list;
	}

	private static HashSet<String> getFileList(String dir) {
		HashSet<String> set = new HashSet<String>();
		for (File f : new File(dir).listFiles())
			if (f.isDirectory() || areAfter(f.getName()))
				set.add(f.getName());

		return set;
	}

	private static boolean isExists(String dir) {
		return new File(dir).exists();
	}
}
