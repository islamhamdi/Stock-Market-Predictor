package GraphEdges;

import java.util.Date;

public class MentionedEdge extends GraphEdge {
	int userID;

	public MentionedEdge(int tweetID, int userId, int index, Date timeStamp) {
		this.tweetID = tweetID;
		this.userID = userId;
		this.index = index;
		this.timeStamp = timeStamp;
	}
}
