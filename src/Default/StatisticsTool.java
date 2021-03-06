package Default;

import java.io.File;
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
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
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
	private TreeMap<String, NodeIdentifier> hashtagsMap;
	private ArrayList<NodeIdentifier> nodesList;
	private double[] featureValues;
	private String curCompanyName;
	private String curFileName;

	private Parser streamer;
	private Graph graph;
	private GraphModel graphModel;
	private ActivityFeatures activityFeatures;
	private GraphFeatures graphFeatures;
	private AttributeModel attributeModel;

	public StatisticsTool() throws IOException {
		this.nodeCounter = this.edgeCounter = this.totalFollowersCounter = this.totalFriendsCounter = 0;
		this.featureValues = null;

		this.nodeMap = new TreeMap<Long, NodeIdentifier>();
		this.hashtagsMap = new TreeMap<String, NodeIdentifier>();
		this.usersMap = new TreeMap<Long, NodeIdentifier>();
		this.nodesList = new ArrayList<NodeIdentifier>();
		this.graphFeatures = new GraphFeatures();
		this.activityFeatures = new ActivityFeatures();

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

	void resetTool(String curCompanyName, String curFileName, String filePath)
			throws IOException {
		this.curCompanyName = curCompanyName;
		this.curFileName = curFileName;

		// initialize parser
		this.streamer = new Parser(filePath);
		this.streamer.initializeParser();

		this.nodeCounter = this.edgeCounter = this.totalFollowersCounter = this.totalFriendsCounter = 0;
		this.featureValues = null;

		this.nodeMap.clear();
		this.hashtagsMap.clear();
		this.usersMap.clear();
		this.nodesList.clear();
		this.graphFeatures = new GraphFeatures();
		this.activityFeatures = new ActivityFeatures();

		attributeModel = Lookup.getDefault().lookup(AttributeController.class)
				.getModel();
		graphModel.clear();
		graph = graphModel.getUndirectedGraph();
	}

	void parseData() throws Throwable {

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

				// Perform original tweets check on twitter data-type only
				if (Helper.getDataType(Global.files_to_run) == Global.twitter) {
					// Check for original Retweeted Status

					Status retweetedStatus = curTweet.getRetweetedStatus();
					// only happens with stock-twit status treated as twitter
					// status
					if (retweetedStatus != null) {
						Long originalTweetId = retweetedStatus.getId();
						if (nodeMap.containsKey(originalTweetId)) {
							NodeIdentifier origTweetNID = nodeMap
									.get(originalTweetId);

							Edge newEdge = graphModel.factory().newEdge(
									tweetNID.node, origTweetNID.node);
							graph.addEdge(newEdge);
							edgeCounter++;
						}
					}
				}
				activityFeatures.incRTID(); // increment number of re-tweets
			}
			activityFeatures.incTID();// increment number of tweets/retweets

			// Check for hashTag, financial symbols
			handleHashTagsAndSymbols(curTweet, tweetNID.node);

			// Handle created/mentioned users
			handleUsers(curTweet, tweetNID.node);

		}
		finalize();
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

		// add similarity nodes only for stockTwits data
		if (!(Helper.getDataType(Global.files_to_run) == Global.stocktwits))
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
		activityFeatures.setTHTG(hashtagsMap.size());
		activityFeatures.setUFLW(totalFollowersCounter);
		activityFeatures.setUFRN(totalFriendsCounter);
		setSentimentFeatures();
		activityFeatures.printActivityFeatures();
	}

	private void setSentimentFeatures() throws Exception {
		String posPath = null, negPath = null;
		String[] paths = new String[] { Global.sentimentTwitterPath,
				Global.sentimentStockTwitPath };

		int startIndex = 0, loopCounter = 0;
		if (Global.files_to_run == Global.twitter) {// TWITTER
			startIndex = 0;
			loopCounter = 1;
		} else if (Global.files_to_run == Global.stocktwits) {// STOCK TWITS
			startIndex = 1;
			loopCounter = 2;
		} else if (Global.files_to_run == Global.combined) {// COMBINED
			startIndex = 0;
			loopCounter = 2;
		} else {
			// "Twitter/StockTwits/Combined data are only sentiment supported!"
			// Setting sentiment features to number of tweets
			activityFeatures.setPOS(activityFeatures.getTID());
			activityFeatures.setNEG(activityFeatures.getTID());
			activityFeatures.setPOS_NEG(activityFeatures.getTID());
			return;
		}

		for (int i = startIndex; i < loopCounter; i++) {
			posPath = negPath = paths[i];
			posPath += "/positive/" + curCompanyName + "/" + curFileName;
			negPath += "/negative/" + curCompanyName + "/" + curFileName;

			File statusDir = new File(posPath);
			int posTweets = 0;
			if (statusDir.exists()) {
				Parser posParser = new Parser(posPath);
				posParser.initializeParser();
				posTweets = posParser.countNumberOfStatus();
			}
			statusDir = new File(negPath);

			int negTweets = 0;
			if (statusDir.exists()) {
				Parser negParser = new Parser(negPath);
				negParser.initializeParser();
				negTweets = negParser.countNumberOfStatus();
			}

			activityFeatures.addToPOS(posTweets);
			activityFeatures.addToNEG(negTweets);
			activityFeatures.addToPOS_NEG(posTweets - negTweets);
		}
	}

	void buildGraphFeatures() throws Exception {
		graphFeatures.setNUM_NODES(nodeCounter);
		graphFeatures.setNUM_EDGES(edgeCounter);

		// Get Number of connected components in the graph
		ConnectedComponents c = new ConnectedComponents();
		c.execute(graphModel, attributeModel);
		graphFeatures.setNUM_CMP(c.getConnectedComponentsCount());

		// Maximum diameter in any component in the graph
		if (activityFeatures.getTID() <= Global.GRAPH_DISTANCE_THRESHOLD) {
			GraphDistance distance = new GraphDistance();
			distance.execute(graphModel, attributeModel);
			graphFeatures.setMAX_DIST(distance.getDiameter());
		} else {
			graphFeatures.setMAX_DIST(Global.GRAPH_DIST_VAL);
		}

		// Average degree of nodes in the graph
		Degree d = new Degree();
		d.execute(graphModel, attributeModel);
		graphFeatures.setAVG_DEGREE(d.getAverageDegree());

		// Graph density
		GraphDensity dens = new GraphDensity();
		dens.execute(graphModel, attributeModel);
		graphFeatures.setGRAPH_DENSITY(dens.getDensity());

		// Average path length
		if (activityFeatures.getTID() <= Global.GRAPH_DISTANCE_THRESHOLD) {
			GraphDistance avg_path = new GraphDistance();
			avg_path.execute(graphModel, attributeModel);
			graphFeatures.setAVG_PATH_LEN(avg_path.getPathLength());
		} else {
			graphFeatures.setAVG_PATH_LEN(Global.AVG_PATH_LEN_VAL);
		}

		// Graph modularity
		Modularity mod = new Modularity();
		mod.execute(graphModel, attributeModel);
		graphFeatures.setMODULARITY(mod.getModularity());

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
