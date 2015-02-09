package com.slidespeech.server.converter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class VideoConversion {

	protected final Log _logger;
	private String targetDir;
	
	public VideoConversion() 
	{
		_logger = LogFactory.getLog(getClass());
	}
	
	public String convert(String targetDir, String code, String lang, int version)
			throws IllegalArgumentException, ExecuteException, IOException {
		
		this.targetDir = targetDir;
		_logger.info("video conv! dir: " + this.targetDir + " ,pres: "+code+lang);
		
		//check number of slides of presentation
		File directory = new File(targetDir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String fileName) 
			{
				//only include slide files
				return fileName.matches("slide_.*.mp3");
			    //return fileName.endsWith(".mp3");
			}
		};
		File[] listOfFiles = directory.listFiles(filter);

		ArrayList<String> fileNames = new ArrayList<String>();

		for (File file : listOfFiles) 
		{
			fileNames.add(file.getName());
		}
		
		VideoConversion.sortMp3FileNamesBasedOnSlideNumber(fileNames);
		
		String concatFileNames = "";
		
		for (int i = 0; i < fileNames.size(); i++) 
		{
			String audioFileName = fileNames.get(i);
			
			//create one video for each slide
			//String audioFileName = listOfFiles[i].getName();
			
			String audio_length = this.detect_mp3_length(targetDir+audioFileName);
			_logger.debug(audioFileName +" LEN:" + audio_length );
			concatFileNames += this.export_slide(i,audioFileName,audio_length) + " ";
		}
		//concatenate videos
		CommandLine commandConcat = new CommandLine("/bin/bash");
		commandConcat.addArguments(new String[] {
	            "-c",
	            "avimerge -o "+this.targetDir+"slidespeech_" + code + lang + "_v" + version + ".avi -i "+concatFileNames
	    },false);
		
		_logger.debug("concat avi: "+ commandConcat.toString() );
	    DefaultExecutor exec = new DefaultExecutor();
		
		exec.execute(commandConcat);
		//delete tmp video files
		File dir = new File(this.targetDir);
		FileFilter fileFilter = new WildcardFileFilter("tmp_*.avi");
		File[] tmpFiles = dir.listFiles(fileFilter);
		for (int i = 0; i < tmpFiles.length; i++) {
			tmpFiles[i].delete();
		}
		
		
		return "slidespeech_" + code + lang + "_v" + version + ".avi";
		
	}
	
	private String export_slide(int slideNo, String audioFileName, String mp3len) {
		//create tmp-video file for one slide combining image and audio file
		CommandLine command = new CommandLine("/bin/bash");
	    command.addArguments(new String[] {
	            "-c",
	            "ffmpeg -loop_input -t "+mp3len+" -y -i "+this.targetDir+"slide_"+slideNo+".png -i "+this.targetDir+audioFileName+" -acodec copy -vcodec libx264 "+this.targetDir+"tmp_"+slideNo+".avi"
	    },false);
	    DefaultExecutor exec = new DefaultExecutor();
	    try {
			exec.execute(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return this.targetDir+"tmp_"+slideNo+".avi";
	}
	
	private String detect_mp3_length(String filename) {
		String mp3Len = "00:00:00.00";
		try {
			//parsing ffmpegs output for length of audio file 
		    CommandLine command = new CommandLine("/bin/bash");
		    command.addArguments(new String[] {
		            "-c",
		            "ffmpeg -i "+filename+" 2>&1 | grep Duration | cut -f1 -d, | cut -f2,3,4,5 -d:"
		    },false);
		    
		    DefaultExecutor exec = new DefaultExecutor();
		    //adding stream handler to get the shell output
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		    exec.setStreamHandler(streamHandler);
		    ExecuteWatchdog watchdog = new ExecuteWatchdog(120000);//60sec
		    exec.setWatchdog(watchdog);
		    int exitvalue = exec.execute(command);
		    
		    streamHandler.stop();
		    mp3Len = outputStream.toString();
		    outputStream.close();
		    
		    if (exec.isFailure(exitvalue) && watchdog.killedProcess()) {
		        // it was killed on purpose by the watchdog
		    }
		  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp3Len.trim();
	}

	public static void sortMp3FileNamesBasedOnSlideNumber(ArrayList<String> list) 
	{
		Comparator<String> comparator = new Comparator<String>() 
		{
			@Override
			public int compare(String o1, String o2) 
			{
				return getFirstNumberInString(o1).compareTo(getFirstNumberInString(o2));
			}

			private Integer getFirstNumberInString(String str) 
			{
				String number = "";
				boolean numberMode = false;
				for (int i = 0; i < str.length(); i ++)
				{
					char charAt = str.charAt(i);
					
					String temp = "" + charAt;

					if (temp.matches("[0-9]"))
					{
						number += temp;
						numberMode = true;
					}
					else if (numberMode)
					{
						break;
					}
				}
				int parseInt = Integer.parseInt(number);

				return parseInt;
			}
		};
	
		Collections.sort(list, comparator);
	}
	
}
