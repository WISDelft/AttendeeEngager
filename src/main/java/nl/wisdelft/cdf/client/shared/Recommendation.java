/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014 Delft University of Technology Web Information Systems
 */
@Entity
@Bindable
@Portable
@NamedQueries({
		@NamedQuery(
				name = "getRecommendationByUserId",
				query = "SELECT r FROM Recommendation r WHERE r.userID = :userID ORDER BY r.dateSend DESC"),
		@NamedQuery(name = "getRecByVenue", query = "SELECT r FROM Recommendation r WHERE r.userID = :userID AND r.venueID = :venueID") })
public class Recommendation implements Comparable<Recommendation> {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_ID", nullable = false, updatable = false)
	private TwitterUser user;

	/**
	 * Used on the initial creation of the Recommendation.
	 */
	private long userID;

	private String venueID;
	@ManyToOne(optional = true)
	@JoinColumn(name = "venue_ID", nullable = true, updatable = false)
	private Venue venue;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable
	private List<VenueEvent> events = new ArrayList<VenueEvent>();

	private float probability;
	/**
	 * Date the recommendation was created and sent to the user
	 */
	private Date dateSend;

	/**
	 * Feedback of the user about the recommendation
	 */
	private int feedback = 0;

	/**
	 * Number of times the recommendation was clicked
	 */
	private int timesClicked = 0;

	/**
	 * Number of times the page of the recommended venue was visited
	 */
	private int timesPageVisited = 0;

	/**
	 * Required for ORM
	 */
	public Recommendation() {

	}

	public Recommendation(long userID, String venueID, float probability) {
		this.userID = userID;
		this.venueID = venueID;
		this.probability = probability;
	}

	public TwitterUser getUser() {
		return user;
	}

	public int getTimesClicked() {
		return timesClicked;
	}

	public void setTimesClicked(int timesClicked) {
		this.timesClicked = timesClicked;
	}

	public void incTimesClicked() {
		this.timesClicked++;
	}

	public int getTimesPageVisited() {
		return timesPageVisited;
	}

	public void setTimesPageVisited(int timesPageVisited) {
		this.timesPageVisited = timesPageVisited;
	}

	public void incTimesPageVisited() {
		this.timesPageVisited++;
	}

	public int getFeedback() {
		return feedback;
	}

	public void setFeedback(int feedback) {
		this.feedback = feedback;
	}

	public void setUser(TwitterUser user) {
		this.user = user;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getVenueID() {
		return venueID;
	}

	public void setVenueID(String venueID) {
		this.venueID = venueID;
	}

	public float getProbability() {
		return probability;
	}

	public void setProbability(float probability) {
		this.probability = probability;
	}

	public Date getDateSend() {
		return dateSend;
	}

	public void setDateSend(Date dateSend) {
		this.dateSend = dateSend;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public List<VenueEvent> getEvents() {
		return events;
	}

	public void setEvents(List<VenueEvent> events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "Recommendation {user: " + userID + ", venue: " + venueID + "}";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Recommendation o) {
		return Float.compare(this.probability, o.probability);
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
		if (!(o instanceof Recommendation)) return false;
		// cast to native object is now safe
		Recommendation other = (Recommendation) o;

		return this.id > 0 && other.id > 0 && this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id.hashCode();
	}
}