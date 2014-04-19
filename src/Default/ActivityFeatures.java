package Default;

public class ActivityFeatures {
	private int RTID; // number of re-tweets
	private int RTU; // number of diff users that have re-tweeted
	private int TID; // number of tweets
	private int TUSM; // number of tweets that mention any user
	private int UFRN; // avg number of friends for user that posted in graph
	private int THTG;// number of hash-tags used in all tweets
	private int TURL; // number of tweets with URLs
	private int UFLW; // avg number of followers for user
	private int UID; // number diff users that posted a tweet
	private int NEG, POS, POS_NEG; // sentiment analysis statistics
	private static String[] ACTIVITY_FEATURES_NAMES = new String[] { "RTID",
			"RTU", "TID", "TSUM", "UFRN", "THTG", "TURL", "UFLW", "UID", "NEG",
			"POS", "POS_NEG" };

	public ActivityFeatures() {
		this.RTID = 0;
		this.RTU = 0;
		this.TID = 0;
		this.TUSM = 0;
		this.UFRN = 0;
		this.THTG = 0;
		this.TURL = 0;
		this.UFLW = 0;
		this.UID = 0;
		this.NEG = 0;
		this.POS = 0;
		this.POS_NEG = 0;
	}

	public int getNEG() {
		return NEG;
	}

	public void setNEG(int nEG) {
		this.NEG = nEG;
	}

	public int getPOS() {
		return POS;
	}

	public void setPOS(int pOS) {
		this.POS = pOS;
	}

	public int getPOS_NEG() {
		return POS_NEG;
	}

	public void setPOS_NEG(int pOS_NEG) {
		this.POS_NEG = pOS_NEG;
	}

	public void setRTID(int rTID) {
		RTID = rTID;
	}

	public void setRTU(int rTU) {
		RTU = rTU;
	}

	public void setTID(int tID) {
		TID = tID;
	}

	public void setTUSM(int tUSM) {
		TUSM = tUSM;
	}

	public void setUFRN(int uFRN) {
		UFRN = uFRN;
	}

	public void setTHTG(int tHTG) {
		THTG = tHTG;
	}

	public void setTURL(int tURL) {
		TURL = tURL;
	}

	public void setUFLW(int uFLW) {
		UFLW = uFLW;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public void incNEG() {
		NEG++;
	}

	public void incPOS() {
		POS++;
	}

	public void incPOS_NEG() {
		POS_NEG++;
	}

	public void decPOS_NEG() {
		POS_NEG--;
	}

	public void incRTID() {
		RTID++;
	}

	public void incRTU() {
		RTU++;
	}

	public void incTID() {
		TID++;
	}

	public void incTUSM() {
		TUSM++;
	}

	public void incUFRN() {
		UFRN++;
	}

	public void incTHTG() {
		THTG++;
	}

	public void incTURL() {
		TURL++;
	}

	public void incUFLW() {
		UFLW++;
	}

	public void incUID() {
		UID++;
	}

	@Override
	public String toString() {
		return "ActivityFeatures [RTID=" + RTID + ", RTU=" + RTU + ", TID="
				+ TID + ", TUSM=" + TUSM + ", UFRN=" + UFRN + ", THTG=" + THTG
				+ ", TURL=" + TURL + ", UFLW=" + UFLW + ", UID=" + UID
				+ ", NEG=" + NEG + ", POS=" + POS + ", POS_NEG=" + POS_NEG
				+ "]";
	}

	public static String[] getActivityFeaturesList() {
		return ACTIVITY_FEATURES_NAMES;
	}

	public double[] getValues() {
		return new double[] { this.RTID, this.RTU, this.TID, this.TUSM,
				this.UFRN, this.THTG, this.TURL, this.UFLW, this.UID, this.NEG,
				this.POS, this.POS_NEG };
	}

	public void printActivityFeatures() {
		System.out.println(this.toString());
	}

}
