package GraphEdges;

import java.util.Date;

public class CitedEdge extends GraphEdge {
	String url;

	public CitedEdge(int tweetId, String url, int index, Date timeStamp) {
		this.tweetID = tweetId;
		this.url = url;
		this.index = index;
		this.timeStamp = timeStamp;
	}
}
