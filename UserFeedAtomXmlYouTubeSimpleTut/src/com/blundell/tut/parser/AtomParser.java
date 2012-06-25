package com.blundell.tut.parser;

import java.util.List;

import com.blundell.tut.domain.Video;

public interface AtomParser {

	List<Video> getVideos();

	public class ParseException extends Exception {
		public ParseException(Exception origin) {
			super(origin);
		}
	}
}