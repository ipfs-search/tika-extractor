package org.ipfs_search.tika_extractor;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;


public class ExtractorTestProfile implements QuarkusTestProfile {
    // Override timeouts
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "extractor.connect-timeout","600",
            "extractor.read-timeout", "600",
            "extractor.body-content-write-limit", "262143"
        );
    }
}
