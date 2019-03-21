/**
 * PlaybackControlsViewModel.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.database.LastPlayed;

// The PlaybackControlsViewModel provides the interface between the UI and the data layer of the app, represented by the Repository
class PlaybackControlsViewModel extends AndroidViewModel {

    private final static String TAG = PlaybackControlsViewModel.class.getName();

    private final DataRepository mRepository;

    private LiveData<LastPlayed> mLastPlayed;

    public PlaybackControlsViewModel(Application application) {
        super(application);

        mRepository = ((BasicApp) application).getRepository();
    }

    // Expose the LiveData queries so the UI can observe it.
    public LiveData<LastPlayed> loadLastPlayed() {

        mLastPlayed = mRepository.getLastPlayed();
        return mLastPlayed;
    }

}

