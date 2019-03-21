/**
 * Show.java
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


@SuppressWarnings("ALL")
@Entity(tableName = "show_table",
        indices = {@Index("date"), @Index("coverage"), @Index("year"), @Index("description"), @Index("downloads"), @Index("identifier"), @Index("bandId"), @Index(value = {"date", "coverage"})})
public class Show {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int sourceId;
    private String avgRating;   // <<== CHANGE
    private String coverage;
    private String date;
    private int downloads;
    private String identifier;
    private String source;
    private String subject;
    private String title;
    private int year;
    private String venue;
    private String city;
    private String state;
    private int showId;
    private String description;             // <<== Search for songs in here!!!!
    private int bandId;
    private String altIdentifiers;
    private int totalDownloads;
    private int favorite;


    @Ignore
    public Show(int sourceId, String avgRating, String coverage, String date, int downloads, String identifier, String source, String subject, String title, int year, String venue, String city, String state, int showId, String description, int bandId, String altIdentifiers, int totalDownloads) {
        this.sourceId = sourceId;
        this.avgRating = avgRating;
        this.coverage = coverage;
        this.date = date;
        this.downloads = downloads;
        this.identifier = identifier;
        this.source = source;
        this.subject = subject;
        this.title = title;
        this.year = year;
        this.venue = venue;
        this.city = city;
        this.state = state;
        this.showId = showId;
        this.description = description;
        this.bandId = bandId;
        this.altIdentifiers = altIdentifiers;
        this.totalDownloads = totalDownloads;
        this.favorite = 0;
    }

    public Show(int id, int sourceId, String avgRating, String coverage, String date, int downloads, String identifier, String source, String subject, String title, int year, String venue, String city, String state, int showId, String description, int bandId, String altIdentifiers, int totalDownloads) {
        this.id = id;
        this.sourceId = sourceId;
        this.avgRating = avgRating;
        this.coverage = coverage;
        this.date = date;
        this.downloads = downloads;
        this.identifier = identifier;
        this.source = source;
        this.subject = subject;
        this.title = title;
        this.year = year;
        this.venue = venue;
        this.city = city;
        this.state = state;
        this.showId = showId;
        this.description = description;
        this.bandId = bandId;
        this.altIdentifiers = altIdentifiers;
        this.totalDownloads = totalDownloads;
        this.favorite = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBandId() {
        return bandId;
    }

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    public String getAltIdentifiers() {
        return altIdentifiers;
    }

    public void setAltIdentifiers(String altIdentifiers) {
        this.altIdentifiers = altIdentifiers;
    }

    public int getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

}