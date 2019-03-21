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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.ui.ShowListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


public class MediaNotificationManager extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 676;
    private static final int REQUEST_CODE = 100;

    private static final String CHANNEL_ID = "stream_channel_01";
    private static final String CHANNEL_NAME = "Dead-Stream Notifications";
    private static final String CHANNEL_DESCRIPTION = "Dead-Stream: is currently playing";

    private static final String ACTION_PAUSE = "com.himebaugh.deadstreams.player.PAUSE";
    private static final String ACTION_PLAY = "com.himebaugh.deadstreams.player.PLAY";
    private static final String ACTION_NEXT = "com.himebaugh.deadstreams.player.NEXT";
    private static final String ACTION_PREV = "com.himebaugh.deadstreams.player.PREV";

    private final MusicService mService;

    private final NotificationManager mNotificationManager;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    private PendingIntent stopIntent;

    private boolean mStarted;

    public MediaNotificationManager(MusicService service) {

        mService = service;

        String pkg = mService.getPackageName();

        PendingIntent playIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent prevIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        mPlayAction = new NotificationCompat.Action(R.drawable.ic_play_arrow_white_24dp, mService.getString(R.string.label_play), playIntent);
        mPauseAction = new NotificationCompat.Action(R.drawable.ic_pause_white_24dp, mService.getString(R.string.label_pause), pauseIntent);
        mNextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp, mService.getString(R.string.label_next), nextIntent);
        mPrevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp, mService.getString(R.string.label_previous), prevIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);

        mService.registerReceiver(this, filter);

        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancelAll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PAUSE:
                    mService.mCallback.onPause();
                    break;
                case ACTION_PLAY:
                    mService.mCallback.onPlay();
                    break;
                case ACTION_NEXT:
                    mService.mCallback.onSkipToNext();
                    break;
                case ACTION_PREV:
                    mService.mCallback.onSkipToPrevious();
                    break;
            }
        }
    }

    public void update(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token) {
        if (state == null || state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
            mService.stopForeground(true);
            try {
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore receiver not registered
            }
            mService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

        // create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API level 26 ("Android O") supports notification channels.
            createNotificationChannel();
        }

        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);

        MediaDescriptionCompat description = metadata.getDescription();

        notificationBuilder
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setShowCancelButton(true).setCancelButtonIntent(stopIntent).setMediaSession(token))
                .setColor(mService.getApplication().getResources().getColor(R.color.colorPrimaryDark))
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getDescription())      // changed from getSubtitle()
                .setColorized(false)
                .setAutoCancel(false)
                .setOngoing(isPlaying)
                .setWhen(isPlaying ? System.currentTimeMillis() - state.getPosition() : 0)
                .setShowWhen(isPlaying)
                .setUsesChronometer(isPlaying);

        // IT SEEMS OLDER PHONES NOTIFICATION TEXT COLOR CAN'T BE CHANGED.
        // ... IF I SET THE NOTIFICATION BACKGROUND TO WHITE, I CAN'T SEE THE TEXT.
        // ... THEREFORE, COLORED NOTIFICATION BACKGROUND FOR OLDER PHONES.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // brown bear with brown background
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mService.getApplication().getResources(), R.drawable.dancing_bear_brown));
         } else {
            // green bear with white background
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mService.getApplication().getResources(), returnARandomDancingBear() ));
        }

        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(mPrevAction);
        }

        notificationBuilder.addAction(isPlaying ? mPauseAction : mPlayAction);

        // If skip to prev action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(mNextAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying && !mStarted) {
            mService.startService(new Intent(mService.getApplicationContext(), MusicService.class));
            mService.startForeground(NOTIFICATION_ID, notification);
            mStarted = true;
        } else {
            if (!isPlaying) {
                mService.stopForeground(false);
                mStarted = false;
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public int returnARandomDancingBear() {
        List<Integer> list = new ArrayList<>();
        list.add(R.drawable.dancing_bear_blue);
        list.add(R.drawable.dancing_bear_green);
        list.add(R.drawable.dancing_bear_orange);
        list.add(R.drawable.dancing_bear_pink);
        list.add(R.drawable.dancing_bear_yellow);
        list.add(R.drawable.dancing_skeleton);
        return list.get(new Random().nextInt(list.size()));
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, ShowListActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // create channel
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

}