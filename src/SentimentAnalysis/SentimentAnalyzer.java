package SentimentAnalysis;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import twitter4j.Status;

public class SentimentAnalyzer {
	private static StanfordCoreNLP pipeline;

	private static byte[] bytes = new byte[200000000];

	// res[0] negative, res[1] neutral, res[2] positive, res[3] positive - negative
	public static int[] start(File f) throws IOException {
		System.out.println(f.getName());
		FileInputStream fis;
		DataInputStream dis = new DataInputStream(fis = new FileInputStream(f));
		dis.read(bytes);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

		int[] arr = new int[4];
		try {
			while (true) {
				Status status = (Status) ois.readObject();
				int analysis = findSentiment(status.getText());
				if (analysis == 0 || analysis == 1)
					arr[0]++;
				else if (analysis == 2)
					arr[1]++;
				else if (analysis == 3 || analysis == 4)
					arr[2]++;
			}
		} catch (Exception e) {

		}

		arr[3] = arr[2] - arr[0];

		fis.close();
		dis.close();

		return arr;
	}

	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

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
		int[] arr = start(new File("tweets180 @1395594263148"));
		System.out.println(Arrays.toString(arr));
	}
}