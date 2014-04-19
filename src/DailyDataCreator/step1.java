package DailyDataCreator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import twitter4j.Status;

public class step1 {
	static String companies[];

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Scanner sc = new Scanner(new File("TrackingCompanies.txt"));
		companies = new String[169];
		for (int i = 0; i < 169; i++) {
			companies[i] = sc.next();
		}

		String out = "./out1/";

		File dir = new File(out);

		if (!dir.exists())			dir.mkdir();
		SeperateOnCompanies("./input/", out);
	}

	public static void SeperateOnCompanies(String Indir, String Outdir)
			throws IOException, ClassNotFoundException {
		int n = companies.length;
		System.out.println("=> Start dividing tweets on Companies files");
		// writer
		FileOutputStream[] FIS = new FileOutputStream[n];
		ObjectOutputStream[] OOS = new ObjectOutputStream[n];
		for (int i = 0; i < OOS.length; i++) {
			FIS[i] = new FileOutputStream(Outdir + companies[i]);
			OOS[i] = new ObjectOutputStream(FIS[i]);
		}

		int[] counter = new int[n];
		HashSet<Long> set = new HashSet<Long>();
		int cnt = 0;
		// input files
		File inputDir = new File(Indir);
		File[] files = inputDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			set.clear();
			cnt = 0;
			if (files[i].isFile()) {
				System.out.println("Load File : " + files[i].getName());

				FileInputStream fr = new FileInputStream(
						files[i].getAbsolutePath());
				ObjectInputStream is = new ObjectInputStream(fr);
				Status msg = (Status) is.readObject();

				while (msg != null) {
					cnt++;
					long id = msg.getId();
					String body = msg.getText();
					if (!set.contains(id)) {
						set.add(id);
						for (int k = 0; k < n; k++) {
							if (body.indexOf(companies[k]) >= 0) {
								OOS[k].writeObject(msg);
								counter[k]++;
							}
						}
					}

					try {
						msg = (Status) is.readObject();
					} catch (Exception e) {
						fr.close();
						is.close();
						break;
					}
				}
				System.out.println("total = " + cnt + " to " + set.size());
			}

		}
		for (int i = 0; i < n; i++) {
			OOS[i].flush();
			FIS[i].flush();
			OOS[i].close();
			FIS[i].close();
		}

		for (int i = 0; i < n; i++) {
			System.out.println(companies[i] + "=" + counter[i]);
		}
		System.out.println("=> Finish dividing tweets on companies files\n");
	}
}