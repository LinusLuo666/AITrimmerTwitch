package com.aitrimmer.ffmpeg;

import java.util.Objects;

/**
 * Handles resolution of the effective compression preset using defaults and overrides.
 */
public final class CompressionPresetConfig {
    private final CompressionPreset defaultPreset;

    public CompressionPresetConfig(CompressionPreset defaultPreset) {
        this.defaultPreset = Objects.requireNonNull(defaultPreset, "defaultPreset");
    }

    public CompressionPresetConfig() {
        this(CompressionPreset.createDefault());
    }

    public CompressionPreset resolve(CompressionOverrides overrides) {
        return defaultPreset.withOverrides(overrides);
    }

    public CompressionPreset getDefaultPreset() {
        return defaultPreset;
    }
}
