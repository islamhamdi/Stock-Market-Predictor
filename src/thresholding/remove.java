package thresholding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import twitter4j.Status;

import Default.Global;
import StockTwitsCreator.MyStatus;

public class remove {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		// 0 Twitter - 1 StockTwits - 2 Combined - 3 PosTwitter - 4 NegTwitter -
		// 5 PosStockTwit - 6 NegStockTwit

		File folder = new File(Global.dataPaths[4]);
		File[] folders = folder.listFiles();

		for (int i = 0; i < folders.length; i++) {
			folder = new File(folders[i].getAbsolutePath());
			File[] files = folder.listFiles();

			int k = 0;

			for (int c = 0; c < files.length; c++) {
				FileInputStream fr = new FileInputStream(files[c]);
				ObjectInputStream is = new ObjectInputStream(fr);

				Status s = null;
				try {
					s = (Status) is.readObject();
				} catch (Exception e) {
				}
				k = 0;
				while (s != null) {
					try {
						k++;
						s = (Status) is.readObject();
					} catch (Exception e) {
						break;
					}
				}

				fr.close();
				is.close();

				if (k < Global.min_tweets_perFile) {
					System.out.println(files[c].getAbsolutePath());
					files[c].delete();
				}

				// System.out.println(folders[i].getName() + " " + counter);
				// if (counter > 2000)
				// strings.add(folders[i].getName() + " " + counter);

			}
			// Collections.sort(strings);
			// for (int i = 0; i < strings.size(); i++) {
			// System.out.println(strings.get(i));
			// }
		}
	}
}
