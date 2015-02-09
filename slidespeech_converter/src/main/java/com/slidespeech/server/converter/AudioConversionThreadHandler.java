package com.slidespeech.server.converter;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Processed;
import com.slidespeech.server.service.AudioConversionService;

public class AudioConversionThreadHandler extends ConverterThreadHandler
{
	private String outputFormat;
	public void setOutputFormat(String outputFormat)
	{
		this.outputFormat = outputFormat;
	}

	public void setInputFormat(String inputFormat)
	{
		this.inputFormat = inputFormat;
	}

	private String inputFormat;

	@Override
	public void convert() throws Exception
	{
		System.out.println("Converter received audio conversion for: "
				+ code + lang + "");
		
		assert (destinationFolder != null);
		assert (inputFormat != null);
		assert (outputFormat != null);

			AudioConversionService.convert(destinationFolder + fileName,
					inputFormat, outputFormat);
			
			System.out.println("Converter finished audio conversion: "
					+ destinationFolder + fileName + "."
					+ outputFormat);
			
	}

	@Override
	public void prepareOkMessage(Processed pMsg)
	{
		pMsg.setType("audio");
		pMsg.setFileName(fileName);
	}

	@Override
	void setTask(ConversionTask task)
	{
		String baseFileName = task.getFileName().substring(0,
				task.getFileName().length() - 4);
		
		setFileName(baseFileName);
		setDestionationFolder(task.getPath());
		setLang(task.getLang());
		setCode(task.getCode());
		setUserID(task.getUserID());

		String ext = task.getFileName().substring(
				task.getFileName().length() - 3);

		setInputFormat(ext);
		setOutputFormat(task.getFormat());
		
	}

}
