package com.slidespeech.server.converter;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.sun.star.lang.IllegalArgumentException;

@RunWith(Arquillian.class)
public class ParallelConverterTest
{
	@Rule
	public TestName name = new TestName();
	private PrintWriter _out;
	private long startTime;

	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createMultipleInstanceTestArchive();
	}

	@Before
	public void startup()
	{
		FileWriter outFile;
		try
		{
			startTime = System.currentTimeMillis();
			outFile = new FileWriter("/tmp/runtimeRecorder.txt", true);
			_out = new PrintWriter(outFile);
			_out.println("open");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@After
	public void tearDown()
	{
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		_out.println(name.getMethodName() + " : " + elapsedTime + " ms");

		_out.println("close");
		_out.close();
	}

	@Test
	public void testTwoPresentationInDifferentDirectories() throws IOException
	{
		String testFile = "jasper.ppt";

		Converter conv = new FullConversion();
		Presentation result;
		try
		{
			result = conv.convert(TestHelper.destionationFolder1OnVM, testFile,
					"en" , "female");
			// System.out.println(result);
			Assert.assertEquals(3, result.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			Assert.fail();
		}

		String testFile_90_Days_in_Android_Market_V30 = "90_Days_in_Android_Market_V3.0.pptx";
		Converter conv2 = new FullConversion();
		Presentation result2;
		try
		{
			result2 = conv2.convert(TestHelper.destionationFolder2OnVM,
					testFile_90_Days_in_Android_Market_V30, "en" , "female");
			Assert.assertEquals(36, result2.getSlides().size());

		} catch (IllegalArgumentException e)
		{
			Assert.fail();
		}
	}

	@Test
	public void testTTServiceParallel()
	{
		File file = new File(TestHelper.destionationFolder1OnVM
				+ "testFile.mp3");
		assertFalse(file.exists());

		ConverterThreadHandler startTTServiceConversionThread = startTTServiceConversionThread(
				TestHelper.destionationFolder1OnVM, "testFile",
				"This are Speakernotes", "en", "both");

		File file2 = new File(TestHelper.destionationFolder2OnVM
				+ "testFile.mp3");
		assertFalse(file2.exists());

		ConverterThreadHandler startTTServiceConversionThread2 = startTTServiceConversionThread(
				TestHelper.destionationFolder2OnVM, "testFile",
				"This are more Speakernotes", "en", "both");

		startTTServiceConversionThread2.waitUntilThreadCompletion();
		startTTServiceConversionThread.waitUntilThreadCompletion();
		assertTrue(file.exists());

		assertTrue(file2.exists());

		assertTrue(file2.length() > file.length());
	}

	private ConverterThreadHandler startTTServiceConversionThread(
			String destionationFolder, String fileName, String speakerNotes,
			String language, String type)
	{
		TextToSpeechConversionThreadHandler converterCallback = new TextToSpeechConversionThreadHandler();

		converterCallback.setDestionationFolder(destionationFolder);
		converterCallback.setFileName(fileName);
		converterCallback.setSpeakerNotes(speakerNotes);
		converterCallback.setOutputFormat(type);
		converterCallback.setLang(language);

		converterCallback.startConversionThread();

		return converterCallback;
	}

	@Test
	public void testConversionThreadSequentialConversion() throws IOException,
			InterruptedException
	{
		String testFile = "jasper.ppt";

		FullConversionThreadHandler converterCallback = new FullConversionThreadHandler();

		converterCallback
				.setDestionationFolder(TestHelper.destionationFolder1OnVM);
		converterCallback.setLang("en");
		converterCallback.setFileName(testFile);

		converterCallback.startConversionThread();

		converterCallback.waitUntilThreadCompletion();

		Presentation resultPresentation = converterCallback
				.getResultPresentation();

		Assert.assertEquals(3, resultPresentation.getSlides().size());

		String testFile_90_Days_in_Android_Market_V30 = "90_Days_in_Android_Market_V3.0.pptx";

		FullConversionThreadHandler converterCallback2 = new FullConversionThreadHandler();

		converterCallback2
				.setDestionationFolder(TestHelper.destionationFolder2OnVM);
		converterCallback2.setLang("en");
		converterCallback2.setFileName(testFile_90_Days_in_Android_Market_V30);
		converterCallback2.startConversionThread();
		converterCallback2.waitUntilThreadCompletion();

		Presentation resultPresentation2 = converterCallback2
				.getResultPresentation();
		Assert.assertEquals(36, resultPresentation2.getSlides().size());
	}

	@Test
	public void testInvalidZip()
	{
		String testFileInvalidZip = "invalidzipFile.zip";

		ZIPConverterThreadHandler converterCallback4 = new ZIPConverterThreadHandler();

		converterCallback4
				.setDestionationFolder(TestHelper.destionationFolder4OnVM);
		converterCallback4.setFileName(testFileInvalidZip);
		converterCallback4.setLang("en");

		converterCallback4.startConversionThread();

		assertTrue(converterCallback4.waitUntilThreadCompletion());

		Presentation resultPresentation = converterCallback4
				.getResultPresentation();

		assertNull(resultPresentation);
	}

	
	//This one fails to to arquillian exception 
//	@Test
//	public void testFourDifferentKindsOfConverterParrallel()
//	{
//		String testFile = "test.zip";
//		String destionationFolder = TestHelper.destionationFolder1OnVM;
//		ZIPConverterThreadHandler converterCallbackZIP = startZIPConversionThread(
//				testFile, destionationFolder);
//
//		testFile = "dominoe.pdf";
//		destionationFolder = TestHelper.destionationFolder2OnVM;
//		PDFConverterThreadHandler converterPDFThreadHandler = startPDFConversionThread(
//				testFile, destionationFolder);
//
//		testFile = "90_Days_in_Android_Market_V3.0.pptx";
//		destionationFolder = TestHelper.destionationFolder3OnVM;
//		FullConversionThreadHandler converterCallbackPPTX = startFullConversionThread(
//				testFile, destionationFolder);
//
//		testFile = "Best practice for storing and transforming presentations.odp";
//		destionationFolder = TestHelper.destionationFolder4OnVM;
//		FullConversionThreadHandler converterCallbackODP = startFullConversionThread(
//				testFile, destionationFolder);
//
//		assertTrue(converterCallbackZIP.waitUntilThreadCompletion());
//		Assert.assertEquals(1, converterCallbackZIP.getResultPresentation()
//				.getSlides().size());
//
//		assertTrue(converterPDFThreadHandler.waitUntilThreadCompletion());
//
//		assertTrue(converterPDFThreadHandler.isCompleted());
//
//		Presentation resultPresentation = converterPDFThreadHandler
//				.getResultPresentation();
//
//		assertNotNull(resultPresentation);
//
//		List<Slide> slides = resultPresentation.getSlides();
//		Assert.assertEquals(19, slides.size());
//
//		assertTrue(converterCallbackPPTX.waitUntilThreadCompletion());
//
//		assertTrue(converterCallbackPPTX.isCompleted());
//
//		assertTrue(converterCallbackPPTX.getException() == null);
//
//		resultPresentation = converterCallbackPPTX.getResultPresentation();
//
//		assertNotNull(resultPresentation);
//
//		slides = resultPresentation.getSlides();
//
//		Assert.assertEquals(36, slides.size());
//
//		assertTrue(converterCallbackODP.waitUntilThreadCompletion());
//		Assert.assertEquals(18, converterCallbackODP.getResultPresentation()
//				.getSlides().size());
//	}

	@Test
	public void testPDFConverterParrallel()
	{

		String testFile = "dominoe.pdf";
		String destionationFolder = TestHelper.destionationFolder2OnVM;
		PDFConverterThreadHandler converterPDFThreadHandler = startPDFConversionThread(
				testFile, destionationFolder);

		assertTrue(converterPDFThreadHandler.waitUntilThreadCompletion());

		assertTrue(converterPDFThreadHandler.isCompleted());

		Presentation resultPresentation = converterPDFThreadHandler
				.getResultPresentation();

		assertNotNull(resultPresentation);

		List<Slide> slides = resultPresentation.getSlides();
		Assert.assertEquals(19, slides.size());
	}

	@Test
	public void convertTwoImagesSequential()
	{
		String testFile = "tulip.png";
		String destionationFolder = TestHelper.destionationFolder1OnVM;
		ImageConverterThreadHandler converterCallbackimg1 = startImageConversionThread(
				testFile, destionationFolder, "This is a tulip");

		converterCallbackimg1.waitUntilThreadCompletion();

		Presentation resultPresentation = converterCallbackimg1
				.getResultPresentation();

		Assert.assertEquals(1, resultPresentation.getSlides().size());

		Slide slide = resultPresentation.getSlides().get(0);

		assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">This is a tulip</voice><br/></speak>",slide.getText());

		testFile = "Screenshot.png";
		destionationFolder = TestHelper.destionationFolder2OnVM;
		ImageConverterThreadHandler converterCallbackimg2 = startImageConversionThread(
				testFile, destionationFolder, "This is a Screenshot");

		converterCallbackimg2.waitUntilThreadCompletion();

		resultPresentation = converterCallbackimg2.getResultPresentation();

		Assert.assertEquals(1, resultPresentation.getSlides().size());

		slide = resultPresentation.getSlides().get(0);

		assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">This is a Screenshot</voice><br/></speak>", slide.getText());
	}

	@Test
	public void convertTwoImagesParallel()
	{
		String testFile = "tulip.png";
		String destionationFolder = TestHelper.destionationFolder1OnVM;
		ImageConverterThreadHandler converterCallbackimg1 = startImageConversionThread(
				testFile, destionationFolder, "This is a tulip");

		testFile = "Screenshot.png";
		destionationFolder = TestHelper.destionationFolder2OnVM;
		ImageConverterThreadHandler converterCallbackimg2 = startImageConversionThread(
				testFile, destionationFolder, "This is a Screenshot");

		converterCallbackimg2.waitUntilThreadCompletion();
		converterCallbackimg1.waitUntilThreadCompletion();

		Presentation resultPresentation = converterCallbackimg1
				.getResultPresentation();

		Assert.assertEquals(1, resultPresentation.getSlides().size());

		Slide slide = resultPresentation.getSlides().get(0);

		assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">This is a tulip</voice><br/></speak>", slide.getText());
		
		resultPresentation = converterCallbackimg2.getResultPresentation();

		Assert.assertEquals(1, resultPresentation.getSlides().size());

		slide = resultPresentation.getSlides().get(0);

		assertEquals("<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\"><voice gender=\"female\" xml:lang=\"en\">This is a Screenshot</voice><br/></speak>", slide.getText());
	}

	@Test
	public void testOneConverterSequential()
	{

		String testFile = "Best practice for storing and transforming presentations.odp";
		String destionationFolder = TestHelper.destionationFolder4OnVM;
		FullConversionThreadHandler converterCallbackODP = startFullConversionThread(
				testFile, destionationFolder);

		assertTrue(converterCallbackODP.waitUntilThreadCompletion());
		Assert.assertEquals(18, converterCallbackODP.getResultPresentation()
				.getSlides().size());
	}

	@Test
	public void testTwoConverterSequential()
	{
		String testFile = "90_Days_in_Android_Market_V3.0.pptx";
		String destionationFolder = TestHelper.destionationFolder3OnVM;

		FullConversionThreadHandler converterCallbackPPTX = startFullConversionThread(
				testFile, destionationFolder);
		
		assertTrue(converterCallbackPPTX.waitUntilThreadCompletion());

		assertTrue(converterCallbackPPTX.getException() == null);

		Assert.assertEquals(36, converterCallbackPPTX.getResultPresentation()
				.getSlides().size());
		
		_out.write("Succesful finished testTwoConverterSequential");
		
//		testFile = "Best practice for storing and transforming presentations.odp";
//		destionationFolder = TestHelper.destionationFolder4OnVM;
//		FullConversionThreadHandler converterCallbackODP = startFullConversionThread(
//				testFile, destionationFolder);
//
//		assertTrue(converterCallbackODP.waitUntilThreadCompletion());
//		Assert.assertEquals(18, converterCallbackODP.getResultPresentation()
//				.getSlides().size());
	}

	// this test does not pass, It is a Arquillian error - should be ficed in one of the following arquillian releases
//	@Test
//	public void testFourDifferentKindsOfConverterSequential()
//	{
//		String testFile = "test.zip";
//		String destionationFolder = TestHelper.destionationFolder1OnVM;
//		ZIPConverterThreadHandler converterCallbackZIP = startZIPConversionThread(
//				testFile, destionationFolder);
//		assertTrue(converterCallbackZIP.waitUntilThreadCompletion());
//		Assert.assertEquals(1, converterCallbackZIP.getResultPresentation()
//				.getSlides().size());
//
//		testFile = "dominoe.pdf";
//		destionationFolder = TestHelper.destionationFolder2OnVM;
//		PDFConverterThreadHandler converterCallbackPDF = startPDFConversionThread(
//				testFile, destionationFolder);
//		assertTrue(converterCallbackPDF.waitUntilThreadCompletion());
//
//		assertTrue(converterCallbackPDF.getException() == null);
//		Assert.assertEquals(19, converterCallbackPDF.getResultPresentation()
//				.getSlides().size());
//
//		testFile = "90_Days_in_Android_Market_V3.0.pptx";
//		destionationFolder = TestHelper.destionationFolder3OnVM;
//		FullConversionThreadHandler converterCallbackPPTX = startFullConversionThread(
//				testFile, destionationFolder);
//		assertTrue(converterCallbackPPTX.waitUntilThreadCompletion());
//
//		assertTrue(converterCallbackPPTX.getException() == null);
//
//		Assert.assertEquals(36, converterCallbackPPTX.getResultPresentation()
//				.getSlides().size());
//
//		testFile = "Best practice for storing and transforming presentations.odp";
//		destionationFolder = TestHelper.destionationFolder4OnVM;
//		FullConversionThreadHandler converterCallbackODP = startFullConversionThread(
//				testFile, destionationFolder);
//
//		assertTrue(converterCallbackODP.waitUntilThreadCompletion());
//		Assert.assertEquals(18, converterCallbackODP.getResultPresentation()
//				.getSlides().size());
//	}

	// this test does not pass, It is a Arquillian error/ NoClassDefFound in
	// PDFCOnverter
	// @Test
	// public void testConvngersionSixThreadSequentialConversions()
	// throws IOException, InterruptedException
	// {
	// long startTime = System.currentTimeMillis();
	//
	// _out.println("start testConversionSixThreadSequentialConversions");
	// _out.flush();
	//
	// String testFile = "jasper.ppt";
	// String destionationFolder = TestHelper.destionationFolder1OnVM;
	// FullConversionThreadHandler converterCallback1 =
	// startFullConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback1.waitUntilThreadCompletion());
	// Assert.assertEquals(3,
	// converterCallback1.getResultPresentation().getSlides().size());
	// _out.println("First Done");
	// _out.flush();
	//
	// testFile =
	// "Best practice for storing and transforming presentations.odp";
	// destionationFolder = TestHelper.destionationFolder2OnVM;
	// FullConversionThreadHandler converterCallback2 =
	// startFullConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback2.waitUntilThreadCompletion());
	// Assert.assertEquals(18,
	// converterCallback2.getResultPresentation().getSlides().size());
	// _out.println("Second Done");
	// _out.flush();
	//
	// testFile = "3MT_thesis_competion_frederik_schmidt_final.pptx";
	// destionationFolder = TestHelper.destionationFolder3OnVM;
	// FullConversionThreadHandler converterCallback3 =
	// startFullConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback3.waitUntilThreadCompletion());
	// Assert.assertEquals(1,
	// converterCallback3.getResultPresentation().getSlides().size());
	// _out.println("Thrid Done");
	// _out.flush();
	//
	// testFile = "90_Days_in_Android_Market_V3.0.pptx";
	// destionationFolder = TestHelper.destionationFolder4OnVM;
	// FullConversionThreadHandler converterCallback4 =
	// startFullConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback4.waitUntilThreadCompletion());
	// Assert.assertEquals(36,
	// converterCallback4.getResultPresentation().getSlides().size());
	// _out.println("Fourth Done");
	// _out.flush();
	//
	// _out.println("Begin Five");
	// _out.flush();
	// testFile = "dominoe.pdf";
	// destionationFolder = TestHelper.destionationFolder5OnVM;
	//
	// PDFConverterThreadHandler converterCallback5 =
	// startPDFConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback5.waitUntilThreadCompletion());
	// Assert.assertEquals(19,
	// converterCallback5.getResultPresentation().getSlides().size());
	// _out.println("Fifth Done");
	// _out.flush();
	//
	// testFile = "Lecture.pdf";
	// _out.println("Fifth middle");
	// _out.flush();
	// destionationFolder = TestHelper.destionationFolder6OnVM;
	// PDFConverterThreadHandler converterCallback6 =
	// startPDFConversionThread(testFile, destionationFolder);
	// assertTrue(converterCallback6.waitUntilThreadCompletion());
	// Assert.assertEquals(9,
	// converterCallback6.getResultPresentation().getSlides().size());
	// _out.println("Six Done");
	// _out.flush();
	//
	// long stopTime = System.currentTimeMillis();
	// long elapsedTime = stopTime - startTime;
	//
	// _out.println("Sequential 6 presentations : " + elapsedTime);
	// }

	private ZIPConverterThreadHandler startZIPConversionThread(String fileName,
			String destionationFolder)
	{
		ZIPConverterThreadHandler converterCallback = new ZIPConverterThreadHandler();

		converterCallback.setDestionationFolder(destionationFolder);
		converterCallback.setFileName(fileName);
		converterCallback.setLang("en");

		converterCallback.startConversionThread();

		return converterCallback;
	}

	private PDFConverterThreadHandler startPDFConversionThread(String fileName,
			String destionationFolder)
	{
		PDFConverterThreadHandler converterCallback = new PDFConverterThreadHandler();

		converterCallback.setDestionationFolder(destionationFolder);
		converterCallback.setFileName(fileName);
		converterCallback.setLang("en");

		converterCallback.startConversionThread();

		return converterCallback;
	}

	private FullConversionThreadHandler startFullConversionThread(
			String fileName, String destionationFolder)
	{
		FullConversionThreadHandler converterCallback = new FullConversionThreadHandler();

		converterCallback.setDestionationFolder(destionationFolder);
		converterCallback.setFileName(fileName);
		converterCallback.setLang("en");

		converterCallback.startConversionThread();

		return converterCallback;
	}

	// private FullConversionCallback startVideoConversionThread(String
	// fileName, String destionationFolder)
	// {
	//
	// VideoConversionCallback converterCallback = new
	// VideoConversionCallback();
	//
	// converterCallback.setCode("code");
	// converterCallback.setLang("en");
	// converterCallback.setVersion(1);
	//
	// final ConverterThread converterThread = new ConverterThread(
	// converterCallback);
	//
	// converterThread.convert();
	//
	// converterCallback.setDestionationFolder(destionationFolder);
	// converterCallback.setFileName(fileName);
	// converterCallback.setLang("en");
	//
	// return converterCallback;
	// }

	private ImageConverterThreadHandler startImageConversionThread(
			String fileName, String destionationFolder, String speakerNotes)
	{
		ImageConverterThreadHandler converterCallback = new ImageConverterThreadHandler();

		converterCallback.setFileName(fileName);
		converterCallback.setLang("en");
		converterCallback.setDestionationFolder(destionationFolder);
		converterCallback.setSpeakernotes(speakerNotes);

		converterCallback.startConversionThread();

		return converterCallback;
	}

	@Test
	public void testConversionpptOdpZipImg() throws IOException,
			InterruptedException
	{
		long startTime = System.currentTimeMillis();

		String testFile = "jasper.ppt";
		String destionationFolder = TestHelper.destionationFolder1OnVM;
		FullConversionThreadHandler converterCallback1 = startFullConversionThread(
				testFile, destionationFolder);

		testFile = "Best practice for storing and transforming presentations.odp";
		destionationFolder = TestHelper.destionationFolder2OnVM;
		FullConversionThreadHandler converterCallback2 = startFullConversionThread(
				testFile, destionationFolder);

		testFile = "3MT_thesis_competion_frederik_schmidt_final.pptx";
		destionationFolder = TestHelper.destionationFolder3OnVM;
		FullConversionThreadHandler converterCallback3 = startFullConversionThread(
				testFile, destionationFolder);

		testFile = "90_Days_in_Android_Market_V3.0.pptx";
		destionationFolder = TestHelper.destionationFolder4OnVM;
		FullConversionThreadHandler converterCallback4 = startFullConversionThread(
				testFile, destionationFolder);

		testFile = "dominoe.pdf";
		destionationFolder = TestHelper.destionationFolder5OnVM;
		PDFConverterThreadHandler converterCallback5 = startPDFConversionThread(
				testFile, destionationFolder);

		testFile = "Lecture.pdf";
		destionationFolder = TestHelper.destionationFolder6OnVM;
		PDFConverterThreadHandler converterCallback6 = startPDFConversionThread(
				testFile, destionationFolder);

		testFile = "test.zip";
		destionationFolder = TestHelper.destionationFolder7OnVM;
		ZIPConverterThreadHandler converterCallback7 = startZIPConversionThread(
				testFile, destionationFolder);

		testFile = "tulip.png";
		destionationFolder = TestHelper.destionationFolder8OnVM;
		ImageConverterThreadHandler converterCallback8 = startImageConversionThread(
				testFile, destionationFolder, "This is a tulip");

		assertTrue(converterCallback1.waitUntilThreadCompletion());
		Assert.assertEquals(3, converterCallback1.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback1 done");

		assertTrue(converterCallback2.waitUntilThreadCompletion());
		Assert.assertEquals(18, converterCallback2.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback2 done");

		assertTrue(converterCallback3.waitUntilThreadCompletion());
		Assert.assertEquals(1, converterCallback3.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback3 done");

		assertTrue(converterCallback4.waitUntilThreadCompletion());
		Assert.assertEquals(36, converterCallback4.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback4 done");

		assertTrue(converterCallback5.waitUntilThreadCompletion());
		Assert.assertEquals(19, converterCallback5.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback5 done");

		assertTrue(converterCallback6.waitUntilThreadCompletion());
		Assert.assertEquals(9, converterCallback6.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback6 done");

		assertTrue(converterCallback7.waitUntilThreadCompletion());
		Assert.assertEquals(1, converterCallback7.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback7 done");
		assertTrue(converterCallback8.waitUntilThreadCompletion());
		Assert.assertEquals(1, converterCallback8.getResultPresentation()
				.getSlides().size());

		_out.println("converterCallback8 done");

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		_out.println("Parallel 8 presentations : " + elapsedTime);
	}

}
