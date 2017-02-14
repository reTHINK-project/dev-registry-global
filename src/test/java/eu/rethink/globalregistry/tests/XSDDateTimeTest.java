package eu.rethink.globalregistry.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.rethink.globalregistry.util.XSDDateTime;

public class XSDDateTimeTest
{
	//valid
	public String validXSD1 = "2017-02-07T12:13:14+02:00";
	public String validXSD2 = "2017-02-07T12:13:14Z";
	
	//invalid
	public String invalidXSD1 = "2017-02-07T25:13:14+0100";
	public String invalidXSD2 = "42017-02-07T12:13:14+01:00";
	public String invalidXSD3 = "2017-02-07T12:13:61+01:00";
	public String invalidXSD4 = "2017-02-07Tl2:13:14+01:00";
	public String invalidXSD5 = "2017-02-07T12:13:14ZZ";
	
	@Test
	public void XSDDateTimeValidationTest()
	{
		assertTrue(XSDDateTime.validateXSDDateTime(validXSD1));
		assertTrue(XSDDateTime.validateXSDDateTime(validXSD2));
		
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD1));
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD2));
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD3));
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD4));
		assertFalse(XSDDateTime.validateXSDDateTime(invalidXSD5));
	}
}