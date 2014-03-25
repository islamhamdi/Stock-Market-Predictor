import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.NodeShape;

public class Constants {
	public static final int TWEET_NODE_SIZE = 20;
	public static final int HASHTAG_NODE_SIZE = 20;
	public static final int USER_NODE_SIZE = 20;
	public static final int URL_NODE_SIZE = 20;
	public static final int SIMILARITY_NODE_SIZE = 20;

	public static final ColorImpl TWEET_NODE_COLOR = new ColorImpl(255, 0, 0);// RED
	public static final ColorImpl HASHTAG_NODE_COLOR = new ColorImpl(255, 255,
			0);// YELLOW
	public static final ColorImpl USER_NODE_COLOR = new ColorImpl(0, 255, 0);// GREEN
	public static final ColorImpl URL_NODE_COLOR = new ColorImpl(0, 0, 255);// BLUE
	public static final ColorImpl SIMILARITY_NODE_COLOR = new ColorImpl(192,
			192, 192);// LIGHT Grey

	public static final NodeShape TWEET_NODE_SHAPE = NodeShape.DIAMOND;
	public static final NodeShape HASHTAG_NODE_SHAPE = NodeShape.SQUARE;
	public static final NodeShape USER_NODE_SHAPE = NodeShape.DISC;
	public static final NodeShape URL_NODE_SHAPE = NodeShape.TRIANGLE;
	public static final NodeShape SIMILARITY_NODE_SHAPE = NodeShape.IMAGE;

	public static final double MIN_JACCARD_THRESHOLD = .8;
	public static final double MAX_JACCARD_THRESHOLD = .9;
}
