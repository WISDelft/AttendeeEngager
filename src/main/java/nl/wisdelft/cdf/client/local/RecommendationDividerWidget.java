/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import java.util.Date;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import nl.wisdelft.cdf.client.local.status.LanguageChanged;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.shared.DateTimeFormatInfo;
import com.google.gwt.i18n.shared.impl.cldr.DateTimeFormatInfoImpl_en;
import com.google.gwt.i18n.shared.impl.cldr.DateTimeFormatInfoImpl_it;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author Jasper Oosterman
 * @created Apr 2, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Templated("UserDashboard.html#recommendation-divider-template")
public class RecommendationDividerWidget extends Composite {

	@Inject
	@DataField
	private InlineLabel nrRecommendations;

	@Inject
	@DataField
	private InlineLabel dateField;

	@Inject
	Logger logger;

	@Inject
	TranslationService translation;

	private int nrRecs = 0;
	private Date dividerDate;

	private DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);

	public void setDate(Date dividerDate) {
		this.dividerDate = dividerDate;
		String lang = translation.getActiveLocale();
		if (lang == null) {
			updateDateField("en");
		}
		else {
			updateDateField(translation.getActiveLocale());
		}
	}

	public void updateDateField(String lang) {
		if ("en".equals(lang)) {
			DateTimeFormatInfo info = new DateTimeFormatInfoImpl_en();
			format = new DateTimeFormat(info.dateFormatFull(), info) {
			};
		}
		else if ("it".equals(lang)) {
			DateTimeFormatInfo info = new DateTimeFormatInfoImpl_it();
			format = new DateTimeFormat(info.dateFormatFull(), info) {
			};
		}
		String localDate = format.format(dividerDate);
		dateField.setText(localDate);
	}

	public void languageChanged(@Observes @LanguageChanged UIPreference pref) {
		String lang = pref.getLocale();
		updateDateField(lang);
	}

	public void setNrRecommendations(int nr) {
		nrRecs = nr;
		nrRecommendations.setText("" + nrRecs);
	}

	public void incNrRecommendations() {
		nrRecs++;
		nrRecommendations.setText("" + nrRecs);
	}

}
