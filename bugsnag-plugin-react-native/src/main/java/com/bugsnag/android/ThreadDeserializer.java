package com.bugsnag.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class ThreadDeserializer implements MapDeserializer<Thread> {

    private final StackframeDeserializer stackframeDeserializer;
    private final Logger logger;

    ThreadDeserializer(StackframeDeserializer stackframeDeserializer, Logger logger) {
        this.stackframeDeserializer = stackframeDeserializer;
        this.logger = logger;
    }

    @Override
    public Thread deserialize(Map<String, Object> map) {
        String type = MapUtils.getOrThrow(map, "type");
        List<Map<String, Object>> stacktrace = MapUtils.getOrThrow(map, "stacktrace");
        List<Stackframe> frames = new ArrayList<>();

        for (Map<String, Object> frame : stacktrace) {
            frames.add(stackframeDeserializer.deserialize(frame));
        }

        return new Thread(
                MapUtils.<Long>getOrThrow(map, "id"),
                MapUtils.<String>getOrThrow(map, "name"),
                ThreadType.valueOf(type.toUpperCase(Locale.US)),
                MapUtils.<Boolean>getOrThrow(map, "errorReportingThread"),
                new Stacktrace(logger, frames),
                logger
        );
    }
}
