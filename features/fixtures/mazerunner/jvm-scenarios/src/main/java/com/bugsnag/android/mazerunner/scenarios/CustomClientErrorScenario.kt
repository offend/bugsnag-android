package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.createCustomHeaderDelivery

/**
 * Sends a handled exception to Bugsnag using a custom API client which modifies the request.
 */
internal class CustomClientErrorScenario(
    config: Configuration,
    context: Context,
    eventMetadata: String
) : Scenario(config, context, eventMetadata) {

    init {
        config.delivery = createCustomHeaderDelivery()
        config.autoTrackSessions = false
    }

    override fun startScenario() {

        super.startScenario()
        Bugsnag.notify(RuntimeException("Hello"))
    }
}
