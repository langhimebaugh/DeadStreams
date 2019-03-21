/**
 * SongListAdapter.java
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
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Song;
import com.himebaugh.deadstreams.player.MusicLibrary;

import java.util.ArrayList;
import java.util.List;


public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ItemViewHolder> {

    private static final String TAG = SongListAdapter.class.getSimpleName();

    private final List<MediaBrowserCompat.MediaItem> mMediaItemList;
    private final Activity mActivity;

    private String mCurrentMediaId;

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private final OnClickHandler mClickHandler;
    private final OnLongClickHandler  mLongClickHandler;

    // The interface that receives onClick messages
    public interface OnClickHandler {
        void onClick(@NonNull MediaBrowserCompat.MediaItem mediaItem);
    }

    public interface OnLongClickHandler {
        void onLongClick(@NonNull MediaBrowserCompat.MediaItem mediaItem);
    }

    public SongListAdapter(Activity activity, OnClickHandler clickHandler, OnLongClickHandler longClickHandler) {

        mMediaItemList = new ArrayList<>();
        mActivity = activity;
        mClickHandler = clickHandler;
        mLongClickHandler = longClickHandler;
    }

    public void add(MediaBrowserCompat.MediaItem mediaItem) {

        mMediaItemList.add(mediaItem);
        mMediaItemList.notifyAll();
    }

    public void addAll(List<MediaBrowserCompat.MediaItem> mediaItemList) {

        mMediaItemList.addAll(mediaItemList);
    }

    public void clear() {

        mMediaItemList.clear();
    }

    public void setData(List<MediaBrowserCompat.MediaItem> mediaItemList) {
        mMediaItemList.clear();
        if (mediaItemList != null) {
            mMediaItemList.addAll(mediaItemList);
        }
        notifyDataSetChanged();
    }

    public void setCurrentMediaMetadata(MediaMetadataCompat mediaMetadata) {
        mCurrentMediaId = mediaMetadata != null
                ? mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                : null;
    }

//    public void setData(List<Song> songs) {
//        this.data.clear();
//        if (songs != null) {
//            data.addAll(songs);
//        }
//        this.notifyDataSetChanged();
//    }

//    public void loadSongs(List<Song> songList) {
//
//        mShowList = songList;
//        notifyDataSetChanged();
//    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_list_item, viewGroup, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(mActivity);
        MediaMetadataCompat mCurrentMetadata = controller.getMetadata();
        PlaybackStateCompat mCurrentState = controller.getPlaybackState();

        MediaBrowserCompat.MediaItem item = mMediaItemList.get(position);

        int itemState = ItemViewHolder.STATE_NONE;

        if (item.isPlayable()) {
            String itemMediaId = item.getDescription().getMediaId();

            int playbackState = PlaybackStateCompat.STATE_NONE;
            itemState = ItemViewHolder.STATE_PLAYABLE;

            if (mCurrentState != null) {
                playbackState = mCurrentState.getState();
            }
            if (mCurrentMetadata != null && itemMediaId.equals(mCurrentMetadata.getDescription().getMediaId())) {
                if (playbackState == PlaybackStateCompat.STATE_PLAYING || playbackState == PlaybackStateCompat.STATE_BUFFERING) {
                    itemState = ItemViewHolder.STATE_PLAYING;
                } else if (playbackState != PlaybackStateCompat.STATE_ERROR) {
                    itemState = ItemViewHolder.STATE_PAUSED;
                }
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.selectedBackground));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        holder.bind(item.getDescription(), mCurrentMetadata, itemState);
    }

    @Override
    public int getItemCount() {
        if (mMediaItemList == null) {
            return 0;
        }
        return mMediaItemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        static final int STATE_INVALID = -1;
        static final int STATE_NONE = 0;
        static final int STATE_PLAYABLE = 1;
        static final int STATE_PAUSED = 2;
        static final int STATE_PLAYING = 3;

        final ImageView mImageView;
        final TextView mTitleView;
        final TextView mDescriptionView;
        final TextView mSongLengthView;
        final Context mContext;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mContext = itemView.getContext();

            mImageView = itemView.findViewById(R.id.play_eq);
            mTitleView = itemView.findViewById(R.id.title);
            mDescriptionView = itemView.findViewById(R.id.description);
            mSongLengthView = itemView.findViewById(R.id.song_length);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bind(MediaDescriptionCompat mediaDescriptionCompat, MediaMetadataCompat metadataCompat, int state) {

            Integer cachedState = STATE_INVALID;

            if (null != itemView) {
                // not sure what this is doing
                cachedState = (Integer) itemView.getTag(R.id.tag_mediaitem_state_cache);
            }

            Log.i(TAG, "onLongClick: MusicLibrary.getSongID()=" + MusicLibrary.getSongID(mediaDescriptionCompat.getMediaId()) );

            //metadataCompat.getDescription().

            mTitleView.setText(mediaDescriptionCompat.getTitle());
            mDescriptionView.setText(mediaDescriptionCompat.getDescription());    // getSubtitle()

            // mSongLengthView.setText(mediaDescriptionCompat.getSubtitle());
            mSongLengthView.setText( MusicLibrary.getSongLENGTH(mediaDescriptionCompat.getMediaId()) );

            if (cachedState == null || cachedState != state) {
                switch (state) {
                    case STATE_PLAYABLE:
                        mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_black_36dp));
                        mImageView.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.songListIconNotPlayingColor)));
                        }
                        break;
                    case STATE_PLAYING:
                        AnimationDrawable animation = (AnimationDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_equalizer_white_36dp);
                        mImageView.setImageDrawable(animation);
                        mImageView.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.songListIconPlayingColor)));
                        }
                        animation.start();
                        break;
                    case STATE_PAUSED:
                        mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_equalizer1_white_36dp));
                        mImageView.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.songListIconNotPlayingColor)));
                        }
                        break;
                    default:
                        mImageView.setVisibility(View.GONE);
                }
                itemView.setTag(R.id.tag_mediaitem_state_cache, state);
            }
        }

        @Override
        public void onClick(View view) {

            MediaBrowserCompat.MediaItem mediaItem = mMediaItemList.get(getAdapterPosition());
            mClickHandler.onClick(mediaItem);
        }

        @Override
        public boolean onLongClick(View v) {

            MediaBrowserCompat.MediaItem mediaItem = mMediaItemList.get(getAdapterPosition());
            mLongClickHandler.onLongClick(mediaItem);
            return true;
        }
    }

}