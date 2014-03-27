package SentimentAnalysis;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {

	public TweetWithSentiment findSentiment(String line) {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
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
			return null;
		}
		TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(line, toCss(mainSentiment));
		return tweetWithSentiment;

	}

	private String toCss(int sentiment) {
		switch (sentiment) {
		case 0:
			return "very bad";
		case 1:
			return "bad";
		case 2:
			return "neutral";
		case 3:
			return "good";
		case 4:
			return "very good";
		default:
			return "";
		}
	}

	public static void main(String[] args) throws IOException {
		SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
		BufferedReader br = new BufferedReader(new FileReader("hanafy4.txt"));

		BufferedWriter bw = new BufferedWriter(new FileWriter("analysis.txt"));
		String s;
		while ((s = br.readLine()) != null) {
			for (int i = 0; i < 8; i++)
				br.readLine();																																																																			

			String text = "";
			while (!(s = br.readLine()).equals("=8=7=6=5="))
				text += s;

			TweetWithSentiment tweetWithSentiment = sentimentAnalyzer.findSentiment(text);
			bw.write(tweetWithSentiment.toString());
			bw.newLine();
		}
		
		bw.close();
		br.close();
	}

	private class TweetWithSentiment {
		String ll, ss;

		public TweetWithSentiment(String line, String css) {
			ll = line;
			ss = css;
		}

		public String toString() {
			return ll + "\n" + ss + "\n";
		}

	}
}