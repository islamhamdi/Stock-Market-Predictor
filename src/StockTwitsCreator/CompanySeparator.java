package StockTwitsCreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.HashMap;

import Default.Global;


public class CompanySeparator {
	private HashMap<String, Integer> companySet;

	public CompanySeparator() {
		companySet = new HashMap<String, Integer>();
		for (int i = 0; i < Global.companies.length; i++)
			companySet.put(Global.companies[i], i);
	}

	// ***** File Format *****
	// time(string format is like 2014-02-19T18:36:12Z )
	// username
	// #followers
	// #following(friendz)
	// #ideas ->#posts may be useful in the future (read and neglect)
	// #followingStoks -> may be sueful (read and neglect)
	// location ->may be empty /null
	// source url
	// message id
	// body(txt) may be more than one line
	// =8=7=6=5=

	public void Seperate(String directory) throws IOException,
			ClassNotFoundException {
		System.out.println("=> Start dividing tweets on Companies files");

		BufferedWriter[] data = new BufferedWriter[Global.companies.length];
		int[] counter = new int[Global.companies.length];

		File dir = new File(directory + "/" + Global.seperated);
		dir.mkdir();

		for (int i = 0; i < Global.companies.length; i++) {
			counter[i] = 0;
		}

		HashSet<Long> tweetSet = readHashSet(directory);

		File statusDir = new File(directory + "/input");
		File[] files = statusDir.listFiles();
		StringBuilder status = new StringBuilder(), message = new StringBuilder();
		int totalTweets = 0;
		int[] tweetsCnt = new int[Global.companies.length];

		for (int i = 0; i < files.length; i++) {
			System.out.println("Load File : " + files[i].getName());

			BufferedReader br = new BufferedReader(new FileReader(
					files[i].getAbsolutePath()));

			while (br.ready()) {
				totalTweets++;
				status.setLength(0);
				message.setLength(0);

				for (int lines = 0; lines < 8; lines++)
					status.append(br.readLine()).append('\n');

				long tweetID = Long.parseLong(br.readLine());
				status.append(tweetID).append('\n');

				// read message
				String s;
				while (!(s = br.readLine()).equals(Global.lineSeparator)) {
					message.append(s).append(' ');
					status.append(s).append('\n');
				}

				if (tweetSet.contains(tweetID))
					continue;

				tweetSet.add(tweetID);

				status.append(Global.lineSeparator + "\n");
				for (int k = 0; k < Global.companies.length; k++) {
					boolean found = message.indexOf(Global.companies[k]) >= 0;

					if (found) {
						tweetsCnt[k]++;

						if (counter[k] % 100000 == 0) {
							if (data[k] != null)
								data[k].close();

							data[k] = new BufferedWriter(new FileWriter(
									directory + "/" + Global.seperated + "/"
											+ Global.companies[k]));
						}

						data[k].write(status.toString());
						counter[k]++;
					}
				}

			}

			br.close();
		}

		for (int i = 0; i < data.length; i++)
			if (data[i] != null)
				data[i].close();

		writeHashSet(directory, tweetSet);

		System.out.println("=> Finish dividing tweets on companies files\n");
		for (int i = 0; i < tweetsCnt.length; i++)
			System.out.println("Total tweets of company " + Global.companies[i]
					+ " : " + tweetsCnt[i]);

		System.out.println("\nTotal tweets : " + totalTweets
				+ ", Unique tweets : " + tweetSet.size() + "\n");
	}

	private HashSet<Long> readHashSet(String directory) throws IOException,
			ClassNotFoundException {
		FileInputStream fin;
		try {
			fin = new FileInputStream(directory + Global.idSet);
		} catch (FileNotFoundException e) {
			writeHashSet(directory, new HashSet<Long>());
			fin = new FileInputStream(directory + Global.idSet);
		}

		ObjectInputStream ois = new ObjectInputStream(fin);
		HashSet<Long> set = (HashSet<Long>) ois.readObject();
		ois.close();

		return set;
	}

	private void writeHashSet(String directory, HashSet<Long> set)
			throws IOException {
		FileOutputStream fout = new FileOutputStream(directory + Global.idSet);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(set);

		oos.close();
		fout.close();
	}
}
