/**
 * DataRepository.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams;

import android.os.AsyncTask;
import android.util.Log;

import com.himebaugh.deadstreams.database.AppDatabase;
import com.himebaugh.deadstreams.database.LastPlayed;
import com.himebaugh.deadstreams.database.LastPlayedDao;
import com.himebaugh.deadstreams.database.Playlist;
import com.himebaugh.deadstreams.database.PlaylistDao;
import com.himebaugh.deadstreams.database.PlaylistSong;
import com.himebaugh.deadstreams.database.PlaylistSongDao;
import com.himebaugh.deadstreams.database.Show;
import com.himebaugh.deadstreams.database.ShowDao;
import com.himebaugh.deadstreams.database.Song;
import com.himebaugh.deadstreams.database.SongDao;
import com.himebaugh.deadstreams.database.SongsAtoZ;
import com.himebaugh.deadstreams.database.SongsAtoZDao;

import java.util.List;

import androidx.lifecycle.LiveData;


public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private static DataRepository sInstance;

    private final ShowDao mShowDao;
    private final LiveData<List<Show>> mAllShows;
    private final LiveData<List<Show>> mFavoriteShows;

    private final SongDao mSongDao;

    private final SongsAtoZDao mSongsAtoZDao;

    private final LastPlayedDao mLastPlayedDao;
    private final LiveData<LastPlayed> mLastPlayedLiveData;

    private final PlaylistDao mPlaylistDao;
    private final LiveData<List<Playlist>> mPlaylists;

    private final PlaylistSongDao mPlaylistSongDao;
    //private final LiveData<List<PlaylistSong>> mPlaylistSongs;


    private DataRepository(final AppDatabase database) {

        mShowDao = database.showDao();
        mAllShows = mShowDao.loadAllShows();
        mFavoriteShows = mShowDao.loadFavoriteShows();

        mSongDao = database.songDao();

        mSongsAtoZDao = database.songsAtoZDao();

        mLastPlayedDao = database.lastPlayedDao();
        mLastPlayedLiveData = mLastPlayedDao.getLastPlayedLiveData();

        mPlaylistDao = database.playlistDao();
        mPlaylists = mPlaylistDao.loadAllPlaylistsLiveData();

        mPlaylistSongDao = database.playlistSongDao();
        //mPlaylistSongs = mPlaylistSongDao.loadPlaylistByPlaylistIdLiveData();
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    // Get the list of shows from the database and get notified when the data changes.
    public LiveData<List<Show>> loadAllShows() {

        return mAllShows;
    }

    public LiveData<List<Show>> loadFavoriteShows() {

        return mFavoriteShows;
    }

    public LiveData<List<Song>> loadSongsByIdentifierLiveData(String identifier) {

        return mSongDao.loadSongListLiveDataByIdentifier(identifier);
    }

    public List<Song> loadSongsByIdentifier(String identifier) {

        return mSongDao.loadSongListByIdentifier(identifier);
    }

    public List<Song> loadSongListByTitle(String title) {

        return mSongDao.loadSongListByTitle(title);
    }

    public List<SongsAtoZ> loadAllSongsAtoZ() {

        return mSongsAtoZDao.loadAllSongsAtoZ();
    }

    public LiveData<List<Song>> loadRandomSongsLiveData() {

        return mSongDao.loadRandomSongsLiveData();
    }

    public List<Song> loadRandomSongs() {

        return mSongDao.loadRandomSongs();
    }

    public LiveData<List<Playlist>> loadAllPlaylistsLiveData() {

        return mPlaylistDao.loadAllPlaylistsLiveData();
    }

    public LiveData<List<PlaylistSong>> loadPlaylistByPlaylistId(int id) {

        return mPlaylistSongDao.loadPlaylistByPlaylistId(id);
    }

    // I think I can do this, since this method is being called from LoadSongsJobService
    // Initiating from LoadSongsJobService should keep it off the UI thread.
    public void insertSongList(List<Song> songList) {

        mSongDao.insertSongList(songList);
    }


    public void updateSongs(List<Song> songList) {

        mSongDao.updateSongs(songList);
    }



    public void updateShow(Show show) {

        new updateShowAsyncTask(mShowDao).execute(show);
    }

    // Updates a Show in the database.
    // ie when a user clicks the favorite button
    private static class updateShowAsyncTask extends AsyncTask<Show, Void, Void> {
        private final ShowDao mAsyncShowDao;

        updateShowAsyncTask(ShowDao dao) {
            mAsyncShowDao = dao;
        }

        @Override
        protected Void doInBackground(final Show... params) {
            mAsyncShowDao.updateShow(params[0]);
            return null;
        }
    }

    // ***********************************************************
    // Used to update the song info on PlaybackControlsFragment
    // ***********************************************************
    public LiveData<LastPlayed> getLastPlayed() {

        return mLastPlayedLiveData;
    }

    public void updateLastPlayed(LastPlayed lastPlayed) {

        new updateLastPlayedAsyncTask(mLastPlayedDao).execute(lastPlayed);
    }

    // Updates a Show in the database.
    // ie when a user clicks the favorite button
    private static class updateLastPlayedAsyncTask extends AsyncTask<LastPlayed, Void, Void> {
        private final LastPlayedDao mAsyncLastPlayedDao;

        updateLastPlayedAsyncTask(LastPlayedDao lastPlayedDao) {
            mAsyncLastPlayedDao = lastPlayedDao;
        }

        @Override
        protected Void doInBackground(final LastPlayed... lastPlayed) {
            mAsyncLastPlayedDao.updateLastPlayed(lastPlayed[0]);
            return null;
        }
    }

    // to initialize the database
    public Long[] insertAllShows(List<Show> showList) {

        Long[] rowsInserted = mShowDao.insertAllShows(showList);

        return rowsInserted;
    }

    // to initialize the database
    public Long[] insertAllSongs(List<Song> songList) {

        Long[] rowsInserted = mSongDao.insertAllSongs(songList);

        return rowsInserted;
    }

    // to initialize the database
    public Long[] insertAllSongsAtoZ(List<SongsAtoZ> songsAtoZList) {

        Long[] rowsInserted = mSongsAtoZDao.insertAllSongsAtoZ(songsAtoZList);

        return rowsInserted;
    }

    // to initialize the database
    public long insertLastPlayed(LastPlayed lastPlayed) {

        return mLastPlayedDao.insertLastPlayed(lastPlayed);
    }

    // to initialize the database
    public Long[] insertPlaylists(List<Playlist> playlists) {

        Long[] rowsInserted = mPlaylistDao.insertPlaylists(playlists);

        return rowsInserted;
    }

    // to initialize the database
    public Long[] insertPlaylistSongs(List<PlaylistSong> playlistSongs) {

        Long[] rowsInserted = mPlaylistSongDao.insertPlaylistSongs(playlistSongs);

        return rowsInserted;
    }


    public void addSongToPlaylist(long playlistId, long songId) {

        new addSongToPlaylistAsyncTask(mShowDao, mSongDao, mPlaylistSongDao).execute(playlistId,songId);
    }

    // Add a Song to the playlist
    private static class addSongToPlaylistAsyncTask extends AsyncTask<Long, Void, Void> {
        private final ShowDao mAsyncShowDao;
        private final SongDao mAsyncSongDao;
        private final PlaylistSongDao mAsyncPlaylistSongDao;

        addSongToPlaylistAsyncTask(ShowDao showDao, SongDao songDao, PlaylistSongDao playlistSongDao) {

            mAsyncShowDao = showDao;
            mAsyncSongDao = songDao;
            mAsyncPlaylistSongDao = playlistSongDao;
        }

        @Override
        protected Void doInBackground(Long... longs) {

            // MESSY BUT....

            long playlistId = longs[0];
            long songId = longs[1];

            Song song = mAsyncSongDao.loadSongById((int) songId);

            Show show = mAsyncShowDao.loadShowByIdentifier2(song.getIdentifier());

            PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setPlaylistId((int) playlistId);
            playlistSong.setShowCity(show.getCity());
            playlistSong.setShowCoverage(show.getCoverage());
            playlistSong.setShowDate(show.getDate());
            playlistSong.setShowId(show.getShowId());
            playlistSong.setShowIdentifier(show.getIdentifier());
            playlistSong.setShowSourceId(show.getSourceId());
            playlistSong.setShowState(show.getState());
            playlistSong.setShowVenue(show.getVenue());
            playlistSong.setSongAlbum(song.getAlbum());
            playlistSong.setSongBitrate(song.getBitrate());
            playlistSong.setSongCreator(song.getCreator());
            playlistSong.setSongFormat(song.getFormat());
            playlistSong.setSongLength(song.getLength());
            playlistSong.setSongName(song.getName());
            playlistSong.setSongTitle(song.getTitle());
            playlistSong.setSongTrack(song.getTrack());

            mAsyncPlaylistSongDao.insertPlaylistSong(playlistSong);
            return null;
        }
    }



}