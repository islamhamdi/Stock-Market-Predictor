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

import Default.Global;
import StockTwitsCreator.MyStatus;

import twitter4j.Status;

public class TweetCounter {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

//		ArrayList<String> strings = new ArrayList<String>();
		File folder = new File(Global.dataPaths[1]);
		File[] folders = folder.listFiles();
		Arrays.sort(folders);

		for (int i = 0; i < folders.length; i++) {
			folder = new File(folders[i].getAbsolutePath());
			File[] files = folder.listFiles();

			int counter = 0;
			int k = 0;
			for (int c = 0; c < files.length; c++) {
				FileInputStream fr = new FileInputStream(files[c]);
				ObjectInputStream is = new ObjectInputStream(fr);

				MyStatus s;
				try {
					s = (MyStatus) is.readObject();
				} catch (Exception e) {
					continue;
				}
				k = 0;
				while (s != null) {
					k++;
					try {
						s = (MyStatus) is.readObject();
					} catch (Exception e) {
						break;
					}
				}

				counter += k;
//				is.reset();
				fr.close();
				is.close();
			}
//			System.out.println(folders[i].getName());
			System.out.println( counter);
			// if (counter > 2000)
//			strings.add(folders[i].getName() + " " + counter);

		}
//		Collections.sort(strings);
//		for (int i = 0; i < strings.size(); i++) {
//			System.out.println(strings.get(i));
//		}
	}
}
