package GraphNode;
import java.util.Date;

public class URLNode extends GraphNode {
	String url;
	String expandedUrl;

	public URLNode(String url, String expanded, int id, int index,
			Date firstDate) {
		this.url = url;
		this.expandedUrl = expanded;
		this.id = id;
		this.index = index;
		this.timeStamp = firstDate;
	}
}
