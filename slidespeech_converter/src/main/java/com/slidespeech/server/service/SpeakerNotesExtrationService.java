package com.slidespeech.server.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;

public class SpeakerNotesExtrationService
{
	public synchronized static void loadSpeakerNotes(
			final Presentation presenation, String xmlFileName)
			throws IOException
	{
		File xmlFile = new File(xmlFileName);
		if (!xmlFile.exists())
		{
			throw new IllegalArgumentException("cannot find xmlFile "
					+ xmlFileName);
		}
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler()
			{
				boolean paragraph = false;
				private int currentSlide = -1;
				private StringBuilder sb;

				synchronized public void startElement(String uri,
						String localName, String qName, Attributes attributes)
						throws SAXException
				{
					if (qName.equalsIgnoreCase("paragraph"))
					{
						sb=new StringBuilder();
						paragraph = true;
						currentSlide++;
					}
				}

				synchronized public void endElement(String uri,
						String localName, String qName) throws SAXException
				{
					if (qName.equalsIgnoreCase("paragraph"))
					{
						String speakerNotes = sb.toString().trim();
						
						try
						{
							speakerNotes = URLDecoder.decode(speakerNotes, "UTF-8");
						} catch (UnsupportedEncodingException e)
						{
							e.printStackTrace();
						}
						
						Slide slide = presenation.getSlide(currentSlide);
						slide.setText(speakerNotes);
						paragraph = false;
					}
				}
				
				synchronized public void characters(char ch[], int start,
						int length) throws SAXException
				{

					if (paragraph)
					{
						
						if (sb!=null)
						{
					        for (int i=start; i<start+length; i++) {
					            sb.append(ch[i]);
					        }
					    }

						
						
//					
					}
				}
			};

			//saxParser.parse(xmlFileName, handler);
			
		  InputStream inputStream= new FileInputStream(xmlFileName);
   	      Reader reader = new InputStreamReader(inputStream,"UTF-8");

   	      InputSource is = new InputSource(reader);
   	      is.setEncoding("UTF-8");
			
		  saxParser.parse(is, handler);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
