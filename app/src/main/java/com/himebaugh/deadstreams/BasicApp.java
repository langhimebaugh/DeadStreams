/*
 * Copyright 2017, The Android Open Source Project
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

package com.himebaugh.deadstreams;

import android.app.Application;

import com.himebaugh.deadstreams.database.AppDatabase;

/**
 * Android Application class. Used for accessing singletons.
 */
public class BasicApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();

        // I'd like to find a better way...
//        if (!initialDownloadComplete()) {
//            // if no data try to download again
//            if (NetworkUtils.isNetworkAvailable(this)) {
//                InitializeDatabaseJobService.startActionLoadData(this);
//            }
//        }

    }


    private AppDatabase getDatabase() {

        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {

        return DataRepository.getInstance(getDatabase());
    }

    /*
     * I hate to do this, but I can't figure out a way around it.
     * I download initial data with InitializeDatabaseJobService in AppDatabase, but if internet is down at the time,
     * it will never get downloaded.  So now, I query initialDownloadComplete() in on create every time!!!
     * NOW HANDLED IN SPLASH ACTIVITY
     */
//    public Boolean initialDownloadComplete() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean result = sharedPreferences.getBoolean(getString(R.string.initial_download_complete), false);
//        return result;
//    }

}