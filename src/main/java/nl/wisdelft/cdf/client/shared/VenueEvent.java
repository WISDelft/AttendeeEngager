/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Jasper Oosterman
 * @created Mar 28, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
@Bindable
@Portable
@JsonIgnoreProperties({ "recommendations" })
public class VenueEvent {

	@Id
	private String id;
	@ManyToOne
	@JoinColumn(name = "venueId", nullable = false)
	private Venue venue;
	private String name;
	private String url;
	private Date startTime;
	private Date endTime;
	@JsonIgnore
	@ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
	private List<Recommendation> recommendations = new ArrayList<Recommendation>();

	/**
	 * Required for ORM
	 */
	public VenueEvent() {}

	public VenueEvent(String id, Venue venue, String name, String url) {
		this.id = id;
		this.venue = venue;
		this.name = name;
		this.url = url;

	}

	public String getId() {
		return id;
	}

	public List<Recommendation> getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(List<Recommendation> recommendations) {
		this.recommendations = recommendations;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}
}
