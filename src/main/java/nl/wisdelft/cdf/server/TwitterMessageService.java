/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nl.wisdelft.cdf.client.shared.TwitterMessage;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * @organization Delft University of Technology Web Information Systems
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TwitterMessageService {
	/**
	 * A JPA EntityManager which is configured according to the
	 * {@code forge-default} persistence context defined in
	 * {@code /WEB-INF/persistence.xml}. Note that this field is not initialized
	 * by the application: it is injected by the EJB container.
	 */
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	/**
	 * Stores the given new TwitterMessage in the database, assigning it a unique
	 * ID. When this method returns, the given entity object will have its ID
	 * property set.
	 * 
	 * @param entity The TwitterMessage instance to store.
	 * @throws EntityExistsException If the given TwitterMessage is already in the
	 *           database.
	 */
	public void create(TwitterMessage entity) {
		em.persist(entity);
	}

	/**
	 * Updates the state of the given TwitterMessage in the database.
	 * 
	 * @param id The unique identifier for the given TwitterMessage.
	 * @param entity The TwitterMessage to update the database with.
	 */
	public void update(Long id, TwitterMessage entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	/**
	 * Removes the TwitterMessage with the given ID from the database.
	 * 
	 * @param id The unique ID of the TwitterMessage to delete. Must not be null.
	 * @throws IllegalArgumentException if {@code id} is null, or if there is no
	 *           TwitterMessage with that ID in the database.
	 */
	public void delete(Long id) {
		TwitterMessage uc = em.find(TwitterMessage.class, id);
		em.remove(uc);
	}

	/**
	 * Returns the TwitterMessage with the given unique ID.
	 * 
	 * @return The TwitterMessage with the given unique ID, or null if there is no
	 *         such TwitterMessage in the database.
	 * @throws IllegalArgumentException if {@code id} is null.
	 */
	public TwitterMessage getById(Long id) {
		return em.find(TwitterMessage.class, id);
	}
}
