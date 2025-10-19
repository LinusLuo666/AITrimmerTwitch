package com.aitrimmer.ffmpeg;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompressionPresetConfigTest {

    @Test
    void usesDefaultWhenNoOverridesProvided() {
        CompressionPresetConfig config = new CompressionPresetConfig();
        CompressionPreset resolved = config.resolve(null);
        assertEquals("libx264", resolved.getVideoCodec());
        assertEquals("aac", resolved.getAudioCodec());
        assertEquals("medium", resolved.getPreset());
        assertEquals(23, resolved.getCrf());
        assertEquals(List.of("-c:v", "libx264", "-preset", "medium", "-crf", "23", "-c:a", "aac", "-b:a", "128k"), resolved.toArgs());
    }

    @Test
    void appliesUserOverrides() {
        CompressionPresetConfig config = new CompressionPresetConfig();
        CompressionOverrides overrides = CompressionOverrides.builder()
                .videoCodec("libx265")
                .audioCodec("opus")
                .videoBitrate("4M")
                .audioBitrate("192k")
                .crf(20)
                .preset("slow")
                .extraArgs(List.of("-movflags", "+faststart"))
                .build();

        CompressionPreset resolved = config.resolve(overrides);
        assertEquals("libx265", resolved.getVideoCodec());
        assertEquals("opus", resolved.getAudioCodec());
        assertEquals("4M", resolved.getVideoBitrate());
        assertEquals("192k", resolved.getAudioBitrate());
        assertEquals(20, resolved.getCrf());
        assertEquals("slow", resolved.getPreset());
        assertEquals(List.of("-movflags", "+faststart"), resolved.getExtraArgs());
        assertTrue(resolved.toArgs().containsAll(List.of("-movflags", "+faststart")));
    }
}
