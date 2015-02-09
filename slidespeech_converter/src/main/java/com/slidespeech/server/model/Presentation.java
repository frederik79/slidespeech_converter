package com.slidespeech.server.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.slidespeech.server.converter.TextToSpeechConversionThreadHandler;
import com.slidespeech.server.service.TextToSpeechService;

public class Presentation
{

	private static final String TTS = "Cereproc";

	public List<Slide> S;
	private boolean lowres;
	protected final Log _logger;

	public Presentation()
	{
		this.S = new ArrayList<Slide>();
		this.lowres = false;
		this._logger = LogFactory.getLog(getClass());
	}

	@JsonIgnore
	public List<Slide> getSlides()
	{
		return this.S;
	}

	@JsonIgnore
	public Slide getSlide(int index)
	{
		return this.S.get(index);
	}

	public void addSlide(Slide newSlide)
	{
		this.S.add(newSlide);
	}

	/*public boolean createJsonOutput(String targetDir) throws IOException,
			JsonGenerationException, JsonMappingException
	{
		boolean result = false;

		// check if json file exists

		File jsonFile = new File(targetDir + "data.json");

		if (jsonFile.exists())
		{
			jsonFile.delete();
		}
		assert (!jsonFile.exists());

		// write JSON to a file
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(jsonFile, this);

		if (jsonFile.exists())
		{
			result = true;
		}
		return result;
	}*/

	public void convertSpeakerNotesToMp3(String targetDir,
			String lang, String voice)
	{
		int pageNo = getSlides().size();
		
		MaxConversionParallelHandler maxConversionParallelHandler = new MaxConversionParallelHandler(8);
		
		for (int i = 0; i < pageNo; i++)
		{
			System.out.println("convert Slide " + i);
			
			boolean waitUntilNewConversionCanBeAccepted = maxConversionParallelHandler.waitUntilNewConversionCanBeAccepted();

			//assert(waitUntilNewConversionCanBeAccepted);
			
			// text2speech
			List<Slide> slides = getSlides();

			assert (slides.size() < i);

			TextToSpeechConversionThreadHandler threadHandler = new TextToSpeechConversionThreadHandler();

			threadHandler.setDestionationFolder(targetDir);
			threadHandler.setFileName("slide_" + i);
			threadHandler.setSpeakerNotes(slides.get(i).getText());
			threadHandler.setOutputFormat("both");
			threadHandler.setLang(lang);
			threadHandler.setVoice(voice);
			threadHandler.setOutputSlide(getSlide(i));

			threadHandler.startConversionThread();
			
			maxConversionParallelHandler.addConversion(threadHandler);
		}
		
		maxConversionParallelHandler.waitUntilAllConversionsFinished();
	}
	
	/*public void createZipFiles(String targetDir)
	{
		// create zip files
		_logger.info("Creating zip file");
		createZip(targetDir);

		if (this.lowres)
		{
			_logger.info("Creating lowres-zip file");
			createZip(targetDir + "lowres/");
			// move to main dir
			File zipfile = new File(targetDir + "lowres/output.zip");
			zipfile.renameTo(new File(targetDir + "output.zip"));
		}

	}*/

	public void cleanupDirectory(String targetDir)
	{
		String[] cmd;
		// delete lowres source files & .wavs / tts-XMLs
		try
		{
			if (this.lowres)
			{
				/*
				 * cmd = new String[] { "bash", "-c", "mv " + targetDir +
				 * "lowres/output.zip " + targetDir + "lowres.zip" };
				 * Runtime.getRuntime().exec(cmd);
				 */
				cmd = new String[]
				{ "bash", "-c", "rm -R " + targetDir + "lowres/" };
				Runtime.getRuntime().exec(cmd);
			}

			cmd = new String[]
			{ "bash", "-c", "rm " + targetDir + "*.wav" };
			Runtime.getRuntime().exec(cmd);
			if (TTS == "Cereproc")
			{
				cmd = new String[]
				{ "bash", "-c", "rm " + targetDir + "*.xml" };
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createZip(String targetDir)
	{
		// These are the files to include in the ZIP file
		List<File> fileList = new ArrayList<File>();

		File directoryToZip = new File(targetDir);
		File[] files = directoryToZip.listFiles();
		for (File file : files)
		{
			// png|json|jpg files only
			if (file.getName().matches("([^\\s]+(\\.(?i)(png|json|jpg))$)"))
			{
				fileList.add(file);
			}
		}
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try
		{
			// Create the ZIP file
			String target = targetDir + "output.zip";
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					target));

			// Compress the files
			for (File file : fileList)
			{
				FileInputStream in = new FileInputStream(file);

				// we want the zipEntry's path to be a relative path to the
				// directory being zipped
				// so chop off the rest of the path
				String zipFilePath = file.getCanonicalPath().substring(
						directoryToZip.getCanonicalPath().length() + 1,
						file.getCanonicalPath().length());
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(zipFilePath));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}

			// Complete the ZIP file
			out.close();

		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.io.IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createThumbnail(String targetDir)
	{
		// create thumbnail:
		if (this.getSlide(0) != null)
		{
			String[] cmd;

			String firstSlideFN = this.getSlide(0).getImage();
			try
			{
				cmd = new String[]
				{
						"bash",
						"-c",
						"convert "
								+ targetDir
								+ firstSlideFN
								+ " -resize x100 -background white -flatten -quality 82 "
								+ targetDir + "thumb.jpg" };
				Process p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
				cmd = new String[]
				{
						"bash",
						"-c",
						"cp " + targetDir + "thumb.jpg " + targetDir
								+ "lowres/" };
				Runtime.getRuntime().exec(cmd);
				_logger.info("thumbnail created");

			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (java.io.IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
		{
			_logger.error("create thumbnail failed, first slide not available");
		}

	}

	public void createLowResSlides(String targetDir)
	{
		String[] cmd;
		this.lowres = true;

		// create low-res version
		_logger.info("low res images");
		new File(targetDir + "/lowres").mkdir();
		try
		{
			cmd = new String[]
			{
					"bash",
					"-c",
					"cd "
							+ targetDir
							+ ";for i in $(ls *.png); do convert $i -resize 640 -quality 95 -colors 256 -depth 8 lowres/$i; done" };
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			cmd = new String[]
			{ "bash", "-c",
					"cp " + targetDir + "data.json " + targetDir + "lowres/" };
			Runtime.getRuntime().exec(cmd);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.io.IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createSlideshareImages(String fileName, String targetDir) 
	{
		
		
	}

}
