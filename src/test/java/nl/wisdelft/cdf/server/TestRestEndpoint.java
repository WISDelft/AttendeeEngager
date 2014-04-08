/**
 * 
 */
package nl.wisdelft.cdf.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Ignore
public class TestRestEndpoint extends BaseTest {
	// private String server =
	// "http://crowdery.st.ewi.tudelft.nl:8080/attendee-engager/rest/twittermessages%s";
	private String server = "http://127.0.0.1:8888/attendee-engager/rest/twittermessages%s?gwt.codesvr=127.0.0.1:9997";

	@Test
	public void testGET() throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(String.format(server, "/1")).openConnection();
		con.setRequestMethod("GET");
		con.setDoInput(true);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null)
			System.out.println(line);
		in.close();
	}

	@Test
	public void testPOSTURL() throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(String.format(server, "/cdf")
				+ "&user=joosterman&message=testURL&sendAsDirectMessage=true").openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null)
			System.out.println(line);
		in.close();
	}

	@Test
	public void testPOSTJSON() throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(String.format(server, "")).openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		String message = "{\"user\":\"joosterman\",\"message\":\"testJSON\",\"sendAsDirectMessage\":true}";
		con.setRequestProperty("Content-Type", "application/json");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		writer.write(message);
		writer.newLine();
		writer.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null)
			System.out.println(line);
		in.close();
	}
}
