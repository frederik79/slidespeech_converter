package com.slidespeech.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;


public abstract class AbstractService {

	private final static int execTimeout = 60000;//60sec
	
	protected static void execCommand(String cmd) throws ExecuteException, IOException{
		int exitvalue = 0;
		CommandLine command = new CommandLine("/bin/bash");
	    command.addArguments(new String[] {"-c",cmd},false);
	    DefaultExecutor exec = new DefaultExecutor();
	    ExecuteWatchdog watchdog = new ExecuteWatchdog(execTimeout);
	    exec.setWatchdog(watchdog);
	    try{
	    	exitvalue = exec.execute(command);
	    }catch(ExecuteException exe){
	    	if (watchdog.killedProcess()) {
		        // it was killed on purpose by the watchdog
	    		throw new ExecuteException("Timeout executing shell command: "+cmd,exitvalue);
		    }else{
		    	//Other exception during execution
		    	throw exe;
		    }
	    }
	}
	
	protected static String execCommandReturnOutput(String cmd) throws ExecuteException, IOException{
		String ret = "";
		int exitvalue = 0;
		
		CommandLine command = new CommandLine("/bin/bash");
	    command.addArguments(new String[] {"-c",cmd},false);
	    
	    DefaultExecutor exec = new DefaultExecutor();
	    //adding stream handler to get the shell output
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
	    exec.setStreamHandler(streamHandler);
	    ExecuteWatchdog watchdog = new ExecuteWatchdog(execTimeout);
	    exec.setWatchdog(watchdog);
	    try{
	    	exitvalue = exec.execute(command);
	    }catch(ExecuteException exe){
	    	if (watchdog.killedProcess()) {
		        // it was killed on purpose by the watchdog
	    		throw new ExecuteException("Timeout executing shell command: "+cmd,exitvalue);
		    }else{
		    	//Other exception during execution
		    	throw exe;
		    }
	    }finally{
	    	streamHandler.stop();
		    ret = outputStream.toString();
		    outputStream.close();
	    }
		return ret;
	}
}
