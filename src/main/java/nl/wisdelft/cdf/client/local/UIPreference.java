/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Jasper Oosterman
 * @created Mar 28, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
public class UIPreference {
	/**
	 * There is only one UIPreference for each client
	 */
	@Id
	private int id = 1;

	private String locale;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
