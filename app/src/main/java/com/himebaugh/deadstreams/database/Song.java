/**
 * Song.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.database;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "song_table",
        indices = {@Index("identifier"), @Index("title"), @Index("track"), @Index("isRandom"), @Index(value = {"identifier", "track"}), @Index(value = {"identifier", "name"}) })
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String identifier;          // relates back to show
    private String name;
    private String source;
    private String creator;
    private String title;
    private String track;
    private String album;
    private String bitrate;
    private String length;
    private String format;
    private String original;
    private String mtime;
    private String size;
    private String md5;
    private String crc32;
    private String sha1;
    private String height;
    private String width;
    private Boolean isRandom;

    @Ignore
    public Song() {
    }

    // USED ONLY ON INITIALIZE... so forcing data...
    @Ignore
    public Song(String identifier, String name, String title, String track, String album, String bitrate, String length) {
        this.identifier = identifier;
        this.name = name;
        this.title = title;
        this.track = track;
        this.album = album;
        this.bitrate = bitrate;
        this.length = length;
        this.creator = "Grateful Dead";
        this.format = "VBR MP3";
        this.isRandom = false;
    }

    @Ignore
    public Song(String identifier, String name, String source, String creator, String title, String track, String album, String bitrate, String length, String format, String original, String mtime, String size, String md5, String crc32, String sha1, String height, String width, Boolean isRandom) {
        this.identifier = identifier;
        this.name = name;
        this.source = source;
        this.creator = creator;
        this.title = title;
        this.track = track;
        this.album = album;
        this.bitrate = bitrate;
        this.length = length;
        this.format = format;
        this.original = original;
        this.mtime = mtime;
        this.size = size;
        this.md5 = md5;
        this.crc32 = crc32;
        this.sha1 = sha1;
        this.height = height;
        this.width = width;
        this.isRandom = isRandom;
    }

    public Song(int id, String identifier, String name, String source, String creator, String title, String track, String album, String bitrate, String length, String format, String original, String mtime, String size, String md5, String crc32, String sha1, String height, String width, Boolean isRandom) {
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.source = source;
        this.creator = creator;
        this.title = title;
        this.track = track;
        this.album = album;
        this.bitrate = bitrate;
        this.length = length;
        this.format = format;
        this.original = original;
        this.mtime = mtime;
        this.size = size;
        this.md5 = md5;
        this.crc32 = crc32;
        this.sha1 = sha1;
        this.height = height;
        this.width = width;
        this.isRandom = isRandom;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getCrc32() {
        return crc32;
    }

    public void setCrc32(String crc32) {
        this.crc32 = crc32;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Boolean getRandom() {
        return isRandom;
    }

    public void setRandom(Boolean random) {
        isRandom = random;
    }

    // getTrack() + " - " + getTitle() + "\n" + album + "\n" + name
    @Override
    public String toString() {
        return getTrack() + " - " + getTitle() + " - " + getLength();
    }

}
