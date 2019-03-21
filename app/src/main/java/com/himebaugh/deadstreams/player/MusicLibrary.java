/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.himebaugh.deadstreams.player;


import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.himebaugh.deadstreams.database.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class MusicLibrary {

    private static final String TAG = MusicLibrary.class.getSimpleName();

    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final HashMap<String, String> albumRes = new HashMap<>();
    private static final HashMap<String, String> musicFileName = new HashMap<>();
    private static final HashMap<String, String> songLENGTH = new HashMap<>();
    private static final HashMap<String, Integer> songID = new HashMap<>();

    static {

        Log.i(TAG, "MusicLibrary - static initializer: ");

    }

    public static String getRoot() {
        return "root";
    }

    public static String getSongUri(String mediaId) {

        return mediaId;
    }

    private static String getAlbumArtUri(String albumArtResName) {

        return "";
    }

    private static String getAlbumRes(String mediaId) {

        return albumRes.containsKey(mediaId) ? albumRes.get(mediaId) : "";
    }

    public static String getMusicFilename(String mediaId) {
        return musicFileName.containsKey(mediaId) ? musicFileName.get(mediaId) : null;
    }

    public static String getSongLENGTH(String mediaId) {

        return songLENGTH.containsKey(mediaId) ? songLENGTH.get(mediaId) : "";
    }

    public static int getSongID(String mediaId) {

        return songID.containsKey(mediaId) ? songID.get(mediaId) : 0;
    }

    private static String getAlbumBitmap(Context context, String mediaId) {
        //return BitmapFactory.decodeResource(context.getResources(), MusicLibrary.getAlbumRes(mediaId));
        return MusicLibrary.getAlbumRes(mediaId);
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public static String getPreviousSong(String currentMediaId) {
        String prevMediaId = music.lowerKey(currentMediaId);
        if (prevMediaId == null) {
            prevMediaId = music.firstKey();
        }
        return prevMediaId;
    }

    public static String getNextSong(String currentMediaId) {
        String nextMediaId = music.higherKey(currentMediaId);
        if (nextMediaId == null) {
            nextMediaId = music.firstKey();
        }
        return nextMediaId;
    }

    public static MediaMetadataCompat getMetadata(Context context, String mediaId) {
        MediaMetadataCompat metadataWithoutBitmap = music.get(mediaId);
        String albumArt = getAlbumBitmap(context, mediaId);

        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        for (String key :
                new String[] {
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        MediaMetadataCompat.METADATA_KEY_GENRE,
                        MediaMetadataCompat.METADATA_KEY_TITLE
                }) {
            builder.putString(key, metadataWithoutBitmap.getString(key));
        }
        builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        //builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArt);
        return builder.build();
    }

    //
    //@SuppressLint("WrongConstant")
    private static void createMediaMetadataCompat(

            String mediaId,
            String title,
            String subtitle,
            String artist,
            String album,
            String genre,
            long duration,
            long track,
            String albumArtResId,
            String albumArtResName,
            String musicFilename,
            String songLength,
            int songId) {

        music.put(mediaId,
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)     // <<<<<<<<<<
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)                 // <<<<<<<<<<
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration * 1000)
                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getAlbumArtUri(albumArtResName))
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getAlbumArtUri(albumArtResName))
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, subtitle)
                        .build());

        albumRes.put(mediaId, albumArtResId);

        musicFileName.put(mediaId, musicFilename);

        songLENGTH.put(mediaId, songLength);

        songID.put(mediaId, songId);
    }

    public static boolean loadSongList(List<Song> songList) {

        boolean successful = false;
        music.clear();

        if (songList.size() != 0) {

            for (Song song : songList) {

                createMediaMetadataCompat(
                        "http://archive.org/download/" + song.getIdentifier() + "/" + song.getName(),
                        song.getTitle(),
                        "subtitle",           // subtitle song length but consider show identifier...  song.getLength()
                        "Grateful Dead",           // artist  TODO: lookup bandid when other bands exist...
                        song.getAlbum(),
                        "Rock",
                        timeToSeconds(song.getLength()),
                        Integer.parseInt(song.getTrack()),
                        "http://ia802700.us.archive.org/0/items/GratefulDead/gdlogo.jpg",  // not used
                        "album_jazz_blues",
                        song.getName(),
                        song.getLength(),
                        song.getId());
            }
            successful = true;
        }

        return successful;
    }

    private static long timeToSeconds(String timeString) {

        int duration = 0;
        try {
            // String time = "02:30"; //mm:ss
            String[] units = timeString.split(":");     //will break the string up into an array
            int minutes = Integer.parseInt(units[0]);         //first element
            int seconds = Integer.parseInt(units[1]);         //second element
            duration = 60 * minutes + seconds;            //add up our values
        } catch (NumberFormatException e) {

        }

        return duration;
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

}
