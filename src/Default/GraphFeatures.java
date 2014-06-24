package Default;

public class GraphFeatures {
	private int NUM_NODES; // number of nodes
	private int NUM_EDGES; // number of edges
	private int NUM_CMP; // number of connected components
	private double MAX_DIST; // max diameter for any component
	private double AVG_DEGREE; // average degree of nodes in the graph
	private double GRAPH_DENSITY; // how much the graph is dense
	private double AVG_PATH_LEN; // average path length in the graph
	private double MODULARITY; // Measures how well a network decomposes into
								// modular communities.

	private static final String[] GRAPH_FEATURES_NAMES = new String[] {
			"NUM_NODES", "NUM_EDGES", "NUM_CMP", "MAX_DIST", "AVG_DEGREE",
			"GRAPH_DENSITY", "AVG_PATH_LEN", "MODULARITY" };

	public GraphFeatures() {
		this.NUM_NODES = 0;
		this.NUM_EDGES = 0;
		this.NUM_CMP = 0;
		this.MAX_DIST = 0;
		this.AVG_DEGREE = 0;
		this.GRAPH_DENSITY = 0;
		this.AVG_PATH_LEN = 0;
		this.MODULARITY = 0;
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

	public double getAVG_DEGREE() {
		return AVG_DEGREE;
	}

	public void setAVG_DEGREE(double aVG_DEGREE) {
		AVG_DEGREE = aVG_DEGREE;
	}

	public double getGRAPH_DENSITY() {
		return GRAPH_DENSITY;
	}

	public void setGRAPH_DENSITY(double gRAPH_DENSITY) {
		GRAPH_DENSITY = gRAPH_DENSITY;
	}

	public double getAVG_PATH_LEN() {
		return AVG_PATH_LEN;
	}

	public void setAVG_PATH_LEN(double aVG_PATH_LEN) {
		AVG_PATH_LEN = aVG_PATH_LEN;
	}

	public double getMODULARITY() {
		return MODULARITY;
	}

	public void setMODULARITY(double mODULARITY) {
		MODULARITY = mODULARITY;
	}

	@Override
	public String toString() {
		return "GraphFeatures [NUM_NODES=" + NUM_NODES + ", NUM_EDGES="
				+ NUM_EDGES + ", NUM_CMP=" + NUM_CMP + ", MAX_DIST=" + MAX_DIST
				+ ", AVG_DEGREE=" + AVG_DEGREE + ", GRAPH_DENSITY="
				+ GRAPH_DENSITY + ", AVG_PATH_LEN=" + AVG_PATH_LEN
				+ ", MODULARITY=" + MODULARITY + "]";
	}

	public static String[] getGraphFeaturesNames() {
		return GRAPH_FEATURES_NAMES;
	}

	public double[] getValues() {
		return new double[] { NUM_NODES, NUM_EDGES, NUM_CMP, MAX_DIST,
				AVG_DEGREE, GRAPH_DENSITY, AVG_PATH_LEN, MODULARITY };
	}

	public void printGraphFeatures() {
		System.out.println(this.toString());
	}
}
