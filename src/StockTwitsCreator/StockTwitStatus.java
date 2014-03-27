package StockTwitsCreator;

import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import Default.Global;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class StockTwitStatus implements Status, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6529293556112189703L;
	private Date date;
	private DateFormat formatter;
	private User user;
	private long tweetID;
	private String source;
	private myPlace place;
	private String text;
	private HashtagEntity[] hashtagEntities;
	private SymbolEntity[] symbolEntities;
	private URLEntity[] URLEntities;
	private UserMentionEntity[] UserMentionEntities;

	public StockTwitStatus(String userName, int followerCount, int friendCount,
			String date, long tweetID, String text, String sourceURL,
			String placeName, HashMap<String, Long> map) {
		this.tweetID = tweetID;
		this.source = sourceURL;
		this.place = new myPlace(placeName);
		this.text = text;
		setDate(date);
		setUser(userName, followerCount, friendCount, map);
		setSymbolEntities(text);
		setHashTagEntities(text);
		setURLEntities(text);
		setUserMentionEntity(map);
	}

	@Override
	public int compareTo(Status o) {
		return 0;
	}

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return null;
	}

	@Override
	public SymbolEntity[] getSymbolEntities() {
		return symbolEntities;
	}

	@Override
	public URLEntity[] getURLEntities() {
		return URLEntities;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return UserMentionEntities;
	}

	@Override
	public long[] getContributors() {
		return null;
	}

	@Override
	public Date getCreatedAt() {
		return date;
	}

	@Override
	public long getCurrentUserRetweetId() {
		return 0;
	}

	@Override
	public int getFavoriteCount() {
		return 0;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return null;
	}

	@Override
	public long getId() {
		return tweetID;
	}

	@Override
	public String getInReplyToScreenName() {
		return null;
	}

	@Override
	public long getInReplyToStatusId() {
		return 0;
	}

	@Override
	public long getInReplyToUserId() {
		return 0;
	}

	@Override
	public String getIsoLanguageCode() {
		return null;
	}

	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public int getRetweetCount() {
		return 0;
	}

	@Override
	public Status getRetweetedStatus() {
		return null;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public boolean isFavorited() {
		return false;
	}

	@Override
	public boolean isPossiblySensitive() {
		return false;
	}

	@Override
	public boolean isRetweet() {
		return getText().matches("(@[a-zA-Z0-9]+:.+).*")
				|| getText().toUpperCase().startsWith("RT");
	}

	@Override
	public boolean isRetweeted() {
		return false;
	}

	@Override
	public boolean isRetweetedByMe() {
		return false;
	}

	@Override
	public boolean isTruncated() {
		return false;
	}

	private void setUserMentionEntity(HashMap<String, Long> map) {
		StringTokenizer st = new StringTokenizer(text);
		ArrayList<UserMentionEntity> list = new ArrayList<UserMentionEntity>();
		while (st.hasMoreTokens()) {
			String tmp = st.nextToken();
			if (tmp.startsWith("@")) {
				String name = tmp.substring(1);
				if (!map.containsKey(name))
					map.put(name, (long) map.size());

				long id = map.get(name);

				list.add(new myUserMentionEntity(id, name));
			}
		}

		UserMentionEntities = new UserMentionEntity[list.size()];
		for (int i = 0; i < list.size(); i++)
			UserMentionEntities[i] = list.get(i);
	}

	private void setURLEntities(String text) {
		String[] sites = { "http", "bit.ly", "goo.gl", "bitly", "ow.ly",
				"tinyurl" };
		StringTokenizer st = new StringTokenizer(text);
		ArrayList<URLEntity> list = new ArrayList<URLEntity>();
		while (st.hasMoreTokens()) {
			String tmp = st.nextToken();
			for (String s : sites)
				if (tmp.startsWith(s) && tmp.length() > 6) {
					list.add(new myURLEntity(tmp));
					break;
				}
		}

		URLEntities = new URLEntity[list.size()];
		for (int i = 0; i < list.size(); i++)
			URLEntities[i] = list.get(i);
	}

	private void setSymbolEntities(String s) {
		ArrayList<SymbolEntity> list = new ArrayList<SymbolEntity>();

		for (int i = 0; i < Global.companies.length; i++)
			if (s.contains(Global.companies[i]))
				list.add(new MySymbolEntity(Global.companies[i]));

		symbolEntities = new SymbolEntity[list.size()];
		for (int i = 0; i < list.size(); i++)
			symbolEntities[i] = list.get(i);
	}

	private void setHashTagEntities(String s) {
		ArrayList<HashtagEntity> list = new ArrayList<HashtagEntity>();

		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String tmp = st.nextToken();
			if (tmp.charAt(0) == '#')
				list.add(new myHashtagEntity(tmp));
		}

		hashtagEntities = new HashtagEntity[list.size()];
		for (int i = 0; i < list.size(); i++)
			hashtagEntities[i] = list.get(i);
	}

	private void setUser(String userName, int followerCount, int friendCount,
			HashMap<String, Long> map) {
		if (!map.containsKey(userName))
			map.put(userName, (long) map.size());

		long userID = map.get(userName);
		user = new myUser(userName, userID, followerCount, friendCount);
	}

	private void setDate(String d) {
		this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
		d = d.replaceAll("[T]|[Z]", " ");
		try {
			date = formatter.parse(d);
			date = new Date(date.getTime() + TimeUnit.HOURS.toMillis(2));

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// ******************************************************************************************************
	private class myUser implements User, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3740008757319979727L;
		private String name;
		private long ID;
		private int followerCount, friendCount;

		public myUser(String userName, long userID, int followerCount,
				int follwingCount) {
			this.name = userName;
			this.ID = userID;
			this.followerCount = followerCount;
			this.friendCount = follwingCount;
		}

		@Override
		public int compareTo(User o) {
			return 0;
		}

		@Override
		public int getAccessLevel() {
			return 0;
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public String getBiggerProfileImageURL() {
			return null;
		}

		@Override
		public String getBiggerProfileImageURLHttps() {
			return null;
		}

		@Override
		public Date getCreatedAt() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public URLEntity[] getDescriptionURLEntities() {
			return null;
		}

		@Override
		public int getFavouritesCount() {
			return 0;
		}

		@Override
		public int getFollowersCount() {
			return followerCount;
		}

		@Override
		public int getFriendsCount() {
			return friendCount;
		}

		@Override
		public long getId() {
			return ID;
		}

		@Override
		public String getLang() {
			return null;
		}

		@Override
		public int getListedCount() {
			return 0;
		}

		@Override
		public String getLocation() {
			return null;
		}

		@Override
		public String getMiniProfileImageURL() {
			return null;
		}

		@Override
		public String getMiniProfileImageURLHttps() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getOriginalProfileImageURL() {
			return null;
		}

		@Override
		public String getOriginalProfileImageURLHttps() {
			return null;
		}

		@Override
		public String getProfileBackgroundColor() {
			return null;
		}

		@Override
		public String getProfileBackgroundImageURL() {
			return null;
		}

		@Override
		public String getProfileBackgroundImageUrl() {
			return null;
		}

		@Override
		public String getProfileBackgroundImageUrlHttps() {
			return null;
		}

		@Override
		public String getProfileBannerIPadRetinaURL() {
			return null;
		}

		@Override
		public String getProfileBannerIPadURL() {
			return null;
		}

		@Override
		public String getProfileBannerMobileRetinaURL() {
			return null;
		}

		@Override
		public String getProfileBannerMobileURL() {
			return null;
		}

		@Override
		public String getProfileBannerRetinaURL() {
			return null;
		}

		@Override
		public String getProfileBannerURL() {
			return null;
		}

		@Override
		public String getProfileImageURL() {
			return null;
		}

		@Override
		public String getProfileImageURLHttps() {
			return null;
		}

		@Override
		public URL getProfileImageUrlHttps() {
			return null;
		}

		@Override
		public String getProfileLinkColor() {
			return null;
		}

		@Override
		public String getProfileSidebarBorderColor() {
			return null;
		}

		@Override
		public String getProfileSidebarFillColor() {
			return null;
		}

		@Override
		public String getProfileTextColor() {
			return null;
		}

		@Override
		public String getScreenName() {
			return null;
		}

		@Override
		public Status getStatus() {
			return null;
		}

		@Override
		public int getStatusesCount() {
			return 0;
		}

		@Override
		public String getTimeZone() {
			return null;
		}

		@Override
		public String getURL() {
			return null;
		}

		@Override
		public URLEntity getURLEntity() {
			return null;
		}

		@Override
		public int getUtcOffset() {
			return 0;
		}

		@Override
		public boolean isContributorsEnabled() {
			return false;
		}

		@Override
		public boolean isFollowRequestSent() {
			return false;
		}

		@Override
		public boolean isGeoEnabled() {
			return false;
		}

		@Override
		public boolean isProfileBackgroundTiled() {
			return false;
		}

		@Override
		public boolean isProfileUseBackgroundImage() {
			return false;
		}

		@Override
		public boolean isProtected() {
			return false;
		}

		@Override
		public boolean isShowAllInlineMedia() {
			return false;
		}

		@Override
		public boolean isTranslator() {
			return false;
		}

		@Override
		public boolean isVerified() {
			return false;
		}
	}

	private class myPlace implements Place, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5088420462195589067L;
		String placeName;

		public myPlace(String place) {
			this.placeName = place;
		}

		@Override
		public int getAccessLevel() {
			return 0;
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public int compareTo(Place o) {
			return 0;
		}

		@Override
		public GeoLocation[][] getBoundingBoxCoordinates() {
			return null;
		}

		@Override
		public String getBoundingBoxType() {
			return null;
		}

		@Override
		public Place[] getContainedWithIn() {
			return null;
		}

		@Override
		public String getCountry() {
			return null;
		}

		@Override
		public String getCountryCode() {
			return null;
		}

		@Override
		public String getFullName() {
			return placeName;
		}

		@Override
		public GeoLocation[][] getGeometryCoordinates() {
			return null;
		}

		@Override
		public String getGeometryType() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getName() {
			return placeName;
		}

		@Override
		public String getPlaceType() {
			return null;
		}

		@Override
		public String getStreetAddress() {
			return null;
		}

		@Override
		public String getURL() {
			return null;
		}
	}

	private class myHashtagEntity implements HashtagEntity, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1634177246812834080L;
		private String text;

		public myHashtagEntity(String text) {
			this.text = text;
		}

		@Override
		public int getEnd() {
			return 0;
		}

		@Override
		public int getStart() {
			return 0;
		}

		@Override
		public String getText() {
			return text;
		}
	}

	private class myURLEntity implements URLEntity, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2193734453206352566L;
		private String url;

		public myURLEntity(String url) {
			this.url = url;
		}

		@Override
		public String getDisplayURL() {
			return url;
		}

		@Override
		public int getEnd() {
			return 0;
		}

		@Override
		public String getExpandedURL() {
			return url;
		}

		@Override
		public int getStart() {
			return 0;
		}

		@Override
		public String getText() {
			return url;
		}

		@Override
		public String getURL() {
			return url;
		}
	}

	private class myUserMentionEntity implements UserMentionEntity,
			Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8001047871540572401L;
		private long id;
		private String name;

		public myUserMentionEntity(long id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public int getEnd() {
			return 0;
		}

		@Override
		public long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getScreenName() {
			return name;
		}

		@Override
		public int getStart() {
			return 0;
		}

		@Override
		public String getText() {
			return null;
		}
	}

	private class MySymbolEntity implements SymbolEntity {
		private String text;

		public MySymbolEntity(String symbol) {
			text = symbol;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public int getEnd() {
			return 0;
		}

		@Override
		public int getStart() {
			return 0;
		}

	}
}
