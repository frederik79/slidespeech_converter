package com.slidespeech.server.message;

import com.slidespeech.server.model.Presentation;

public class Processed extends AbstractMessage  {
	
	private Presentation presentation;
	private String fileName;
	private String speakernotes;
	
	public Processed(){
		this.setSuccess(true);
		this.presentation = null;
		this.fileName = null;
		this.speakernotes = null;
	}
   
	public void setPresentation(Presentation p) { this.presentation = p; }
	public void setFileName(String f) { this.fileName = f;  }
	public void setSpeakernotes(String speakernotes) {this.speakernotes = speakernotes;}
	
	public Presentation getPresentation() { return this.presentation; }
	public String getFileName() { return this.fileName; }
	public String getSpeakernotes() {return speakernotes;}
       
}