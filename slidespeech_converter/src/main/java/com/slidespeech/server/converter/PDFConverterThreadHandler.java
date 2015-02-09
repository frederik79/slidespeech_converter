package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.model.Presentation;

public class PDFConverterThreadHandler extends ConverterThreadHandler
{

	protected Presentation _resultPresentation;
	private String voice = "female";
	
	@Override
	public void convert() throws Exception
	{
		assert(destinationFolder != null);
		assert(fileName != null);
		assert(lang != null);
		
		System.out.println("Converter received pdf conversion: " + destinationFolder
				+ "(" + code + lang + ")");
		
			PDFConverter pdfConversion = new PDFConverter();
						
			_resultPresentation = pdfConversion.convert(destinationFolder, fileName, lang, voice);
			
			assert(_resultPresentation != null);
			
			System.out.println("Converter finished pdf conversion: "
					+ destinationFolder + "(" + fileName + ")");
	}

	@Override
	public void prepareOkMessage(Processed pMsg)
	{
		pMsg.setPresentation(_resultPresentation);
		pMsg.setType("full");
	}
	
	public Presentation getResultPresentation()
	{
		return _resultPresentation;
	}

	@Override
	void setTask(ConversionTask task)
	{
		String fileName = task.getFileName();
		setFileName(fileName);
		setDestionationFolder(task.getPath());
		setLang(task.getLang());
		setCode(task.getCode());
		setUserID(task.getUserID());
		setVoice(task.getVoice());
	}

	private void setVoice(String voice) {
		this.voice = voice;
	}
	
}
