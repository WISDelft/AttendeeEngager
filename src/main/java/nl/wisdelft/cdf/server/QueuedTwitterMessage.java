/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import nl.wisdelft.cdf.client.shared.TwitterMessage;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
@NamedQuery(name = "allQueuedMessages", query = "SELECT q FROM QueuedTwitterMessage q")
public class QueuedTwitterMessage {
	@Id
	private Long id;

	@NotNull
	@OneToOne
	@MapsId
	private TwitterMessage message;

	/**
	 * Contains the reason why the message could not be send
	 */
	private String errorMessage;

	public QueuedTwitterMessage(TwitterMessage message, String errorMessage) {
		this.errorMessage = errorMessage;
		this.message = message;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TwitterMessage getMessage() {
		return message;
	}

	public void setMessage(TwitterMessage message) {
		this.message = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@PrePersist
	private void setIdToMessageId() {
		id = message.getId();
	}
}
