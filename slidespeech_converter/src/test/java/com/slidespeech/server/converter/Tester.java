package com.slidespeech.server.converter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.exec.ExecuteException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.TextToSpeechService;
import com.sun.star.lang.IllegalArgumentException;

@RunWith(Arquillian.class)
public class Tester
{
	
	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createMultipleInstanceTestArchive();
	}
	
	@Test
	public void testRun() 
	{
		Runtime runtime = Runtime.getRuntime();
		
		try 
		{
			File file = new File("/tmp/test123");
			
			file.delete();
			assertTrue(!file.exists());
			
			Process exec = runtime.exec("touch /tmp/test123");
			
			try {
				exec.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			File file2 = new File("/tmp/test123");
			assertTrue(file2.exists());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPdfConversionHolisticLearningEBook()
	{
		String pdfFile = "HolisticLearningEBook.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result;
		try
		{
			System.out.println("running");
			
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdfFile, "en", "female");
			Assert.assertEquals(27, result.getSlides().size());

		} catch (Exception e)
		{
			Assert.fail();
		}
	}
	
	@Test
	public void testTTService()
	{
		try
		{
			File file = new File(TestHelper.destionationFolder1OnVM+"testFile.mp3");
			assertFalse(file.exists());
			
			TextToSpeechService.convert(TestHelper.destionationFolder1OnVM, "testFile", "This are Speakernotes", "en" , "female" , "both");

			assertTrue(file.exists());
			
			long length = file.length();
			assertTrue(length > 10000);
		} 
		catch (ExecuteException e)
		{
			e.printStackTrace();
			fail();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
}
