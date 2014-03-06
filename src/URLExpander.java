import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class URLExpander {
	public static String expandUrl(String shortenedUrl) throws IOException {
		URL url = new URL(shortenedUrl);
		// open connection
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection(Proxy.NO_PROXY);

		// stop following browser redirect
		httpURLConnection.setInstanceFollowRedirects(false);

		// extract location header containing the actual destination URL
		String expandedURL = httpURLConnection.getHeaderField("Location");
		httpURLConnection.disconnect();

		return expandedURL;
	}

	// public static String expandUrl(String shortenedUrl) throws IOException {
	// URLConnection conn = null;
	// URL inputURL = new URL(shortenedUrl);
	// conn = inputURL.openConnection();
	// conn.getHeaderFields();
	// return conn.getURL().toString();
	// }
}