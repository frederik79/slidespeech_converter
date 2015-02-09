package com.slidespeech.server.converter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.slidespeech.server.converter.openoffice.OpenOfficeConnection;
import com.slidespeech.server.model.Presentation;
import com.slidespeech.server.model.Slide;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XExporter;
import com.sun.star.document.XFilter;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.presentation.XPresentationPage;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;

//TODO - This is just the 'make it work' version - 
//we have to restructure the whole thing, add proper treading and decent error handling!!!!!
//.. and yes, I AM ashamed of this piece of code ;)
public class FullConversion implements Converter
{

	protected final Log _logger;
	// private XComponentLoader _desktop = null;
	private XComponent _doc = null;
	// private Presentation _presentation;
	private OpenOfficeService _openOfficeService;

	public FullConversion()
	{
		_logger = LogFactory.getLog(getClass());
		_openOfficeService = new OpenOfficeService();
	}

	public Presentation convert(String targetDir, String fileName, String lang, String voice)
			throws IllegalArgumentException
	{
		Presentation _presentation = new Presentation();

		try
		{
			
			System.out.println("start convertSlidesToImages");
			convertSlidesToImages(targetDir, fileName, _presentation);
			System.out.println("finish convertSlidesToImages");
			
			/*if (!_presentation.createJsonOutput(targetDir))
			{
				throw new IllegalArgumentException("Could not create json file");
			}*/

		} catch (com.slidespeech.server.converter.openoffice.OpenOfficeException e)
		{
			System.out.println("caught open offcie Exception ");
			e.printStackTrace();
			// ooffice service not running -> restart
			if (!_openOfficeService.isRunning())
			{
				_openOfficeService.startService();
			}

			assert (_openOfficeService.isRunning());

			return new FullConversion().convert(targetDir, fileName, lang, voice);
		} catch (com.sun.star.io.IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrappedTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			System.out.println("caught IllegalArgumentException Exception ");
			// loadFromURL -> wrong url rethrow to pass message back to
			// client
			throw e;
		} catch (com.sun.star.uno.Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}

		finally
		{
			// close connection to ooffice
			
			_openOfficeService.closeConnection();
		}

		// text to speech and mp3/ogg compression
		// TODO - split into separate classes & do proper threading!!!!!
		
		_presentation.convertSpeakerNotesToMp3(targetDir, lang, voice);
		
		// _presentation.createLowResSlides(targetDir);
		_presentation.createThumbnail(targetDir);
		//_presentation.createZipFiles(targetDir);
		
		_presentation.createSlideshareImages(fileName, targetDir);
		
		_presentation.cleanupDirectory(targetDir);

		return _presentation;
	}

	synchronized private int convertSlidesToImages(String targetDir, String fileName,
			Presentation _presentation) throws com.sun.star.io.IOException,
			IllegalArgumentException, com.sun.star.uno.Exception,
			IndexOutOfBoundsException, WrappedTargetException,
			UnknownPropertyException
	{
		int pageNo;
		String targetURL = "file://" + targetDir;
		PropertyValue[] loadProps = new PropertyValue[1];
		loadProps[0] = new PropertyValue();
		loadProps[0].Name = "Hidden";
		loadProps[0].Value = new Boolean(true);
		// Export filter properties
		PropertyValue[] pngExportFilterProps = null;
		// connect to ooffice
		OpenOfficeConnection openConnection = _openOfficeService
				.openConnection();

		XComponentLoader _desktop = openConnection.getDesktop();

		// load document
		_doc = _desktop.loadComponentFromURL(targetURL + fileName, "_blank", 0,
				loadProps);

		XMultiComponentFactory xMultiComponentFactory = openConnection
				.getComponentContext().getServiceManager();
		Object graphicExportFilter = xMultiComponentFactory
				.createInstanceWithContext(
						"com.sun.star.drawing.GraphicExportFilter",
						openConnection.getComponentContext());

		// get slide count
		XDrawPagesSupplier dps = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, _doc);
		XDrawPages slides = dps.getDrawPages();
		pageNo = slides.getCount();

		// loop through the slides: extract speakernotes and export pngs
		for (int i = 0; i < pageNo; i++)
		{
			Object drawPage = slides.getByIndex(i);
			XDrawPage slide = (XDrawPage) UnoRuntime.queryInterface(
					XDrawPage.class, drawPage);

			// get dimensions (for 16:9 etc) - use slide 1 for layout,
			// this
			// shouldn't change on the following slides
			if (i == 0)
			{
				XPropertySet xPageProperties = (XPropertySet) UnoRuntime
						.queryInterface(XPropertySet.class, slide);
				int width = ((Integer) xPageProperties
						.getPropertyValue("Width")).intValue();
				int height = ((Integer) xPageProperties
						.getPropertyValue("Height")).intValue();
				pngExportFilterProps = this.getPngExportFilter(width, height);
			}

			String speakernotes = "";
			XPresentationPage xPresentationPage = (XPresentationPage) UnoRuntime
					.queryInterface(XPresentationPage.class, slide);
			if (xPresentationPage != null)
			{
				// extract speakernotes
				XDrawPage xNotesPage = xPresentationPage.getNotesPage();

				int count = xNotesPage.getCount();

				XText xShapeText = null;
				if (count > 1)
				{
					Object note = xNotesPage.getByIndex(1);

					xShapeText = (XText) UnoRuntime.queryInterface(XText.class,
							note);

				}

				if (xShapeText != null)
				{
					speakernotes = xShapeText.getString();
				}
				// export png
				pngExportFilterProps[2] = new PropertyValue();
				pngExportFilterProps[2].Name = "URL";
				pngExportFilterProps[2].Value = targetURL + "slide_" + i
						+ ".png";

				XExporter xExporter = (XExporter) UnoRuntime.queryInterface(
						XExporter.class, graphicExportFilter);
				xExporter.setSourceDocument((XComponent) UnoRuntime
						.queryInterface(XComponent.class, slide));
				// Store slide with graphic export filter
				XFilter xFilter = (XFilter) UnoRuntime.queryInterface(
						XFilter.class, graphicExportFilter);
				xFilter.filter(pngExportFilterProps);

				_logger.info("storeToURLPicture \"" + targetURL + "slide_" + i
						+ ".png\"");

				// Presentation Data
				Slide newSlide = new Slide();
				newSlide.setText(speakernotes);
				newSlide.setImage("slide_" + i + ".png");
				_presentation.addSlide(newSlide);
			}
		}
		return pageNo;
	}

	private PropertyValue[] getPngExportFilter(double widthOriginal,
			double heightOriginal)
	{
		double widthResized = 800;
		double heightResized = 600;
		if (widthOriginal > heightOriginal)
		{
			heightResized = widthResized / widthOriginal * heightOriginal;
		} else
		{
			widthResized = heightResized / heightOriginal * widthOriginal;
		}
		// Export filter properties
		PropertyValue[] exportFilterProps = new PropertyValue[3];
		// Type of image
		exportFilterProps[0] = new PropertyValue();
		exportFilterProps[0].Name = "MediaType";
		exportFilterProps[0].Value = "image/png";
		// Height and width
		PropertyValue[] exportFilterData = new PropertyValue[3];
		exportFilterData[0] = new PropertyValue();
		exportFilterData[0].Name = "PixelWidth";
		exportFilterData[0].Value = (int) Math.round(widthResized);
		exportFilterData[1] = new PropertyValue();
		exportFilterData[1].Name = "PixelHeight";
		exportFilterData[1].Value = (int) Math.round(heightResized);
		exportFilterData[2] = new PropertyValue();
		exportFilterData[2].Name = "Compression";
		exportFilterData[2].Value = 9;
		exportFilterProps[1] = new PropertyValue();
		exportFilterProps[1].Name = "FilterData";
		exportFilterProps[1].Value = exportFilterData;
		return exportFilterProps;
	}

	public String getOpsName()
	{
		return " Full conversion odp/ppt to slidespeech outputs using LibreOffice service";
	}

}
