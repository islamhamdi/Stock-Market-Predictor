package SentimentAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_TargetedSentimentParams;

class SentimentTest {
	public static void main(String[] args) throws IOException, SAXException,
			ParserConfigurationException, XPathExpressionException {
		// Create an AlchemyAPI object.
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");

		String tweetStatus = "@silverjet2: Gainers in AH:  $ARUN +6.7% $FUEL +5.2% $JNPR +2.5% $QLIK +2.5% $PCYC +2.3% $CONN +2.3% $PCLN +1.6% $HPQ +1.4%";

		// Extract sentiment for a text string.
		Document doc = alchemyObj.TextGetTextSentiment(tweetStatus);
		System.out.println(getSentimentFromDocument(doc));

		// Extract Targeted Sentiment from text
		AlchemyAPI_TargetedSentimentParams sentimentParams = new AlchemyAPI_TargetedSentimentParams();
		sentimentParams.setShowSourceText(true);
		doc = alchemyObj.TextGetTargetedSentiment(tweetStatus, "QLIK",
				sentimentParams);
		System.out.println(getStringFromDocument(doc));
		System.out.println(getSentimentFromDocument(doc));

		System.out.println("==============");
		doc = alchemyObj.TextGetTargetedSentiment(tweetStatus, "QLIK",
				sentimentParams);
		System.out.println(getStringFromDocument(doc));
		System.out.println(getSentimentFromDocument(doc));

	}

	private static String getSentimentFromDocument(Document doc) {
		return doc.getElementsByTagName("docSentiment").item(0).getChildNodes()
				.item(0).getNextSibling().getTextContent();
	}

	// utility method
	private static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
