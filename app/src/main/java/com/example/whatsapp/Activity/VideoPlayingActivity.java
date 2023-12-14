package com.example.whatsapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import com.example.whatsapp.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class VideoPlayingActivity extends AppCompatActivity {


    ExoPlayer exoPlayer;
    StyledPlayerView styledPlayerView;
    MediaItem mediaItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playing);
        //Window window = this.getWindow();
        //window.setStatusBarColor(getColor(R.color.statusbarcolor));

        Intent intent = getIntent();
        String uri = intent.getStringExtra("videouri");


        styledPlayerView = findViewById(R.id.myvideoplayer);
        exoPlayer = new ExoPlayer.Builder(VideoPlayingActivity.this).build();

        styledPlayerView.setPlayer(exoPlayer);

        mediaItem = MediaItem.fromUri(Uri.parse(uri));

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();
        exoPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.stop();
        exoPlayer.release();
    }
}