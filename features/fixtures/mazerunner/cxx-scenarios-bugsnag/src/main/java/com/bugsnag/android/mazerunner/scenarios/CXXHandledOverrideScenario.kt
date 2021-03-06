package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration

internal class CXXHandledOverrideScenario(
    config: Configuration,
    context: Context,
    eventMetadata: String?
) : Scenario(config, context, eventMetadata) {

    init {
        System.loadLibrary("cxx-scenarios-bugsnag")
        config.autoTrackSessions = false
        disableSessionDelivery(config)
    }

    external fun activate()

    override fun startScenario() {
        super.startScenario()

        if (eventMetadata != "non-crashy") {
            Bugsnag.startSession()
            activate()
        }
    }
}
