package Default;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class JaccardSimilarity {

	public static double getJaccardCoefficient(String x, String y) {
		StringTokenizer st = new StringTokenizer(x);
		String[] xx = new String[st.countTokens()];
		for (int i = 0; i < xx.length; i++)
			xx[i] = st.nextToken();

		st = new StringTokenizer(y);
		String[] yy = new String[st.countTokens()];
		for (int i = 0; i < yy.length; i++)
			yy[i] = st.nextToken();

		return calculateSimilarity(xx, yy);
	}

	public static double calculateSimilarity(String[] x, String[] y) {
		double sim = 0.0d;
		if ((x != null && y != null) && (x.length > 0 || y.length > 0)) {
			sim = similarity(Arrays.asList(x), Arrays.asList(y));
		} else {
			throw new IllegalArgumentException(
					"The arguments x and y must be not NULL and either x or y must be non-empty.");
		}
		return sim;
	}

	private static double similarity(List<String> x, List<String> y) {

		if (x.size() == 0 || y.size() == 0)
			return 0.0;

		Set<String> unionXY = new HashSet<String>(x);
		unionXY.addAll(y);

		Set<String> intersectionXY = new HashSet<String>(x);
		intersectionXY.retainAll(y);

		return (double) intersectionXY.size() / (double) unionXY.size();
	}

}