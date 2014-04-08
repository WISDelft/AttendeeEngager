/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.net.URL;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import twitter4j.User;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@ApplicationScoped
public class Utility {

	private PropertiesConfiguration config;

	@Inject
	protected Logger logger;

	protected void forceDebug() {
		config.setProperty("debug", true);
	}

	@PostConstruct
	protected void initialize() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL path = loader.getResource("AttendeeEngagerConstants.properties");
		try {
			config = new PropertiesConfiguration(path);
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
		}
		catch (ConfigurationException ex) {
			logger.error("Could not load Properties file", ex);
		}
	}

	/**
	 * Parses the provided string into a Long
	 * 
	 * @param str
	 * @return The long value if the string contains a long and null otherwise
	 */
	public Long parseLong(String str) {
		Long l;
		try {
			l = Long.parseLong(str);
		}
		catch (NumberFormatException ex) {
			l = null;
		}
		return l;
	}

	/**
	 * Parses the provided string into a Long
	 * 
	 * @param str
	 * @return The long value if the string contains a long and null otherwise
	 */
	public Integer parseInt(String str) {
		Integer l;
		try {
			l = Integer.parseInt(str);
		}
		catch (NumberFormatException ex) {
			l = null;
		}
		return l;
	}

	/**
	 * Parses the provided string into a Boolean
	 * 
	 * @param str
	 * @return The boolean value if the string contains a boolean and null
	 *         otherwise
	 */
	public boolean parseBoolean(String str) {
		Boolean b;
		try {
			b = Boolean.parseBoolean(str);
		}
		catch (NumberFormatException ex) {
			b = null;
		}
		return b;
	}

	/**
	 * Creates a random UUID for the user.
	 * 
	 * @param user The user to calculate the dashboardpath for
	 * @return A new random UUID, or the existing dashboardPath if the user
	 *         already has one, or null if the user, user ID or user screenName is
	 *         null
	 */
	public String createDashboardPath(TwitterUser user) {
		if (user.getDashboardPath() != null) {
			return user.getDashboardPath();
		}
		if (user == null || user.getId() == null || user.getId() == 0 || user.getScreenName() == null) {
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		return uuid;
	}

	/**
	 * Retrieves the property from the configuration as a string
	 * 
	 * @param propertyName
	 * @return the property or en empty string if the property is not available
	 */
	public String getPropertyAsString(String propertyName) {
		return config.getString(propertyName, "");
	}

	/**
	 * Retrieves the property from the configuration as a string with the
	 * specified locale, or the default one if that does not exist.
	 * 
	 * @param propertyName
	 * @param locale
	 * @return the (lozalized) property or en empty string if both localized and
	 *         default are not available
	 */
	public String getPropertyAsString(String propertyName, String locale) {
		String localPropertyName = propertyName + "_" + locale;
		String val = getPropertyAsString(localPropertyName);
		if ("".equals(val)) {
			val = getPropertyAsString(propertyName);
		}
		return val;
	}

	/**
	 * Retrieves the property from the configuration as an integer
	 * 
	 * @param propertyName
	 * @return The integer value or null if the property does not exist or is no
	 *         integer
	 */
	public Integer getPropertyAsInt(String propertyName) {
		try {
			return config.getInteger(propertyName, null);
		}
		catch (ConversionException ex) {
			return null;
		}
	}

	/**
	 * Retrieves the property from the configuration as a boolean
	 * 
	 * @param propertyName
	 * @return the parsed value or false if not exists or no boolean value
	 */
	public boolean getPropertyAsBoolean(String propertyName) {
		try {
			return config.getBoolean(propertyName, false);
		}
		catch (ConversionException ex) {
			return false;
		}
	}

	/**
	 * Returns a filled TwitterUser object. The object is not persisted.
	 * 
	 * @param user Twitter4J User object
	 * @return
	 */
	public TwitterUser fromTwitter4JUser(User u) {
		TwitterUser user = new TwitterUser();
		user.setId(u.getId());
		user.setDateCreated(u.getCreatedAt());
		user.setDescription(u.getDescription());
		user.setFollowerCount(u.getFollowersCount());
		user.setFriendsCount(u.getFriendsCount());
		user.setLang(u.getLang());
		user.setLocation(u.getLocation());
		user.setProtectedAccount(u.isProtected());
		user.setScreenName(u.getScreenName());
		user.setURL(u.getURL());
		return user;
	}
}
