package com.orobator.exoplayerdemo;

import android.app.Application;
import timber.log.Timber;

public class ExoPlayerDemoApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());
  }
}
