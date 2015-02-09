package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.TextToSpeechService;

public class TextToSpeechConversionThreadHandler extends ConverterThreadHandler
{
	private String speakerNotes;
	private String outputFormat;
	private String voice = "female";
	private Slide outputSlide = null;
	
	@Override
	public void convert() throws Exception
	{
		System.out.println("Converter received tts preview for: "
				+ code + lang + "");
		
			this.setSpeakerNotes(TextToSpeechService.convert(destinationFolder, fileName, speakerNotes, lang, voice, outputFormat));
			if(this.getOutputSlide() != null){
				this.getOutputSlide().setText(this.getSpeakerNotes());
			}
			
			System.out.println("Converter finished tts preview: "
					+ destinationFolder + fileName + "."
					+ outputFormat);
	}
	
	public void setSpeakerNotes(String speakerNotes)
	{
		this.speakerNotes = speakerNotes;
	}
	
	public void setLang(String lang)
	{
		this.lang = lang;
	}
	
	public void setOutputFormat(String type)
	{
		this.outputFormat = type;
	}


	@Override
	public void prepareOkMessage(Processed pMsg)
	{
		pMsg.setType("tts");
		if (outputFormat.equals("both"))
		{
			pMsg.setFileName(fileName);
		} else
		{
			pMsg.setFileName(fileName + "."
					+ outputFormat);
		}
		pMsg.setSpeakernotes(this.getSpeakerNotes());
	}

	@Override
	void setTask(ConversionTask task)
	{
		setFileName(task.getFileName());
		setDestionationFolder(task.getPath());
		setLang(task.getLang());
		setCode(task.getCode());
		setUserID(task.getUserID());
		setSpeakerNotes(task.getSpeakernotes());
		setOutputFormat(task.getFormat());
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}
	
	public String getSpeakerNotes() {
		return speakerNotes;
	}
	
	public Slide getOutputSlide() {
		return outputSlide;
	}

	public void setOutputSlide(Slide outputSlide) {
		this.outputSlide = outputSlide;
	}
}
