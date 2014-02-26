/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import nl.wisdelft.cdf.client.shared.TwitterUser;

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

	/**
	 * Stores the given new TwitterUser in the database, assigning it a unique ID.
	 * When this method returns, the given entity object will have its ID property
	 * set.
	 * 
	 * @param entity The TwitterUser instance to store.
	 * @throws EntityExistsException If the given TwitterUser is already in the
	 *           database.
	 */
	public void create(TwitterUser entity) {
		em.persist(entity);
	}

	/**
	 * Updates the state of the given TwitterUser in the database.
	 * 
	 * @param id The unique identifier for the given TwitterUser.
	 * @param entity The TwitterUser to update the database with.
	 */
	public void update(Long id, TwitterUser entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	/**
	 * Removes the TwitterUser with the given ID from the database.
	 * 
	 * @param id The unique ID of the TwitterUser to delete. Must not be null.
	 * @throws IllegalArgumentException if {@code id} is null, or if there is no
	 *           TwitterUser with that ID in the database.
	 */
	public void delete(Long id) {
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
		return em.find(TwitterUser.class, id);
	}

	public TwitterUser getByScreenName(String screenName) {
		TwitterUser u;
		try {
			u = em.createNamedQuery("getUserByScreenName", TwitterUser.class).setParameter("screenName", screenName).getSingleResult();
		}
		catch (NoResultException ex) {
			// does not exists
			u = null;
		}
		return u;
	}
}
