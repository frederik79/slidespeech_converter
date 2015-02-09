package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.model.Presentation;

public class ImageConverterThreadHandler extends ConverterThreadHandler
{

	private String speakernotes;
	protected Presentation _resultPresentation;
	private String voice = "female";
	
	public void setSpeakernotes(String speakernotes)
	{
		this.speakernotes = speakernotes;
	}

	@Override
	public void convert() throws Exception
	{
		assert(destinationFolder != null);
		assert(fileName != null);
		assert(lang != null);
		assert(speakernotes != null);
		
		System.out.println("Converter received img conversion: " + destinationFolder
				+ "(" + code + lang + ")");
		
		ImgConverter imgConversion = new ImgConverter();
			
		_resultPresentation = imgConversion.convert(destinationFolder, fileName, lang, voice, speakernotes);
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
		setFileName(task.getFileName());
		setDestionationFolder(task.getPath());
		setLang(task.getLang());
		setCode(task.getCode());
		setUserID(task.getUserID());
		setSpeakernotes(task.getSpeakernotes());
		setVoice(task.getVoice());
	}

	private void setVoice(String voice) {
		this.voice = voice;
	}
}
