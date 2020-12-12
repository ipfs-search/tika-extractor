package org.ipfs_search.tika_extractor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


public class MockServer {
    public static WireMockServer mock;

    @BeforeAll
    public static void setUp() {
        mock = new WireMockServer(options().dynamicPort());
        mock.start();
    }

    @AfterEach
    void afterEach() {
    	mock.resetAll();
    }

    @AfterAll
    public static void tearDown() {
    	if (mock != null) {
    		mock.stop();
    	}
    }
}
