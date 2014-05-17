package KeyStatistics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class KMeans {
	private int noOfClusters;
	private double threshold = 0.00000001;
	public double[] centroids;

	public KMeans(int clusters) {
		noOfClusters = clusters;
		centroids = new double[clusters];
	}

	public ArrayList<Double>[] calculateClusters(double[] data) {
		ArrayList<Double>[] list = new ArrayList[noOfClusters];
		for (int i = 0; i < list.length; i++)
			list[i] = new ArrayList<Double>();

		// initial centroids first k elements
		for (int i = 0; i < noOfClusters && i < data.length; i++)
			centroids[i] = data[i];

		boolean converge = false;
		while (!converge) {
			for (int i = 0; i < list.length; i++)
				list[i].clear();

			// classify data
			for (int i = 0; i < data.length; i++) {
				int clusterIdx = classify(data[i]);
				list[clusterIdx].add(data[i]);
			}

			// recalculate centroids
			double[] newCentroids = new double[noOfClusters];
			for (int centroid = 0; centroid < list.length; centroid++) {
				for (int i = 0; i < list[centroid].size(); i++) {
					newCentroids[centroid] += list[centroid].get(i);
				}

				newCentroids[centroid] /= list[centroid].size();
			}

			// check convergence
			converge = true;
			for (int i = 0; i < noOfClusters; i++)
				converge = converge & (Math.abs(newCentroids[i] - centroids[i]) < threshold);

			centroids = newCentroids;
		}

		return list;
	}

	private int classify(double val) {
		int clusterIndex = getNearestCentroid(val);
		return clusterIndex;
	}

	private int getNearestCentroid(double val) {
		int nearestCentroid = 0;
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < noOfClusters; i++) {
			double distance = getDistance(val, centroids[i]);

			if (distance < minDistance) {
				nearestCentroid = i;
				minDistance = distance;
			}
		}

		return nearestCentroid;
	}

	private double getDistance(double a, double b) {
		double sum = Math.abs(a - b);
		return sum;
	}

}