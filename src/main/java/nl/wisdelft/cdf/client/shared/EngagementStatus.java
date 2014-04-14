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
	 * The user has not been sent any messages yet. (0 in DB)
	 */
	NOT_CONTACTED,
	/**
	 * The initial message to the user has been sent.(1 in DB)
	 */
	CONTACTED,
	/**
	 * The reminder message to the user has been sent.(2 in DB)
	 */
	CONTACTED_REMINDED,
	/**
	 * The user opted out manually or automatically (did not respond within the
	 * allotted time period) and should not be sent any more messages.(3 in DB)
	 */
	OPTED_OUT,
	/**
	 * The user responded positively and can be engaged further. (4 in DB)
	 */
	OPTED_IN
}
