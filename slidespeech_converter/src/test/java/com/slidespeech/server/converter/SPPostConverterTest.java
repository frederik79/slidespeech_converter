package com.slidespeech.server.converter;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.slidespeech.server.spconverter.SPPostConverter;

@RunWith(Arquillian.class)
public class SPPostConverterTest 
{
	
	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createOneInstanceTestArchive();
	}
	
	@Test
	public void testSharePointPPTXZipGeneration() 
	{
		SPPostConverter spPostConverterTest = new SPPostConverter();

		String targetDir = TestHelper.destionationFolder1OnVM;
		File file = new File(targetDir + "download.zip");

		if (file.exists())
		{
			file.delete();
		}
		
		assertTrue(!file.exists());
		String fileName = "90_Days_in_Android_Market_V3.0.pptx";
		
		boolean convert = spPostConverterTest.convert(fileName, targetDir);
		
		assertTrue(convert);
  		
		assertTrue(file.exists());
		
	}
	
}
