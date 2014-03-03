package GraphEdges;

import java.util.Date;

public class AnnotatedEdge extends GraphEdge {
	String hashTag;

	public AnnotatedEdge(int tweetID, String hashTag, int index, Date timeStamp) {
		this.tweetID = tweetID;
		this.hashTag = hashTag;
		this.index = index;
		this.timeStamp = timeStamp;
	}
}
