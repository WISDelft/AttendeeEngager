/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014 Delft University of Technology Web Information Systems
 */
@Entity
@Bindable
@Portable
public class TwitterMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * The user to which the message should be sent.
	 */
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_ID", nullable = false, updatable = false)
	private TwitterUser user;
	/**
	 * Message to be send to the Twitter user
	 */
	@NotNull
	private String message;
	/**
	 * Datetime when the message was created
	 */
	private Date dateCreated;
	/**
	 * Datetime when the message was send to the Twitter user
	 */
	private Date dateSend;
	/**
	 * Datetime after which the message should be send
	 */
	private Date dateSendAfter;
	/**
	 * Indicates that the message should be send as a direct message. False means
	 * send as a tweet mentioning the user.
	 */
	private boolean sendAsDirectMessage;
	/**
	 * Indicates whether the check if the message is allowed to be send should be
	 * performed or overridden.
	 */
	private boolean overrideAllowedToSendCheck;
	/**
	 * If non-null this is the EngagementStatus that should be set for the user
	 * when this message is successfully send.
	 */
	private EngagementStatus userEngagementStatusOnSuccessfulSend = null;
	/**
	 * Indicates how many times the message was unsuccessfully sent
	 */
	private int retryCount;

	/**
	 * Required for ORM
	 */
	public TwitterMessage() {

	}

	public TwitterMessage(TwitterUser user, String message, boolean sendAsDirectMessage) {
		this.user = user;
		this.message = message;
		this.sendAsDirectMessage = sendAsDirectMessage;
	}

	public TwitterMessage(TwitterUser user, String message) {
		this(user, message, false);
	}

	@PrePersist
	void createdAt() {
		this.dateCreated = new Date();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateSend() {
		return dateSend;
	}

	public void setDateSend(Date dateSend) {
		this.dateSend = dateSend;
	}

	public Date getDateSendAfter() {
		return dateSendAfter;
	}

	public void setDateSendAfter(Date dateSendAfter) {
		this.dateSendAfter = dateSendAfter;
	}

	public TwitterUser getUser() {
		return user;
	}

	public void setUser(TwitterUser user) {
		this.user = user;
	}

	public boolean isSendAsDirectMessage() {
		return sendAsDirectMessage;
	}

	public void setSendAsDirectMessage(boolean sendAsDirectMessage) {
		this.sendAsDirectMessage = sendAsDirectMessage;
	}

	public boolean isOverrideAllowedToSendCheck() {
		return overrideAllowedToSendCheck;
	}

	public void setOverrideAllowedToSendCheck(boolean overrideAllowedToSendCheck) {
		this.overrideAllowedToSendCheck = overrideAllowedToSendCheck;
	}

	@Override
	public boolean equals(Object o) {
		// check for self-comparison
		if (this == o) return true;
		// use instanceof instead of getClass here for two reasons
		// 1. if need be, it can match any supertype, and not just one class;
		// 2. it renders an explict check for "that == null" redundant, since
		// it does the check for null already - "null instanceof [type]" always
		// returns false. (See Effective Java by Joshua Bloch.)
		if (!(o instanceof TwitterMessage)) return false;
		// cast to native object is now safe
		TwitterMessage other = (TwitterMessage) o;

		return this.id > 0 && other.id > 0 && this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id.hashCode();
	}

	@Override
	public String toString() {
		return "TwitterMessage {" + "id:" + id + ", user:" + user + ", message:" + message + ", dateSend:" + dateSend + "}";

	}

	public EngagementStatus getUserEngagementStatusOnSuccessfulSend() {
		return userEngagementStatusOnSuccessfulSend;
	}

	public void setUserEngagementStatusOnSuccessfulSend(EngagementStatus userEngagementStatusOnSuccessfulSend) {
		this.userEngagementStatusOnSuccessfulSend = userEngagementStatusOnSuccessfulSend;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int increaseRetryCount() {
		retryCount++;
		return retryCount;
	}
}
