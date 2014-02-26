/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
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
@NamedQuery(name = "getUserByScreenName", query = "SELECT u FROM TwitterUser u WHERE u.screenName = :screenName")
public class TwitterUser {
	@Id
	private Long id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String URL;
	private int followerCount;
	private int friendsCount;
	private Date dateCreated;
	private String lang;
	private boolean protectedAccount;
	private Date dateRetrievedFromTwitter;

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
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
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
}
