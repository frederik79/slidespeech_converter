package com.slidespeech.server.converter;


import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OpenOfficeServiceTest {

	@Deployment
	public static Archive<?> createTestArchive() 
	{
		return TestHelper.createMultipleInstanceTestArchive();
	}

	@Test
	public void testgetOpenOfficeProcessID() throws IOException 
	{
		OpenOfficeService openOfficeService = new OpenOfficeService();
		
		boolean running = openOfficeService.isRunning();
		
		if (!running)
		{
			boolean startService = openOfficeService.startService();
			Assert.assertTrue(startService);
		}
		
		int processID = openOfficeService.getProcessID();
		
		Assert.assertTrue(processID > 0);
	}
	
	@Test
	public void testStopStartOpenOfficeService() throws IOException 
	{
		OpenOfficeService openOfficeService = new OpenOfficeService();
		
		boolean running = openOfficeService.isRunning();
		
		if (!running)
		{
			boolean startService = openOfficeService.startService();
			Assert.assertTrue(startService);
		}
		
		boolean stopService = openOfficeService.stopService();

		Assert.assertTrue(stopService);
		
		Assert.assertFalse(openOfficeService.isRunning());
		
		stopService = openOfficeService.stopService();

		Assert.assertTrue(stopService);
		
		boolean startService = openOfficeService.startService();
		Assert.assertTrue(startService);
		
		Assert.assertTrue(openOfficeService.isRunning());
	}
}









