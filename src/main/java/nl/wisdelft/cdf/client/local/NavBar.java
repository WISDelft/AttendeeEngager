/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import nl.wisdelft.cdf.client.local.status.LanguageChanged;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Jasper Oosterman
 * @created Mar 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Templated("UserDashboard.html#navigation-template")
public class NavBar extends Composite {
	@Inject
	private LocaleSelector selector;

	@Inject
	@DataField
	private Anchor italian;

	@Inject
	@DataField
	private Anchor english;

	@Inject
	private Logger logger;

	@Inject
	@LanguageChanged
	Event<UIPreference> languageChanged;

	private UIPreference pref;

	@Inject
	private EntityManager em;

	public void loadPreferedLanguage() {
		// load the UIPreference (always id=1)
		pref = em.find(UIPreference.class, 1);
		// create if not exists
		if (pref == null) {
			// create a new UIPreference and persist it
			pref = new UIPreference();
			pref.setLocale("en");
			em.persist(pref);
		}
		// if there is a locale specified switch to that locale
		setLocale(pref.getLocale(), false);

		// set the right button to active
		updateLanguageButtons();
	}

	public void updateLanguageButtons() {
		if (pref.getLocale() == null || pref.getLocale().equals("en")) {
			// defaults to english
			((Element) english.getElement().getParentNode()).addClassName("active");
			((Element) italian.getElement().getParentNode()).removeClassName("active");
		}
		else {
			((Element) english.getElement().getParentNode()).removeClassName("active");
			((Element) italian.getElement().getParentNode()).addClassName("active");
		}
	}

	protected void setLocale(String locale, boolean storeAsDefault) {
		selector.select(locale);
		logger.info("Language set to: " + locale);
		if (storeAsDefault) {
			pref.setLocale(locale);
			pref = em.merge(pref);
			em.flush();
		}

		// notify component of the changes
		languageChanged.fire(pref);
	}

	/**
	 * An element has been clicked. If the element has a <code>lang</code>
	 * attribute the language of the UI is set to that language.
	 * 
	 * @param e
	 */
	@EventHandler({ "italian", "english" })
	private void switchLanguage(ClickEvent e) {
		String lang = e.getRelativeElement().getLang();
		if (lang != null && lang != "") {
			setLocale(lang, true);
			updateLanguageButtons();
		}
		e.preventDefault();
	}
}
