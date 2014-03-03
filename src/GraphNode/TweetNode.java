package GraphNode;
import java.util.Date;

public class TweetNode extends GraphNode {
	String text;
	String company;

	public TweetNode(int tweetID, String text, String companyName,
			Date postTime, int index) {
		this.id = tweetID;
		this.text = text;
		this.company = companyName;
		this.timeStamp = postTime;
		this.index = index;
	}
}
