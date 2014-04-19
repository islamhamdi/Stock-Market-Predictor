package Average;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;

import StockTwitsCreator.MyStatus;

import twitter4j.Status;

public class TweetCounter {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		ArrayList<String> strings = new ArrayList<String>();
		File folder = new File(
				"/home/mohamed/Dropbox/Stock Market Daily Data/StockTwits");
		File[] folders = folder.listFiles();

		for (int i = 0; i < folders.length; i++) {
			folder = new File(folders[i].getAbsolutePath());
			File[] files = folder.listFiles();

			int counter = 0;
			int k = 0;
			HashSet<Long> hs = new HashSet<Long>();
			for (int c = 0; c < files.length; c++) {
				FileInputStream fr = new FileInputStream(files[c]);
				ObjectInputStream is = new ObjectInputStream(fr);

				MyStatus s;
				try {
					s =  (MyStatus) is.readObject();
				} catch (Exception e) {
					System.out.println(files[c].getAbsolutePath());
					continue;
				}
				k = 0;
				while (s != null) {
					hs.add(s.getId());
					k++;
					try {
						s = (MyStatus) is.readObject();
					} catch (Exception e) {
						break;
					}
				}

				counter += k;

				fr.close();
				is.close();
			}

			if (counter > 2000)
				strings.add(folders[i].getName());

		}
		Collections.sort(strings);
		for (int i = 0; i < strings.size(); i++) {
			System.out.println(strings.get(i));
		}
	}
}
