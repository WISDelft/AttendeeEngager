/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class QueuedTwitterMessagesService {
	/**
	 * A JPA EntityManager which is configured according to the
	 * {@code forge-default} persistence context defined in
	 * {@code /WEB-INF/persistence.xml}. Note that this field is not initialized
	 * by the application: it is injected by the EJB container.
	 */
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	/**
	 * Stores the given new QueuedTwitterMessage in the database, assigning it a
	 * unique ID. When this method returns, the given entity object will have its
	 * ID property set.
	 * 
	 * @param entity The TwitterMessage instance to store.
	 * @throws EntityExistsException If the given TwitterMessage is already in the
	 *           database.
	 */
	public void create(QueuedTwitterMessage entity) {
		em.persist(entity);
	}

	/**
	 * Updates the state of the given QueuedTwitterMessage in the database.
	 * 
	 * @param id The unique identifier for the given QueuedTwitterMessage.
	 * @param entity The QueuedTwitterMessage to update the database with.
	 */
	public void update(Long id, QueuedTwitterMessage entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	/**
	 * Removes the QueuedTwitterMessage with the given ID from the database.
	 * 
	 * @param id The unique ID of the QueuedTwitterMessage to delete. Must not be
	 *          null.
	 * @throws IllegalArgumentException if {@code id} is null, or if there is no
	 *           QueuedTwitterMessage with that ID in the database.
	 */
	public void delete(Long id) {
		QueuedTwitterMessage uc = em.find(QueuedTwitterMessage.class, id);
		em.remove(uc);
	}

	/**
	 * Returns the QueuedTwitterMessage with the given unique ID.
	 * 
	 * @return The QueuedTwitterMessage with the given unique ID, or null if there
	 *         is no such QueuedTwitterMessage in the database.
	 * @throws IllegalArgumentException if {@code id} is null.
	 */
	public QueuedTwitterMessage getById(Long id) {
		return em.find(QueuedTwitterMessage.class, id);
	}

	public List<QueuedTwitterMessage> getAll() {
		return em.createNamedQuery("allQueuedMessages", QueuedTwitterMessage.class).getResultList();
	}
}
