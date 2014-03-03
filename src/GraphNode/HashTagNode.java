package GraphNode;
import java.util.Date;

public class HashTagNode extends GraphNode {
	String hashTag;

	public HashTagNode(String hashTag, int id, Date firstRelease, int index) {
		this.hashTag = hashTag;
		this.timeStamp = firstRelease;
		this.index = index;
		this.id = id;
	}
}
