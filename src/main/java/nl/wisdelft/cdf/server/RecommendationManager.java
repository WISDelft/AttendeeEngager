/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.client.shared.Venue;
import nl.wisdelft.cdf.client.shared.VenueEvent;
import nl.wisdelft.cdf.server.status.Created;
import nl.wisdelft.cdf.server.status.Sent;
import org.apache.commons.lang3.time.DateUtils;
import org.citydatafusion.cpsma.cpsma4mdw2014.Agents;
import org.citydatafusion.cpsma.cpsma4mdw2014.BlackboardException;
import org.citydatafusion.cpsma.cpsma4mdw2014.BlackboardMediator;
import org.citydatafusion.cpsma.cpsma4mdw2014.utilities.Recommendation;
import org.slf4j.Logger;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

/**
 * @author Jasper Oosterman
 * @created Feb 28, 2014
 * @organization Delft University of Technology - Web Information Systems Checks
 *               Twitter for replies (tweets containing our username), direct
 *               messages, and the followers.
 */
@Singleton
@Startup
public class RecommendationManager {

	private final String MODULE_ACTIVE_PROPERTY = "module_active_processRecommendations";

	@Inject
	private Utility utility;

	@Inject
	private TwitterUserService userService;

	@Inject
	private RecommendationService recService;

	@Inject
	public Logger logger;

	@Inject
	@Created
	Event<nl.wisdelft.cdf.client.shared.Recommendation> newRecommendation;

	@Inject
	@Sent
	Event<TwitterUser> newRecommendationsForUser;

	private SimpleDateFormat blackboardDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
		private static final long serialVersionUID = 1L;

		public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
			StringBuffer toFix = super.format(date, toAppendTo, pos);
			return toFix.insert(toFix.length() - 2, ':');
		};
	};

	private Map<String, Venue> venues;
	private Map<Venue, List<VenueEvent>> events;

	@PostConstruct
	public void inititialize() {
		// retrieve all the venues in the system
		if (!isActive()) {
			logger.info(RecommendationManager.class.getSimpleName() + " not started. Disabled in config.");
			return;
		}
		logger.info(RecommendationManager.class.getSimpleName() + " starting...");
		venues = getAllVenues();
		logger.info("Found " + venues.size() + " venues.");
	}

	@PreDestroy
	public void destroy() {
		logger.info(RecommendationManager.class.getSimpleName() + " stopped.");
	}

	public boolean isActive() {
		return utility.getPropertyAsBoolean(MODULE_ACTIVE_PROPERTY);
	}

	// @Schedule(persistent = false, hour = "*", minute = "50")
	// public void processRecommendationsOnce(Timer timer) {
	// getMorningRecommendations();
	// timer.cancel();
	// }

	/**
	 * Get the recommendation from the timeslot YESTERDAY 17.00 until NOW. The
	 * events are from TODAY.
	 */
	@Schedule(persistent = false, hour = "9")
	@Asynchronous
	private void getMorningRecommendations() {
		if (!isActive()) {
			return;
		}
		// determine the timeslot
		Calendar cal = Calendar.getInstance();
		// determine NOW
		Date now = cal.getTime();
		// initialize NOW values
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		// determine YESTERDAY 17.00
		cal.set(year, month, day, 17, 0, 0);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = cal.getTime();

		// determine eventStart :YESTERDAY 23.59
		cal.set(year, month, day, 23, 59, 59);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date eventStart = cal.getTime();
		// Determin eventEnd: TOMORROW 00.00
		cal.set(year, month, day, 0, 0, 0);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date eventEnd = cal.getTime();

		// get the events
		events = getAllVenueEvents(eventStart, eventEnd, 0);
		processRecommendations(yesterday, now);
	}

	/**
	 * Get the recommendation from the timeslot TODAY 09.00 until NOW. The events
	 * are from TODAY.
	 */
	@Schedule(persistent = false, hour = "17")
	@Asynchronous
	private void getEveningRecommendations() {
		if (!isActive()) {
			return;
		}
		// determine the timeslot
		Calendar cal = Calendar.getInstance();
		// determine NOW
		Date now = cal.getTime();
		// initialize NOW values
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		// determine NOW 09.00
		cal.set(year, month, day, 9, 0, 0);
		Date morning = cal.getTime();

		// determine eventStart :YESTERDAY 23.59
		cal.set(year, month, day, 23, 59, 59);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date eventStart = cal.getTime();
		// Determine eventEnd: TOMORROW 00.00
		cal.set(year, month, day, 0, 0, 0);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date eventEnd = cal.getTime();

		// get the events
		events = getAllVenueEvents(eventStart, eventEnd, 0);
		processRecommendations(morning, now);
	}

	public Venue getVenue(String venueID) {
		if (venues == null) {
			logger.warn("Venue info requested but venues are not yet retrieved from server");
			return null;
		}
		else {
			Venue v = venues.get(venueID);
			if (v == null) {
				logger.warn("Venue requested but unknown to the system: " + venueID);
			}
			return v;
		}
	}

	/**
	 * Gets the associated VenueEvents of the venue
	 * 
	 * @param venue
	 * @return A list (possibly empty) of VenueEvents belonging to the venue
	 */
	public List<VenueEvent> getVenueEvents(Venue venue) {
		List<VenueEvent> e = new ArrayList<VenueEvent>();

		if (events == null) {
			logger.warn("Venue info requested but venues are not yet retrieved from server");
		}
		else {
			if (events.get(venue) != null) {
				e = events.get(venue);
			}
			else {
				logger.warn("No events for venue found. Venue: " + venue);
			}
		}
		return e;
	}

	/**
	 * Retrieves and persists the venues
	 * 
	 * @return
	 */
	protected Map<String, Venue> getAllVenues() {
		Map<String, Venue> venues = new HashMap<String, Venue>();
		//@formatter:off
		String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix owl: <http://purl.org/NET/c4dm/event.owl#>\n"
				+ "prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> "
				+ "prefix dc: <http://purl.org/dc/terms/> "
				+ "prefix j.3: <http://www.w3.org/2006/time#> "
				+ "prefix j.4: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
				+ "SELECT ?venueid ?namevenue ?addressvenue ?venueurl "
				+ "WHERE { "
				+ "?venueid rdf:type cse:Venue ; "
				+ "cse:name ?namevenue ; "
				+ "cse:address ?addressvenue ; "
				+ "cse:url ?venueurl. "
				+ "}";
		//@formatter:on

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/query", q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			String venueID = soln.getResource("venueid").getURI();
			String name = soln.getLiteral("namevenue").getString();
			String address = soln.getLiteral("addressvenue").getString();
			String url = soln.getLiteral("venueurl").getString();
			Venue v = new Venue(venueID, name, address, url);
			venues.put(venueID, v);
			// check if the venue already exists in the DB
			if (recService.getVenueById(venueID) == null) {
				recService.create(v);
			}
			else {
				// recService.update(venueID, v);
			}
		}
		return venues;
	}

	protected Map<Venue, List<VenueEvent>> getAllVenueEvents(Date from, Date to, int intervalHours) {
		Map<Venue, List<VenueEvent>> events = new HashMap<Venue, List<VenueEvent>>();
		if (to == null) {
			to = new Date(from.getTime() + (intervalHours * 60 * 60 * 1000));
		}
		//@formatter:off
		String queryString = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> "
				+ "prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> "
				+ "prefix sioc: <http://rdfs.org/sioc/ns#> "
				+ "prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> "
				+ "prefix dc: <http://purl.org/dc/terms/> "
				+ "prefix event: <http://purl.org/NET/c4dm/event.owl#> "
				+ "prefix tl: <http://www.w3.org/2006/time#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix venue: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/venue/> "
				+ "SELECT ?venue ?namevenue ?addressvenue ?event ?name ?eventURL ?startTime ?endTime "
				+ "WHERE { "
				+ "?venue cse:is_location_of ?event ;"
				+ "cse:name ?namevenue ; "
				+ "cse:address ?addressvenue . "
				+ "?event cse:name ?name ; "
				+ "cse:url ?eventURL ; "
				+ "event:time [ "
				+ "a tl:Interval ; "
				+ "tl:hasBeginning [ tl:inXSDDateTime ?startTime ] ; "
				+ "tl:hasEnd [ tl:inXSDDateTime ?endTime ] "
				+ "] . "
				+ "FILTER (?startTime >= \""+blackboardDateFormat.format(from) +"\"^^xsd:dateTime && ?endTime <= \""+blackboardDateFormat.format(to)+"\"^^xsd:dateTime ) " + "}";
		//@formatter:on
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		logger.info("Getting all events from '" + from + "' to '" + to + "'");
		QueryExecution qexec = QueryExecutionFactory.createServiceRequest("http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/query",
				query);

		ResultSet r = qexec.execSelect();
		int nrEvents = 0;
		while (r.hasNext()) {
			QuerySolution soln = r.nextSolution();
			VenueEvent event = new VenueEvent();
			event.setId(soln.getResource("event").getURI());
			event.setName(soln.getLiteral("name").getString());
			event.setUrl(soln.getLiteral("eventURL").getString());
			Date startTime = ((XSDDateTime) soln.getLiteral("startTime").getValue()).asCalendar().getTime();
			event.setStartTime(startTime);
			Date endTime = ((XSDDateTime) soln.getLiteral("endTime").getValue()).asCalendar().getTime();
			event.setEndTime(endTime);

			Venue venue = getVenue(soln.getResource("venue").toString());
			event.setVenue(venue);
			// initialize the container
			if (!events.containsKey(venue)) {
				events.put(venue, new ArrayList<VenueEvent>());
			}
			// add the event to the container
			events.get(venue).add(event);
			// check if the venueEvent already exists in the DB
			if (recService.getVenueEventById(event.getId()) == null) {
				recService.create(event);
			}
			else {
				// recService.update(event.getId(), event);
			}

			nrEvents++;
		}

		logger.info("Retrieved " + nrEvents + " events for " + events.size() + " venues");
		return events;
	}

	/**
	 * Returns recommendations between a certain time period. Recommendations are
	 * not persisted.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	protected Map<Long, List<nl.wisdelft.cdf.client.shared.Recommendation>> getRecommendationsFromDB(Date from, Date to) {
		BlackboardMediator myBB = new BlackboardMediator(Agents.VVR);
		// Map based storage for recommendation. Key is the userID, value a list
		// of recommendations.
		Map<Long, List<nl.wisdelft.cdf.client.shared.Recommendation>> recommendations = new HashMap<Long, List<nl.wisdelft.cdf.client.shared.Recommendation>>();
		int nrRecommendations = 0;
		// first get all the recommendations from the graph
		try {
			// Get the "data" which is in the interval
			List<String> graphs = myBB.getRecentGraphNames(from, to);
			for (String name : graphs) {
				String model = myBB.getGraph(name);
				// only process correct models
				if (model == null || model.startsWith("Error 404")) {
					continue;
				}
				// Retrieve the recommendations contained in the graph
				Map<Long, Recommendation> recommendation = myBB.retrieveAllRecommendations(model);

				if (recommendation == null) {
					continue;
				}
				logger.debug("Graph " + name + " contained " + recommendation.size() + " recommendations");
				Iterator<Entry<Long, Recommendation>> it = recommendation.entrySet().iterator();
				while (it.hasNext()) {
					// increase the rec counter
					nrRecommendations++;
					Entry<Long, Recommendation> entry = it.next();
					long userID = entry.getKey();
					String venueName = entry.getValue().getObjectName();
					float probability = entry.getValue().getRecommandationProbability();
					// create Recommendation and add it to the list
					nl.wisdelft.cdf.client.shared.Recommendation rec = new nl.wisdelft.cdf.client.shared.Recommendation(userID, venueName, probability);

					if (!recommendations.containsKey(userID)) {
						recommendations.put(userID, new ArrayList<nl.wisdelft.cdf.client.shared.Recommendation>());
					}
					recommendations.get(userID).add(rec);
				}
			}
		}
		catch (BlackboardException ex) {
			logger.error("Could not get recommendations from Blackboard.", ex);
		}

		logger.info("Retrieved " + nrRecommendations + " recommendations for " + recommendations.size() + " users");
		return recommendations;
	}

	/**
	 * Retrieves the recommendations that are created after
	 * <code>Date.now() - timeout</code>. Recommendations for users that are not
	 * ENGAGED are not send.
	 */
	protected void processRecommendations(Date from, Date to) {
		logger.info("Getting all recommendations from '" + from + "' to '" + to + "'");
		// use this date for every recommendation in this batch
		Date now = new Date();

		// get the recommendations
		Integer maxUserRecommendationPerBatch = utility.getPropertyAsInt("maxUserRecommendationPerBatch");
		if (maxUserRecommendationPerBatch == null) maxUserRecommendationPerBatch = 3;
		Map<Long, List<nl.wisdelft.cdf.client.shared.Recommendation>> recommendations = getRecommendationsFromDB(from, to);

		for (Long userID : recommendations.keySet()) {
			// get the recommendations for this user
			List<nl.wisdelft.cdf.client.shared.Recommendation> recs = recommendations.get(userID);
			// in test mode map users onto testusers
			long tempUserID = userID;
			if (utility.getPropertyAsBoolean("test")) {
				tempUserID = getMappedDebugUser(userID);
				if (tempUserID != userID) logger.info("Testing: User " + userID + " remapped to: " + tempUserID);
			}

			// get the user
			TwitterUser user = userService.getById(tempUserID);
			if (user == null) {
				logger.debug("Recommendation not sent to user with ID: " + tempUserID + ". User not in DB");
				continue;
			}
			// check if the user is ENGAGED
			if (user.getEngagementStatus() != EngagementStatus.OPTED_IN) {
				logger.debug("Recommendation not sent to user with ID: " + tempUserID + ". User engagement status is not "
						+ EngagementStatus.OPTED_IN + " but " + user.getEngagementStatus());
				continue;
			}
			// We can send the recommendations!
			// Remove duplicates from the recommendations
			recs = distinct(recs);
			// Remove all recommendations that already have been sent today
			List<nl.wisdelft.cdf.client.shared.Recommendation> previousRecs = recService.getByUser(tempUserID);
			recs = filterSentToday(recs, previousRecs);
			Collections.sort(recs, Collections.reverseOrder());
			logger.info("Removing duplicated and already sent Recommendations leaves " + recs.size() + " recs for: " + tempUserID);
			// send the recommendations (at most maxUserRecommendationPerBatch)
			for (int i = 0; i < maxUserRecommendationPerBatch && i < recs.size(); i++) {
				nl.wisdelft.cdf.client.shared.Recommendation rec = recs.get(i);
				// assigns the recommendations to the test user in test mode
				if (utility.getPropertyAsBoolean("test")) {
					rec.setUserID(tempUserID);
				}

				// recommendations will be send to the user.
				// update additional info
				Venue venue = getVenue(rec.getVenueID());
				rec.setVenue(venue);
				List<VenueEvent> events = getVenueEvents(venue);
				rec.setEvents(events);
				// update the user in the recommendation to the screenName
				rec.setUser(user);
				// update the sent date
				rec.setDateSend(now);
				recService.create(rec);
				logger.info("Recommendation stored in DB. Firing event. " + rec);
				// fire the event
				newRecommendation.fire(rec);

			}
			// Notify him of new recommendations.
			if (recs.size() > 0) {
				newRecommendationsForUser.fire(user);
			}
		}
	}

	/**
	 * Removes all recommendations already send today based on the previous
	 * recommendations
	 * 
	 * @param recs
	 * @param previousRecs
	 * @return
	 */
	private List<nl.wisdelft.cdf.client.shared.Recommendation> filterSentToday(List<nl.wisdelft.cdf.client.shared.Recommendation> recs, List<nl.wisdelft.cdf.client.shared.Recommendation> previousRecs) {
		Date today = new Date();
		Set<String> recommendedVenuesToday = new HashSet<String>();
		// find all the recommendations sent today
		for (nl.wisdelft.cdf.client.shared.Recommendation previousRec : previousRecs) {
			if (DateUtils.isSameDay(previousRec.getDateSend(), today)) {
				recommendedVenuesToday.add(previousRec.getVenueID());
			}
		}
		// filter the current recommendation
		List<nl.wisdelft.cdf.client.shared.Recommendation> filteredRecs = new ArrayList<nl.wisdelft.cdf.client.shared.Recommendation>();
		for (nl.wisdelft.cdf.client.shared.Recommendation rec : recs) {
			if (!recommendedVenuesToday.contains(rec.getVenueID())) {
				filteredRecs.add(rec);
			}
		}
		// return the filtered recs
		return filteredRecs;
	}

	/**
	 * Returns a list of recommendations that does not contains duplicate venues
	 * 
	 * @return
	 */
	private List<nl.wisdelft.cdf.client.shared.Recommendation> distinct(List<nl.wisdelft.cdf.client.shared.Recommendation> recs) {
		List<nl.wisdelft.cdf.client.shared.Recommendation> distinct = new ArrayList<nl.wisdelft.cdf.client.shared.Recommendation>();
		Set<String> venues = new HashSet<String>();
		for (nl.wisdelft.cdf.client.shared.Recommendation rec : recs) {
			if (!venues.contains(rec.getVenueID())) {
				venues.add(rec.getVenueID());
				distinct.add(rec);
			}
		}
		return distinct;
	}

	private Long getMappedDebugUser(Long userID) {
		Map<Long, Long> mapping = new HashMap<Long, Long>();
		// map onto emmanuel
		// mapping.put(98217227l, 108888655l);
		// map onto stefano
		// mapping.put(479986377l, 92023570l);
		// map onto marco
		// mapping.put(47492957l, 82162496l);
		// map onto jasper
		mapping.put(465051930l, 158663891l);
		// map onto Alessandro
		// mapping.put(350162438l, 14049302l);

		if (mapping.containsKey(userID)) {
			return mapping.get(userID);
		}
		else {
			return userID;
		}
	}
}
