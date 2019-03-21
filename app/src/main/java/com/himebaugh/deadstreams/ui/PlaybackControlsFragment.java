/**
 * PlaybackControlsFragment.java
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


import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.LastPlayed;
import com.himebaugh.deadstreams.player.MusicService;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


public class PlaybackControlsFragment extends Fragment {

    /* Define log tag */
    private static final String LOG_TAG = PlaybackControlsFragment.class.getSimpleName();

    int PLAYER_SHEET_PEEK_HEIGHT = 80;

    /* Main class variables */
    private Activity mActivity;
    private View mRootView;
    private View mPlayerSheet;
    private BottomSheetBehavior mPlayerBottomSheetBehavior;

    private TextView mSongNameTextView;
    private TextView mDateVenueTextView;
    private ImageButton mPlayPauseButton;

    private MediaMetadataCompat mCurrentMetadata;
    private PlaybackStateCompat mCurrentState;

    private MediaBrowserCompat mMediaBrowser;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get activity and application contexts
        mActivity = getActivity();

        setupViewModel();

        mMediaBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), MusicService.class), mConnectionCallback, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        mPlayerSheet = mRootView.findViewById(R.id.player_sheet);

        // set up and show station data sheet
        mPlayerBottomSheetBehavior = BottomSheetBehavior.from(mPlayerSheet);

        mPlayerBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mSongNameTextView = mRootView.findViewById(R.id.song_name);
        mDateVenueTextView = mRootView.findViewById(R.id.date_venue);

        mSongNameTextView.setText(R.string.default_song_name);

        mPlayPauseButton = mRootView.findViewById(R.id.play_pause_button);
        mPlayPauseButton.setEnabled(true);
        mPlayPauseButton.setOnClickListener(mPlaybackButtonListener);

        return mRootView;
    }


    private final View.OnClickListener mPlaybackButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final int state = mCurrentState == null ? PlaybackStateCompat.STATE_NONE : mCurrentState.getState();

            switch (v.getId()) {
                case R.id.play_pause_button:

                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {

                        // Paused, so lets PLAY
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();

                    } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_CONNECTING) {

                        // Playing, so lets PAUSE
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().pause();
                    }
                    break;
                case R.id.player_sheet:
                case R.id.song_name:
                case R.id.date_venue:
                //case R.id.show_extras:
                    break;
                default:
                    // do nothing
            }

        }
    };

    private void updatePlaybackState(PlaybackStateCompat state) {

        mCurrentState = state;

        if (state == null) {

            mPlayPauseButton.setVisibility(View.GONE);

            mSongNameTextView.setText(R.string.default_song_name);

            return;
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_NONE:
                mPlayPauseButton.setVisibility(View.GONE);
                mSongNameTextView.setText(R.string.default_song_name);
                mDateVenueTextView.setText("");
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                mPlayPauseButton.setVisibility(View.GONE);
                mSongNameTextView.setText(R.string.default_song_name);
                mDateVenueTextView.setText("");
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mSongNameTextView.setVisibility(View.VISIBLE);
                mDateVenueTextView.setVisibility(View.VISIBLE);
                mPlayPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play_arrow_white_36dp));
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mSongNameTextView.setVisibility(View.VISIBLE);
                mDateVenueTextView.setVisibility(View.VISIBLE);
                mPlayPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause_white_36dp));
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mSongNameTextView.setVisibility(View.VISIBLE);
                mDateVenueTextView.setVisibility(View.VISIBLE);
                mSongNameTextView.setText(R.string.message_audio_is_buffering);
                break;
            case PlaybackStateCompat.STATE_ERROR:
                mPlayPauseButton.setVisibility(View.GONE);
                mSongNameTextView.setText(R.string.error);
                mDateVenueTextView.setText(R.string.message_check_internet);
                break;
            case PlaybackStateCompat.STATE_CONNECTING:
                mPlayPauseButton.setVisibility(View.GONE);
                mSongNameTextView.setText(R.string.message_connecting);
                break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
            case PlaybackStateCompat.STATE_REWINDING:
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                // do nothing
                break;
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {

                        MediaControllerCompat mMediaController = new MediaControllerCompat(getActivity(), token);

                        // Upon connection update the Playback State & Player UI
                        // This handles updating Song Info only initially.
                        updatePlaybackState(mMediaController.getPlaybackState());
                        updatePlayerUI(mMediaController.getMetadata());

                        mMediaController.registerCallback(mMediaControllerCallback);

                        MediaControllerCompat.setMediaController(getActivity(), mMediaController);

                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }


            };

    // Receive callbacks from the MediaController.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {

                    // ONLY CALLED INITIALLY
                    updatePlayerUI(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    updatePlaybackState(state);
                }

                @Override
                public void onSessionDestroyed() {

                    updatePlaybackState(null);
                }
            };

    @Override
    public void onStart() {
        super.onStart();

        if (mMediaBrowser != null && !mMediaBrowser.isConnected()) {
            mMediaBrowser.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            if (mCurrentMetadata != null) {
                mMediaBrowser.unsubscribe(mCurrentMetadata.getDescription().getMediaId());
            }
            mMediaBrowser.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            if (mCurrentMetadata != null) {
                mMediaBrowser.unsubscribe(mCurrentMetadata.getDescription().getMediaId());
            }
            mMediaBrowser.disconnect();
        }
        super.onDestroy();
    }

    private void updatePlayerUI(MediaMetadataCompat metadata) {

        if (metadata == null) {

            mSongNameTextView.setText(R.string.default_song_name);
            mDateVenueTextView.setText("");

        } else {

            mCurrentMetadata = metadata;

            mSongNameTextView.setText(metadata.getDescription().getTitle());
            mDateVenueTextView.setText(metadata.getDescription().getDescription());
        }
    }

    private void setupViewModel() {

        PlaybackControlsViewModel playbackControlsViewModel = ViewModelProviders.of(this).get(PlaybackControlsViewModel.class);

        playbackControlsViewModel.loadLastPlayed().observe(this, new Observer<LastPlayed>() {

            @Override
            public void onChanged(@Nullable LastPlayed lastPlayed) {

                if (lastPlayed != null) {
                    mSongNameTextView.setText(lastPlayed.getSongName());
                    mDateVenueTextView.setText(lastPlayed.getSongDescription());
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save variables on screen orientation change
//        outState.putString(PREF_LOGO_URL,  mLogoUrl);
//        outState.putString(PREF_LINE_1,  mStationNameTextView.getText().toString());
//        outState.putString(PREF_LINE_2, mStationLine2TextView.getText().toString());
//        outState.putString(PREF_LINE_3, mStationLine3TextView.getText().toString());
        //  outState.putParcelable("PLAYBACKEVENT", mPlaybackEvent);

    }
}