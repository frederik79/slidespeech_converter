package com.slidespeech.server.service;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AudioConversionService extends AbstractService
{
	private final static Log _logger = LogFactory.getLog(AudioConversionService.class);
	
	public static void convert(String targetFile,String inputFormat,String outputFormat) throws ExecuteException, IOException {
		String cmd;
		if(outputFormat.equals("both")){
			cmd = getOutputCommand(targetFile,inputFormat,"ogg");
			execCommand(cmd);
			_logger.info("ogg-enc: " + cmd);
			cmd = getOutputCommand(targetFile,inputFormat,"mp3");
			execCommand(cmd);
			_logger.info("mp3-enc: " + cmd);
		}else{
			cmd = getOutputCommand(targetFile,inputFormat,outputFormat);
			execCommand(cmd);
			_logger.info(outputFormat+"-enc: " + cmd);
		}
		
	}
	//mpg321 input.mp3 -w - | oggenc -o output.ogg -
	//oggdec -Q -o - 6016777.ogg | lame -h - 01.mp3
	private static String getOutputCommand(String targetFile,String inputFormat,String outputFormat){
		String cmd = "";
		if(inputFormat.equals("wav")){
			
			if(outputFormat.equals("ogg")){
				cmd = "oggenc -o '" + targetFile + ".ogg' '" + targetFile + ".wav'";
			
			}else{
				cmd = "lame -V2 '" + targetFile + ".wav' '" + targetFile + ".mp3'";
			}
		}else if(inputFormat.equals("mp3")){
				
				if(outputFormat.equals("ogg")){
					cmd = "mpg321 " + targetFile + ".mp3 -w - | oggenc -o " + targetFile + ".ogg -";
				}
				
		}else if(inputFormat.equals("ogg")){
				
				if(outputFormat.equals("mp3")){
					cmd = "oggdec -Q -o - " + targetFile + ".ogg | lame -h - " + targetFile + ".mp3";
				}
				
		}else if(inputFormat.equals("m4a")){
			
			if(outputFormat.equals("ogg")){
				cmd = "faad " + targetFile + ".m4a -o - | oggenc -o " + targetFile + ".ogg -";
			
			}else{
				cmd = "faad " + targetFile + ".m4a -o - | lame -h - " + targetFile + ".mp3";
			}
			
		}else if(inputFormat.equals("3gp")){
			
			if(outputFormat.equals("ogg")){
				cmd = "ffmpeg -i " + targetFile + ".3gp -f ogg -acodec libvorbis -aq 5 " + targetFile + ".ogg";
			}else{
				cmd = "ffmpeg -i " + targetFile + ".3gp " + targetFile + "." + outputFormat;
			}
		}
		return cmd;
	}
	
}