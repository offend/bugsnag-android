package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.Configuration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CXXGetJavaDataScenario extends Scenario {

    static {
        System.loadLibrary("bugsnag-ndk");
        System.loadLibrary("cxx-scenarios-bugsnag");
    }

    public native void activate();

    /**
     *
     */
    public CXXGetJavaDataScenario(@NonNull Configuration config,
                                  @NonNull Context context,
                                  @Nullable String eventMetadata) {
        super(config, context, eventMetadata);
        config.setAutoTrackSessions(false);
        config.addMetadata("notData", "vals", "passMetaData");
        config.setAppVersion("passAppVersion");
        config.setContext("passContext");
        config.setUser("passUserId", "passUserEmail", "passUserName");
    }

    @Override
    public void startScenario() {
        super.startScenario();
        activate();
    }
}
