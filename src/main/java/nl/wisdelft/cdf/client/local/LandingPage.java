/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.inject.Inject;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Jasper Oosterman
 * @created Apr 1, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Page(path = "home", role = DefaultPage.class)
@Templated("LandingPage.html#app-template")
public class LandingPage extends Composite {
	@PageState
	private String errorText;

	@Inject
	@DataField
	Button dashboardSignin, twitterSignin;

	@Inject
	@DataField
	TextBox dashboardCode;

	@Inject
	private TranslationService translate;

	@Inject
	TransitionTo<UserDashboard> gotoDashboard;

	@Inject
	@DataField
	Anchor learnMore;

	@Inject
	@DataField
	NavBar navigationBar;

	@Inject
	Logger logger;

	@DataField
	Element errorMessageContainer = DOM.createDiv();

	@Inject
	@DataField
	InlineLabel errorMessage;

	@PageShown
	public void pageShown() {
		navigationBar.loadPreferedLanguage();
	}

	@PageShowing
	public void showing() {
		if (errorText != null && !errorText.isEmpty()) {
			// show error container
			// errorMessage.setText(errorText);
			errorMessageContainer.removeClassName("hide");
		}
	}

	@EventHandler("dashboardSignin")
	public void redirectToDashboard(ClickEvent e) {
		String code = dashboardCode.getText();
		if (code != null || !"".equals(code)) {
			ListMultimap<String, String> multimap = ArrayListMultimap.create();
			multimap.put("user", code);
			gotoDashboard.go(multimap);
		}
	}

	@EventHandler("twitterSignin")
	public void twitterSignin(ClickEvent e) {
		String home = Window.Location.getPath();
		// find the last slash
		home = home.substring(0, home.lastIndexOf("/"));
		String redirectTo = home + "/twittersignin";
		Window.Location.replace(redirectTo);
	}
}
