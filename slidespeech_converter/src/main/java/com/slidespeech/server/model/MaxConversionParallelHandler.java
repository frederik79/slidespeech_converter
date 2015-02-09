package com.slidespeech.server.model;

import java.util.ArrayList;

import com.slidespeech.server.converter.ConverterThreadHandler;

public class MaxConversionParallelHandler
{
	private static final int MaxTimeOut = 40;
	private int _maxNumberofActiveConversions;
	ArrayList<ConverterThreadHandler> activeConversions;

	public MaxConversionParallelHandler(int numberofMaxActiveConversions)
	{
		activeConversions = new ArrayList<ConverterThreadHandler>();
		this._maxNumberofActiveConversions = numberofMaxActiveConversions;
	}

	synchronized public boolean waitUntilNewConversionCanBeAccepted()
	{
		int timeOutCounter = 0;
		boolean ok = true;

		cleanInactiveConversions();

		while (timeOutCounter < MaxTimeOut)
		{
			timeOutCounter++;

			int numberOfActiveThreads = getNumberOfActiveConversion();

			if (numberOfActiveThreads >= _maxNumberofActiveConversions)
			{
				try
				{
					System.out.println("wait for Thread to Finish " + " numberOfActiveThreads " + numberOfActiveThreads + " maxNumberofActiveConversions" + _maxNumberofActiveConversions);
					Thread.sleep(2000);
				} catch (InterruptedException e)
				{
					System.out.println("Interrupted Exception");
					break;
				}
			} else
			{
				break;
			}
		}

		if (timeOutCounter >= MaxTimeOut)
		{
			ok = false;
		}
		return ok;
	}

	private void cleanInactiveConversions()
	{
		if (activeConversions.size() > 0)
		{
			for (int i = activeConversions.size(); i > 0; i--)
			{
				ConverterThreadHandler converterThreadHandler = activeConversions
						.get(i - 1);

				if (converterThreadHandler.isCompleted())
				{
					activeConversions.remove(i - 1);
				}
			}
		}

	}

	public void addConversion(ConverterThreadHandler handler)
	{

		activeConversions.add(handler);
	}

	public boolean waitUntilAllConversionsFinished()
	{
		for (ConverterThreadHandler conversion : activeConversions)
		{
			conversion.waitUntilThreadCompletion();
		}

		return true;
	}

	public int getNumberOfActiveConversion()
	{
		int activeThreadCounter = 0;

		for (ConverterThreadHandler converterThreadHandler : activeConversions)
		{
			if (!converterThreadHandler.isCompleted())
			{
				activeThreadCounter++;
			}
		}
		return activeThreadCounter;
	}

}
