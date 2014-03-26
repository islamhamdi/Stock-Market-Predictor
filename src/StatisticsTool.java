import java.io.IOException;
import java.util.ArrayList;
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
import twitter4j.SymbolEntity;
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

		public String toString() {
			return "Text = " + text;
		}
	}

	private int nodeCounter, edgeCounter, totalFollowersCounter,
			totalFriendsCounter;

	private TreeMap<Long, NodeIdentifier> nodeMap, usersMap;
	private TreeMap<String, NodeIdentifier> hashtagsMap, urlsMap;
	private ArrayList<NodeIdentifier> nodesList;
	private double[] featureValues;
	private String curCompanyName;

	private Parser streamer;
	private Graph graph;
	private GraphModel graphModel;
	private ActivityFeatures activityFeatures;
	private GraphFeatures graphFeatures;
	private AttributeModel attributeModel;

	public StatisticsTool(String curCompanyName, String path)
			throws IOException {
		this.nodeMap = new TreeMap<Long, NodeIdentifier>();
		this.hashtagsMap = new TreeMap<String, NodeIdentifier>();
		this.urlsMap = new TreeMap<String, NodeIdentifier>();
		this.usersMap = new TreeMap<Long, NodeIdentifier>();
		this.nodesList = new ArrayList<NodeIdentifier>();
		this.graphFeatures = new GraphFeatures();
		this.activityFeatures = new ActivityFeatures();
		this.nodeCounter = this.edgeCounter = this.totalFollowersCounter = this.totalFriendsCounter = 0;
		this.featureValues = null;
		this.curCompanyName = curCompanyName;

		// initialize parser
		this.streamer = new Parser(path);
		this.streamer.initializeParser();

		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();
		GraphController graphController = Lookup.getDefault().lookup(
				GraphController.class);
		attributeModel = Lookup.getDefault().lookup(AttributeController.class)
				.getModel();
		graphModel = graphController.getModel();
		graph = graphModel.getUndirectedGraph();
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

				// Perform original tweets check on twitter data only
				if (Global.files_to_run == Global.TWITTER_DATA) {
					// Check for original Retweeted Status
					Long originalTweetId = curTweet.getRetweetedStatus()
							.getId();
					if (nodeMap.containsKey(originalTweetId)) {
						NodeIdentifier origTweetNID = nodeMap
								.get(originalTweetId);

						Edge newEdge = graphModel.factory().newEdge(
								tweetNID.node, origTweetNID.node);
						graph.addEdge(newEdge);
						edgeCounter++;
					}
				}
				activityFeatures.incRTID(); // increment number of re-tweets
			}
			activityFeatures.incTID();// increment number of tweets/retweets

			// Check for hashTag, financial symbols
			handleHashTagsAndSymbols(curTweet, tweetNID.node);

			// Check for URLs
			handleURLs(curTweet, tweetNID.node);

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
		if (mediaUrls == null)
			mediaUrls = new MediaEntity[0];
		String[] list = new String[urls.length + mediaUrls.length];
		int index = 0;

		for (int i = 0; i < urls.length; i++)
			list[index++] = urls[i].getText();
		for (int i = 0; i < mediaUrls.length; i++)
			list[index++] = mediaUrls[i].getText();

		if (list != null && list.length > 0) {
			for (int i = 0; i < list.length; i++) {

				// URL expansion, from t.co --> bit.ly --> original one
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

	private void handleHashTagsAndSymbols(Status curTweet, Node tweetNode) {

		HashtagEntity[] hashtags = curTweet.getHashtagEntities();
		SymbolEntity[] symbols = curTweet.getSymbolEntities();
		String[] tags = Helper.combineTags(hashtags, symbols);

		if (tags != null && tags.length > 0) {
			for (int i = 0; i < tags.length; i++) {
				String tagText = tags[i];

				// skip $company_name as a symbol
				if (("$" + tagText).equals(this.curCompanyName))
					continue;

				NodeIdentifier tagNID;

				if (hashtagsMap.containsKey(tagText)) {
					tagNID = hashtagsMap.get(tagText);
				} else {
					// create new hash tag node
					tagNID = new NodeIdentifier(graphModel.factory().newNode(
							"" + nodeCounter), "" + (nodeCounter++));
					tagNID.setText(tagText);
					hashtagsMap.put(tagText, tagNID);
					graph.addNode(tagNID.node);
				}

				// repeated hashtags in the same tweet
				if (!graph.isAdjacent(tweetNode, tagNID.node)) {
					Edge newEdge = graphModel.factory().newEdge(tweetNode,
							tagNID.node);
					graph.addEdge(newEdge);
					edgeCounter++;
				}
			}
		}
	}

	void addSimilarityNodes() {

		// No need to add similarity nodes for twitter data
		if (Global.files_to_run == Global.TWITTER_DATA)
			return;

		boolean[] visited = new boolean[nodesList.size()];
		ArrayList<ArrayList<NodeIdentifier>> ans = new ArrayList<ArrayList<NodeIdentifier>>();

		for (int i = 0; i < nodesList.size(); i++) {
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
		}

		// for (int i = 0; i < ans.size(); i++) {
		// ArrayList<NodeIdentifier> current = ans.get(i);
		// if (current.size() > 1) { // similar nodes exist
		// for (int j = 0; j < current.size(); j++) {
		// System.out.println(current.get(j));
		// }
		// System.out.println("=========================");
		// }
		// }

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

	void buildActivityFeatures() throws Exception {
		if (activityFeatures == null)
			throw new Exception("Activity features are not set !");

		activityFeatures.setTHTG(hashtagsMap.size());
		if (!usersMap.isEmpty()) {
			activityFeatures.setUFLW(totalFollowersCounter / usersMap.size());
			activityFeatures.setUFRN(totalFriendsCounter / usersMap.size());
		}
		activityFeatures.printActivityFeatures();
	}

	void buildGraphFeatures() throws Exception {
		if (graphFeatures == null)
			throw new Exception("Graph features are not set !");

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
		graphFeatures.printGraphFeatures();
	}

	double[] getFeaturesValues() throws Exception {
		if (activityFeatures.getValues() == null
				|| graphFeatures.getValues() == null)
			throw new Exception("Features values are not set !");

		if (featureValues == null) {
			featureValues = Helper.combineDoubles(activityFeatures.getValues(),
					graphFeatures.getValues());
		}
		return featureValues;
	}
}
