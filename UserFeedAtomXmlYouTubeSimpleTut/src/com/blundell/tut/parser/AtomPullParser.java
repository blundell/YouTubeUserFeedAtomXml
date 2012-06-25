package com.blundell.tut.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.blundell.tut.domain.Video;

/**
 * A pull parser will only read elements that we want, therefore theoretically faster than a sax parser
 * This is still SO SLOW, there is too much XML that we just don't need (but still have to parse)
 * If your talking to YouTube I definitely recommend using JSON-C
 * like in this tutorial: http://blog.blundell-apps.com/show-youtube-user-videos-in-a-listview/
 * @author paul.blundell
 */
public class AtomPullParser implements AtomParser {
	// Can't retrieve a thumbnail from the XML feed so using a static url - if you were to just use the same image you
	// should just put that image in your /drawable/ folder and use it from there
	private static final String YOUTUBE_LOGO = "http://www.linenfields.co.uk/catalog/view/theme/default/image/youtube-logo.png";

	// Create a list to store are videos in
	private final List<Video> videos;
	private final XmlPullParser parser;

	private boolean inVideoEntry;
	private boolean inTitle;
	private boolean inVideoUrl;

	private String title;
	private String url;

	public AtomPullParser(InputStream atomInputStream) throws ParseException {
		this.videos = new ArrayList<Video>();
		try {
			this.parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(atomInputStream, "UTF-8");
			parse();
		} catch (XmlPullParserException e) {
			// We catch the specific parsers exception and throw our own exception so that we can control the errors
			throw new ParseException(e);
		} catch (IOException e) {
			// We catch the specific parsers exception and throw our own exception so that we can control the errors
			throw new ParseException(e);
		}
	}

	private void parse() throws XmlPullParserException, IOException {
		int event = 0;
		do{
			event = parser.next();
			switch(event){
			case XmlPullParser.START_TAG:
				debug("<"+parser.getName()+">", false);

				if("entry".equals(parser.getName())){
					inVideoEntry = true;
				}
				if("media:title".equals(parser.getName())){
					inTitle = true;
				}

				break;
			case XmlPullParser.TEXT:
				debug(parser.getText(), false);

				if(inTitle){
					title = parser.getText();
				} else
				if(inVideoUrl){
					url = parser.getText();
				}

				break;
			case XmlPullParser.END_TAG:
				debug("<"+parser.getName()+"/>", true);

				if(inVideoEntry){
					if("media:title".equals(parser.getName())){
						inTitle = false;
					} else
					if("yt:videoid".equals(parser.getName())){
						inVideoUrl = false;
					} else
					if("entry".equals(parser.getName())){
						inVideoEntry = false;
						// Loop round our JSON list of videos creating Video objects to use within our app
						videos.add(new Video(title, "http://www.youtube.com/watch?v="+url, YOUTUBE_LOGO));
					}
				}
				break;
			default:
				break;
			}


		} while ( event != XmlPullParser.END_DOCUMENT );
	}

	@Override
	public List<Video> getVideos() {
		return videos;
	}

	// A debug method to view the XML - you would remove this after you have completed the code
	// (I've set the constant to false, change to true to see the XML in your LogCat)
	private static final boolean PRINT_XML = true;
	private void debug(String var, boolean newLine){
		if(PRINT_XML){
			if(newLine)
				System.out.println(var);
			else
				System.out.print(var);
		}
	}
}