package com.slidespeech.server.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.imgscalr.Scalr;

import com.slidespeech.server.model.Slide;

public class PdfToImageService
{
	public synchronized static ArrayList<Slide> convert(String pdfFileName,
			String targetDirectory) throws IOException
	{
		ArrayList<Slide> slides = new ArrayList<Slide>();

		File file = new File(pdfFileName);

		assert (file.exists());
		PDDocument document = PDDocument.load(file);

		ArrayList<PDPage> allPages = (ArrayList<PDPage>) document
				.getDocumentCatalog().getAllPages();

		for (int i = 0; i < allPages.size(); i++)
		{
			String slideNamePreFix = "slide_";
			String slideName = slideNamePreFix + i + ".png";

			String pngPageName = targetDirectory + slideName;

			File pngPageFile = new File(pngPageName);

			if (pngPageFile.exists())
			{
				pngPageFile.delete();
			}
			

			PDPage page = (PDPage) allPages.get(i);

			//just for debug reasons to get fonts
			//Map fonts = page.getResources().getFonts();
			//System.out.println("fonts=" + fonts);
			
			BufferedImage bImg = convertToImage(page);

			assert (bImg != null);

			pngPageFile = new File(targetDirectory + "/" + slideName);
			
			System.out.println("write pngFile " + pngPageFile);
			
			assert(!pngPageFile.exists());
			assert(pngPageFile.canWrite());

			ImageIO.write(bImg, "png", pngPageFile);

			assert (pngPageFile.exists());

			Slide slide = new Slide();
			slide.setImage(slideName);
			
			String speakerNotes = extractPdfNotes(page);
			slide.setText(speakerNotes);

			slides.add(slide);
		}

		document.close();
		
		return slides;
	}

	private static String extractPdfNotes(PDPage page) throws IOException,
			FileNotFoundException
	{
		
		String speakerNotes = "";
		
		COSArrayList annotations = (COSArrayList) page.getAnnotations();
		
		 for (Object object : annotations)
		 {
			 System.out.println(object);
			 
			 if (object instanceof PDAnnotationText)
			 {
				PDAnnotationText pdAnnotationText = (PDAnnotationText) object;
				 
				String contents = pdAnnotationText.getContents();
				
				if (contents != null)
				{
					speakerNotes += contents; 
				}
			 }
		 }
		 return speakerNotes;
	}
/*
	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(Image image)
	{
		if (image instanceof BufferedImage)
		{
			return (BufferedImage) image;
		}
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent
		// Pixels
		boolean hasAlpha = hasAlpha(image);
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try
		{
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha)
			{
				transparency = Transparency.BITMASK;
			}
			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e)
		{
			// The system does not have a screen
		}
		if (bimage == null)
		{
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha)
			{
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	public static boolean hasAlpha(Image image)
	{
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage)
		{
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}
		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try
		{
			pg.grabPixels();
		} catch (InterruptedException e)
		{
		}
		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}*/

	private static BufferedImage convertToImage(PDPage page) throws IOException
	{

		// float width = page.PAGE_SIZE_A4.getWidth();
		// float height = page.PAGE_SIZE_A4.getHeight();

		int widthPt = 1200;
		int heightPt = 900;

		// TODO The following reduces accuracy. It should really be a
		// Dimension2D.Float.

		//Integer rotation = page.getRotation();
		BufferedImage image = null;

		image = page.convertToImage();

		int height = image.getHeight();
		int width = image.getWidth();
		
		if (width > height)
		{
			image = Scalr.resize(image, Scalr.Method.QUALITY,
					Scalr.Mode.FIT_TO_WIDTH, widthPt, heightPt,
					Scalr.OP_ANTIALIAS);
		} 
		else
		{
			BufferedImage canvas = new BufferedImage(widthPt, heightPt,
					BufferedImage.TYPE_INT_ARGB);

			BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.QUALITY,
					Scalr.Mode.FIT_TO_HEIGHT, widthPt, heightPt, Scalr.OP_ANTIALIAS);

			Graphics g = canvas.getGraphics();
			((Graphics2D) g).setBackground(new Color(0, 0, 0, 0) );
			/* if we'd want to continue using BufferedImage.TYPE_BYTE_INDEXED: use white background
			Graphics g = canvas.getGraphics();
			((Graphics2D) g).setPaint ( Color.WHITE );
			g.fillRect ( 0, 0, widthPt, heightPt );
			 */

			g.drawImage(resizedImage, (widthPt - resizedImage.getWidth()) / 2, 0, null);
			
			image = canvas;
		}
		return image;
	}

	// private static BufferedImage resize(BufferedImage image, int width,
	// int height)
	// {
	// int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image
	// .getType();
	// BufferedImage resizedImage = new BufferedImage(width, height, type);
	// Graphics2D g = resizedImage.createGraphics();
	// g.setComposite(AlphaComposite.Src);
	//
	// g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	//
	// g.setRenderingHint(RenderingHints.KEY_RENDERING,
	// RenderingHints.VALUE_RENDER_QUALITY);
	//
	// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// g.drawImage(image, 0, 0, width, height, null);
	// g.dispose();
	// return resizedImage;
	// }
	//
	// public static BufferedImage blurImage(BufferedImage image)
	// {
	// float ninth = 1.0f / 9.0f;
	// float[] blurKernel =
	// { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
	//
	// Map map = new HashMap();
	//
	// map.put(RenderingHints.KEY_INTERPOLATION,
	// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	//
	// map.put(RenderingHints.KEY_RENDERING,
	// RenderingHints.VALUE_RENDER_QUALITY);
	//
	// map.put(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// RenderingHints hints = new RenderingHints(map);
	// BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel),
	// ConvolveOp.EDGE_NO_OP, hints);
	// return op.filter(image, null);
	// }
	//
	// //
	// // private static BufferedImage createCompatibleImage(BufferedImage
	// image)
	// // {
	// // //GraphicsConfiguration gc =
	// // BufferedImageGraphicsConfig.getConfig(image);
	// //
	// // int w = image.getWidth();
	// // int h = image.getHeight();
	// //
	// // h = image.getHeight();
	// // w = image.getHeight();
	// //
	// // //BufferedImage result = gc.createCompatibleImage(w, h,
	// // Transparency.TRANSLUCENT);
	// //
	// // BufferedImage result = new BufferedImage(w, h,
	// Transparency.TRANSLUCENT);
	// //
	// // Graphics2D g2 = result.createGraphics();
	// // g2.drawRenderedImage(image, null);
	// // g2.dispose();
	// // return result;
	// // }
	// //
	// private static BufferedImage resizeTrick(BufferedImage image, int width,
	// int height)
	// {
	// // image = createCompatibleImage(image);
	// image = resize(image, 297, 420);
	// image = blurImage(image);
	// image = resize(image, width, height);
	// return image;
	//
	// }
	//
	// private static BufferedImage resizeImage(BufferedImage originalImage,
	// int type, int IMG_WIDTH, int IMG_HEIGHT)
	// {
	// BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
	// type);
	// Graphics2D g = resizedImage.createGraphics();
	// g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
	// g.dispose();
	//
	// return resizedImage;
	// }
	//
	// private static BufferedImage resizeImageWithHint(
	// BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT)
	// {
	//
	// BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
	// type);
	// Graphics2D g = resizedImage.createGraphics();
	// g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
	// g.dispose();
	// g.setComposite(AlphaComposite.Src);
	//
	// g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	// g.setRenderingHint(RenderingHints.KEY_RENDERING,
	// RenderingHints.VALUE_RENDER_QUALITY);
	// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// return resizedImage;
	// }
}
