package com.slidespeech.server.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.slidespeech.server.model.Slide;

public class ImageAdjustService
{
	private final static int widthPt = 800;
	private final static int heightPt = 600;
	
	public static Slide convert( String path, String sourceFileName, String targetFileName) throws IOException
	{
		File sourceimage = new File(path+sourceFileName);
		String outputFormat = "png";
		int imgType = BufferedImage.TYPE_INT_ARGB;
		
		if(getExtension(sourceFileName).matches("(jpg|jpeg)")){
			outputFormat = "jpg";
			imgType = BufferedImage.TYPE_INT_RGB;
		}
		
		BufferedImage image = ImageIO.read(sourceimage);
		
		int height = image.getHeight();
		int width = image.getWidth();
		
		//has to take aspect ratio into account
		if (width/widthPt > height/heightPt)
		{
			image = Scalr.resize(image, Scalr.Method.QUALITY,
					Scalr.Mode.FIT_TO_WIDTH, widthPt, Scalr.OP_ANTIALIAS);
		} 
		else
		{
			
			BufferedImage canvas = new BufferedImage(widthPt, heightPt,imgType);

			image = Scalr.resize(image, Scalr.Method.QUALITY,
					Scalr.Mode.FIT_TO_HEIGHT, heightPt, Scalr.OP_ANTIALIAS);

			//surrounding canvas, to get 800px width (player uses fixed original width to scale)
			//transparent background for png, white background for jpgs (no alpha channel)
			Graphics g = canvas.getGraphics();
			
			if(imgType == BufferedImage.TYPE_INT_ARGB){
				((Graphics2D) g).setBackground(new Color(0, 0, 0, 0) );
			}else{
				((Graphics2D) g).setPaint ( Color.WHITE );
				g.fillRect ( 0, 0, widthPt, heightPt );
			}

			g.drawImage(image, (widthPt - image.getWidth()) / 2, 0, null);
			
			image = canvas;
		}
		
		ImageIO.write(image, outputFormat, new File(path + targetFileName + "." + outputFormat));
		
		Slide slide = new Slide();
		slide.setImage(targetFileName + "." + outputFormat);
		
		return slide;

	}
	
	private static String getExtension(String filename)
	{
		String ext = "";
		int i = filename.lastIndexOf('.');
	
		if (i > 0 && i < filename.length() - 1)
			ext = filename.substring(i+1).toLowerCase();

		return ext;
	}
}