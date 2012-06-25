package com.blundell.tut.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.blundell.tut.domain.Video;

import android.util.Xml;

/**
 * A SAX parser to read through the XML reading each node.
 * This is SO SLOW, there is too much XML that we just don't need (but still have to parse)
 * If your talking to YouTube I definitely recommend using JSON-C
 * like in this tutorial: http://blog.blundell-apps.com/show-youtube-user-videos-in-a-listview/
 * @author paul.blundell
 */
public class AtomSaxParser extends DefaultHandler implements AtomParser {
	// Can't retrieve a thumbnail from the XML feed so using a static url - if you were to just use the same image you
	// should just put that image in your /drawable/ folder and use it from there
	private static final String YOUTUBE_LOGO = "http://www.linenfields.co.uk/catalog/view/theme/default/image/youtube-logo.png";

	// Create a list to store are videos in
	private final List<Video> videos;
	private StringBuilder nodeValue;
	// These flags are used so we know where abouts we are in the XML we are parsing
	private boolean inVideoEntry;
	private boolean inTitle;
	private boolean inDetails;
	private boolean inVideoUrl;
	// These are the fields that we get out of the XML and save to our Video object
	private String title;
	private String url;

	public AtomSaxParser(InputStream atomInputStream) throws ParseException {
		// instantiate a list of videos to send back
		this.videos = new ArrayList<Video>();
		// use a string-builder to store the text from each xml field
		this.nodeValue = new StringBuilder("");
		// Start parsing the XML
		try {
			Xml.parse(atomInputStream, Xml.Encoding.UTF_8, this);
		} catch (SAXException e) {
			// We catch the specific parsers exceptions and throw our own exception so that we can control the errors
			throw new ParseException(e);
		} catch (IOException e) {
			// We catch the specific parsers exceptions and throw our own exception so that we can control the errors
			throw new ParseException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		debug("<"+localName+">", false);
		// use flags to keep track of where we are in the XML
		if("entry".equals(localName)){
			inVideoEntry = true;
		} else
		if("group".equals(localName)){
			inDetails = true;
		}

		if(inVideoEntry && inDetails){
			if("title".equals(localName)){
				inTitle = true;
			}
			if("videoId".equals(localName)){
				inVideoUrl = true;
			}
		}
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		super.characters(chars, start, length);
		// only save the text of the XML attributes we want
		if(inTitle || inVideoUrl){
			nodeValue.append(new String(chars, start, length));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		debug(nodeValue.toString(), false);
		debug("<"+localName+"/>", true);

		// When we get to the end of an XML node we want save the string-builder as a field
		if(inVideoEntry){
			if(inDetails){
				if(inTitle){
					title = nodeValue.toString();
					inTitle = false;
					nodeValue.setLength(0);
				} else
				if(inVideoUrl){
					url = nodeValue.toString();
					inVideoUrl = false;
					nodeValue.setLength(0);
				}
			}
		}

		if("group".equals(localName)){
			inDetails = false;
		} else
		// When we get to the end of the entry tag </entry> we know we have all the fields for our YouTube Video
		if("entry".equals(localName)){
			addVideo(title, "http://www.youtube.com/watch?v="+url, YOUTUBE_LOGO);
			// Reset for the next video
			inVideoEntry = false;
			title = "";
			url = "";
		}
	}

	private void addVideo(String title, String url, String thumbUrl) {
		// Create the videos object and add it to our list
		videos.add(new Video(title, url, thumbUrl));
	}

	public List<Video> getVideos() {
		return videos;
	}

	// A debug method to view the XML - you would remove this after you have completed the code
	// (I've set the constant to false, change to true to see the XML in your LogCat)
	private static final boolean PRINT_XML = false;
	private void debug(String var, boolean newLine){
		if(PRINT_XML){
			if(newLine)
				System.out.println(var);
			else
				System.out.print(var);
		}
	}
}