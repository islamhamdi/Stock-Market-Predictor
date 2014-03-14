import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import twitter4j.internal.http.HttpClientFactory;

public class URLExpanderTest {
	// public static String expandUrl(String shortenedUrl) throws IOException {
	// URL url = new URL(shortenedUrl);
	// // open connection
	// HttpURLConnection httpURLConnection = (HttpURLConnection) url
	// .openConnection();
	// httpURLConnection.setRequestMethod("GET");
	// httpURLConnection.connect();
	// InputStream is = httpURLConnection.getInputStream();
	// System.out.println("Redirected URL: " + httpURLConnection.getURL());
	// is.close();
	// // stop following browser redirect
	// // httpURLConnection.setInstanceFollowRedirects(true);
	// // extract location header containing the actual destination URL
	// httpURLConnection.getResponseCode();
	//
	// // String expandedURL = httpURLConnection.getHeaderField("Location");
	// // String expandedURL = httpURLConnection.getURL().toString();
	// // httpURLConnection.disconnect();
	//
	// return "";
	// }
	public static String expandUrl(String shortenedUrl) throws IOException {
		URL url = new URL(shortenedUrl);
		// open connection
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		httpURLConnection.setRequestMethod("GET");

		httpURLConnection.getResponseCode();
		// extract location header containing the actual destination URL
		// httpURLConnection.disconnect();

		return httpURLConnection.getURL().toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(expandUrl("http://t.co/KadQKdvx4s"));
		// FileInputStream fin = new FileInputStream(
		// "/home/islamhamdi/Desktop/TwitterStockDataExpanded/$AAPL/01-03-2014");
		// ObjectInputStream ois = new ObjectInputStream(fin);
		//
		// while (true) {
		// try {
		// Status st = (MyStatus) ois.readObject();
		// System.out.println(st.getText());
		// URLEntity[] list = st.getURLEntities();
		// for (int i = 0; i < list.length; i++)
		// System.out.println(list[i].getText());
		// System.out.println();
		// } catch (Exception e) {
		// System.out.println("Done 1");
		// fin.close();
		// ois.close();
		// break;
		// }
		// }

	}
}