package com.slidespeech.server.message;

public class Error extends AbstractMessage {
	
	private String errormessage;
	private String stacktrace;

	public Error(){
		this.setSuccess(false);
		this.errormessage = "";
		this.stacktrace = "";
	}
   
	public void setErrormessage(String m) { this.errormessage = m; }
	public void setStacktrace(String s) { this.stacktrace = s; }
   
	public String getErrormessage() { return this.errormessage; }
	public String getStacktrace() { return this.stacktrace; }
       
}
