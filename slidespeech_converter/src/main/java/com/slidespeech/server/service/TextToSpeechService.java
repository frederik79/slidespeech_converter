package com.slidespeech.server.service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextToSpeechService extends AbstractService
{
	private final static String TTS = "Cereproc";
	private final static Log _logger = LogFactory.getLog(TextToSpeechService.class);
	
	public static String convert(
			String path, String fileName, String speakerNotes, String language, String voice, String outputFormat) throws Exception
	{
		String cmd;
		String targetFile = path+fileName;
		//tts 
		if (TTS == "Cereproc")
		{
			//cereproc needs text to be converted in xml files
			String preTag = "<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"en\">";
			String ttsVoice = "/opt/cereproc/cerevoice_isabella_3.0.3_22k.voice";
			if(language.equals("en")){
				if(voice.equals("female")){
					preTag += "<voice gender=\"female\" xml:lang=\"en\">";
				}else if(voice.equals("male")){
					preTag += "<voice gender=\"male\" xml:lang=\"en\">";
					ttsVoice = "/opt/cereproc/cerevoice_william_3.0.5_22k.voice";
				}
			}else if(language.equals("de")){
				preTag = "<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis11/synthesis.xsd\" xml:lang=\"de\">";
				preTag += "<voice gender=\"male\" xml:lang=\"de\">";
				ttsVoice =  "/opt/cereproc/cerevoice_alex_3.0.0_beta_22k.voice";
			}
			if(! speakerNotes.trim().startsWith("<speak")){
				speakerNotes = preTag + speakerNotes + "</voice><br/></speak>";
			}
			
			String ttsVoices = createXML4Cereproc(targetFile+".xml", speakerNotes);
			if(ttsVoices.equals("") || ttsVoices.equals("ssml parsing failed")){
				ttsVoices = ttsVoice;
			}

			cmd = "/opt/cereproc/txt2wav "
				+ ttsVoices
				+ " /opt/cereproc/license.lic '"
				+ targetFile + ".xml' '"
				+ targetFile + ".wav'";
		} else
		{
			String text = speakerNotes;
			text.replace('"', ' ').replace('`', ' ')
					.replace(';', ' ');
			// TODO remove/replace all characters that mess up the
			// espeak output
			// (basically everything not 0..9,a..z for espeak -
			// other tts are able to handle things far better)
			cmd = "/usr/bin/espeak -v"+language+" -w '" + targetFile + ".wav' '" + text + "'";
		}
		_logger.info("Text2Speech: " + cmd);
		execCommand(cmd);

		AudioConversionService.convert(targetFile,"wav",outputFormat);
		
		//delete tmp xml & wav files
		cmd = "rm " + targetFile + ".wav " + targetFile + ".xml";
				
		_logger.info("cleanup: " + cmd);
		execCommand(cmd);
		
		return speakerNotes;
	}
	
	private static String createXML4Cereproc(String fileName, String speakernotes) throws IOException
	{
		List<String> voices = new ArrayList<String>();
		
		try{
			Document doc = Jsoup.parse(speakernotes, "");
			doc.outputSettings().prettyPrint(false);
			Elements voiceNodes = doc.select("voice");
			
			for (Element voiceNode : voiceNodes) {
				String lang = ( voiceNode.hasAttr("xml:lang") && ! voiceNode.attr("xml:lang").equals("") ) ? voiceNode.attr("xml:lang") : "en";
	        	String gender = ( voiceNode.hasAttr("gender") && ! voiceNode.attr("gender").equals("") ) ? voiceNode.attr("gender") : "female";
	        	String voiceName = ( voiceNode.hasAttr("name") && ! voiceNode.attr("name").equals("") ) ? voiceNode.attr("name") : "";
	        	
	        	//voice name not set by user -> choose one depending on language and gender
	        	if(voiceName.equals("")){
	        		voiceName = "isabella";//default
	        		//if(lang.equalsIgnoreCase("en") && gender.equalsIgnoreCase("female")) voiceName = "isabella";
	        		if(lang.equalsIgnoreCase("en") && gender.equalsIgnoreCase("male")) voiceName = "william";
	        		if(lang.equalsIgnoreCase("de")) voiceName = "alex";
	        		
	        		voiceNode.attr("name",voiceName);
	        		
	        	}
	        	if(!voices.contains(voiceName)){
	        		voices.add(voiceName);
	        		
	        	}
			}
			
		    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		    out.write(doc.select("body").first().html());
		    //out.write(doc.select("body").first().html());
		    out.close();
		       
	       for (int i = 0; i < voices.size(); i++) {
	    	   if(voices.get(i).equals("william"))voices.set(i, "/opt/cereproc/cerevoice_william_3.0.5_22k.voice");
	    	   if(voices.get(i).equals("isabella"))voices.set(i, "/opt/cereproc/cerevoice_isabella_3.0.3_22k.voice");
	    	   if(voices.get(i).equals("alex"))voices.set(i, "/opt/cereproc/cerevoice_alex_3.0.0_beta_22k.voice");
	   		}
		}catch (Exception e){
			//Fallback if ssml parsing fails
			Writer out = new OutputStreamWriter(new FileOutputStream(fileName));
			try
			{
				out.write(speakernotes);
			} finally
			{
				out.close();
			}
			voices.add("ssml parsing failed");
		}

		return StringUtils.join(voices, ",");
	}

}
