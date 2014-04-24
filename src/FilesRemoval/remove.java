package FilesRemoval;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import twitter4j.Status;
import Default.Global;

public class remove {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		// 0 Twitter - 1 StockTwits - 2 Combined - 3 PosTwitter - 4 NegTwitter -
		// 5 PosStockTwit - 6 NegStockTwit
		for (int v = 0; v <= 6; v++) {
			File folder = new File(Global.dataPaths[v]);
			File[] folders = folder.listFiles();
			// Arrays.sort(folders);

			for (int i = 0; i < folders.length; i++) {
				folder = new File(folders[i].getAbsolutePath());
				File[] files = folder.listFiles();

				int counter = 0;
				int k = 0;

				for (int c = 0; c < files.length; c++) {
					FileInputStream fr = new FileInputStream(files[c]);
					ObjectInputStream is = new ObjectInputStream(fr);

					Status s = null;
					try {
						s = (Status) is.readObject();
					} catch (Exception e) {
						// continue;
					}
					k = 0;
					while (s != null) {
						k++;
						try {
							s = (Status) is.readObject();
						} catch (Exception e) {
							break;
						}
					}

					counter += k;
					fr.close();
					is.close();

					if (k < Global.min_tweets_perFile) {
						System.out.println(files[c].getAbsolutePath());
						files[c].delete();
					}

				}

//				System.out.println(folders[i].getName() + " " + counter);
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
