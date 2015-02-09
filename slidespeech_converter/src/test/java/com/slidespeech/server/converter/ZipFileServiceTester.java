package com.slidespeech.server.converter;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.SpeakerNotesExtrationService;
import com.sun.star.lang.IllegalArgumentException;

@RunWith(Arquillian.class)
public class ZipFileServiceTester
{
	
	@Deployment
	public static Archive<?> deploy()
	{
		return TestHelper.createOneInstanceTestArchive();
	}
	
	private String _testPath = TestHelper.destionationFolder1OnVM;

	@Test
	public void testxmlSpeakerNoteExtraction() throws IOException
	{
		String xmlFile = _testPath + "slide-notes.xml";

		Presentation presentation = new Presentation();

		for (int i=0; i<10; i++)
		{
			Slide slide = new Slide();
			presentation.addSlide(slide);
		}

		SpeakerNotesExtrationService.loadSpeakerNotes(presentation, xmlFile);
		
		List<Slide> slides = presentation.getSlides();

		for (Slide slide : slides)
		{
			String speakerNotes = slide.getText();
			
			Assert.assertNotNull(speakerNotes);
			Assert.assertTrue(speakerNotes.length() > 0);
		}
	}
	
	@Test
	public void testxmlSpeakerNoteExtraction_Fred_Jasper_Presentation() throws IOException
	{
		String xmlFile = _testPath + "slide-notes-presentation-fred_jasper_20120503.xml";

		Presentation presentation = new Presentation();

		for (int i=0; i<4; i++)
		{
			Slide slide = new Slide();
			presentation.addSlide(slide);
		}

		SpeakerNotesExtrationService.loadSpeakerNotes(presentation, xmlFile);
		
		Assert.assertEquals("Hi Team, this is a collaborative update from Frederik and Jasper on the development of the application server.\n"+
							"There is a new version running on slidespeech dot com, finally with the search functions working, so have a look around the content created so far.\n"+
							"If you run the development version of the android client make sure you download the latest apk from trello to be able to connect to the server.\n"+
							"We have evaluated a couple of things to set up the foundation for the development on the backend side, Frederik will tell you something more about this later.".trim(),
							presentation.getSlide(0).getText().trim());
		
		Assert.assertEquals("But first, what are the new features on the application server? We are on our way to get the core feature set running.\n"+
							"First we can use short youtube style urls for our content now. Much easier to share and to look at.\n"+
							"You can save preferences associated to your user profile, just two options for the beginning, more will follow soon.\n"+
							"And finally there is a search interface to the catalog of generated content. Start browsing through the slides we have available so far".trim(),
							presentation.getSlide(1).getText().trim());
		
		Assert.assertEquals("Hi this is Frederik, with a quick update on the backend development process.\n"+
							"As I started the work two weeks ago, it was quite a mission to get everything setup for development so Jasper and I have been working on a couple of things to make it as easy as possible for new developers to join the team.\n"+
							"We can now distribute the whole application server as a virtual machine (KVM for development on Linux or VMWare on Windows) and connect with Maven for deployment and testing.\n"+
							"To get the tests executed inside the actual running application we chose the arquillian framework. This approach enables you to test and debug in the real  environment and makes testing across different virtual machines a breeze.\n"+
							"Using this toolchain an experienced java developer can set up his development environment quite easily. We could make this even easier by copying a whole preconfigured Eclipse IDE for example if someone just wants to take on a specific smaller task".trim(),
							presentation.getSlide(2).getText().trim());
		
		Assert.assertEquals("This is our agenda for the next weeks, the most important thing for Jasper will be to connect with James work on the design to get rid of the technical look of our system.\n"+
							"Fred will focus on the conversion process first, to make it as robust as possible and give more detailed information to the user. Following the apache lucene search engine wil be integrated.\n"+
							"Then Jasper is going to add some more features to the web application and the endpoints to enable users to add metadata to the catalog as well and connect those to the Lucene search engine\n"+
							"As soon as this is on the way, Jasper will extend the html5 slideview and implement vertical switching between languages.".trim(),
							presentation.getSlide(3).getText().trim());
		
	}	
	
}
