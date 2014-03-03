package GraphEdges;

import java.util.Date;

public class RetweetedEdge extends GraphEdge {
	int retweetID;

	public RetweetedEdge(int RTId, int tweetId, int index, Date timeStamp) {
		this.index = index;
		this.tweetID = tweetId;
		this.retweetID = RTId;
		this.timeStamp = timeStamp;
	}
}
