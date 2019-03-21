/**
 * AppDatabase.java
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


import android.content.Context;

import com.himebaugh.deadstreams.AppExecutors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Show.class, Song.class, SongsAtoZ.class, LastPlayed.class, Playlist.class, PlaylistSong.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    @SuppressWarnings("unused")
    private final static String TAG = AppDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "deadstreams.db";
    private static final Object LOCK = new Object();
    private static AppDatabase sInstance;

    public abstract ShowDao showDao();

    public abstract SongDao songDao();

    public abstract SongsAtoZDao songsAtoZDao();

    public abstract LastPlayedDao lastPlayedDao();

    public abstract PlaylistDao playlistDao();

    public abstract PlaylistSongDao playlistSongDao();

    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {

        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                }
            }
        }

        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context context, final AppExecutors executors) {

        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)

                // To Populate database
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        // Initializing the database seems like it belongs here,
                        // but I had to move to ShowListActivity to avoid error of creating database with internet down
                        // would be empty forever

//                        if (NetworkUtils.isNetworkAvailable(context)) {
//
////                            executors.diskIO().execute(() -> {
////
////                                AppDatabase database = AppDatabase.getInstance(context, executors);
////
////                                List<Show> showList = NetworkUtils.getShowList();
////
////                                if (showList != null) {     // probably not null if NetworkAvailable
////                                    database.showDao().insertAllShows(showList);
////                                }
////
////                                LastPlayed lastPlayed = new LastPlayed();
////                                lastPlayed.setId(1);
////                                lastPlayed.setSongName(context.getString(R.string.default_song_name));
////                                database.lastPlayedDao().insertLastPlayed(lastPlayed);
////                            });
//
//                            InitializeDatabaseJobService.startActionLoadData(context);
//
//                        } else {
//                            // avoids an error, but...
//                            // database will be empty... need to do something.
//                            Log.i(TAG, "onCreate:  SCHEDULED? -or- problem no network");
//
//                            // InitializeDatabaseJobService.scheduleLoadShows(context);
//
//                        }
                    }
                }).build();
    }




//    Just sent this to Mentors, but would like input from others here.
//    What is best practice when building Room database and loading with initial data upon first run?
//    Initializing the database seems like it belongs in the onCreate below, but creating
//    the database with internet down or another error occurring would leave it empty forever.
//    Read other comments in onCreate below...
//
//    private static AppDatabase buildDatabase(final Context context) {
//
//        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
//
//                // To Populate database
//                .addCallback(new Callback() {
//                    @Override
//                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                        super.onCreate(db);
//
//                        // This JobService downloads json data as a one time event from internet and inserts into DataRepository and all is good.
//                        // BUT if the internet is down or some other error occurs, it will not be able to
//                        // successfully complete. Sure it handles errors so it won't crash but no data is retrieved.
//                        // I'm already in onCreate, so it's too late and database is created & empty forever????
//
//                        LoadDataJobService.startActionLoadData(context);
//                    }
//                }).build();
//    }

    //           if (NetworkUtils.isNetworkAvailable(context)) {

}
