package SentimentAnalysis;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

import twitter4j.Status;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class DataSeparator {

	private static String dataDir = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/";
	private static String outputDir = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Sentiment-Twits/";
	private static String FileSetPath = "/media/MyData/DropBox/Dropbox/Stock Market Daily Data/Sentiment-Twits/FileSet.txt";
	private static HashSet<String> fileSet;
	private static BufferedWriter fileSetWriter;
	private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	private static StanfordCoreNLP pipeline;

	private static int findSentiment(String line) {
		int mainSentiment = 0;
		if (line != null && line.length() > 0) {
			int longest = 0;
			Annotation annotation = pipeline.process(line);
			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
				String partText = sentence.toString();
				if (partText.length() > longest) {
					mainSentiment = sentiment;
					longest = partText.length();
				}

			}
		}

		if (mainSentiment > 4 || mainSentiment < 0) {
			return -1;
		}

		return mainSentiment;
	}

	public static void main(String[] args) throws IOException {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);

		fileSetWriter = new BufferedWriter(new FileWriter(FileSetPath, true));

		fileSet = readSet();

		System.out.println("Start Twitter ...");
		run(dataDir + "Twitter", outputDir + "Twitter/");

		System.out.println("Start StockTwits ...");
		run(dataDir + "StockTwits", outputDir + "StockTwits/");
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

	private static void run(String inputPath, String outputPath) throws IOException {
		File file = new File(inputPath);

		File[] companyList = file.listFiles();

		for (File comp : companyList) {
			System.out.println("Read Company : " + comp.getName());

			File[] dataList = comp.listFiles();

			// create output file for this company
			File pos = new File(outputPath + "positive/" + comp.getName());
			pos.mkdir();

			File neg = new File(outputPath + "negative/" + comp.getName());
			neg.mkdir();

			// sort files on date
			Arrays.sort(dataList, new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {
					try {
						return dateFormat.parse(f1.getName()).compareTo(dateFormat.parse(f2.getName()));
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
						FileInputStream fin = new FileInputStream(f.getAbsolutePath());
						ObjectInputStream ois = new ObjectInputStream(fin);

						// create positive output file
						FileOutputStream foutPos = new FileOutputStream(pos.getAbsolutePath() + "/" + f.getName());
						ObjectOutputStream oosPos = new ObjectOutputStream(foutPos);

						// create negative output file
						FileOutputStream foutNeg = new FileOutputStream(neg.getAbsolutePath() + "/" + f.getName());
						ObjectOutputStream oosNeg = new ObjectOutputStream(foutNeg);

						// read status file
						while (true) {
							try {
								Status s = (Status) ois.readObject();
								int analysis = findSentiment(s.getText());
								if (analysis == 0 || analysis == 1) {
									// negative status
									oosNeg.writeObject(s);
								} else if (analysis == 2 || analysis == 3 || analysis == 4) {
									// negative status
									oosPos.writeObject(s);
								}

							} catch (Exception e) {
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
