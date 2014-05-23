package SentimentAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import twitter4j.Status;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_TargetedSentimentParams;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DataSeparatorAlchemyAPI {

	private static String dataDir = "/home/islamhamdi/Dropbox/Stock Market Daily Data/";
	private static String outputDir = "/home/islamhamdi/Dropbox/Stock Market Daily Data/Sentiment-Twits/";
	private static String FileSetPath = "/home/islamhamdi/Dropbox/Stock Market Daily Data/Sentiment-Twits/FileSetAlchemy.txt";
	private static HashSet<String> fileSet;
	private static BufferedWriter fileSetWriter;
	private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	private static AlchemyAPI alchemyObj;

	private static int findSentiment(String line, String companyName)
			throws XPathExpressionException, IOException, SAXException,
			ParserConfigurationException {
		AlchemyAPI_TargetedSentimentParams sentimentParams = new AlchemyAPI_TargetedSentimentParams();
		sentimentParams.setShowSourceText(true);
		Document doc = alchemyObj.TextGetTargetedSentiment(line, companyName,
				sentimentParams);
		String sent = doc.getElementsByTagName("docSentiment").item(0)
				.getChildNodes().item(0).getNextSibling().getTextContent();
		// System.out.println(line + " \t\t " + sent);
		if (sent.equals("negative"))
			return 0;
		else
			return 2;
	}

	public static void main(String[] args) throws IOException {
		alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");

		fileSetWriter = new BufferedWriter(new FileWriter(FileSetPath, true));

		fileSet = readSet();

		System.out.println("Start StockTwits ...");
		run(dataDir + "StockTwits", outputDir + "StockTwits/");

		System.out.println("Start Twitter ...");
		run(dataDir + "Twitter", outputDir + "Twitter/");
	}

	private static HashSet<String> readSet() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(FileSetPath));
		String s;

		HashSet<String> set = new HashSet<String>();
		while ((s = br.readLine()) != null) {
			set.add(s);
		}

		br.close();

		return set;
	}

	private static void run(String inputPath, String outputPath)
			throws IOException {
		File file = new File(inputPath);

		File[] companyList = file.listFiles();

		for (File comp : companyList) {
			System.out.println("Read Company : " + comp.getName());

			File[] dataList = comp.listFiles();

			// create output file for this company
			File pos = new File(outputPath + "positive_alchemy/"
					+ comp.getName());
			pos.mkdir();

			File neg = new File(outputPath + "negative_alchemy/"
					+ comp.getName());
			neg.mkdir();

			// sort files on date
			Arrays.sort(dataList, new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {
					try {
						return dateFormat.parse(f1.getName()).compareTo(
								dateFormat.parse(f2.getName()));
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					}
				}
			});

			for (File f : dataList)
				if (!fileSet.contains(f.getAbsolutePath())) {
					System.out.println("Read File : " + f.getName());

					// open status file
					try {
						// open status file
						FileInputStream fin = new FileInputStream(
								f.getAbsolutePath());
						ObjectInputStream ois = new ObjectInputStream(fin);

						// create positive output file
						FileOutputStream foutPos = new FileOutputStream(
								pos.getAbsolutePath() + "/" + f.getName());
						ObjectOutputStream oosPos = new ObjectOutputStream(
								foutPos);

						// create negative output file
						FileOutputStream foutNeg = new FileOutputStream(
								neg.getAbsolutePath() + "/" + f.getName());
						ObjectOutputStream oosNeg = new ObjectOutputStream(
								foutNeg);

						// read status file
						while (true) {
							try {
								Status s = (Status) ois.readObject();
								int analysis = findSentiment(s.getText(), comp
										.getName().substring(1));
								if (analysis == 0 || analysis == 1) {
									// negative status
									oosNeg.writeObject(s);
								} else if (analysis == 2 || analysis == 3
										|| analysis == 4) {
									// positive status
									oosPos.writeObject(s);
								}

							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
						}

						ois.close();
						fin.close();

						oosPos.close();
						foutPos.close();

						oosNeg.close();
						foutNeg.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					fileSetWriter.write(f.getAbsolutePath() + "\n");
					fileSetWriter.flush();
				}

		}
	}
}
