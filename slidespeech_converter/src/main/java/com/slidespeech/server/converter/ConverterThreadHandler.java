package com.slidespeech.server.converter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;

import com.slidespeech.server.message.AbstractMessage;
import com.slidespeech.server.message.ConversionTask;
import com.slidespeech.server.message.Error;
import com.slidespeech.server.message.Processed;

public abstract class ConverterThreadHandler
{
	private CountDownLatch latch;
	protected Message _message;
	protected String destinationFolder;
	protected String fileName;
	protected String lang;
	protected String code;
	protected int userID;
	protected Exception exception;
	private AbstractMessage _response;
	private boolean completed = false;

	public ConverterThreadHandler()
	{
		// Converter Callback - without sending jms message
		latch = new CountDownLatch(1);
		_message = null;
		exception = null;
		completed = false;
	}

	public void completed()
	{
		if (_message != null)
		{
			sendMessage();
		}
		latch.countDown();
		completed = true;
	}
	
	
	public void startConversionThread()
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					convert();
				} 
				catch (Exception e)
				{
					exception = e;
					
					System.out.println("Converter Exception : ");
					e.printStackTrace();
					System.out.println();
					
				}
				completed();
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
	}
	

	private void sendMessage()
	{
		
		_response = null;
		
		if (exception == null)
		{
			_response = new Processed();
			_response.setLang(lang);
			_response.setCode(code);
			_response.setUserID(userID);
			
			prepareOkMessage((Processed) _response);
		}
		else
		{
			_response = new Error();
			prepareErrorMessage((Error) _response);
		}
		
		try
		{
			if (_message.getJMSReplyTo() != null)
			{
				// synchronous reply requested
				MessagePublisher.getInstance().sendMessage(_response,
						_message.getJMSReplyTo());
			} else
			{
				// send reply on processedQueue
				MessagePublisher.getInstance().sendMessage(_response);
			}
		} catch (JMSException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean waitUntilThreadCompletion()
	{
		boolean result = true;

		try
		{
			result = latch.await(600, TimeUnit.SECONDS);
		} catch (InterruptedException e1)
		{
			e1.printStackTrace();
			result = false;
		}

		return result;
	}

	abstract public void convert() throws Exception;
	abstract public void prepareOkMessage(Processed pMsg);
	abstract void setTask(ConversionTask task);

	public void setDestionationFolder(String destionationFolder)
	{
		this.destinationFolder = destionationFolder;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}
	
	public void setCode(String code)
	{
		this.code = code;
	}
	
	public void setUserID(int userID)
	{
		this.userID = userID;
	}
	
	public void setMessage(Message message)
	{
		_message = message;
	}

	private void prepareErrorMessage(Error eMsg)
	{
		
		eMsg.setUserID(userID);
		eMsg.setLang(lang);
		eMsg.setCode(code);
		
		assert(exception != null);
		
		eMsg.setErrormessage(exception.getMessage() != null ? exception.getMessage()
				: exception.toString());
		eMsg.setStacktrace(this.stackTrace2String(exception));
	}
	
	private String stackTrace2String(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String myException = sw.toString();
		try
		{
			sw.close();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		pw.close();
		return myException;
	}

	public boolean isCompleted()
	{
		return completed;
	}
	
	public Exception getException()
	{
		return exception;
	}
	
}
