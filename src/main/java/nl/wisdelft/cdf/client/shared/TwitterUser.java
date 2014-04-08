/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.hibernate.annotations.Index;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
@Bindable
@Portable
@NamedQueries({ @NamedQuery(name = "getUserByScreenName", query = "SELECT u FROM TwitterUser u WHERE u.screenName = :screenName"),
		@NamedQuery(name = "getUserByDashboardPath", query = "SELECT u FROM TwitterUser u WHERE u.dashboardPath = :dashboardPath"),
		@NamedQuery(name = "allUsers", query = "SELECT u FROM TwitterUser u") })
public class TwitterUser {
	/**
	 * Twitter ID of the user
	 */
	@Id
	private Long id;
	private String name;
	@Index(name = "screenNameIndex")
	private String screenName;
	private String location;
	private String description;
	private String url;
	private int followerCount;
	private int friendsCount;
	private Date dateCreated;
	private String lang;
	private boolean protectedAccount;
	@Index(name = "dashboardPathIndex")
	private String dashboardPath;
	private String langPreference;
	private String accessToken;
	private String accessTokenSecret;

	/**
	 * Date when we got the user information from Twitter
	 */
	private Date dateRetrievedFromTwitter;
	/**
	 * The current engagement status of the user
	 */
	private EngagementStatus engagementStatus;
	/**
	 * Date of the last message that was sent to the user
	 */
	private Date dateLastContacted;
	/**
	 * Whether the user follows our account. If true, than direct messages can be
	 * send.
	 */
	private boolean follower;

	/**
	 * Required for ORM
	 */
	public TwitterUser() {

	}

	public EngagementStatus getEngagementStatus() {
		return engagementStatus;
	}

	public void setEngagementStatus(EngagementStatus engagementStatus) {
		this.engagementStatus = engagementStatus;
	}

	public Date getDateLastContacted() {
		return dateLastContacted;
	}

	public void setDateLastContacted(Date dateLastContacted) {
		this.dateLastContacted = dateLastContacted;
	}

	public boolean isFollower() {
		return follower;
	}

	public void setFollower(boolean isFollower) {
		this.follower = isFollower;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String uRL) {
		url = uRL;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean isProtectedAccount() {
		return protectedAccount;
	}

	public void setProtectedAccount(boolean protectedAccount) {
		this.protectedAccount = protectedAccount;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateRetrievedFromTwitter() {
		return dateRetrievedFromTwitter;
	}

	public void setDateRetrievedFromTwitter(Date dateRetrievedFromTwitter) {
		this.dateRetrievedFromTwitter = dateRetrievedFromTwitter;
	}

	public String getDashboardPath() {
		return dashboardPath;
	}

	public void setDashboardPath(String dashboardPath) {
		this.dashboardPath = dashboardPath;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	@Override
	public String toString() {
		return "TwitterUser {" + "screenName:" + screenName + ", id:" + id + ", status:" + engagementStatus + "}";
	}

	public String getLangPreference() {
		return langPreference;
	}

	public void setLangPreference(String langPreference) {
		this.langPreference = langPreference;
	}
}
