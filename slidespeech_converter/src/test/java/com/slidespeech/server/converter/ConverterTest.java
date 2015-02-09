package com.slidespeech.server.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class ConverterTest
{
	private String _testPath = TestHelper.destionationFolder1OnVM;
	
	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createOneInstanceTestArchive();
	}

	@Test
	public void testSimpleDeploymentTest() 
	{
		System.out.println("test");
		Assert.assertTrue(true);
	}
	
	@Test
	public void testPdfConversionHolisticLearningEBook() throws IOException
	{
		String pdfFile = "HolisticLearningEBook.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdfFile, "en", "female");
			Assert.assertEquals(27, result.getSlides().size());

		} catch (IllegalArgumentException e)
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
	
	@Test
	public void testImgConverter() throws IOException
	{
		String pdfFile = "tulip.png";
		ImgConverter conv = new ImgConverter();
		
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdfFile, "en", "female" , "This is a tulip");
			
			Assert.assertEquals(1, result.getSlides().size());
			
			Slide slide = result.getSlides().get(0);
			
			System.out.println(slide.getText());
			
			assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">This is a tulip</voice><br/></speak>", slide.getText());
			
		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}
	
	@Test
	public void testPdfConversionWikipedia() throws IOException
	{
		String pdftFile = "Wikipedia.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdftFile, "en" , "female");
			Assert.assertEquals(2, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}
	
	

	@Test
	public void testEasyZipConversion() throws IOException
	{
		String zipFile = "test.zip";
		ZipConverter conv = new ZipConverter();

		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, zipFile, "en" , "female");
			Assert.assertEquals(1, result.getSlides().size());

			Slide slide = result.getSlide(0);
			Assert.assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">Script</voice><br/></speak>", slide.getText());
			
			
		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testinvalidZipConversion() throws IOException
	{
		String zipFile = "invalidzipFile.zip";

		File file = new File(TestHelper.destionationFolder1OnVM + zipFile);
		assert (file.exists());
		ZipConverter conv = new ZipConverter();

		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, zipFile, "en" , "female");
			Assert.fail();
		} catch (IllegalArgumentException e)
		{
			// this is okay, test is supposed to throw an exception
		}
	}

	@Test
	public void testZipConversionWithEmptySpeakerNotes() throws IOException
	{
		String zipFile = "example.zip";
		ZipConverter conv = new ZipConverter();

		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, zipFile, "en" , "female");
			Assert.assertEquals(3, result.getSlides().size());

			List<Slide> slides = result.getSlides();
			Slide slide1 = result.getSlide(0);
			Slide slide2 = result.getSlide(1);
			Slide slide3 = result.getSlide(2);

			Assert.assertTrue(slide1.getText().contains("Here are some straight speaker notes."));
			Assert.assertTrue(slide2.getText().contains("There are no speakernotes on this slide <break time=\"5000ms\"/>"));
			Assert.assertTrue(slide3.getText().contains("Here are some straight speaker notes on slide 3."));

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testHeatherZipConversion()
	{
		String zipFile = "20120509_heather.zip";
		ZipConverter conv = new ZipConverter();

		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, zipFile, "en" , "female");
			Assert.assertEquals(10, result.getSlides().size());

			List<Slide> slides = result.getSlides();

			for (Slide slide : slides)
			{
				String text = slide.getText();

				Assert.assertTrue(text.length() > 0);
			}

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testPdfConversion() throws IOException
	{
		String pdftFile = "Lecture.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdftFile, "en" , "female");
			Assert.assertEquals(9, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testPdfLandscapeConversion() throws IOException
	{
		String pdftFile = "testPrintFromFirefox.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdftFile, "en" , "female");
			Assert.assertEquals(3, result.getSlides().size());
		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testPdfDominoePortraitConversion() throws IOException
	{
		String pdftFile = "dominoe.pdf";
		PDFConverter conv = new PDFConverter();
		Presentation result = null;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, pdftFile, "en" , "female");
			Assert.assertEquals(19, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
		
		List<Slide> slides = result.getSlides();
		
		ArrayList<String> speakerNotes= new ArrayList<String>(); 
				
		speakerNotes.add("Dominoe. I was in between. Fiercely independent, had money saved up, and some time before I went west.");
		speakerNotes.add("My roommate called. Someone found a runaway dog. Dominoe.");
		speakerNotes.add("Named her after the damsel in Sharky's Machine.");
		speakerNotes.add("Malnourished, skittish, afraid of a raised hand.");
		speakerNotes.add("We walked into the woods, she always following a few yards away, my buddy.");
		speakerNotes.add("One day we drove farther and walked in the autumn leaves.");
				
		for (int i = 0; i< speakerNotes.size(); i++)
		{
			String actualText = slides.get(i).getText();
			String expectedText = speakerNotes.get(i);
			
			Assert.assertTrue(actualText.contains(expectedText));
		}
	}

	@Test
	public void testLongPresentation() throws IOException
	{
		String testFile_90_Days_in_Android_Market_V30 = "90_Days_in_Android_Market_V3.0.pptx";
		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath,
					testFile_90_Days_in_Android_Market_V30, "en" , "female");
			Assert.assertEquals(36, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testJasperConversion() throws IOException
	{
		String testFile = "jasper.ppt";

		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath, testFile, "en" , "female");
			// System.out.println(result);
			Assert.assertEquals(3, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}

		// System.out.println(result);
	}

	/*
	 * TODO - test fails - in this presentation some of the speakernotes are not
	 * retreived.
	 */

	// @Test
	// public void testDominoeConversion() throws IOException
	// {
	// String testFile = "dominoe-notes.ppt";
	//
	// Converter conv = new FullConversion();
	// Presentation result;
	// try
	// {
	// result = conv.convert(_testPath, testFile, "en");
	//
	// // System.out.println(result);
	// Assert.assertEquals(19, result.getSlides().size());
	//
	// List<Slide> slides = result.getSlides();
	//
	// Assert.assertEquals("Dominoe.I was in between. Fiercely independent, had money saved up, and some time before I went west.",
	// slides.get(0).getText());
	//
	// } catch (IllegalArgumentException e)
	// {
	// Assert.fail();
	// }
	// }

	@Test
	public void testBestPracticesConversion() throws IOException
	{
		String testFile = "Best practice for storing and transforming presentations.odp";

		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath, testFile, "en" , "female");
			Assert.assertEquals(18, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testfailed_conversion_test_john_graves_may18()
			throws IOException
	{
		  String testFile = "failed_conversion_test_john_graves_may18.ppt";
	
		Runtime runtime = Runtime.getRuntime();
		
		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath, testFile, "en" , "female");
			Assert.assertEquals(1, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testBecomingHuman() throws IOException
	{
		String testFile = "Becoming Human.odp";
		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath, testFile, "en" , "female");
			Assert.assertEquals(2, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testSlideSpeechPromoGerman() throws IOException
	{
		String testFile = "SlideSpeechPromoGerman1.0.odp";
		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(_testPath, testFile, "de" , "female");
			Assert.assertEquals(4, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testConverstionEmptySpeakernotes() throws IOException
	{
		String testFile = "pres_with_speakernotes_on_only_a_few_slides.pptx";
		Converter conv = new FullConversion();
		Presentation presentation;
		try
		{
			presentation = conv.convert(_testPath, testFile, "en" , "female");
			Assert.assertEquals(4, presentation.getSlides().size());

			List<Slide> slides = presentation.getSlides();

			Slide slide1 = slides.get(0);
			Slide slide2 = slides.get(1);
			Slide slide3 = slides.get(2);
			Slide slide4 = slides.get(3);

			Assert.assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">.</voice><br/></speak>", slide1.getText());
			
			Assert.assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">There are no speakernotes on this slide <break time=\"5000ms\"/></voice><br/></speak>",
					slide2.getText());
			
			Assert.assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">Asd asd asd asd</voice><br/></speak>",
					slide3.getText());
			
			Assert.assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">There are no speakernotes on this slide <break time=\"5000ms\"/></voice><br/></speak>",
					slide4.getText());
			

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}
	
	
}