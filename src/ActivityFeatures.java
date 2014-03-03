public class ActivityFeatures {
	private int RTID; // number of re-tweets
	private int RTU; // number of diff users that have re-tweeted
	private int TGEO; // number of tweets with geo-location
	private int TID; // number of tweets
	private int TUSM; // number of tweets that mention any user
	private int UFRN; // avg number of friends for user that posted in graph
	private int THTG;// number of hash-tags used in all tweets
	private int TURL; // number of tweets with URLs
	private int UFLW; // avg number of followers for user
	private int UID; // number diff users that posted a tweet

	public ActivityFeatures() {
		this.RTID = 0;
		this.RTU = 0;
		this.TGEO = 0;
		this.TID = 0;
		this.TUSM = 0;
		this.UFRN = 0;
		this.THTG = 0;
		this.TURL = 0;
		this.UFLW = 0;
		this.UID = 0;
	}

	public void setRTID(int rTID) {
		RTID = rTID;
	}

	public void setRTU(int rTU) {
		RTU = rTU;
	}

	public void setTGEO(int tGEO) {
		TGEO = tGEO;
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

	public void incRTID() {
		RTID++;
	}

	public void incRTU() {
		RTU++;
	}

	public void incTGEO() {
		TGEO++;
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
		return "ActivityFeatures [RTID=" + RTID + ", RTU=" + RTU + ", TGEO="
				+ TGEO + ", TID=" + TID + ", TUSM=" + TUSM + ", UFRN=" + UFRN
				+ ", THTG=" + THTG + ", TURL=" + TURL + ", UFLW=" + UFLW
				+ ", UID=" + UID + "]";
	}

	public String[] getActivityFeaturesList() {
		return new String[] { "RTID", "RTU", "TGEO", "TID", "TSUM", "UFRN",
				"THTG", "TURL", "UFLW", "UID" };
	}

	public double[] getValues() {
		return new double[] { this.RTID, this.RTU, this.TGEO, this.TID,
				this.TUSM, this.UFRN, this.THTG, this.TURL, this.UFLW, this.UID };
	}

	public void printActivityFeatures() {
		System.out.println(this.toString());
	}

}
