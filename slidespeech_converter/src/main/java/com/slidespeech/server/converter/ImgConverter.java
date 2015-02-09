package com.slidespeech.server.converter;

import java.io.IOException;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.ImageAdjustService;
import com.sun.star.lang.IllegalArgumentException;

public class ImgConverter
{

	public Presentation convert(String targetDir, String imgFileName, String lang, String voice, String speakernotes) throws IllegalArgumentException
	{
		Presentation presentation = new Presentation();

		try
		{

			Slide slide = ImageAdjustService.convert(targetDir,imgFileName,"slide_0");
			
			slide.setText(speakernotes);
			
			presentation.addSlide(slide);
			
			presentation.convertSpeakerNotesToMp3(targetDir, lang, voice);

			//presentation.createJsonOutput(targetDir);
			
			presentation.createThumbnail(targetDir);
			//presentation.createLowResSlides(targetDir);

			//presentation.createZipFiles(targetDir);
			presentation.cleanupDirectory(targetDir);
			

		} catch (IOException e)
		{
			throw new IllegalArgumentException("Invalid Img File");
		}

		return presentation;
	}

}
