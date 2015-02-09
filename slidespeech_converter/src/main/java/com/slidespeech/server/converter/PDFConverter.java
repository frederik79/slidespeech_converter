package com.slidespeech.server.converter;

import java.io.IOException;
import java.util.ArrayList;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.PdfToImageService;
import com.sun.star.lang.IllegalArgumentException;

public class PDFConverter implements Converter

{

	public Presentation convert(String targetDir, String filename, String lang, String voice)
			throws IllegalArgumentException
	{
		Presentation presentation = new Presentation();
		
		ArrayList<Slide> slides;
		try
		{
			System.out.println("fffff - 1 - ");
			slides = PdfToImageService.convert(targetDir +"/"+ filename, targetDir);
			System.out.println("fffff - 2 - ");
			
			for (Slide slide : slides)
			{
				presentation.addSlide(slide);
			}

			System.out.println("fffff - 3 - ");
			//presentation.createJsonOutput(targetDir);
			
			System.out.println("fffff - 4 - ");
			presentation.convertSpeakerNotesToMp3(targetDir, lang, voice);
			
			System.out.println("fffff - 5 - ");
			presentation.createThumbnail(targetDir);
			//presentation.createLowResSlides(targetDir);

			System.out.println("fffff - 6 - ");
			//presentation.createZipFiles(targetDir);
			
			System.out.println("fffff - 7 - ");
			presentation.cleanupDirectory(targetDir);
			System.out.println("fffff - 8 - ");
			
		} catch (IOException e)
		{
			System.out.println("zzzzz - 7 - ");
			e.printStackTrace();
			System.out.println("zzzzz - 7 - ");
			
			throw new RuntimeException("Conversion Error");
		}

		assert(presentation != null);
		
		return presentation;
	}

	@Override
	public String getOpsName()
	{
		// TODO Auto-generated method stub
		return "PDF Converter";
	}
}
