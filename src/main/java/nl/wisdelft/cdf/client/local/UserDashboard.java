/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import java.util.List;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.EngagementStatus;
import nl.wisdelft.cdf.client.shared.Recommendation;
import nl.wisdelft.cdf.client.shared.TwitterUser;
import nl.wisdelft.cdf.client.shared.TwitterUserEndpoint;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author Jasper Oosterman
 * @created Feb 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */

@Page(path = "dashboard")
@Templated("UserDashboard.html#app-template")
public class UserDashboard extends Composite {
	@PageState
	private String user;

	private TwitterUser twitterUser;

	@Inject
	@DataField
	private InlineLabel screenName, screenNameBig, status, following, receivedRecommendations;

	@Inject
	@DataField
	Button optinBig, optoutBig, btnEnglish, btnItalian, optoutSmall;

	@DataField
	Element recommendationQuestionBig = DOM.createDiv();

	@DataField
	Element noRecommendationMessage = DOM.createDiv();

	@Inject
	@DataField
	NavBar navigationBar;

	private DateTimeFormat dayFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);

	@DataField
	FlowPanel recommendationList = new FlowPanel();

	@Inject
	Instance<RecommendationDividerWidget> dividerInstance;

	@Inject
	Instance<RecommendationWidget> recommendationInstance;

	@Inject
	private Logger logger;

	@Inject
	private Caller<TwitterUserEndpoint> endpoint;

	@Inject
	private TransitionTo<LandingPage> gotoLandingPage;

	@Inject
	private TranslationService translation;

	@PageShown
	public void pageShowing() {
		navigationBar.loadPreferedLanguage();
	}

	@PageShowing
	public void showing() {
		// initially hide the list
		recommendationList.setVisible(false);
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(com.google.gwt.http.client.Response response) {
				// 200 indicates the user was found
				if (response.getStatusCode() == 200) {
					twitterUser = MarshallingWrapper.fromJSON(response.getText(), TwitterUser.class);
					updateUserInformation();
					getRecommendations();
					// twitter user as retrieved succesfully, update the dashboard visited
					// count
					dashboardVisited();

				}
				else {
					Multimap<String, String> params = HashMultimap.create();
					params.put("errorText", "User unknown");
					gotoLandingPage.go(params);
				}
			}
		}).getByDashboardPath(user);

	}

	public void dashboardVisited() {
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(Response response) {
				logger.info("Dashboard visited updated");
			}
		}).dashboardVisited(twitterUser.getId());
	}

	/**
	 * Updates the communication language preference of the user. The locale of
	 * the page does not change.
	 * 
	 * @param e
	 */
	@EventHandler({ "btnEnglish", "btnItalian" })
	private void clickEnglish(ClickEvent e) {
		final Element button = e.getRelativeElement();
		String lang = button.getLang();
		twitterUser.setLangPreference(lang);
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(Response response) {
				updateCommunicationButtonStatus();
			}
		}).updateCommunicationLocale(twitterUser.getId(), lang);
	}

	public void updateCommunicationButtonStatus() {
		String lang = twitterUser.getLangPreference();
		// defaults to English
		if (lang == null || lang.equals("en")) {
			btnItalian.removeStyleName("btn-primary");
			btnItalian.addStyleName("btn-default");
			btnEnglish.removeStyleName("btn-default");
			btnEnglish.addStyleName("btn-primary");
		}
		else {
			btnItalian.removeStyleName("btn-default");
			btnItalian.addStyleName("btn-primary");
			btnEnglish.removeStyleName("btn-primary");
			btnEnglish.addStyleName("btn-default");
		}
	}

	public void updateUserInformation() {

		// set the screenname
		screenName.setText("@" + twitterUser.getScreenName());
		screenNameBig.setText("@" + twitterUser.getScreenName());

		logger.info("Following:" + twitterUser.isFollower());
		// set following
		if (twitterUser.isFollower()) {
			following.setText("yes");
		}
		else {
			following.setText("no");
		}
		// set the engagement status
		EngagementStatus es = twitterUser.getEngagementStatus();
		if (es == EngagementStatus.OPTED_IN) {
			status.setText("Opted in");
		}
		else if (es == EngagementStatus.OPTED_OUT) {
			status.setText("Opted out");
		}
		else {
			status.setText("Not chosen yet");
		}
		// only show the big box on top if we are not opted in
		if (es != EngagementStatus.OPTED_IN) {
			recommendationQuestionBig.removeClassName("hide");
		}
		else {
			recommendationQuestionBig.addClassName("hide");
		}
		// only show the big opt-out button is we are not opted-out
		optoutBig.setVisible(es != EngagementStatus.OPTED_OUT);
		// show the small opt-out button only when we are opted in
		optoutSmall.setVisible(es == EngagementStatus.OPTED_IN);

		updateCommunicationButtonStatus();
	}

	public void getRecommendations() {
		logger.info("Getting recommendations");
		endpoint.call(new RemoteCallback<List<Recommendation>>() {

			@Override
			public void callback(List<Recommendation> recommendations) {
				logger.info("Received " + recommendations.size() + " recommendations");
				receivedRecommendations.setText(Integer.toString(recommendations.size()));

				if (recommendations.size() == 0) {
					noRecommendationMessage.removeClassName("hide");
					recommendationList.setVisible(false);
				}
				else {
					noRecommendationMessage.addClassName("hide");
					createRecommendationList(recommendations);
					// show the list with the right content
					recommendationList.setVisible(true);
				}
			}
		}).getAllRecommendations(twitterUser.getId());
	}

	public void createRecommendationList(List<Recommendation> recs) {
		// empty the panel
		recommendationList.clear();
		String currentDay = null;
		RecommendationDividerWidget currentDivider = null;
		for (Recommendation rec : recs) {
			// get the day of the recommendation
			String strDay = dayFormat.format(rec.getDateSend());
			// check if we need to add a new header
			if (currentDay == null || !strDay.equals(currentDay)) {
				currentDivider = dividerInstance.get();
				currentDivider.setDate(rec.getDateSend());
				recommendationList.add(currentDivider);
				currentDay = strDay;
			}
			// add the recommendation
			RecommendationWidget rw = recommendationInstance.get();
			rw.setRecommendation(rec);
			recommendationList.add(rw);
			// increase the count
			currentDivider.incNrRecommendations();
		}
	}

	public void setStatus(EngagementStatus status) {
		endpoint.call(new ResponseCallback() {
			@Override
			public void callback(Response response) {
				if (response.getStatusCode() == 200) {
					logger.info("Status stored on server");
					updateUserInformation();
				}
				else {
					logger.warn("Status not stored on server");
				}
			}
		}).updateStatus(twitterUser.getId(), status.toString());
	}

	@EventHandler("optinBig")
	public void optin(ClickEvent e) {
		if (twitterUser.getEngagementStatus() != EngagementStatus.OPTED_IN) {
			twitterUser.setEngagementStatus(EngagementStatus.OPTED_IN);
			setStatus(EngagementStatus.OPTED_IN);
		}
	}

	@EventHandler({ "optoutBig", "optoutSmall" })
	public void optout(ClickEvent e) {
		if (twitterUser.getEngagementStatus() != EngagementStatus.OPTED_OUT) {
			twitterUser.setEngagementStatus(EngagementStatus.OPTED_OUT);
			setStatus(EngagementStatus.OPTED_OUT);
		}
	}
}
