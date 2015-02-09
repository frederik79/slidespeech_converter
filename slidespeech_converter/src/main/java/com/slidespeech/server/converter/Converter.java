package com.slidespeech.server.converter;

import com.slidespeech.server.model.Presentation;
import com.sun.star.lang.IllegalArgumentException;


public interface Converter {
	Presentation convert(String path, String filename, String lang, String voice) throws IllegalArgumentException;
	String getOpsName();
}
