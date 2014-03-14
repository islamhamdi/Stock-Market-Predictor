package StockTwits;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import twitter4j.Status;

public class StatusWriter {

	public void Write(String directory) throws IOException, ClassNotFoundException {
		System.out.println("=> Start converting .txt files to status files\n");

		File dir = new File(directory + "/" + GLOBAL.companiesFolder);
		dir.mkdir();

		// map of (username , userID)
		HashMap<String, Long> map = readHashMap(directory);
		int tweetsCount = 0;
		for (int i = 0; i < GLOBAL.companies.length; i++) {
			System.out.println("=> Start Reading Company : " + GLOBAL.companies[i] + " files");

			File statusDir = new File(directory + "/" + GLOBAL.seperated + "/");
			File[] files = statusDir.listFiles();

			for (int j = 0; j < files.length; j++) {
				StringTokenizer st = new StringTokenizer(files[j].getName());

				if (st.nextToken().equals(GLOBAL.companies[i])) {
					System.out.println("Load file : " + files[j].getName());

					ArrayList<Status> list = createStatus(files[j], map);
					tweetsCount += list.size();

					// write status to file
					writeList(directory + "/" + GLOBAL.companiesFolder + "/" + GLOBAL.companies[i], list);
				}

			}
		}

		writeHashMap(directory, map);

		System.out.println("=> Finish Writing all files");
		System.out.println("Total Tweets : " + tweetsCount);
	}

	private void writeList(String directory, ArrayList<Status> list) throws IOException {
		FileOutputStream fout = new FileOutputStream(directory + " @ " + System.currentTimeMillis());
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		for (Status s : list) {
			oos.writeObject(s);
		}

		oos.close();
		fout.close();
	}

	private void writeHashMap(String directory, HashMap<String, Long> map) throws IOException {
		FileOutputStream fout = new FileOutputStream(directory + GLOBAL.idMap);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(map);

		oos.close();
		fout.close();
	}

	private HashMap<String, Long> readHashMap(String directory) throws IOException, ClassNotFoundException {
		FileInputStream fin;
		try {
			fin = new FileInputStream(directory + GLOBAL.idMap);
		} catch (FileNotFoundException e) {
			writeHashMap(directory, new HashMap<String, Long>());
			fin = new FileInputStream(directory + GLOBAL.idMap);
		}

		ObjectInputStream ois = new ObjectInputStream(fin);
		HashMap<String, Long> map = (HashMap<String, Long>) ois.readObject();
		ois.close();
		
		return map;
	}

	private ArrayList<Status> createStatus(File file, HashMap<String, Long> map) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
		ArrayList<Status> list = new ArrayList<Status>();

		while (br.ready()) {
			String date = br.readLine();
			String userName = br.readLine();
			int followersCnt = Integer.parseInt(br.readLine());
			int friendsCnt = Integer.parseInt(br.readLine());
			String ideas = br.readLine(); // neglect
			String followingStock = br.readLine(); // neglect
			String location = br.readLine();
			String sourceURL = br.readLine();
			long tweetID = Long.parseLong(br.readLine());
			String tweetText = "";

			// read message
			String s;
			while (!(s = br.readLine()).equals(GLOBAL.lineSeparator)) {
				tweetText += s;
			}

			Status status = new MyStatus(userName, followersCnt, friendsCnt, date, tweetID, tweetText, sourceURL,
					location, map);
			list.add(status);
		}
		
		br.close();
		return list;
	}

}
