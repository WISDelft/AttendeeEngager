/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	 * Screenname or ID of the Twitter user
	 */
	@NotNull
	private String user;
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
	 * Indicates whether the message is allowed to be send. Null means not
	 * determined yet.
	 */
	private Boolean allowedToBeSend;
	/**
	 * Indicates that the message should be send as a direct message. False means
	 * send as a tweet mentioning the user.
	 */
	private boolean sendAsDirectMessage;

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

	public Boolean getAllowedToBeSend() {
		return allowedToBeSend;
	}

	public void setAllowedToBeSend(Boolean allowedToBeSend) {
		this.allowedToBeSend = allowedToBeSend;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isSendAsDirectMessage() {
		return sendAsDirectMessage;
	}

	public void setSendAsDirectMessage(boolean sendAsDirectMessage) {
		this.sendAsDirectMessage = sendAsDirectMessage;
	}
}
