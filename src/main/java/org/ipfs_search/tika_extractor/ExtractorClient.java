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

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
// import org.jboss.resteasy.client.jaxrs.ResteasyClient;
// import org.jboss.resteasy.client.jaxrs.engines.vertx.VertxClientHttpEngine;

import org.jboss.logging.Logger;


@ApplicationScoped
public class ExtractorClient {
    private static final Logger LOG = Logger.getLogger(ExtractorResource.class);
    private Client client;

    @Inject
    public ExtractorClient(ExtractorConfiguration configuration) {
        // Note: we're using a blocking client for now as async clients seem to be buggy.
        // Ideal situation is to use RestEasyClient's VertxClientHttpEngine wrapper with native transports.
        ExecutorService executorService = Executors.newCachedThreadPool();

        // TODO: Use below once async client is used.
        // ExecutorService executorService = Executors.newFixedThreadPool(configuration.ClientWorkerThreads);

        // TODO: Use vertx client for native transport
        // Use Vertx client for better async I/O
        // No clue why this is not working.
        // Ref: https://docs.jboss.org/resteasy/docs/4.4.0.Final/userguide/html_single/index.html#vertx_client
        // VertxClientHttpEngine engine = new VertxClientHttpEngine();

        //  client = ((ResteasyClientBuilder)ClientBuilder.newBuilder())
        //             .clientEngine(engine).build();

        //config().setReadTimeout(100)

        client = ((ResteasyClientBuilder)ClientBuilder.newBuilder())
                // ReadTimeout is not respected when async engine is used.
                // .useAsyncHttpEngine() // Specific to ResteasyClientBuilder
        		.readTimeout(configuration.ReadTimeout, TimeUnit.MILLISECONDS) // Generic ClientBuilder property
        		.connectTimeout(configuration.ConnectTimeout, TimeUnit.MILLISECONDS) // Generic ClientBuilder property
                .connectionPoolSize(configuration.ConnectionPoolSize) // Specific to ResteasyClientBuilder
                .maxPooledPerRoute(configuration.MaxPooledPerRoute) // Specific to ResteasyClientBuilder
                .responseBufferSize(configuration.ClientResponseBufferSize) // Specific to ResteasyClientBuilder
                .executorService(executorService)
                .build();

    	LOG.infof("Configured extractor client: %s", client);
    }

    CompletionStage<InputStream> get(String url) {
    	LOG.infof("ExtractorClient GET: %s", url);

        return client.target(url)
                .request()
                .rx()
                .get(InputStream.class);
    }
}
