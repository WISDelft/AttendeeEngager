/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Jasper Oosterman
 * @created Mar 25, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Entity
@Bindable
@Portable
public class Venue {

	@Id
	private String id;
	private String name;
	private String address;
	private String url;

	/**
	 * Required for ORM
	 */
	public Venue() {}

	public Venue(String id, String name, String address, String url) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.url = url;
	}

	public String getId() {
		return id;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
		if (!(o instanceof Venue)) return false;
		// cast to native object is now safe
		Venue other = (Venue) o;

		return this.id != null && other.id != null && this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id.hashCode();
	}

	@Override
	public String toString() {
		return "Venue {" + id + ", " + name + ", " + address + "}";
	}
}
