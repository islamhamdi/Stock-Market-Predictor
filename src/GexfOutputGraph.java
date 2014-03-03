import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class GexfOutputGraph {

	static class NodeIdentifier {
		Node node;
		String nodeIndex;
		String text;

		public NodeIdentifier(Node n, String id) {
			node = n;
			nodeIndex = id;
		}

		public void setText(String s) {
			text = s;
		}
	}

	private Parser streamer;
	public static Gexf gexf;
	public static Calendar date;
	public static Graph graph;
	public static AttributeList attrList;
	private TreeMap<Long, NodeIdentifier> nodeMap;
	private TreeMap<String, NodeIdentifier> hashtagsMap;
	private TreeMap<String, NodeIdentifier> urlsMap;
	private TreeMap<Long, NodeIdentifier> usersMap;
	private ArrayList<NodeIdentifier> nodesList;
	private int nodeCounter, edgeCounter, totalFollowersCounter,
			totalFriendsCounter;
	private FileOutputStream fout;
	private ObjectOutputStream oos;
	private ActivityFeatures features;

	private void generateGexfFile() {
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(
				"/home/islamhamdi/Desktop/TwitterStockData/twitter_graph.gexf");
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleUsers(Status curTweet, Node tweetNode) {
		// Creator user
		Long creatorUserID = curTweet.getUser().getId();
		NodeIdentifier userNID;

		if (usersMap.containsKey(creatorUserID)) {
			userNID = usersMap.get(creatorUserID);
		} else {
			// new user registration
			userNID = new NodeIdentifier(graph.createNode("" + nodeCounter), ""
					+ (nodeCounter++));
			userNID.node.setLabel("@").setSize(Constants.USER_NODE_SIZE)
					.setColor(Constants.USER_NODE_COLOR).getShapeEntity()
					.setNodeShape(Constants.USER_NODE_SHAPE);
			userNID.setText(curTweet.getUser().getScreenName());
			usersMap.put(creatorUserID, userNID);

			// increment number of diff users that have re-tweeted
			if (curTweet.isRetweet())
				features.incRTU();

			features.incUID();
			totalFollowersCounter += curTweet.getUser().getFollowersCount();
			totalFriendsCounter += curTweet.getUser().getFriendsCount();
		}

		tweetNode.connectTo("" + (edgeCounter++), "Creates", userNID.node);

		// Mentioned users
		UserMentionEntity[] usersList = curTweet.getUserMentionEntities();
		if (usersList != null && usersList.length > 0) {
			// either start @ 0 and add a check for retweet user mentions
			// or start with index i = 1
			for (int i = 0; i < usersList.length; i++) {
				Long curUserID = usersList[i].getId();
				NodeIdentifier curUserNID;

				if (usersMap.containsKey(curUserID)) {
					curUserNID = usersMap.get(curUserID);
				} else {
					// new user registration
					curUserNID = new NodeIdentifier(graph.createNode(""
							+ nodeCounter), "" + (nodeCounter++));
					curUserNID.node.setLabel("@")
							.setSize(Constants.USER_NODE_SIZE)
							.setColor(Constants.USER_NODE_COLOR)
							.getShapeEntity()
							.setNodeShape(Constants.USER_NODE_SHAPE);
					curUserNID.setText(usersList[i].getScreenName());
					usersMap.put(curUserID, curUserNID);
				}

				// a reply to a user mentioning the original user
				if (!tweetNode.hasEdgeTo(curUserNID.nodeIndex))
					tweetNode.connectTo("" + (edgeCounter++), "Mentioned",
							curUserNID.node);
			}

			// tweets that mention any user
			features.incTUSM();
		}
	}

	private void handleURLs(Status curTweet, Node tweetNode) throws IOException {
		URLEntity[] urls = curTweet.getURLEntities();
		MediaEntity[] mediaUrls = curTweet.getMediaEntities();
		String[] list = new String[urls.length + mediaUrls.length];
		int index = 0;

		for (int i = 0; i < urls.length; i++)
			list[index++] = urls[i].getText();
		for (int i = 0; i < mediaUrls.length; i++)
			list[index++] = mediaUrls[i].getText();

		if (list != null && list.length > 0) {
			for (int i = 0; i < list.length; i++) {

				// URL expansion, from t.co --> bit.ly --> original one
				// String curUrlText = URLExpander.expandUrl(list[i]);
				String curUrlText = list[i];
				NodeIdentifier urlNID;

				if (urlsMap.containsKey(curUrlText)) {
					urlNID = urlsMap.get(curUrlText);
				} else {
					// create new url node
					urlNID = new NodeIdentifier(graph.createNode(""
							+ nodeCounter), "" + (nodeCounter++));
					urlNID.node.setLabel("*").setSize(Constants.URL_NODE_SIZE)
							.setColor(Constants.URL_NODE_COLOR)
							.getShapeEntity()
							.setNodeShape(Constants.URL_NODE_SHAPE);
					urlNID.setText(curUrlText);
					urlsMap.put(curUrlText, urlNID);
				}

				// repeated URL in the tweet
				if (!tweetNode.hasEdgeTo(urlNID.nodeIndex)) {
					tweetNode.connectTo("" + (edgeCounter++), "Cited",
							urlNID.node);
				}
			}

			// number of tweets with URLs
			features.incTURL();
		}
	}

	private void handleHashTags(Status curTweet, Node tweetNode) {
		HashtagEntity[] hashtags = curTweet.getHashtagEntities();

		if (hashtags != null && hashtags.length > 0) {
			for (int i = 0; i < hashtags.length; i++) {
				String hashtagText = hashtags[i].getText();
				NodeIdentifier hashtagNID;

				if (hashtagsMap.containsKey(hashtagText)) {
					hashtagNID = hashtagsMap.get(hashtagText);
				} else {
					// create new hash tag node
					hashtagNID = new NodeIdentifier(graph.createNode(""
							+ nodeCounter), "" + (nodeCounter++));
					hashtagNID.node.setLabel("#")
							.setSize(Constants.HASHTAG_NODE_SIZE)
							.setColor(Constants.HASHTAG_NODE_COLOR)
							.getShapeEntity()
							.setNodeShape(Constants.HASHTAG_NODE_SHAPE);
					hashtagNID.setText(hashtagText);
					hashtagsMap.put(hashtagText, hashtagNID);
				}

				// repeated hashtags in the same tweet
				if (!tweetNode.hasEdgeTo(hashtagNID.nodeIndex)) {
					tweetNode.connectTo("" + (edgeCounter++), "Annotated",
							hashtagNID.node);
				}
			}
		}
	}

	private void parseData() throws IOException {

		Status curTweet;
		while ((curTweet = streamer.getNextStatus()) != null) {

			// TWEET Node
			Long tweetID = curTweet.getId();

			NodeIdentifier tweetNID = new NodeIdentifier(graph.createNode(""
					+ nodeCounter), "" + (nodeCounter++));
			tweetNID.node.setLabel("+").setSize(Constants.TWEET_NODE_SIZE)
					.setColor(Constants.TWEET_NODE_COLOR).getShapeEntity()
					.setNodeShape(Constants.TWEET_NODE_SHAPE);
			tweetNID.setText(curTweet.getText());
			nodeMap.put(tweetID, tweetNID);
			nodesList.add(tweetNID);

			if (curTweet.isRetweet()) {
				// Check for original Retweeted Status
				Long originalTweetId = curTweet.getRetweetedStatus().getId();
				if (nodeMap.containsKey(originalTweetId)) {
					NodeIdentifier origTweetNID = nodeMap.get(originalTweetId);
					tweetNID.node.connectTo("" + (edgeCounter++), "Retweet",
							origTweetNID.node);
				}
				features.incRTID(); // increment number of re-tweets
			} else {
				features.incTID();// increment number of tweets

				if (curTweet.getGeoLocation() != null)
					features.incTGEO();// increment #tweets with geo-location
			}

			// Check for hashTag, financial symbols
			handleHashTags(curTweet, tweetNID.node);

			// Check for URLs
			handleURLs(curTweet, tweetNID.node);

			// Handle created/mentioned users
			handleUsers(curTweet, tweetNID.node);
		}
		oos.close();
	}

	private void addSimilarityNodes() {
		boolean[] visited = new boolean[nodesList.size()];
		ArrayList<ArrayList<NodeIdentifier>> ans = new ArrayList<ArrayList<NodeIdentifier>>();

		for (int i = 0; i < nodesList.size(); i++)
			if (!visited[i]) {
				NodeIdentifier currentNID = nodesList.get(i);
				ArrayList<NodeIdentifier> tempAns = new ArrayList<NodeIdentifier>();
				tempAns.add(currentNID);
				visited[i] = true;

				for (int j = i + 1; j < nodesList.size(); j++)
					if (!visited[j]) {
						NodeIdentifier next = nodesList.get(j);
						if (JaccardSimilarity.getJaccardCoefficient(
								currentNID.text, next.text) > Constants.JACCARD_THRESHOLD) {
							visited[j] = true;
							tempAns.add(next);
						}
					}
				ans.add(tempAns);
			}

		for (int i = 0; i < ans.size(); i++) {
			ArrayList<NodeIdentifier> current = ans.get(i);
			if (current.size() > 1) { // similar nodes exist
				NodeIdentifier similarityNID = new NodeIdentifier(
						graph.createNode("" + nodeCounter), ""
								+ (nodeCounter++));
				similarityNID.node.setLabel("^")
						.setSize(Constants.SIMILARITY_NODE_SIZE)
						.setColor(Constants.SIMILARITY_NODE_COLOR)
						.getShapeEntity()
						.setNodeShape(Constants.SIMILARITY_NODE_SHAPE);

				for (int j = 0; j < current.size(); j++) {
					similarityNID.node.connectTo("" + (edgeCounter++),
							"Similar", current.get(j).node);
				}
			}
		}
	}

	private void buildActivityFeatures() {
		features.setTHTG(hashtagsMap.size());
		if (!usersMap.isEmpty()) {
			features.setUFLW(totalFollowersCounter / usersMap.size());
			features.setUFRN(totalFriendsCounter / usersMap.size());
		}
		features.printActivityFeatures();
	}

	private void initialize(String path, EdgeType graphType, Mode graphMode)
			throws IOException {

		// initialize parser
		streamer = new Parser(path);
		streamer.initializeParser();

		// initialize graph topology
		nodeMap = new TreeMap<Long, NodeIdentifier>();
		hashtagsMap = new TreeMap<String, NodeIdentifier>();
		urlsMap = new TreeMap<String, NodeIdentifier>();
		usersMap = new TreeMap<Long, NodeIdentifier>();
		nodesList = new ArrayList<NodeIdentifier>();
		fout = new FileOutputStream("/home/islamhamdi/Desktop/artificial.txt");
		oos = new ObjectOutputStream(fout);
		features = new ActivityFeatures();
		nodeCounter = edgeCounter = totalFollowersCounter = totalFriendsCounter = 0;

		// gexf initialization
		gexf = new GexfImpl();
		date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime())
				.setCreator("Gephi.org").setDescription("TwitStock");
		gexf.setVisualization(true);

		// setting graph properties
		graph = gexf.getGraph();
		graph.setDefaultEdgeType(graphType).setMode(graphMode);
		attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
	}

	public static void main(String[] args) throws IOException {
		GexfOutputGraph gexfGraph = new GexfOutputGraph();
		gexfGraph.initialize("/home/islamhamdi/Desktop/TwitterStockData",
				EdgeType.UNDIRECTED, Mode.STATIC);
		gexfGraph.parseData();
		gexfGraph.addSimilarityNodes();
		gexfGraph.buildActivityFeatures();
		gexfGraph.generateGexfFile();
	}
}
