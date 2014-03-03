import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class StatisticsTool {
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
	public static Graph graph;
	private TreeMap<Long, NodeIdentifier> nodeMap, usersMap;
	private TreeMap<String, NodeIdentifier> hashtagsMap, urlsMap;
	private ArrayList<NodeIdentifier> nodesList;
	private int nodeCounter, edgeCounter, totalFollowersCounter,
			totalFriendsCounter;
	private FileOutputStream fout;
	private ObjectOutputStream oos;
	private GraphModel graphModel;
	private ActivityFeatures activityFeatures = new ActivityFeatures();
	private GraphFeatures graphFeatures = new GraphFeatures();
	private String[] featuresList;
	private AttributeModel attributeModel;

	private void handleUsers(Status curTweet, Node tweetNode) {
		// Creator user
		Long creatorUserID = curTweet.getUser().getId();
		NodeIdentifier userNID;

		if (usersMap.containsKey(creatorUserID)) {
			userNID = usersMap.get(creatorUserID);
		} else {
			// new user registration
			userNID = new NodeIdentifier(graphModel.factory().newNode(
					"" + nodeCounter), "" + (nodeCounter++));
			userNID.setText(curTweet.getUser().getScreenName());
			usersMap.put(creatorUserID, userNID);
			graph.addNode(userNID.node);

			// increment number of diff users that have re-tweeted
			if (curTweet.isRetweet())
				activityFeatures.incRTU();

			activityFeatures.incUID();
			totalFollowersCounter += curTweet.getUser().getFollowersCount();
			totalFriendsCounter += curTweet.getUser().getFriendsCount();
		}

		Edge newwEdge = graphModel.factory().newEdge(tweetNode, userNID.node);
		graph.addEdge(newwEdge);
		edgeCounter++;

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
					curUserNID = new NodeIdentifier(graphModel.factory()
							.newNode("" + nodeCounter), "" + (nodeCounter++));
					curUserNID.setText(usersList[i].getScreenName());
					usersMap.put(curUserID, curUserNID);
					graph.addNode(curUserNID.node);
				}

				// a reply to a user mentioning the original user
				if (!graph.isAdjacent(tweetNode, curUserNID.node)) {
					Edge newEdge = graphModel.factory().newEdge(tweetNode,
							curUserNID.node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
			}

			// tweets that mention any user
			activityFeatures.incTUSM();
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
				// TODO String curUrlText = URLExpander.expandUrl(list[i]);
				String curUrlText = list[i];
				NodeIdentifier urlNID;

				if (urlsMap.containsKey(curUrlText)) {
					urlNID = urlsMap.get(curUrlText);
				} else {
					// create new url node
					urlNID = new NodeIdentifier(graphModel.factory().newNode(
							"" + nodeCounter), "" + (nodeCounter++));
					urlNID.setText(curUrlText);
					urlsMap.put(curUrlText, urlNID);
					graph.addNode(urlNID.node);
				}

				// repeated URL in the tweet
				if (!graph.isAdjacent(tweetNode, urlNID.node)) {
					Edge newEdge = graphModel.factory().newEdge(tweetNode,
							urlNID.node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
			}

			// number of tweets with URLs
			activityFeatures.incTURL();
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
					hashtagNID = new NodeIdentifier(graphModel.factory()
							.newNode("" + nodeCounter), "" + (nodeCounter++));
					hashtagNID.setText(hashtagText);
					hashtagsMap.put(hashtagText, hashtagNID);
					graph.addNode(hashtagNID.node);
				}

				// repeated hashtags in the same tweet
				if (!graph.isAdjacent(tweetNode, hashtagNID.node)) {
					Edge newEdge = graphModel.factory().newEdge(tweetNode,
							hashtagNID.node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
			}
		}
	}

	void parseData() throws IOException {

		Status curTweet;
		while ((curTweet = streamer.getNextStatus()) != null) {

			// TWEET Node
			Long tweetID = curTweet.getId();

			NodeIdentifier tweetNID = new NodeIdentifier(graphModel.factory()
					.newNode("" + nodeCounter), "" + (nodeCounter++));

			tweetNID.setText(curTweet.getText());
			nodeMap.put(tweetID, tweetNID);
			nodesList.add(tweetNID);
			graph.addNode(tweetNID.node);

			if (curTweet.isRetweet()) {
				// Check for original Retweeted Status
				Long originalTweetId = curTweet.getRetweetedStatus().getId();
				if (nodeMap.containsKey(originalTweetId)) {
					NodeIdentifier origTweetNID = nodeMap.get(originalTweetId);

					Edge newEdge = graphModel.factory().newEdge(tweetNID.node,
							origTweetNID.node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
				activityFeatures.incRTID(); // increment number of re-tweets
			} else {
				activityFeatures.incTID();// increment number of tweets

				if (curTweet.getGeoLocation() != null)
					activityFeatures.incTGEO();// increment #tweets with
												// geo-location
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

	void addSimilarityNodes() {
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
				NodeIdentifier similarityNID = new NodeIdentifier(graphModel
						.factory().newNode("" + nodeCounter), ""
						+ (nodeCounter++));
				graph.addNode(similarityNID.node);

				for (int j = 0; j < current.size(); j++) {
					Edge newEdge = graphModel.factory().newEdge(
							similarityNID.node, current.get(j).node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
			}
		}
	}

	void buildActivityFeatures() {
		activityFeatures.setTHTG(hashtagsMap.size());
		if (!usersMap.isEmpty()) {
			activityFeatures.setUFLW(totalFollowersCounter / usersMap.size());
			activityFeatures.setUFRN(totalFriendsCounter / usersMap.size());
		}
		activityFeatures.printActivityFeatures();
	}

	void buildGraphFeatures() {
		graphFeatures.setNUM_NODES(nodeCounter);
		graphFeatures.setNUM_EDGES(edgeCounter);

		// Get Number of connected components in the graph
		ConnectedComponents c = new ConnectedComponents();
		c.execute(graphModel, attributeModel);
		graphFeatures.setNUM_CMP(c.getConnectedComponentsCount());

		// Maximum diameter in any component in the graph
		GraphDistance distance = new GraphDistance();
		distance.execute(graphModel, attributeModel);
		graphFeatures.setMAX_DIST(distance.getDiameter());
		System.out.println(distance.getReport());
		graphFeatures.printGraphFeatures();
	}

	String[] getFeaturesList() {
		String[] gphFeatures = graphFeatures.getGraphFeaturesList();
		String[] actFeatures = activityFeatures.getActivityFeaturesList();
		featuresList = new String[gphFeatures.length + actFeatures.length];
		int index = 0;
		for (int i = 0; i < gphFeatures.length; i++)
			featuresList[index++] = gphFeatures[i];
		for (int i = 0; i < actFeatures.length; i++)
			featuresList[index++] = actFeatures[i];
		return featuresList;
	}

	double[] getFeatureValues() {
		int index = 0;
		double[] gphValues = graphFeatures.getValues();
		double[] actValues = activityFeatures.getValues();
		double[] featureValues = new double[gphValues.length + actValues.length];

		for (int i = 0; i < actValues.length; i++)
			featureValues[index++] = actValues[i];

		for (int i = 0; i < gphValues.length; i++)
			featureValues[index++] = gphValues[i];

		return featureValues;
	}

	void initialize(String path) throws IOException {

		// initialize parser
		streamer = new Parser(path);
		streamer.initializeParser();

		// initialize graph topology
		nodeMap = new TreeMap<Long, NodeIdentifier>();
		hashtagsMap = new TreeMap<String, NodeIdentifier>();
		urlsMap = new TreeMap<String, NodeIdentifier>();
		usersMap = new TreeMap<Long, NodeIdentifier>();
		nodesList = new ArrayList<NodeIdentifier>();
		graphFeatures = new GraphFeatures();
		activityFeatures = new ActivityFeatures();
		fout = new FileOutputStream("/home/islamhamdi/Desktop/artificial.txt");
		oos = new ObjectOutputStream(fout);
		nodeCounter = edgeCounter = totalFollowersCounter = totalFriendsCounter = 0;

		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();
		GraphController graphController = Lookup.getDefault().lookup(
				GraphController.class);
		attributeModel = Lookup.getDefault().lookup(AttributeController.class)
				.getModel();
		graphModel = graphController.getModel();
		graph = graphModel.getGraph();
	}

	public static void main(String[] args) throws IOException {
		StatisticsTool gexfGraph = new StatisticsTool();
		gexfGraph.initialize("/home/islamhamdi/Desktop/TwitterStockData");
		gexfGraph.parseData();
		gexfGraph.addSimilarityNodes();
		gexfGraph.buildActivityFeatures();
		gexfGraph.buildGraphFeatures();
	}
}