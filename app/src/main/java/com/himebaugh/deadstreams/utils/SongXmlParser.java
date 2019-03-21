/**
 * SongXmlParser.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.utils;

import android.util.Xml;

import com.himebaugh.deadstreams.database.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class SongXmlParser {

    private final static String TAG = SongXmlParser.class.getSimpleName();

    // names of the XML tags
    private static final String FILES = "files";
    private static final String FILE = "file";

    private static final String NAME = "name";
    private static final String SOURCE = "source";

    private static final String CREATOR = "creator";
    private static final String TITLE = "title";
    private static final String TRACK = "track";
    private static final String ALBUM = "album";
    private static final String BITRATE = "bitrate";
    private static final String LENGTH = "length";
    private static final String FORMAT = "format";
    private static final String ORIGINAL = "original";
    private static final String MTIME = "mtime";
    private static final String SIZE = "size";
    private static final String MD5 = "md5";
    private static final String CRC32 = "crc32";
    private static final String SHA1 = "sha1";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";


    private ArrayList<Song> songList = null;
    private Song currentSong = null;
    private boolean done = false;
    private String currentTag = null;

    public ArrayList<Song> parse(String songXmlResults) {

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(songXmlResults));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        try {

            int eventType = parser.getEventType();

            // Following logic modified from http://www.ibm.com/developerworks/library/x-android/
            // Also look at http://developer.android.com/training/basics/network-ops/xml.html

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        songList = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if (currentTag.equalsIgnoreCase(FILE)) {
                            currentSong = new Song();

                            currentSong.setName(parser.getAttributeValue(null, NAME));
                            currentSong.setSource(parser.getAttributeValue(null, SOURCE));

                        } else if (currentSong != null) {
                            if (currentTag.equalsIgnoreCase(CREATOR)) {
                                // currentSong.setCreator(Integer.parseInt(parser.nextText()));
                                currentSong.setCreator(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(TITLE)) {
                                currentSong.setTitle(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(TRACK)) {
                                currentSong.setTrack(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(ALBUM)) {
                                currentSong.setAlbum(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(BITRATE)) {
                                currentSong.setBitrate(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(LENGTH)) {
                                currentSong.setLength(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(FORMAT)) {
                                currentSong.setFormat(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(ORIGINAL)) {
                                currentSong.setOriginal(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(MTIME)) {
                                currentSong.setMtime(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(SIZE)) {
                                currentSong.setSize(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(MD5)) {
                                currentSong.setMd5(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(CRC32)) {
                                currentSong.setCrc32(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(SHA1)) {
                                currentSong.setSha1(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(HEIGHT)) {
                                currentSong.setHeight(parser.nextText());
                            } else if (currentTag.equalsIgnoreCase(WIDTH)) {
                                currentSong.setWidth(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = parser.getName();
                        if (currentTag.equalsIgnoreCase(FILE) && currentSong != null) {
                            songList.add(currentSong);
                        } else if (currentTag.equalsIgnoreCase(FILES)) {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return songList;
    }
}
