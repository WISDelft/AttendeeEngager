/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.TwitterUserEndpoint;
import nl.wisdelft.cdf.client.shared.VenueEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author Jasper Oosterman
 * @created Mar 31, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Templated("UserDashboard.html#recommendation-template")
public class RecommendationWidget extends Composite {
	@Inject
	@DataField
	InlineLabel recVenueName;

	@Inject
	@DataField
	InlineLabel recVenueAddress;

	@Inject
	@DataField
	Anchor collapseLink, visitPage;

	@DataField
	Element collapsePanel = DOM.createDiv();

	@Inject
	@DataField
	Button voteUp, voteDown;

	@Inject
	@DataField
	ListWidget<VenueEvent, VenueEventWidget> venueEventListWidget;

	@Inject
	private Caller<TwitterUserEndpoint> endpoint;

	private Recommendation rec;

	protected void init() {
		if (rec.getVenue() != null) {
			recVenueName.setText(rec.getVenue().getName());
			recVenueAddress.setText(rec.getVenue().getAddress());
			visitPage.setHref(rec.getVenue().getUrl());
		}
		// get unique id
		String unique = HTMLPanel.createUniqueId();
		collapsePanel.setId(unique);
		collapseLink.setHref("#" + unique);
		venueEventListWidget.setItems(rec.getEvents());
		updateFeedbackButtons();
	}

	private void syncRecommendationOnServer() {
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(com.google.gwt.http.client.Response response) {}
		}).updateRecommendation(rec.getUserID(), rec.getId(), rec);
	}

	public void updateFeedbackButtons() {
		if (rec.getFeedback() == 1) {
			voteDown.removeStyleName("btn-danger");
			voteDown.addStyleName("btn-default");
			voteUp.removeStyleName("btn-default");
			voteUp.addStyleName("btn-success");
		}
		else if (rec.getFeedback() == -1) {
			voteDown.removeStyleName("btn-default");
			voteDown.addStyleName("btn-danger");
			voteUp.removeStyleName("btn-success");
			voteUp.addStyleName("btn-default");
		}
	}

	@EventHandler("voteUp")
	public void voteUp(ClickEvent e) {
		rec.setFeedback(1);
		updateFeedbackButtons();
		syncRecommendationOnServer();

	}

	@EventHandler("voteDown")
	public void voteDown(ClickEvent e) {
		rec.setFeedback(-1);
		updateFeedbackButtons();
		syncRecommendationOnServer();
	}

	@EventHandler("visitPage")
	public void visitPage(ClickEvent e) {
		rec.incTimesPageVisited();
		syncRecommendationOnServer();
		Window.open(rec.getVenue().getUrl(), "_blank", "");
	}

	@EventHandler("collapseLink")
	public void expandPanel(ClickEvent e) {
		rec.incTimesClicked();
		syncRecommendationOnServer();
	}

	public void setRecommendation(Recommendation rec) {
		this.rec = rec;
		if (rec != null) {
			init();
		}
	}

}
