public class GraphFeatures {
	private int NUM_NODES; // number of nodes
	private int NUM_EDGES; // number of edges
	private int NUM_CMP; // number of connected components
	private double MAX_DIST; // max diameter for any component
	private static final String[] GRAPH_FEATURES_NAMES = new String[] {
			"NUM_NODES", "NUM_EDGES", "NUM_CMP", "MAX_DIST" };

	public GraphFeatures() {
		this.NUM_NODES = 0;
		this.NUM_EDGES = 0;
		this.NUM_CMP = 0;
		this.MAX_DIST = 0;
	}

	public int getNUM_NODES() {
		return NUM_NODES;
	}

	public void setNUM_NODES(int nUM_NODES) {
		NUM_NODES = nUM_NODES;
	}

	public int getNUM_EDGES() {
		return NUM_EDGES;
	}

	public void setNUM_EDGES(int nUM_EDGES) {
		NUM_EDGES = nUM_EDGES;
	}

	public int getNUM_CMP() {
		return NUM_CMP;
	}

	public void setNUM_CMP(int nUM_CMP) {
		NUM_CMP = nUM_CMP;
	}

	public double getMAX_DIST() {
		return MAX_DIST;
	}

	public void setMAX_DIST(double mAX_DIST) {
		MAX_DIST = mAX_DIST;
	}

	@Override
	public String toString() {
		return "GraphFeatures [NUM_NODES=" + NUM_NODES + ", NUM_EDGES="
				+ NUM_EDGES + ", NUM_CMP=" + NUM_CMP + ", MAX_DIST=" + MAX_DIST
				+ "]";
	}

	public static String[] getGraphFeaturesNames() {
		return GRAPH_FEATURES_NAMES;
	}

	public double[] getValues() {
		return new double[] { NUM_NODES, NUM_EDGES, NUM_CMP, MAX_DIST };
	}

	public void printGraphFeatures() {
		System.out.println(this.toString());
	}
}
