package eu.rethink.globalregistry.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.rethink.globalregistry.util.XSDDateTime;

public class XSDDateTimeTest
{
	//valid
	public String validXSD1 = "2017-02-07T12:13:14+01:00"; 
	public String validXSD2 = "2017-02-07T12:13:14Z";
	
	//invalid
	public String invalidXSD1 = "2017-02-07T25:13:14+01:00";
	
	@Test
	public void XSDDateTimeValidationTest()
	{
		assertTrue(XSDDateTime.validateXSDDateTime(validXSD1));
		assertTrue(XSDDateTime.validateXSDDateTime(validXSD2));
		
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD1));
	}
}