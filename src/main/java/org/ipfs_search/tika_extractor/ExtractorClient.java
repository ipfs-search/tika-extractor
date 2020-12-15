package org.ipfs_search.tika_extractor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.CompletionStage;
import java.io.InputStream;
import org.jboss.logging.Logger;


@ApplicationScoped
public class ExtractorClient {
    private static final Logger LOG = Logger.getLogger(ExtractorResource.class);
    private Client client;

    @Inject
    public ExtractorClient(ExtractorConfiguration configuration) {
        // Requests are indeed blocking here!
        ExecutorService executorService = Executors.newCachedThreadPool();

        client = ClientBuilder.newBuilder()
        		.readTimeout(configuration.ReadTimeout, TimeUnit.MILLISECONDS)
        		.connectTimeout(configuration.ConnectTimeout, TimeUnit.MILLISECONDS)
                .executorService(executorService)
                .build();

    	LOG.info("Configured extractor client");
    }

    CompletionStage<InputStream> get(String url) {
    	LOG.infof("ExtractorClient GET: %s", url);

        return client.target(url)
                .request()
                .rx()
                .get(InputStream.class);
    }
}
