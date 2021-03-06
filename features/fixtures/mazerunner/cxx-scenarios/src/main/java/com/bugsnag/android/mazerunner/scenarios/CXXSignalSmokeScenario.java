package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.Breadcrumb;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Configuration;
import com.bugsnag.android.Event;
import com.bugsnag.android.OnBreadcrumbCallback;
import com.bugsnag.android.OnErrorCallback;
import com.bugsnag.android.Severity;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CXXSignalSmokeScenario extends Scenario {

    static {
        System.loadLibrary("cxx-scenarios");
    }

    public native int crash(int value);

    /**
     *
     */
    public CXXSignalSmokeScenario(@NonNull Configuration config,
                                  @NonNull Context context,
                                  @Nullable String eventMetadata) {
        super(config, context, eventMetadata);
        config.setAutoTrackSessions(false);
        config.setAppType("Overwritten");
        config.setAppVersion("9.9.9");
        config.setVersionCode(999);
        config.setReleaseStage("CXXSignalSmokeScenario");
        Set<String> enabledReleaseStages = new HashSet<>();
        enabledReleaseStages.add("CXXSignalSmokeScenario");
        config.setEnabledReleaseStages(enabledReleaseStages);
        config.setContext("CXXSignalSmokeScenario");
        config.setUser("ABC", "ABC@CBA.CA", "CXXSignalSmokeScenario");
        config.addMetadata("TestData", "Source", "CXXSignalSmokeScenario");
        Set<String> redactedKeys = new HashSet<>();
        redactedKeys.add("redacted");
        config.setRedactedKeys(redactedKeys);
        config.addOnBreadcrumb(new OnBreadcrumbCallback() {
            @Override
            public boolean onBreadcrumb(@NonNull Breadcrumb breadcrumb) {
                Map<String, Object> metadata = breadcrumb.getMetadata();
                metadata.put("Source", "CXXSignalSmokeScenario");
                breadcrumb.setMetadata(metadata);
                return true;
            }
        });

        config.addOnError(new OnErrorCallback() {
            @Override
            public boolean onError(@NonNull Event event) {
                event.addMetadata("TestData", "Callback", true);
                event.addMetadata("TestData", "redacted", false);
                event.setSeverity(Severity.INFO);
                return true;
            }
        });
        disableSessionDelivery(config);
    }

    @Override
    public void startScenario() {
        super.startScenario();
        Bugsnag.startSession();
        Bugsnag.leaveBreadcrumb("CXXSignalSmokeScenario");
        Handler main = new Handler(Looper.getMainLooper());
        main.postDelayed(new Runnable() {
            @Override
            public void run() {
                crash(2726);
            }
        }, 500);
    }
}
