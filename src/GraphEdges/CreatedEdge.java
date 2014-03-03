package GraphEdges;

import java.util.Date;

public class CreatedEdge extends GraphEdge {
	int userId;

	public CreatedEdge(int tweetId, int userId, int index, Date timestamp) {
		this.tweetID = tweetId;
		this.userId = userId;
		this.index = index;
		this.timeStamp = timestamp;
	}
}
