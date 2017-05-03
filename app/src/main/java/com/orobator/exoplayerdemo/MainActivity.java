package com.orobator.exoplayerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import java.io.File;

public class MainActivity extends AppCompatActivity implements ExoPlayer.EventListener {
  SimpleExoPlayer exoPlayer;
  EventLogger eventLogger;
  Handler mainHandler;
  @BindView(R.id.exo_state_TextView) TextView exoPlayerStateTextView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    mainHandler = new Handler();

    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this, null,
        DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
    DefaultTrackSelector trackSelector = new DefaultTrackSelector();
    exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
    eventLogger = new EventLogger(trackSelector);
  }

  @Override protected void onResume() {
    super.onResume();

    onPlayerStateChanged(exoPlayer.getPlayWhenReady(), exoPlayer.getPlaybackState());
  }

  @OnClick(R.id.init_player_Button) void initializePlayer() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        return;
      }
    }

    Toast.makeText(this, "Initializing Player", Toast.LENGTH_SHORT).show();

    exoPlayer.addListener(eventLogger);
    exoPlayer.addListener(this);
    exoPlayer.setAudioDebugListener(eventLogger);
    exoPlayer.setMetadataOutput(eventLogger);
    exoPlayer.setPlayWhenReady(false);

    File songFile = new File("/sdcard/Music/Music/Tory Lanez/Say It/01 Say It.mp3");

    Uri songUri = Uri.fromFile(songFile);

    MediaSource mediaSource =
        new ExtractorMediaSource(songUri, new DefaultDataSourceFactory(this, "Dummy User Agent"),
            new DefaultExtractorsFactory(), mainHandler, eventLogger);

    exoPlayer.prepare(mediaSource);
  }

  @OnClick(R.id.pause_button) void pauseExoPlayer() {
    exoPlayer.setPlayWhenReady(false);
  }

  @OnClick(R.id.play_button) void playExoPlayer() {
    exoPlayer.setPlayWhenReady(true);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      initializePlayer();
    } else {
      Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
    }
  }

  @Override public void onTimelineChanged(Timeline timeline, Object manifest) {

  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

  }

  @Override public void onLoadingChanged(boolean isLoading) {

  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    String playbackStateString = "";

    switch (playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        playbackStateString = "Buffering";
        break;
      case ExoPlayer.STATE_ENDED:
        playbackStateString = "Ended";
        break;
      case ExoPlayer.STATE_IDLE:
        playbackStateString = "Idle";
        break;
      case ExoPlayer.STATE_READY:
        playbackStateString = "Ready";
        break;
    }

    exoPlayerStateTextView.setText(
        "Play when ready: " + playWhenReady + "\tPlayback State: " + playbackStateString);
  }

  @Override public void onPlayerError(ExoPlaybackException error) {

  }

  @Override public void onPositionDiscontinuity() {

  }

  @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

  }
}
