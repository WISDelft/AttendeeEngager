/**
 * 
 */
package nl.wisdelft.cdf.client.shared;

/**
 * @author Jasper Oosterman
 * @created Feb 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public enum EngagementStatus {
	/**
	 * The user has not been sent any messages yet.
	 */
	NOT_CONTACTED,
	/**
	 * The initial message to the user has been sent.
	 */
	CONTACTED,
	/**
	 * The reminder message to the user has been sent.
	 */
	CONTACTED_REMINDED,
	/**
	 * The user opted out manually or automatically (did not respond within the
	 * allotted time period) and should not be sent any more messages.
	 */
	OPTED_OUT,
	/**
	 * The user responded positively and can be engaged further
	 */
	OPTED_IN
}
