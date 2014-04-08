/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import java.util.Date;
import javax.inject.Inject;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import com.google.gwt.i18n.client.DateTimeFormat;
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
	private InlineLabel dateDay, dateMonth, dateDayNumber, nrRecommendations;

	@Inject
	Logger logger;

	private int nrRecs = 0;

	private final DateTimeFormat formatDay = DateTimeFormat.getFormat("EEEE");
	private final DateTimeFormat formatMonth = DateTimeFormat.getFormat("MMMM");
	private final DateTimeFormat formatDayNr = DateTimeFormat.getFormat("d");

	public void setDate(Date dividerDate) {
		if (dividerDate != null) {
			// parse the date in small pieces
			String day = formatDay.format(dividerDate);
			String month = formatMonth.format(dividerDate);
			String dayNumber = formatDayNr.format(dividerDate);

			// fill the datafields
			dateDay.setText(day);
			dateMonth.setText(month);
			dateDayNumber.setText(dayNumber);
		}
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
