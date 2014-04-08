/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jasper Oosterman
 * @created Mar 3, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
public class LinkClicked {

	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * The link that was clicked
	 */
	private String link;
	/**
	 * The moment the link was clicked
	 */
	private Date dateClicked;
	private String sessionID;
	private String remoteAddr;
	private String remoteHost;

	/**
	 * Required for ORM
	 */
	@SuppressWarnings("unused")
	private LinkClicked() {

	}

	/**
	 * Initializes the object with the link and the current date
	 * 
	 * @param link
	 */
	public LinkClicked(String link) {
		this(link, new Date());
	}

	/**
	 * Initialized the object with the link and the provided date
	 * 
	 * @param link
	 * @param dateClicked
	 */
	public LinkClicked(String link, Date dateClicked) {
		this.link = link;
		this.dateClicked = dateClicked;
	}

	public String getLink() {
		return link;
	}

	public Date getDateClicked() {
		return dateClicked;
	}

	public Long getId() {
		return id;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setDateClicked(Date dateClicked) {
		this.dateClicked = dateClicked;
	}

	public String toString() {
		return gson.toJson(this);
	}
}
