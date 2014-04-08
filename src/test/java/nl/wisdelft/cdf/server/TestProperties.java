/**
 * 
 */
package nl.wisdelft.cdf.server;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Jasper Oosterman
 * @created Feb 27, 2014
 * @organization Delft University of Technology - Web Information Systems
 */
public class TestProperties extends BaseTest {

	@Test
	public void testExistingProperty() {
		String s = utility.getPropertyAsString("testUser");
		Assert.assertNotNull(s);
	}

	@Test
	public void testNonExistingProperty() {
		String s = utility.getPropertyAsString("thisdoes_not _exist");
		Assert.assertNotNull(s);
		Assert.assertEquals(s, "");
	}

	@Test
	public void testBooleanExisting() {
		Boolean s = utility.getPropertyAsBoolean("debug");
		Assert.assertNotNull(s);
	}

	@Test
	public void testBooleanNonExisting() {
		Boolean s = utility.getPropertyAsBoolean("thisdoes_not _exist");
		Assert.assertNotNull(s);
		Assert.assertFalse(s);
	}

	@Test
	public void testServerLocalization() {
		String property = "welcomeMessage";
		String def = utility.getPropertyAsString(property);
		Assert.assertNotNull(def);
		Assert.assertFalse("".equals(def));
		String en = utility.getPropertyAsString(property, "en");
		Assert.assertEquals(def, en);
		String it = utility.getPropertyAsString(property, "it");
		Assert.assertFalse(en.equals(it));

	}
}
