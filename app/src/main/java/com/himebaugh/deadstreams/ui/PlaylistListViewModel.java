/**
 * PlaylistListViewModel.java
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

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.database.Playlist;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


class PlaylistListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final LiveData<List<Playlist>> mPlaylists;

    public PlaylistListViewModel(Application application) {
        super(application);

        mRepository = ((BasicApp) application).getRepository();

        mPlaylists = mRepository.loadAllPlaylistsLiveData();
    }

    LiveData<List<Playlist>> loadAllPlaylistsLiveData() {

        return mPlaylists;
    }

}
