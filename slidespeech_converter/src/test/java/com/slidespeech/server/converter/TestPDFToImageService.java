package com.slidespeech.server.converter;

import java.io.IOException;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.slidespeech.server.service.PdfToImageService;

@RunWith(Arquillian.class)
public class TestPDFToImageService
{
	private static String _testPath = "/presentation/";
	
	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createOneInstanceTestArchive();
	}

	@Test
	public void testconversion()
	{
		try
		{
			String filename = "Lecture.pdf";
			
			String testPAthOnVM = TestHelper.destionationFolder1OnVM;
			
			String targetDirDirectory = testPAthOnVM;
			PdfToImageService.convert(testPAthOnVM + filename, targetDirDirectory);
		} catch (IOException e)
		{
			Assert.fail();
		}
	}
}
