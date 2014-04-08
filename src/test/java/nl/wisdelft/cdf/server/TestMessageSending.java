/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jasper Oosterman
 * @created Feb 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Ignore
public class TestMessageSending {

	@Test
	public void sendTestMessageLocal() throws ClientProtocolException, IOException, URISyntaxException {
		String url = "http://127.0.0.1:8888/attendee-engager/rest/twittermessages";
		URIBuilder builder = new URIBuilder(url);
		builder.addParameter("gwt.codesvr", "127.0.0.1:9997");
		builder.addParameter("test", "true");
		builder.addParameter("user", "joosterman");
		builder.addParameter("message", "testmessage:" + new Date());

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(builder.build());

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
	}

	/**
	 * Tests whether a test message can be send on the live server. Test messages
	 * are always send to the test account (both Direct Message and Status Update.
	 * Current test user is 'CDFbot'. Server replies with HTTP 200 OK on
	 * successful send.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void sendTestMessageLive() throws ClientProtocolException, IOException, URISyntaxException {
		// base url for sending twitter messages
		String url = "http://citydatafusion.tudelft.nl:8080/attendee-engager/rest/twittermessages";
		URIBuilder builder = new URIBuilder(url);
		// add parameters
		builder.addParameter("test", "true");
		builder.addParameter("user", "joosterman");
		builder.addParameter("sendAsDirectMessage", "true");
		builder.addParameter("message", "testmessage:" + new Date());
		// create Request and POST object
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(builder.build());

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
	}

}
