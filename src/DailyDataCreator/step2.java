package DailyDataCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import Default.Global;

import twitter4j.Status;

public class step2 {
	static SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyy");
	static Date st;

	public step2() throws Exception {
		st = sdf2.parse("20-2-2014");
		String finaloutputDir = Global.dataPaths[0];
		File dir = new File(finaloutputDir);
		if (!dir.exists())
			dir.mkdir();
		// String inputDir = "./Serialized Data/";
		String inputDir = "./out1/";
		dir = new File(inputDir);

		if (!dir.exists())
			throw new Exception("input folder doesnt exist");

		separateCompany(inputDir, finaloutputDir);

	}

	public static void main(String[] args) throws Exception {
		step2 s2 = new step2();
	}

	static void separateCompany(String inDir, String outDir) throws Exception {
		HashMap<String, ObjectOutputStream> writer = new HashMap<String, ObjectOutputStream>();
		Map<String, Integer> counter = new HashMap<String, Integer>();

		// input files
		File inputDir = new File(inDir);
		File[] input = inputDir.listFiles();

		for (int i = 0; i < input.length; i++) {
			ArrayList<ObjectOutputStream> arrlst = new ArrayList<ObjectOutputStream>();
			if (input[i].isFile()) {
				// create company dir
				String fullDir = outDir + "/" + input[i].getName();
				File dir = new File(fullDir);
				if (!dir.exists())
					dir.mkdir();

				System.out.println(input[i].getName());

				// read input file
				FileInputStream fr = new FileInputStream(
						input[i].getAbsolutePath());
				ObjectInputStream is = new ObjectInputStream(fr);

				File companydir = new File(fullDir);
				File[] allDays = companydir.listFiles();

				// open files
				for (int k = 0; k < allDays.length; k++) {
					ObjectOutputStream oos;
					if (allDays[k].isFile()) {
						oos = new ObjectOutputStream(new FileOutputStream(
								allDays[k].getAbsolutePath(), true));
						arrlst.add(oos);
						writer.put(allDays[k].getName(), oos);
						counter.put(allDays[k].getName(), 0);
					}
				}
				System.out.println("read file " + input[i].getName());
				Status msg = null;
				try {
					msg = (Status) is.readObject();
				} catch (Exception e) {
					System.out
							.println(input[i].getAbsolutePath() + " is Empty");
				}
				while (msg != null) {
					Date d = msg.getCreatedAt();

					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
					formatter.setTimeZone(TimeZone
							.getTimeZone("America/New_York"));

					String name = formatter.format(d);
					d = formatter.parse(name);
					// write if ok
					if (d.after(st)) {
						ObjectOutputStream oos;
						if (writer.get(name) != null) {
							oos = writer.get(name);
							counter.put(name, counter.get(name) + 1);
						} else {
							counter.put(name, 1);
							oos = new ObjectOutputStream(new FileOutputStream(
									fullDir + "/" + name, true));
							arrlst.add(oos);
							writer.put(name, oos);
						}
						oos.writeObject(msg);
					}
					try {
						msg = (Status) is.readObject();
					} catch (Exception e) {
						fr.close();
						is.close();
						break;
					}
				}
				for (int j = 0; j < arrlst.size(); j++) {
					arrlst.get(j).flush();
					arrlst.get(j).close();
				}

				arrlst.clear();
				writer.clear();
				for (Map.Entry<String, Integer> entry : counter.entrySet()) {
					System.out.println(entry.getKey() + " " + entry.getValue());
				}
				counter.clear();
			}
		}

	}
}