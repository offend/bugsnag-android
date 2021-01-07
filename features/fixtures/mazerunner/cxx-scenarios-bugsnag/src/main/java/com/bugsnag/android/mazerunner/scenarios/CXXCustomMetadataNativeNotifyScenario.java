package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Configuration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CXXCustomMetadataNativeNotifyScenario extends Scenario {

    static {
        System.loadLibrary("cxx-scenarios-bugsnag");
    }

    public native void activate();

    public CXXCustomMetadataNativeNotifyScenario(@NonNull Configuration config,
                                                 @NonNull Context context,
                                                 @Nullable String eventMetadata) {
        super(config, context, eventMetadata);
    }

    @Override
    public void startScenario() {
        super.startScenario();
        Bugsnag.addMetadata("fruit", "orange", "meyer");
        Bugsnag.addMetadata("fruit", "counters", 302);
        Bugsnag.addMetadata("fruit", "ripe", false);
        activate();
    }
}