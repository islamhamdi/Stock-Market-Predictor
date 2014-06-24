package Average;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import java_cup.runtime.virtual_parse_stack;

import Default.Global;

import twitter4j.Status;

public class TweetCounter {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		int t[] = new int[7];
		int v[] = new int[24];
		// ArrayList<String> strings = new ArrayList<String>();
		File folder = new File(Global.dataPaths[0]);
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

				Status s;
				try {
					s = (Status) is.readObject();
					int kk = s.getCreatedAt().getDay();
					t[kk]++;
					v[s.getCreatedAt().getHours()]++;
				} catch (Exception e) {
					continue;
				}
				k = 0;
				while (s != null) {
					k++;
					try {
						s = (Status) is.readObject();
						int kk = s.getCreatedAt().getDay();
						t[kk]++;
						v[s.getCreatedAt().getHours()]++;
					} catch (Exception e) {
						break;
					}
				}

				counter += k;
				// is.reset();
				fr.close();
				is.close();
			}
			// System.out.println(folders[i].getName());
			System.out.println(Arrays.toString(t));
			// System.out.println(counter);
			// if (counter > 2000)
			// strings.add(folders[i].getName() + " " + counter);

		}
		System.out.println("last");
		System.out.println(Arrays.toString(t));
		System.out.println(Arrays.toString(v));
		// Collections.sort(strings);
		// for (int i = 0; i < strings.size(); i++) {
		// System.out.println(strings.get(i));
		// }
	}
}
