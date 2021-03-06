package nl.wisdelft.cdf.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This is the entry point to the client portion of the web application. At
 * compile time, Errai finds the {@code @EntryPoint} annotation on this class
 * and generates bootstrap code that creates an instance of this class when the
 * page loads. This client-side bootstrap code will also call the
 * {@link #init()} method because it is annotated with the
 * {@code @PostConstruct} annotation.
 */
@EntryPoint
@Bundle("bundle.json")
public class App {

	@Inject
	private Navigation navigation;

	@PostConstruct
	private void init() {
		// This is specifying the relative path to the REST endpoint used to store
		// complaints on the server. When compiling the native mobile app of this
		// demo, this needs to be changed to an absolute URL.
		RestClient.setApplicationRoot("/attendee-engager/rest");
		RestClient.setJacksonMarshallingActive(true);
		// RestClient.setApplicationRoot("http://10.15.16.207:8080/errai-tutorial/rest");
		RootPanel.get("rootPanel").add(navigation.getContentPanel());
	}

}