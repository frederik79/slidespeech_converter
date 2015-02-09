package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;

public class VideoConversionThreadHandler extends ConverterThreadHandler
{

	private int version;
	private String videoFilename;
	
	public void setVersion(int version)
	{
		this.version = version;
	}

	public VideoConversionThreadHandler()
	{
		version = 0;
	}
	
	public String getVideoFilename()
	{
		return videoFilename;
	}

	@Override
	public void convert() throws Exception
	{
		System.out.println("Converter received video conversion: "
				+ destinationFolder + "(" + code + lang + ")");
		
		assert(destinationFolder != null);
		assert(code != null);
		assert(lang != null);
		
		VideoConversion conv = new VideoConversion();
		videoFilename = conv.convert(destinationFolder,code,lang,version);
		
		System.out.println("user:" + userID
				+ "Converter finished video conversion: " + destinationFolder
				+ "(" + videoFilename + ")");
	}

	@Override
	public void prepareOkMessage(Processed pMsg)
	{
		pMsg.setFileName(videoFilename);
		pMsg.setType("video");
	}

	@Override
	void setTask(ConversionTask task)
	{
		int version = task.getVersion();
		setDestionationFolder(task.getPath());
		setLang(task.getLang());
		setCode(task.getCode());
		setUserID(task.getUserID());
		setVersion(version);
		
	}
	
}
