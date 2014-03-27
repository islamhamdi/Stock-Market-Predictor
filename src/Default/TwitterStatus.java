package Default;

import java.io.Serializable;
import java.util.Date;

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

public class TwitterStatus implements Status, Serializable {
	private static final long serialVersionUID = 3987387094467681107L;
	/**
	 * 
	 */
	private long tweetID;
	private String source;
	private String text;
	private Date date;
	private User user;
	private Status retweetedStatus;
	private HashtagEntity[] hashtagEntities;
	private SymbolEntity[] symbolEntities;
	private URLEntity[] URLEntities;
	private UserMentionEntity[] UserMentionEntities;

	public TwitterStatus(Status status, String[] URLs) {
		this.tweetID = status.getId();
		this.source = status.getSource();
		this.text = status.getText();
		this.date = status.getCreatedAt();
		this.user = status.getUser();
		this.retweetedStatus = status.getRetweetedStatus();
		this.hashtagEntities = status.getHashtagEntities();
		this.UserMentionEntities = status.getUserMentionEntities();
		this.symbolEntities = status.getSymbolEntities();

		URLEntities = new myURLEntity[URLs.length];
		for (int i = 0; i < URLs.length; i++)
			URLEntities[i] = new myURLEntity(URLs[i]);
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
		return null;
	}

	@Override
	public int getRetweetCount() {
		return 0;
	}

	@Override
	public Status getRetweetedStatus() {
		return this.retweetedStatus;
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
		return this.retweetedStatus != null;
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
}
