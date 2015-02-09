package com.slidespeech.server.spconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class SPPostConverter 
{
	public boolean convert(String fileName, String targetDir) 
	{
		boolean result = false;
		
		HttpClient httpclient = new DefaultHttpClient();
		try {
			String string = "http://ppt2png.cloudapp.net:8080" +
					"/Convert.svc/upload";
			
			HttpPost httppost = new HttpPost(string);
			
			File file = new File(targetDir + fileName);
			
			MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
			 
			// For File parameters
			entity.addPart( file.getAbsolutePath(), new FileBody(file , "multipart/form-data" ));
			 
			httppost.setEntity( entity );
			
			System.out.println("executing request " + httppost.getRequestLine());
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			
			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());
			
			if (resEntity != null) {
				System.out.println("Response content length: " + resEntity.getContentLength());
				System.out.println("Chunked?: " + resEntity.isChunked());
				
				
				if (resEntity != null) 
				{
					String parentName = file.getParent();
					
					File parentFile = new File(parentName);
					
                    FileOutputStream fos = 
                        new java.io.FileOutputStream(parentFile.getAbsolutePath() + "/download.zip");
                    resEntity.writeTo(fos);
                    fos.close();
				}
				result =  true;
			}
			EntityUtils.consume(resEntity);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}
	
	

	
}
