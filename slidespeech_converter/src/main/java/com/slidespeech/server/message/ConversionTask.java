package com.slidespeech.server.message;

public class ConversionTask  extends AbstractMessage {

	private String path;
	private String fileName;
	private String speakernotes;
	private String voice;
	private String format;
	private String method;
	private int version;

	public ConversionTask(){
		this.path = "";
		this.fileName = "";
		this.speakernotes = "";
		this.voice = "female";
		this.method = "fullConversion";
		this.version = 1;
	}
   
	public void setPath(String p) { this.path = p; }
	public void setFileName(String n) { this.fileName = n; }
	public void setSpeakernotes(String s) { this.speakernotes = s; }
	public void setVoice(String v) { this.voice = v; }
	public void setFormat(String f) { this.format = f; }
	public void setVersion(int v) { this.version = v; }
	public void setMethod(String m) { this.method = m; }
   
	public String getPath() { return this.path; }
	public String getFileName() { return this.fileName; }
	public String getSpeakernotes() { return this.speakernotes; }
	public String getVoice() { return this.voice; }
	public String getFormat() { return this.format; }
	public int getVersion() { return this.version; }
	public String getMethod() { return this.method; }

}
