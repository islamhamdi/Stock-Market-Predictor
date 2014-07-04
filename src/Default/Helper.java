package Default;

import twitter4j.HashtagEntity;
import twitter4j.SymbolEntity;

public class Helper {
	public static double[] combineDoubles(double[] a, double[] b) {
		int length = a.length + b.length;
		double[] result = new double[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static String[] combineStrings(String[] a, String[] b) {
		int length = a.length + b.length;
		String[] result = new String[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static String[] combineTags(HashtagEntity[] a, SymbolEntity[] b) {
		String[] result = new String[a.length + b.length];
		int tagIndex = 0;
		for (int i = 0; i < a.length; i++)
			result[tagIndex++] = a[i].getText();
		for (int i = 0; i < b.length; i++)
			result[tagIndex++] = b[i].getText();
		return result;
	}

	public static String[] getFeaturesList() {
		return combineStrings(ActivityFeatures.getActivityFeaturesList(),
				GraphFeatures.getGraphFeaturesNames());
	}

	public static int getDataType(int files_to_run) {
		int ans = -1;
		switch (files_to_run) {
		case Global.twitter:
		case Global.postwitter:
		case Global.negtwitter:
			ans = Global.twitter;
			break;

		case Global.stocktwits:
		case Global.posstocktwits:
		case Global.negstocktwits:
			ans = Global.stocktwits;
			break;

		case Global.combined:
			ans = Global.combined;
		default:
			ans = -2;
			break;
		}
		return ans;
	}
}
