package com.slidespeech.server.converter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;

public class Mp3SortTest 
{
	@Test
	public void testsorting() 
	{
		ArrayList<String> test = new ArrayList<String>();
		
		test.add("slide1.mp3");
		test.add("slide2.mp3");
		test.add("slide12.mp3");
		test.add("slide3.mp3");
		test.add("slide15.mp3");
		test.add("slide14_v1.mp3");
		test.add("slide4.mp3");
		test.add("slide10.mp3");
		test.add("slide5.mp3");
		test.add("slide13_v4.mp3");
		test.add("slide6.mp3");
		
		VideoConversion.sortMp3FileNamesBasedOnSlideNumber(test);
		
		assertEquals(test.get(10), "slide15.mp3");
		assertEquals(test.get(9), "slide14_v1.mp3");
		assertEquals(test.get(8), "slide13_v4.mp3");
		assertEquals(test.get(7), "slide12.mp3");
		assertEquals(test.get(6), "slide10.mp3");
		
	}
}
