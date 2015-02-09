package com.slidespeech.server.message;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class AbstractMessage {
	
	private int userID;
	private boolean success;
	private String code;
	private String lang;
	private String type;

	public AbstractMessage(){
		this.userID = 0;
		this.success = true;
		this.code = "";
		this.lang = "en";
		this.type = "full";
	}

	public void setUserID(int u) { this.userID = u; }
	public void setSuccess(boolean b) { this.success = b; }
	public void setCode(String c) { this.code = c;  }
	public void setLang(String l) { this.lang = l; }
	public void setType(String t) { this.type = t; }
   
	public int getUserID() { return this.userID; }
	public boolean getSuccess() { return this.success;}
	public String getCode() { return this.code; }
	public String getLang() { return this.lang; }
	public String getType() { return this.type; }
}