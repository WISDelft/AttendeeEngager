/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.TwitterMessage;
import nl.wisdelft.cdf.client.shared.TwitterMessageEndpoint;
import nl.wisdelft.cdf.server.Sent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Jasper Oosterman
 * @created Feb 25, 2014 Delft University of Technology Web Information Systems
 */
@Page(role = DefaultPage.class)
@Templated("MessageForm.html#app-template")
@Dependent
public class MessageForm extends Composite {
	@Inject
	@AutoBound
	private DataBinder<TwitterMessage> twitterMessageBinder;

	@Inject
	@Bound
	@DataField
	private TextBox user;

	@Inject
	@Bound
	@DataField
	private TextBox message;

	@Inject
	@Bound(property = "sendAsDirectMessage")
	@DataField
	private CheckBox directMessage;

	@Inject
	@DataField
	private Button submit;

	@Inject
	@DataField
	FlexTable sentMessages;

	@Inject
	private Caller<TwitterMessageEndpoint> endpoint;

	@EventHandler("submit")
	private void onSubmit(ClickEvent e) {
		// Execute the REST call to store the message on the server
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(Response response) {
				if (response.getStatusCode() != Response.SC_CREATED) {
					Window.alert("Message could not be created. Error: " + response.getStatusText());
				}
			}
		}).create(twitterMessageBinder.getModel());
		e.preventDefault();
	}

	public void processMessage(@Observes @Sent TwitterMessage message) {
		int row = sentMessages.insertRow(1);
		sentMessages.setText(row, 0, message.getUser());
		sentMessages.setText(row, 1, message.getDateSend().toString());
		sentMessages.setText(row, 2, message.getMessage());
	}

	@PostConstruct
	private void buildSentMessagesTable() {
		int row = sentMessages.insertRow(0);
		sentMessages.setHTML(row, 0, "<b>User</b>");
		sentMessages.setHTML(row, 1, "<b>Created at</b>");
		sentMessages.setHTML(row, 2, "<b>Message</b>");

	}
}
