package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.model.Presentation;

public class FullConversionThreadHandler extends ConverterThreadHandler
{
	protected Presentation _resultPresentation;
	private String voice = "female";
	
	@Override
	public void convert() throws Exception
	{
		System.out
		.println("Converter received full conversion: " + fileName);
		
		assert(destinationFolder != null);
		assert(fileName != null);
		assert(lang != null);
		
		
			FullConversion fullConversion = new FullConversion();
			_resultPresentation = fullConversion.convert(destinationFolder, fileName, lang, voice);
	}

	public void prepareOkMessage(Processed pMsg)
	{
		
		pMsg.setLang(lang);
		pMsg.setCode(code);
		pMsg.setUserID(userID);
		
		Presentation resultPresentation = getResultPresentation();
		
		pMsg.setPresentation(resultPresentation);
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
		setVoice(task.getVoice());
	}

	private void setVoice(String voice) {
		this.voice = voice;
	}
}
