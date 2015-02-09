package com.slidespeech.server.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.slidespeech.server.service.PdfToImageService;
import com.slidespeech.server.service.SpeakerNotesExtrationService;
import com.sun.star.lang.IllegalArgumentException;

public class ZipConverter implements Converter
{

	public Presentation convert(String targetDir, String zipFileName,
			String lang, String voice) throws IllegalArgumentException
	{
		Presentation presentation = new Presentation();
		String zipFileDirectory = "/zipFile";
		String zipTargetDirectory = targetDir + zipFileDirectory;

		try
		{
			createEmptyDirectory(zipTargetDirectory);
			String zipFileNameComplete = targetDir + zipFileName;
			unZipIt(zipFileNameComplete, zipTargetDirectory);

			String pdfFileName = zipFileDirectory + "/CONTENTS/contents.pdf";

			File file = new File(targetDir+pdfFileName);
			if (!file.exists())
			{
				throw new IllegalArgumentException("Invalid Zip File");
			}
			
			ArrayList<Slide> slides;
			slides = PdfToImageService.convert(targetDir +"/"+ pdfFileName, targetDir);
			for (Slide slide : slides)
			{
				presentation.addSlide(slide);
			}
			
			String xmlFileComplete = zipTargetDirectory
					+ "/CONTENTS/slide-notes.xml";

			File xmlFile = new File(xmlFileComplete);
			
			if(xmlFile.exists())
			{
				SpeakerNotesExtrationService.loadSpeakerNotes(presentation,
						xmlFileComplete);
				
				presentation.convertSpeakerNotesToMp3(targetDir, lang, voice);
				
			}

			//presentation.createJsonOutput(targetDir);
			
			presentation.createThumbnail(targetDir);
			//presentation.createLowResSlides(targetDir);

			//presentation.createZipFiles(targetDir);
			presentation.cleanupDirectory(targetDir);
			
			//remove temp unzip dir
			FileUtils.deleteDirectory(new File(zipTargetDirectory));

		} catch (IOException e)
		{
			throw new IllegalArgumentException("Invalid Zip File");
		}

		return presentation;
	}

	private void createEmptyDirectory(String targetDirectory)
			throws IOException
	{
		File file = new File(targetDirectory);

		if (file.exists())
		{
			delete(file);
		}
		assert (!file.exists());
		file.mkdir();
		assert (file.exists());
	}

	public static void delete(File file) throws IOException
	{

		if (file.isDirectory())
		{

			// directory is empty, then delete it
			if (file.list().length == 0)
			{

				file.delete();
				System.out.println("Directory is deleted : "
						+ file.getAbsolutePath());

			} else
			{

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files)
				{
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0)
				{
					file.delete();
					System.out.println("Directory is deleted : "
							+ file.getAbsolutePath());
				}
			}

		} else
		{
			// if file, then delete it
			file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public void unZipIt(String zipFile, String outputFolder)
	{

		byte[] buffer = new byte[1024];

		try
		{

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists())
			{
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null)
			{

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public String getOpsName()
	{
		return "Zip Converter";
	}

}
