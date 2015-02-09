package com.slidespeech.server.converter;

import java.io.IOException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.model.MaxConversionParallelHandler;

public class ConversionListener implements MessageListener
{

	private ObjectMapper mapper = new ObjectMapper();
	
	private static MaxConversionParallelHandler maxConversionParallelHandler = new MaxConversionParallelHandler(1);
	
	public void onMessage(Message message)
	{
		try
		{
			
			String messageJSON = ((TextMessage) message).getText();

			ConversionTask task = mapper.readValue(messageJSON,
					ConversionTask.class);

			ConverterThreadHandler converter = null;

			if (task.getMethod().equals("ttsConversion"))
			{
				converter = new TextToSpeechConversionThreadHandler();

			} else if (task.getMethod().equals("audioConversion"))
			{
				converter = new AudioConversionThreadHandler();

			} else if (task.getMethod().equals("fullConversion"))
			{
				converter = new FullConversionThreadHandler();

			} else if (task.getMethod().equals("videoConversion"))
			{
				converter = new VideoConversionThreadHandler();

			} else if (task.getMethod().equals("pdfConversion"))
			{
				converter = new PDFConverterThreadHandler();

			} else if (task.getMethod().equals("zipConversion"))
			{
				converter = new ZIPConverterThreadHandler();

			} else if (task.getMethod().equals("imgConversion"))
			{
				converter = new ImageConverterThreadHandler();
			}

			converter.setTask(task);
			converter.setMessage(message);

			converter.startConversionThread();
			//converter.waitUntilThreadCompletion();
			
			maxConversionParallelHandler.addConversion(converter);
			
			boolean waitUntilNewConversionCanBeAccepted = maxConversionParallelHandler.waitUntilNewConversionCanBeAccepted();
			assert(waitUntilNewConversionCanBeAccepted);

		} catch (JMSException e)
		{
			e.printStackTrace();
		} catch (JsonParseException e)
		{
			e.printStackTrace();
		} catch (JsonMappingException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}