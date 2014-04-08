/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import org.slf4j.Logger;
import twitter4j.TwitterException;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TwitterUserService {
	/**
	 * A JPA EntityManager which is configured according to the
	 * {@code forge-default} persistence context defined in
	 * {@code /WEB-INF/persistence.xml}. Note that this field is not initialized
	 * by the application: it is injected by the EJB container.
	 */
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@Inject
	private Utility utility;
	@Inject
	private TwitterConnection twitter;

	@Inject
	private Logger logger;

	/**
	 * Stores the given new TwitterUser in the database. The provided user should
	 * have an ID and not already be persisted.
	 * 
	 * @param entity The TwitterUser instance to store.
	 * @return The newly persisted TwitterUser or the persisted entity if it
	 *         existed
	 * @throws EntityExistsException if the user with that ID exists in the DB.
	 */
	public synchronized TwitterUser create(TwitterUser user) {
		logger.debug("Creating user by entity: " + user + "...");
		if (user == null) {
			logger.warn("Tried to create a <null> user");
			return null;
		}
		// check if the user has an ID
		if (user.getId() == null) {
			logger.warn("Tried to create a user with a <null> ID");
			return null;
		}
		// check if the user already exists in the persistence context
		if (em.contains(user)) {
			logger.warn("Tried to create an already persisted user");
			return null;
		}

		// it is a new user, update the dashboard, persist and fire a new user event
		user.setDashboardPath(utility.createDashboardPath(user));
		user.setEngagementStatus(EngagementStatus.NOT_CONTACTED);
		em.persist(user);
		em.flush();
		logger.info("User persisted:" + user);
		return user;
	}

	/**
	 * Retrieves the user from Twitter if a user with ScreenName or ID does not
	 * yet exists in the DB.
	 * 
	 * @param name ScreenName or ID
	 * @return The newly persisted TwitterUser, the existing persisted entity if
	 *         it already existed or null, if the user could not be found on
	 *         Twitter.
	 */

	public synchronized TwitterUser createFromScreenNameOrID(String name) throws TwitterException {
		logger.debug("Creating user via ScreenName or ID: " + name + "...");
		// check if the user already exists in the DB
		TwitterUser user = getByScreenNameOrID(name);
		if (user == null) {
			// the user does not exist yet in the DB, get it from twitter;
			user = twitter.getUser(name);
			if (user != null) {
				// user retrieved from Twitter, create and persist it
				logger.debug("User retrieved from Twitter: " + name);
				return create(user);
			}
			else {
				// user not found on Twitter, return null
				logger.info("User could not be found on twitter: " + name);
				return null;
			}
		}
		else {
			// user exists, return the DB version
			logger.warn("User already in DB: " + name);
			return null;
		}
	}

	/**
	 * Updates the state of the given TwitterUser in the database.
	 * 
	 * @param id The unique identifier for the given TwitterUser.
	 * @param entity The TwitterUser to update the database with.
	 */
	public synchronized void update(Long id, TwitterUser entity) {
		entity.setId(id);
		entity = em.merge(entity);
		em.flush();
	}

	/**
	 * Removes the TwitterUser with the given ID from the database.
	 * 
	 * @param id The unique ID of the TwitterUser to delete. Must not be null.
	 * @throws IllegalArgumentException if {@code id} is null, or if there is no
	 *           TwitterUser with that ID in the database.
	 */
	public synchronized void delete(Long id) {
		TwitterUser uc = em.find(TwitterUser.class, id);
		em.remove(uc);
	}

	/**
	 * Returns the TwitterUser with the given unique ID.
	 * 
	 * @return The TwitterUser with the given unique ID, or null if there is no
	 *         such TwitterUser in the database.
	 * @throws IllegalArgumentException if {@code id} is null.
	 */
	public TwitterUser getById(Long id) {
		logger.debug("Fetching DB user via ID: " + id);
		TwitterUser user = em.find(TwitterUser.class, id);
		if (user == null) {
			logger.debug("User not in DB: " + id);
		}
		else {
			logger.debug("User found in DB: " + id);
		}
		return user;
	}

	/**
	 * @param dashboardPath
	 * @return
	 */
	public TwitterUser getByDashboardPath(String dashboardPath) {
		logger.debug("Fetching DB user via DashboardPath: " + dashboardPath);
		TwitterUser u;
		try {
			u = em.createNamedQuery("getUserByDashboardPath", TwitterUser.class).setParameter("dashboardPath", dashboardPath).getSingleResult();
			logger.debug("User found in DB: " + dashboardPath);
		}
		catch (NoResultException ex) {
			u = null;
			logger.info("User not in DB: " + dashboardPath);
		}
		return u;
	}

	public TwitterUser getByScreenName(String screenName) {
		logger.debug("Fetching DB user via ScreenName: " + screenName);
		TwitterUser u;
		try {
			u = em.createNamedQuery("getUserByScreenName", TwitterUser.class).setParameter("screenName", screenName).getSingleResult();
			logger.debug("User found in DB: " + screenName);
		}
		catch (NoResultException ex) {
			u = null;
			logger.info("User not in DB: " + screenName);
		}
		return u;
	}

	/**
	 * Helper method to determine whether the provided name is an ID or ScreenName
	 * and gets the TwitterUser accordingly
	 * 
	 * @param name
	 * @return
	 */
	public TwitterUser getByScreenNameOrID(String name) {
		Long id = utility.parseLong(name);
		if (id != null) {
			// it was an ID
			return getById(id);
		}
		else {
			// it was a ScreenName
			return getByScreenName(name);
		}
	}

	public List<TwitterUser> getAll() {
		return em.createNamedQuery("allUsers", TwitterUser.class).getResultList();
	}
}
