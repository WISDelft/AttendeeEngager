/**
 * 
 */
package nl.wisdelft.cdf.server.status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * An event annotated with this qualifier indicated that an Entity has been
 * stored in the DB for the first time.
 * 
 * @author Jasper Oosterman
 * @created Feb 26, 2014
 * @organization Delft University of Technology - Web Information Systems
 */

@Qualifier
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Created {}
