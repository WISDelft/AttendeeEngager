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
import javax.persistence.TypedQuery;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.Venue;
import nl.wisdelft.cdf.client.shared.VenueEvent;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014
 * @organization Delft University of Technology Web Information Systems
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class RecommendationService {
	/**
	 * A JPA EntityManager which is configured according to the
	 * {@code forge-default} persistence context defined in
	 * {@code /WEB-INF/persistence.xml}. Note that this field is not initialized
	 * by the application: it is injected by the EJB container.
	 */
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	/**
	 * Stores the given new Recommendation in the database, assigning it a unique
	 * ID. When this method returns, the given entity object will have its ID
	 * property set.
	 * 
	 * @param entity The Recommendation instance to store.
	 * @throws EntityExistsException If the given Recommendation is already in the
	 *           database.
	 */
	public void create(Recommendation entity) {
		em.persist(entity);
	}

	public void create(Venue entity) {
		em.persist(entity);
	}

	public void create(VenueEvent entity) {
		em.persist(entity);
	}

	/**
	 * Updates the state of the given Recommendation in the database.
	 * 
	 * @param id The unique identifier for the given Recommendation.
	 * @param entity The Recommendation to update the database with.
	 */
	public void update(Long id, Recommendation entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	public void update(String id, Venue entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	public void update(String id, VenueEvent entity) {
		entity.setId(id);
		entity = em.merge(entity);
	}

	/**
	 * Removes the Recommendation with the given ID from the database.
	 * 
	 * @param id The unique ID of the Recommendation to delete. Must not be null.
	 * @throws IllegalArgumentException if {@code id} is null, or if there is no
	 *           Recommendation with that ID in the database.
	 */
	public void delete(Long id) {
		Recommendation uc = em.find(Recommendation.class, id);
		em.remove(uc);
	}

	/**
	 * Returns the Recommendation with the given unique ID.
	 * 
	 * @return The Recommendation with the given unique ID, or null if there is no
	 *         such Recommendation in the database.
	 * @throws IllegalArgumentException if {@code id} is null.
	 */
	public Recommendation getById(Long id) {
		return em.find(Recommendation.class, id);
	}

	public Venue getVenueById(String id) {
		return em.find(Venue.class, id);
	}

	public VenueEvent getVenueEventById(String id) {
		return em.find(VenueEvent.class, id);
	}

	/**
	 * Returns all Recommendations given to a user sorted by date, newest first
	 * 
	 * @param userID
	 * @return
	 */
	public List<Recommendation> getByUser(long userID) {
		TypedQuery<Recommendation> q = em.createNamedQuery("getRecommendationByUserId", Recommendation.class);
		q.setParameter("userID", userID);
		return q.getResultList();
	}

	public List<Recommendation> getByUserAndVenue(long userID, String venueID) {
		TypedQuery<Recommendation> q = em.createNamedQuery("getRecByVenue", Recommendation.class);
		q.setParameter("userID", userID);
		q.setParameter("venueID", venueID);
		return q.getResultList();
	}
}
