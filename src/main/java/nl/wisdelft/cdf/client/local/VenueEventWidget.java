/**
 * 
 */
package nl.wisdelft.cdf.client.local;

import javax.inject.Inject;
import nl.wisdelft.cdf.client.shared.VenueEvent;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author Jasper Oosterman
 * @created Mar 31, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
@Templated("UserDashboard.html#event-template")
public class VenueEventWidget extends Composite implements HasModel<VenueEvent> {
	@Inject
	@AutoBound
	DataBinder<VenueEvent> venueEventBinder;

	@Inject
	@DataField
	@Bound(property = "name")
	InlineLabel eventName;

	protected void init() {
		VenueEvent event = getModel();
		// update the link of the main widget
		Element eventLink = this.getElement();
		eventLink.setAttribute("href", event.getUrl());
		eventLink.setAttribute("target", "_blank");

	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.errai.ui.client.widget.HasModel#getModel()
	 */
	@Override
	public VenueEvent getModel() {
		return venueEventBinder.getModel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.errai.ui.client.widget.HasModel#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(VenueEvent model) {
		venueEventBinder.setModel(model);
		init();
	}
}