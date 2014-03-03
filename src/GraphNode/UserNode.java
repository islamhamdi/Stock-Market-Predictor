package GraphNode;
import java.util.Date;

import twitter4j.Location;

public class UserNode extends GraphNode {
	String username;
	int followersCount;
	int friendsCount;
	Location location;

	public UserNode(String username, int followersCount, int friendsCount,
			Location loc, int id, int index, Date firstTweetDate) {
		this.username = username;
		this.followersCount = followersCount;
		this.friendsCount = friendsCount;
		this.location = loc;
		this.id = id;
		this.index = index;
		this.timeStamp = firstTweetDate;
	}
}
