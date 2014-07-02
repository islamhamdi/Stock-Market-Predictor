package Default;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
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

		public String toString() {
			return "Text = " + text;
		}
	}

	private Parser streamer;
	public static Gexf gexf;
	public static Calendar date;
	public static Graph graph;
	public static AttributeList attrList;
	private TreeMap<Long, NodeIdentifier> nodeMap, usersMap;
	private TreeMap<String, NodeIdentifier> hashtagsMap;
	private ArrayList<NodeIdentifier> nodesList;
	private int nodeCounter, edgeCounter;
	private String curCompanyName;

	private void generateGexfFile() {
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(
				"/home/islamhamdi/Desktop/StockTwitsData/$AAPL/stoc_twit_graph.gexf");
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
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
				
				Status retweetedStatus = curTweet.getRetweetedStatus();
					// only happens with stock-twit status treated as twitter
					// status
				if (retweetedStatus != null) {
					Long originalTweetId = retweetedStatus.getId();
					if (nodeMap.containsKey(originalTweetId)) {
						NodeIdentifier origTweetNID = nodeMap
								.get(originalTweetId);
						tweetNID.node.connectTo("" + (edgeCounter++), "Retweet",
							origTweetNID.node);
					}
				}
			}

			// Check for hashTag, financial symbols
			handleHashTagsAndSymbols(curTweet, tweetNID.node);

			// Handle created/mentioned users
			handleUsers(curTweet, tweetNID.node);
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
			userNID.setText(curTweet.getUser().getScreenName());
			userNID.node.setLabel("@").setSize(Constants.USER_NODE_SIZE)
					.setColor(Constants.USER_NODE_COLOR).getShapeEntity()
					.setNodeShape(Constants.USER_NODE_SHAPE);
			userNID.setText(curTweet.getUser().getScreenName());
			usersMap.put(creatorUserID, userNID);
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
		}
	}

	private void handleHashTagsAndSymbols(Status curTweet, Node tweetNode) {
		HashtagEntity[] hashtags = curTweet.getHashtagEntities();
		SymbolEntity[] symbols = curTweet.getSymbolEntities();
		String[] tags = Helper.combineTags(hashtags, symbols);

		if (tags != null && tags.length > 0) {
			for (int i = 0; i < tags.length; i++) {
				String tagText = tags[i];
				if (tagText.startsWith("$"))
					tagText = tagText.substring(1);

				// skip $company_name as a symbol
				if (("$" + tagText).equals(this.curCompanyName))
					continue;

				NodeIdentifier tagNID;

				if (hashtagsMap.containsKey(tagText)) {
					tagNID = hashtagsMap.get(tagText);
				} else {
					// create new hash tag node
					tagNID = new NodeIdentifier(graph.createNode(""
							+ nodeCounter), "" + (nodeCounter++));
					tagNID.node.setLabel("#")
							.setSize(Constants.HASHTAG_NODE_SIZE)
							.setColor(Constants.HASHTAG_NODE_COLOR)
							.getShapeEntity()
							.setNodeShape(Constants.HASHTAG_NODE_SHAPE);
					tagNID.setText(tagText);
					hashtagsMap.put(tagText, tagNID);
				}

				// repeated hashtags in the same tweet
				if (!tweetNode.hasEdgeTo(tagNID.nodeIndex)) {
					tweetNode.connectTo("" + (edgeCounter++), "Annotated",
							tagNID.node);
				}
			}
		}
	}

	

	private void addSimilarityNodes() {
		boolean[] visited = new boolean[nodesList.size()];
		ArrayList<ArrayList<NodeIdentifier>> ans = new ArrayList<ArrayList<NodeIdentifier>>();

		for (int i = 0; i < nodesList.size(); i++)
			if (!visited[i]) {
				NodeIdentifier currentNID = nodesList.get(i);
				
				String tweetText = currentNID.text;

				// a retweet starts with @username: or RT
				if (!(tweetText.matches("(@[a-zA-Z0-9]+:.+).*") || tweetText
						.toUpperCase().startsWith("RT")))
					continue;

				ArrayList<NodeIdentifier> tempAns = new ArrayList<NodeIdentifier>();
				tempAns.add(currentNID);
				visited[i] = true;

				for (int j = i + 1; j < nodesList.size(); j++)
					if (!visited[j]) {
						NodeIdentifier next = nodesList.get(j);
						String retweetBody = tweetText.substring(tweetText
								.indexOf(' ') + 1);
						double cofff = JaccardSimilarity.getJaccardCoefficient(
								retweetBody, next.text);
						if (cofff > Constants.MIN_JACCARD_THRESHOLD
								&& cofff < Constants.MAX_JACCARD_THRESHOLD) {
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

	private void initialize(String company, String path, EdgeType graphType, Mode graphMode)
			throws IOException {

		// initialize parser
		streamer = new Parser(path);
		streamer.initializeParser();

		// initialize graph topology
		nodeMap = new TreeMap<Long, NodeIdentifier>();
		hashtagsMap = new TreeMap<String, NodeIdentifier>();
		usersMap = new TreeMap<Long, NodeIdentifier>();
		nodesList = new ArrayList<NodeIdentifier>();
		nodeCounter = edgeCounter = 0;

		this.curCompanyName = company;

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
		gexfGraph.initialize("$APPL",
				"/home/islamhamdi/Desktop/StockTwitsData/$AAPL/02-05-2014",
				EdgeType.UNDIRECTED, Mode.STATIC);
		gexfGraph.parseData();
		gexfGraph.addSimilarityNodes();
		gexfGraph.generateGexfFile();
	}
}
