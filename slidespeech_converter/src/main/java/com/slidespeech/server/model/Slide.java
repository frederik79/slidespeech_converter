package com.slidespeech.server.model;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Slide
{
	public String t;
	public String q;
	public String l;
	public String i;

	public Slide()
	{
		this.t = "tag";
		this.q = "";
		this.l = "en";
		this.i = "";
	}

	@JsonIgnore
	public void setTag(String tag)
	{
		this.t = tag;
	}

	@JsonIgnore
	public void setText(String text)
	{
		this.q = text;
	}

	@JsonIgnore
	public void setLang(String lang)
	{
		this.l = lang;
	}

	@JsonIgnore
	public void setImage(String img)
	{
		this.i = img;
	}

	@JsonIgnore
	public String getTag()
	{
		return this.t;
	}

	@JsonIgnore
	public String getText()
	{
		if (q.length() == 0)
		{
			q = "There are no speakernotes on this slide <break time=\"5000ms\"/>";
		}

		return this.q;
	}

	@JsonIgnore
	public String getLang()
	{
		return this.l;
	}

	@JsonIgnore
	public String getImage()
	{
		return this.i;
	}
}
