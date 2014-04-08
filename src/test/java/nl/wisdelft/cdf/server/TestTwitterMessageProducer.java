/**
 * 
 */
package nl.wisdelft.cdf.server;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import junit.framework.Assert;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.Venue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

/**
 * @author Jasper Oosterman
 * @created Mar 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TestTwitterMessageProducer extends BaseTest {
	private TwitterMessageProducer producer;

	@Before
	public void setup() throws JMSException {
		producer = new TwitterMessageProducer();
		producer.logger = LoggerFactory.getLogger(TwitterMessageProducer.class);
		producer.utility = utility;
		producer.initialize();
		// mock the actual sending of messages to the queue
		producer.producer = Mockito.mock(MessageProducer.class);
		// Mock the the storing of messages in the DB
		producer.messageService = Mockito.mock(TwitterMessageService.class);

	}

	@After
	public void destroy() {
		producer.destroy();
	}

	@Test
	public void testActiveMQ() {
		Assert.assertNotNull(producer.connection);
		Assert.assertNotNull(producer.session);
		Assert.assertNotNull(producer.producer);
	}

	@Test
	public void testCreateMessage() throws JMSException {
		TextMessage message = producer.session.createTextMessage();
		Assert.assertNotNull(message);
	}

	@Test
	public void testLocalQueue() throws JMSException {
		Assert.assertTrue(producer.localMessageQueue.isEmpty());
		producer.queueMessageForSending(new TwitterMessage());
		Assert.assertTrue(producer.localMessageQueue.isEmpty());
	}

	@Test
	public void testClosedConnection() throws JMSException {
		Assert.assertTrue(producer.localMessageQueue.isEmpty());
		producer.connection.close();
		producer.queueMessageForSending(new TwitterMessage());
		Assert.assertFalse(producer.localMessageQueue.isEmpty());
	}

	@Test
	public void testClosedConnectionRestart() throws JMSException {
		testClosedConnection();
		producer.initialize();
		// again mock out the actual putting of the message in the queue
		producer.producer = Mockito.mock(MessageProducer.class);
		Assert.assertFalse(producer.localMessageQueue.isEmpty());
		producer.queueMessageForSending(new TwitterMessage());
		Assert.assertTrue(producer.localMessageQueue.isEmpty());
	}

	@Test
	public void testSendRecommendation() throws JMSException {
		Assert.assertTrue(producer.localMessageQueue.isEmpty());
		producer.connection.close();
		// Mock the getting retrieving of the venue
		RecommendationManager manager = Mockito.mock(RecommendationManager.class);
		Mockito.when(manager.getVenue("venueName")).thenReturn(new Venue("venueName", "name", "address", "url"));
		producer.recManager = manager;

		Recommendation rec = new Recommendation(158663891, "venueName", 1.0f);
		producer.processRecommendation(rec);
		Assert.assertFalse(producer.localMessageQueue.isEmpty());
		System.out.println(producer.localMessageQueue.peek());
	}
}
