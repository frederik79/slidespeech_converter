package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.model.Presentation;

public class ZIPConverterThreadHandler extends ConverterThreadHandler
{
	protected Presentation _resultPresentation;
	private String voice = "female";
	
	@Override
	public void convert() throws Exception
	{
		assert (destinationFolder != null);
		assert (fileName != null);
		assert (lang != null);

		System.out.println("Converter received zip conversion: " + destinationFolder + "("
				+ code + lang + ")");

		ZipConverter fullConversion = new ZipConverter();
		_resultPresentation = fullConversion.convert(destinationFolder,
				fileName, lang, voice);

		System.out.println("Converter finished zip conversion: "
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
