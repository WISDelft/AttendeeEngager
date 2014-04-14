/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.Venue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * @author Jasper Oosterman
 * @created Mar 24, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TestRecommendations extends BaseTest {

	private RecommendationManager manager;
	SimpleDateFormat blackboardDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Before
	public void setup() {
		manager = new RecommendationManager();
		manager.logger = LoggerFactory.getLogger(RecommendationManager.class);
	}

	@Test
	public void getRecommendations() throws ParseException {
		// get the recommendations
		Date to = Calendar.getInstance().getTime();
		Date from = null;
		from = blackboardDateFormat.parse("2013-03-19T00:00:00+0200");

		Map<Long, List<Recommendation>> recs = manager.getRecommendationsFromDB(from, to);
		Assert.assertNotNull(recs);
		Assert.assertTrue(recs.size() > 0);
	}

	@Test
	public void getAllVenues() {
		Map<String, Venue> venues = manager.getAllVenues();
		Assert.assertNotNull(venues);
		Assert.assertTrue(venues.size() > 0);
	}

	@Test
	public void getEvents() {
		Date someTime = new Date(1365670800000l);
		manager.getAllVenueEvents(someTime, null, 24);
	}
}
