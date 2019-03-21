/**
 * LoadSongsJobService.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.player;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.database.Song;
import com.himebaugh.deadstreams.database.SongsAtoZ;
import com.himebaugh.deadstreams.utils.NetworkUtils;
import com.himebaugh.deadstreams.utils.SongXmlParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class LoadSongsJobService extends JobIntentService {

    private final static String TAG = LoadSongsJobService.class.getSimpleName();

    private static final String ACTION_LOAD_SONGS = "com.himebaugh.deadstreams.player.action.LOAD_SONGS";

    private static final String ACTION_LOAD_RANDOM_SONGS = "com.himebaugh.deadstreams.player.action.LOAD_RANDOM_SONGS";

    private static final String EXTRA_SHOW_IDENTIFIER = "com.himebaugh.deadstreams.player.extra.SHOW_IDENTIFIER";

    private static final String ACTION_VERIFY_LOADED = "com.himebaugh.deadstreams.player.action.VERIFY_LOADED";

    private static Callback mCallback;


    // Unique job ID for this service
    private static final int JOB_ID = 1000;

    // *******************************************************************************
    // Starts this service to perform action LoadSongs.
    // If the service is already performing a task this action will be queued.
    // *******************************************************************************
    // NOTE: startActionLoadSongs is called in ShowListFragment when the user clicks on a show.
    //       This checks the Repository to see if songs are stored locally:
    //          - If they are, then ALL IS GOOD
    //          - If Not, songs are downloaded and processed. Then calls startActionVerifyLoaded. [Below]
    //          - Basically it waits until the songs are downloaded then loads them into the MusicLibrary
    //            for the MusicService to use.
    //       At the same time, this calls SongListActivity which calls SongListFragment
    //          it has a callback, mConnectionCallback and the adapter gets refreshed once the MusicService's callback
    //          gets notified that the MusicLibrary has been updated.
    //
    // It's a long chain of events.

    // THE FLOW...
    // ShowListAdapter: onClick: adapterPosition=4
    // ShowListAdapter: onClick: getShow_id()=584
    // ShowListFragment: onClick: LoadSongsService.startActionLoadSongs...
    // LoadSongsJobService: LoadSongsService - handleActionLoadSongs: START
    //   SongListActivity: onCreate: intent != null && intent.hasExtra(ShowListFragment.EXTRA_SHOW_ID)5 gd87-04-03.sennme80.clark-miller.24898.sbeok.shnf
    //   DataRepository: loadSongListLiveDataByIdentifier:
    //   PlaybackControlsFragment: onChanged: lastPlayed=Tennessee Jed
    //   PlaybackControlsFragment: mConnectionPlayerCallback onConnected:
    //   PlaybackControlsFragment: updatePlaybackState: state = NULL
    //   MusicService: onLoadChildren: parentMediaId = root
    //   MusicService: onLoadChildren: result.detach()
    //   MusicLibrary: loadSongList: 20
    //   LoadSongsJobService: Downloaded & Saved.
    // LoadSongsJobService: LoadSongsService - handleActionLoadSongs: END
    // LoadSongsJobService: LoadSongsService - handleActionVerifyLoaded: START
    //   DataRepository: doInBackground: DO #5 - insertSongsAsyncTask insertSongList
    // LoadSongsJobService: LoadSongsService - handleActionVerifyLoaded: END
    // MusicService: onMusicCatalogReady: success
    // SongListFragment: onChildrenLoaded: mSubscriptionCallback


    public static void startActionLoadSongs(Context context, String showIdentifier) {
        Intent intent = new Intent(context, LoadSongsJobService.class);
        intent.setAction(ACTION_LOAD_SONGS);
        intent.putExtra(EXTRA_SHOW_IDENTIFIER, showIdentifier);

        enqueueWork(context, LoadSongsJobService.class, JOB_ID, intent);
    }

    public static void startActionLoadRandomSongs(Context context) {
        Intent intent = new Intent(context, LoadSongsJobService.class);
        intent.setAction(ACTION_LOAD_RANDOM_SONGS);

        enqueueWork(context, LoadSongsJobService.class, JOB_ID, intent);
    }

    public static void startActionVerifyLoaded(Context context, final Callback callback) {
        Intent intent = new Intent(context, LoadSongsJobService.class);
        intent.setAction(ACTION_VERIFY_LOADED);

        mCallback = callback;

        enqueueWork(context, LoadSongsJobService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        final String action = intent.getAction();
        if (ACTION_LOAD_SONGS.equals(action)) {
            final String showIdentifier = intent.getStringExtra(EXTRA_SHOW_IDENTIFIER);
            handleActionLoadSongs(showIdentifier);

        } else if (ACTION_LOAD_RANDOM_SONGS.equals(action)) {

            handleActionLoadRandomSongs();

        } else if (ACTION_VERIFY_LOADED.equals(action)) {

            handleActionVerifyLoaded();
        }
    }

    private void handleActionLoadSongs(String showIdentifier) {

        DataRepository mRepository = ((BasicApp) getApplication()).getRepository();

        // See if Songs for the Show are already stored locally in the database.

        // UNFORTUNATELY THIS GETS CALLED EVERY TIME I CLICK ON A SHOW...
        // IN THE FUTURE: UPDATE THE SHOW TABLE SO I WOULD KNOW NOT TO CALL THIS.

        List<Song> songList = mRepository.loadSongsByIdentifier(showIdentifier);

        if (!showIdentifier.equals("RANDOM")) {


            if (songList.size() == 0) {

                // No Songs by Identifier are stored locally
                // So lets download the songs.
                // Grab XML from url = http://archive.org/download/identifier/identifier_files.xml

                // example...
                // http://archive.org/download/gd1974-09-18.sbd.snchk.miller.32292.sbeok.flac16/gd1974-09-18.sbd.snchk.miller.32292.sbeok.flac16_files.xml

                String urlString = "https://archive.org/download/" + showIdentifier + "/" + showIdentifier + "_files.xml";
                URL url = NetworkUtils.buildUrl(urlString);
                String songXmlResults = NetworkUtils.getXmlFromHttpUrl(url);

                // Pass XML to SongXmlParser & return songList
                SongXmlParser parser = new SongXmlParser();
                songList = parser.parse(songXmlResults);

                // Filter for MP3 files  (either in <format>VBR MP3</format> or in <file name="gd1969-08-30d1t03.mp3" source="derivative">)
                // Sort by track in <track>03</track>
                List<Song> deleteCandidates = new ArrayList<>();
                // Pass 1 - collect delete candidates
                for (Song song : songList) {
                    song.setIdentifier(showIdentifier);
                    if (null == song.getTrack() || !song.getName().endsWith(".mp3")) {
                        deleteCandidates.add(song);
                    }
                }
                // Pass 2 - delete
                for (Song deleteCandidate : deleteCandidates) {
                    songList.remove(deleteCandidate);
                }
                // Sort by Track
                Collections.sort(songList, new SortByTrack());

                // MusicLibrary.loadSongList(songList);

                // Save the songs locally
                mRepository.insertSongList(songList);
            }

        }

        MusicLibrary.loadSongList(songList);
    }

    private static class SortByTrack implements Comparator<Song> {
        // Used for sorting in ascending order of song track number
        @Override
        public int compare(Song a, Song b) {

            // Assuming tracks are 01,02,03,04,05 etc.
            return Integer.parseInt(a.getTrack()) - Integer.parseInt(b.getTrack());
        }
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    // Called in MusicService...
    private void handleActionVerifyLoaded() {

        Boolean success = false;

        try {
            Thread.sleep(2000); // delay for 2 seconds to avoid error
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int counter = 0;

        while (MusicLibrary.getMediaItems().size() == 0 && counter < 15) {
            try {
                Thread.sleep(1000); // sleep for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter++;
        }

        if (counter < 14) {
            success = true;
        }

        if (mCallback != null) {
            mCallback.onMusicCatalogReady(success);
        }
    }


    private void handleActionLoadRandomSongs() {

        // #1 Randomly select 25 songs from Songs A-Z
        // #2 Search Song.title for exact match of the name
        //      ... X = record count
        //      ... get a random number between 1 and X
        //      ... select the song based on the number and add to the list
        //          identifier relates back to the show

        DataRepository mRepository = ((BasicApp) getApplication()).getRepository();

        // REMOVE OLD RANDOM SONGS
        List<Song> songList1 = mRepository.loadRandomSongs();
        for (Song song : songList1) {
            song.setRandom(false);
        }
        mRepository.updateSongs(songList1);
        // DONE


        List<SongsAtoZ> songsAtoZList = mRepository.loadAllSongsAtoZ();

        List<Song> songList = new ArrayList<>();

        do {
            // get a random record
            int randomRecord = new Random().nextInt(songsAtoZList.size());

            // grab a random Song using the randomRecord
            String randomSongName = songsAtoZList.get(randomRecord).getDisplayName();

            // remove song so it doesn't get used again this round.
            songsAtoZList.remove(randomRecord);

            List<Song> tempList = mRepository.loadSongListByTitle(randomSongName);

            if (tempList.size() > 0) {

                int randomRecord2 = new Random().nextInt(tempList.size());

                songList.add(tempList.get(randomRecord2));

            } else {
                Log.i(TAG, "NOT FOUND: "+randomSongName);
            }

            //
        } while (songList.size() < 25);

        for (Song song : songList) {

            song.setRandom(true);
        }

        // ADD RANDOM SONGS
        mRepository.updateSongs(songList);

        MusicLibrary.loadSongList(songList);
    }

}