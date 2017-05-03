package com.orobator.exoplayerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.widget.SeekBar;
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
  @BindView(R.id.exo_state_text_view) TextView exoPlayerStateTextView;
  @BindView(R.id.seek_bar) AppCompatSeekBar seekBar;
  @BindView(R.id.time_text_view) TextView timeTextView;

  Runnable updateProgressRunnable = new Runnable() {
    @Override public void run() {
      updatePlayerTime();
    }
  };

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

  @OnClick(R.id.init_player_button) void initializePlayer() {
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

    File drakkarNoirFile = new File("/sdcard/Music/Music/Phoenix/Bankrupt!/06 Drakkar Noir.mp3");

    Uri drakkarNoirUri = Uri.fromFile(drakkarNoirFile);

    MediaSource drakkarNoirMediaSource = new ExtractorMediaSource(drakkarNoirUri,
        new DefaultDataSourceFactory(this, getString(R.string.app_name)),
            new DefaultExtractorsFactory(), mainHandler, eventLogger);

    File chloroformFile = new File("/sdcard/Music/Music/Phoenix/Bankrupt!/07 Chloroform.mp3");

    Uri chloroformUri = Uri.fromFile(chloroformFile);

    final MediaSource chloroformMediaSource = new ExtractorMediaSource(chloroformUri,
        new DefaultDataSourceFactory(this, getString(R.string.app_name)), new DefaultExtractorsFactory(),
        mainHandler, eventLogger);

    final AppendingMediaSource mediaSource = new AppendingMediaSource(drakkarNoirMediaSource);
    exoPlayer.prepare(mediaSource);

    seekBar.postDelayed(new Runnable() {
      @Override public void run() {
        mediaSource.appendSource(exoPlayer, chloroformMediaSource);
      }
    }, 1000);

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          exoPlayer.seekTo(progress);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
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
    updatePlayerTime();
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

    updatePlayerTime();
  }

  private void updatePlayerTime() {
    seekBar.setMax((int) exoPlayer.getDuration());
    seekBar.setProgress((int) exoPlayer.getCurrentPosition());
    timeTextView.setText("Current Position: "
        + exoPlayer.getCurrentPosition()
        + "\tDuration in ms: "
        + exoPlayer.getDuration());

    // Cancel any pending updates and schedule a new one if necessary.
    seekBar.removeCallbacks(updateProgressRunnable);
    int playbackState = exoPlayer == null ? ExoPlayer.STATE_IDLE : exoPlayer.getPlaybackState();
    if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
      seekBar.postDelayed(updateProgressRunnable, 200);
    }
  }

  @Override protected void onPause() {
    super.onPause();

    // TODO Doing this here is a bug for multi-window
    seekBar.removeCallbacks(updateProgressRunnable);
  }

  @Override public void onPlayerError(ExoPlaybackException error) {

  }

  @Override public void onPositionDiscontinuity() {
    updatePlayerTime();
  }

  @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

  }
}
